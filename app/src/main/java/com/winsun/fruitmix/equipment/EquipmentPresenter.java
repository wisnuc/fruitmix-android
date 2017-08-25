package com.winsun.fruitmix.equipment;

import android.databinding.ViewDataBinding;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.EquipmentItemBinding;
import com.winsun.fruitmix.databinding.EquipmentUserItemBinding;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.model.EquipmentInfo;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2017/8/15.
 */

public class EquipmentPresenter {

    public static final String TAG = EquipmentPresenter.class.getSimpleName();

    private EquipmentViewPagerAdapter equipmentViewPagerAdapter;

    private EquipmentUserRecyclerViewAdapter equipmentUserRecyclerViewAdapter;

    private EquipmentSearchView equipmentSearchView;

    private LoadingViewModel loadingViewModel;

    private NoContentViewModel noContentViewModel;

    private final List<Equipment> mUserLoadedEquipments = new ArrayList<>();

    private Equipment currentEquipment;

    private List<List<User>> mUserExpandableLists;

    private CustomHandler mHandler;

    private static final int DATA_CHANGE = 0x0001;

    private static final int RETRY_GET_DATA = 0x0002;

    private static final int DISCOVERY_TIMEOUT = 0x0003;

    private static final int RESUME_DISCOVERY = 0x0004;

    private static final int RETRY_DELAY_MILLSECOND = 20 * 1000;

    private static final int DISCOVERY_TIMEOUT_TIME = 6 * 1000;
    private static final int RESUME_DISCOVERY_INTERVAL = 3 * 1000;

    private EquipmentSearchManager mEquipmentSearchManager;

    private EquipmentRemoteDataSource mEquipmentRemoteDataSource;

    private LoginUseCase loginUseCase;

    private ThreadManager threadManagerImpl;

    private Random random;

    private int preAvatarBgColor = 0;

    private boolean onPause = false;

    private boolean hasFindEquipment = false;

    private class CustomHandler extends Handler {

        WeakReference<EquipmentPresenter> weakReference = null;

