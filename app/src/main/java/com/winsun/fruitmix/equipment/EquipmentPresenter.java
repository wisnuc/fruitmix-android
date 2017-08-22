package com.winsun.fruitmix.equipment;

import android.databinding.ViewDataBinding;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.winsun.fruitmix.EquipmentSearchActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.EquipmentItemBinding;
import com.winsun.fruitmix.databinding.EquipmentUserItemBinding;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BaseBindingViewHolder;
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

    private final List<Equipment> mUserLoadedEquipments = new ArrayList<>();

    private Equipment currentEquipment;

    private List<List<User>> mUserExpandableLists;

    private CustomHandler mHandler;

    private static final int DATA_CHANGE = 0x0001;

    private static final int RETRY_GET_USER = 0x0002;

    private static final int RETRY_DELAY_MILLSECOND = 20 * 1000;

    private EquipmentSearchManager mEquipmentSearchManager;

    private EquipmentRemoteDataSource mEquipmentRemoteDataSource;

    private LoginUseCase loginUseCase;

    private ThreadManager threadManager;

    private Random random;

    private int preAvatarBgColor = 0;

    private boolean onPause = false;

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

                case RETRY_GET_USER:

                    Equipment equipment = (Equipment) msg.obj;

                    Log.d(TAG, "handleMessage: retry get user equipment host0: " + equipment.getHosts().get(0));

                    getUserList(equipment);

                default:
            }
        }
    }

    public EquipmentPresenter(LoadingViewModel loadingViewModel, EquipmentSearchView equipmentSearchView,
                              EquipmentSearchManager mEquipmentSearchManager, EquipmentRemoteDataSource mEquipmentRemoteDataSource,
                              LoginUseCase loginUseCase, ThreadManager threadManager) {
        this.loadingViewModel = loadingViewModel;
        this.equipmentSearchView = equipmentSearchView;
        this.mEquipmentSearchManager = mEquipmentSearchManager;
        this.mEquipmentRemoteDataSource = mEquipmentRemoteDataSource;
        this.loginUseCase = loginUseCase;
        this.threadManager = threadManager;

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

    public void onResume() {

        startDiscovery();

        onPause = false;

    }

    public void onPause() {

        stopDiscovery();

        onPause = true;

        mHandler.removeMessages(RETRY_GET_USER);
        mHandler.removeMessages(DATA_CHANGE);

    }

    public void onDestroy() {

        mHandler.removeMessages(RETRY_GET_USER);
        mHandler.removeMessages(DATA_CHANGE);

        mHandler = null;

        equipmentSearchView = null;

    }

    public void handleInputIpbyByUser(String ip) {
        List<String> hosts = new ArrayList<>();
        hosts.add(ip);

        Equipment equipment = new Equipment("Winsuc Appliction " + ip, hosts, 6666);
        getUserList(equipment);
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

                getUserList(equipment);
            }
        });

    }

    private void stopDiscovery() {

        mEquipmentSearchManager.stopDiscovery();
    }

    private void getUserList(final Equipment equipment) {

        threadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                getUserInThread(equipment);

            }
        });


    }

    private void getEquipmentHostAlias(final Equipment equipment) {
        mEquipmentRemoteDataSource.getEquipmentHostAlias(equipment, new BaseLoadDataCallback<String>() {
            @Override
            public void onSucceed(List<String> data, OperationResult operationResult) {

                for (String alias : data) {

                    List<String> hosts = equipment.getHosts();
                    if (!hosts.contains(alias)) {
                        hosts.add(alias);
                    }

                }

                getUserInThread(equipment);

            }

            @Override
            public void onFail(OperationResult operationResult) {

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
            Message message = Message.obtain(mHandler, RETRY_GET_USER, equipment);
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

            equipmentItemViewModel.name.set(equipment.getModel() + "-" + equipment.getSerialNumber());

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

                    threadManager.runOnCacheThread(new Runnable() {
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

                threadManager.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {

                        equipmentSearchView.handleLoginWithUserSucceed(data);
                    }
                });

            }

            @Override
            public void onFail(OperationResult result) {

                threadManager.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        equipmentSearchView.handleLoginWithUserFail(currentEquipment, user);
                    }
                });

            }


        });
    }

}