        CustomHandler(EquipmentPresenter presenter, Looper looper) {
            super(looper);
            weakReference = new WeakReference<>(presenter);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DATA_CHANGE:

                    if (loadingViewModel.showLoading.get()) {
                        loadingViewModel.showLoading.set(false);

                        refreshEquipment(0);
                    }

                    EquipmentViewPagerAdapter adapter = weakReference.get().equipmentViewPagerAdapter;

                    adapter.setEquipments(mUserLoadedEquipments);

                    adapter.notifyDataSetChanged();

                    break;

                case RETRY_GET_DATA:

                    Equipment equipment = (Equipment) msg.obj;

                    Log.d(TAG, "handleMessage: retry get user equipment host0: " + equipment.getHosts().get(0));

                    getEquipmentInfo(equipment);

                    break;

                case DISCOVERY_TIMEOUT:

                    if (hasFindEquipment)
                        return;

                    noContentViewModel.showNoContent.set(true);
                    loadingViewModel.showLoading.set(false);

                    stopDiscovery();

                    mHandler.sendEmptyMessageDelayed(RESUME_DISCOVERY, RESUME_DISCOVERY_INTERVAL);

                    break;

                case RESUME_DISCOVERY:

                    noContentViewModel.showNoContent.set(false);
                    loadingViewModel.showLoading.set(true);

                    startDiscovery();

                    mHandler.sendEmptyMessageDelayed(DISCOVERY_TIMEOUT, DISCOVERY_TIMEOUT_TIME);

                    break;

                default:
            }
        }
    }

    public EquipmentPresenter(LoadingViewModel loadingViewModel, NoContentViewModel noContentViewModel, EquipmentSearchView equipmentSearchView,
                              EquipmentSearchManager mEquipmentSearchManager, EquipmentRemoteDataSource mEquipmentRemoteDataSource,
                              LoginUseCase loginUseCase, ThreadManager threadManagerImpl) {
        this.loadingViewModel = loadingViewModel;
        this.noContentViewModel = noContentViewModel;
        this.equipmentSearchView = equipmentSearchView;
        this.mEquipmentSearchManager = mEquipmentSearchManager;
        this.mEquipmentRemoteDataSource = mEquipmentRemoteDataSource;
        this.loginUseCase = loginUseCase;
        this.threadManagerImpl = threadManagerImpl;

        mUserExpandableLists = new ArrayList<>();

        mHandler = new CustomHandler(this, Looper.getMainLooper());

        random = new Random();

        equipmentUserRecyclerViewAdapter = new EquipmentUserRecyclerViewAdapter();
        equipmentViewPagerAdapter = new EquipmentViewPagerAdapter();

    }

    public EquipmentUserRecyclerViewAdapter getEquipmentUserRecyclerViewAdapter() {
        return equipmentUserRecyclerViewAdapter;
    }

    public EquipmentViewPagerAdapter getEquipmentViewPagerAdapter() {
        return equipmentViewPagerAdapter;
    }

    public void onCreate(){

        mHandler.sendEmptyMessage(RESUME_DISCOVERY);

    }

    public void onResume() {

        onPause = false;

    }

    public void onPause() {

        onPause = true;

        mHandler.removeMessages(RETRY_GET_DATA);
        mHandler.removeMessages(DATA_CHANGE);

    }

    public void onDestroy() {

        stopDiscovery();

        mHandler.removeMessages(DISCOVERY_TIMEOUT);
        mHandler.removeMessages(RESUME_DISCOVERY);

        mHandler.removeMessages(RETRY_GET_DATA);
        mHandler.removeMessages(DATA_CHANGE);

        mHandler = null;

        equipmentSearchView = null;

    }

    public void handleInputIpbyByUser(String ip) {
        List<String> hosts = new ArrayList<>();
        hosts.add(ip);

        Equipment equipment = new Equipment("Winsuc Appliction " + ip, hosts, 6666);
        getEquipmentInfo(equipment);
    }

    public void refreshEquipment(int position) {

        if (mUserLoadedEquipments.size() < position)
            return;

        currentEquipment = mUserLoadedEquipments.get(position);

        int size = equipmentUserRecyclerViewAdapter.mCurrentUsers.size();

        equipmentUserRecyclerViewAdapter.clearCurrentUsers();

        equipmentUserRecyclerViewAdapter.notifyItemRangeRemoved(0, size);

        List<User> users = mUserExpandableLists.get(position);

        equipmentUserRecyclerViewAdapter.setCurrentUsers(users);

        equipmentUserRecyclerViewAdapter.notifyItemRangeInserted(0, users.size());

    }

    private void startDiscovery() {

        mEquipmentSearchManager.startDiscovery(new EquipmentSearchManager.IEquipmentDiscoveryListener() {
            @Override
            public void call(Equipment equipment) {

                getEquipmentInfo(equipment);

                hasFindEquipment = true;

                mHandler.removeMessages(DISCOVERY_TIMEOUT);

            }
        });

        mHandler.sendEmptyMessageDelayed(DISCOVERY_TIMEOUT, DISCOVERY_TIMEOUT_TIME);

    }

    private void stopDiscovery() {

        mEquipmentSearchManager.stopDiscovery();
    }

    private void getEquipmentInfo(final Equipment equipment) {

        threadManagerImpl.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                getEquipmentInfoInThread(equipment);

            }
        });


    }

    private void getEquipmentInfoInThread(final Equipment equipment) {
        mEquipmentRemoteDataSource.getEquipmentInfo(equipment.getHosts().get(0), new BaseLoadDataCallback<EquipmentInfo>() {
            @Override
            public void onSucceed(List<EquipmentInfo> data, OperationResult operationResult) {

                equipment.setEquipmentInfo(data.get(0));

                getUserInThread(equipment);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                EquipmentInfo equipmentInfo = new EquipmentInfo();
                equipmentInfo.setLabel("未知");
                equipmentInfo.setType("未知");

                equipment.setEquipmentInfo(equipmentInfo);

                getUserInThread(equipment);

            }
        });
    }

    private void getUserInThread(final Equipment equipment) {
        mEquipmentRemoteDataSource.getUsersInEquipment(equipment, new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                handleRetrieveUsers(data, equipment);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                handleRetrieveUserFail(equipment);
            }
        });
    }

    private void handleRetrieveUserFail(Equipment equipment) {
        if (mHandler != null && !onPause) {
            Message message = Message.obtain(mHandler, RETRY_GET_DATA, equipment);
            mHandler.sendMessageDelayed(message, RETRY_DELAY_MILLSECOND);
        }
    }

    private void handleRetrieveUsers(List<User> data, Equipment equipment) {
        if (data.isEmpty())
            return;

        synchronized (mUserLoadedEquipments) {

            for (Equipment userLoadedEquipment : mUserLoadedEquipments) {
                if (userLoadedEquipment == null || userLoadedEquipment.getHosts().contains(equipment.getHosts().get(0))
                        || userLoadedEquipment.getSerialNumber().equals(equipment.getSerialNumber()))
                    return;
            }

            mUserLoadedEquipments.add(equipment);
            mUserExpandableLists.add(data);

            Log.d(TAG, "EquipmentSearch: " + mUserExpandableLists.toString());
        }

        //update list
        if (mHandler != null && !onPause)
            mHandler.sendEmptyMessage(DATA_CHANGE);
    }


    private class EquipmentViewPagerAdapter extends PagerAdapter {

        private List<Equipment> mEquipments;

        EquipmentViewPagerAdapter() {
            mEquipments = new ArrayList<>();
        }

        void setEquipments(List<Equipment> equipments) {
            mEquipments.clear();
            mEquipments.addAll(equipments);
        }

        @Override
        public int getCount() {
            return mEquipments.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            EquipmentItemBinding binding = EquipmentItemBinding.inflate(LayoutInflater.from(container.getContext()), container, false);

            container.addView(binding.getRoot());

            EquipmentItemViewModel equipmentItemViewModel = new EquipmentItemViewModel();

            Equipment equipment = mEquipments.get(position);

            EquipmentInfo equipmentInfo = equipment.getEquipmentInfo();

            if (equipmentInfo != null) {

                equipmentItemViewModel.type.set(equipmentInfo.getType());
                equipmentItemViewModel.label.set(equipmentInfo.getLabel());

                if (equipmentInfo.getType().equals(EquipmentInfo.WS215I)) {

                    equipmentItemViewModel.equipmentIconID.set(R.drawable.equipment_215i);

                    equipmentItemViewModel.cardBackgroundColorID.set(ContextCompat.getColor(container.getContext(), R.color.login_ui_blue));

                    equipmentItemViewModel.backgroundColorID.set(R.color.equipment_ui_blue);

                    equipmentSearchView.setBackgroundColor(R.color.equipment_ui_blue);


                } else {

                    equipmentItemViewModel.equipmentIconID.set(R.drawable.virtual_machine);
                    equipmentItemViewModel.cardBackgroundColorID.set(ContextCompat.getColor(container.getContext(), R.color.virtual_machine_foreground_color));
                    equipmentItemViewModel.backgroundColorID.set(R.color.virtual_machine_background_color);

                    equipmentSearchView.setBackgroundColor(R.color.virtual_machine_background_color);

                }

            }

            List<String> hosts = equipment.getHosts();

            String and = container.getContext().getString(R.string.and);

            StringBuilder builder = new StringBuilder();
            for (String host : hosts) {
                builder.append(and);
                builder.append(host);
            }

            equipmentItemViewModel.ip.set(builder.substring(1));

            binding.setEquipmentItemViewModel(equipmentItemViewModel);

            return binding.getRoot();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((View) object);

        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private class EquipmentUserRecyclerViewAdapter extends RecyclerView.Adapter<EquipmentUserViewHolder> {

        private List<User> mCurrentUsers;

        EquipmentUserRecyclerViewAdapter() {
            mCurrentUsers = new ArrayList<>();
        }

        public void clearCurrentUsers() {
            mCurrentUsers.clear();
        }

        public void setCurrentUsers(List<User> currentUsers) {
            mCurrentUsers.addAll(currentUsers);
        }

        @Override
        public EquipmentUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            EquipmentUserItemBinding binding = EquipmentUserItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

            return new EquipmentUserViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(EquipmentUserViewHolder holder, int position) {

            holder.refreshView(mCurrentUsers.get(position));

        }

        @Override
        public int getItemCount() {
            return mCurrentUsers.size();
        }

    }

    private class EquipmentUserViewHolder extends BindingViewHolder {

        private TextView mUserDefaultPortrait;
        private TextView mChildName;

        private ViewGroup equipmentUserItemLayout;

        private EquipmentUserItemBinding binding;

        EquipmentUserViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            binding = (EquipmentUserItemBinding) viewDataBinding;

            mUserDefaultPortrait = binding.userDefaultPortrait;
            mChildName = binding.equipmentChildName;
            equipmentUserItemLayout = binding.equipmentUserItemLayout;
        }

        public void refreshView(final User user) {

            String childName = user.getUserName();

            if (childName.length() > 20) {
                childName = childName.substring(0, 20);
                childName += binding.getRoot().getContext().getString(R.string.android_ellipsize);
            }

            mChildName.setText(childName);

            String firstLetter = Util.getUserNameFirstLetter(childName);
            mUserDefaultPortrait.setText(firstLetter);

            if (user.getDefaultAvatarBgColor() == 0) {

                int avatarBgColor = random.nextInt(3) + 1;

                if (preAvatarBgColor != 0) {

                    if (avatarBgColor == preAvatarBgColor) {
                        if (avatarBgColor == 3) {
                            avatarBgColor--;
                        } else if (avatarBgColor == 1) {
                            avatarBgColor++;
                        } else {

                            if (random.nextBoolean()) {
                                avatarBgColor++;
                            } else {
                                avatarBgColor--;
                            }

                        }
                    }

                    preAvatarBgColor = avatarBgColor;

                } else {
                    preAvatarBgColor = avatarBgColor;
                }


                user.setDefaultAvatarBgColor(avatarBgColor);
            }

            mUserDefaultPortrait.setBackgroundResource(user.getDefaultAvatarBgColorResourceId());

            equipmentUserItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    threadManagerImpl.runOnCacheThread(new Runnable() {
                        @Override
                        public void run() {
                            loginWithUserInThread(user);
                        }
                    });

                }
            });

        }
    }


    private void loginWithUserInThread(final User user) {
        loginUseCase.loginWithUser(user, new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(final Boolean data, OperationResult result) {

                threadManagerImpl.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {

                        equipmentSearchView.handleLoginWithUserSucceed(data);
                    }
                });

            }

            @Override
            public void onFail(OperationResult result) {

                threadManagerImpl.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        equipmentSearchView.handleLoginWithUserFail(currentEquipment, user);
                    }
                });

            }


        });
    }

}
