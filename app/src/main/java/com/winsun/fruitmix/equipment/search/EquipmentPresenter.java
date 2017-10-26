package com.winsun.fruitmix.equipment.search;

import android.databinding.ViewDataBinding;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.component.UserAvatar;
import com.winsun.fruitmix.databinding.EquipmentItemBinding;
import com.winsun.fruitmix.databinding.EquipmentUserItemBinding;
import com.winsun.fruitmix.equipment.search.data.EquipmentDataSource;
import com.winsun.fruitmix.equipment.search.data.EquipmentSearchManager;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.equipment.search.data.Equipment;
import com.winsun.fruitmix.equipment.search.data.EquipmentTypeInfo;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
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

    private EquipmentSearchViewModel equipmentSearchViewModel;

    private final List<Equipment> mUserLoadedEquipments = new ArrayList<>();

    private List<String> equipmentSerialNumbers = new ArrayList<>();

    private List<String> equipmentIps = new ArrayList<>();

    private Equipment currentEquipment;

    private List<List<User>> mUserExpandableLists;

    private CustomHandler mHandler;

    private static final int DATA_CHANGE = 0x0001;

    private static final int RETRY_GET_DATA = 0x0002;

    private static final int DISCOVERY_TIMEOUT = 0x0003;

    private static final int RESUME_DISCOVERY = 0x0004;

    private static final int RETRY_DELAY_MILLISECOND = 20 * 1000;

    private static final int DISCOVERY_TIMEOUT_TIME = 6 * 1000;
    private static final int RESUME_DISCOVERY_INTERVAL = 3 * 1000;

    private EquipmentSearchManager mEquipmentSearchManager;

    private EquipmentDataSource mEquipmentDataSource;

    private LoginUseCase loginUseCase;

    private ImageLoader imageLoader;

    private Random random;

    private int preAvatarBgColor = 0;

    private boolean onPause = false;

    private boolean hasFindEquipment = false;

    private boolean hasRefreshFirstEquipmentUsers = false;

    private class CustomHandler extends Handler {

        WeakReference<EquipmentPresenter> weakReference = null;

        private boolean hasForceRefresh = false;

        CustomHandler(EquipmentPresenter presenter, Looper looper) {
            super(looper);
            weakReference = new WeakReference<>(presenter);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DATA_CHANGE:

                    EquipmentViewPagerAdapter adapter = weakReference.get().equipmentViewPagerAdapter;

                    if (!hasFindEquipment)
                        hasFindEquipment = true;

                    handleFindEquipment(adapter);

                    break;

                case RETRY_GET_DATA:

                    Equipment equipment = (Equipment) msg.obj;

                    Log.d(TAG, "handleMessage: retry get user equipment_blue host0: " + equipment.getHosts().get(0));

                    getEquipmentInfo(equipment);

                    break;

                case DISCOVERY_TIMEOUT:

                    if (!hasFindEquipment) {

                        loadingViewModel.showLoading.set(false);

                        equipmentSearchViewModel.showEquipmentViewPager.set(true);
                        equipmentSearchViewModel.showEquipmentViewPagerIndicator.set(false);
                        equipmentSearchViewModel.showEquipmentUsers.set(false);

                        equipmentViewPagerAdapter.setEquipments(Collections.singletonList(Equipment.NULL));
                        equipmentViewPagerAdapter.notifyDataSetChanged();
                    } else {

                        handleFindEquipment(weakReference.get().equipmentViewPagerAdapter);

                    }

                    break;

                case RESUME_DISCOVERY:

                    if (!hasFindEquipment) {

                        equipmentSearchViewModel.showEquipmentViewPager.set(false);
                        equipmentSearchViewModel.showEquipmentViewPagerIndicator.set(false);
                        equipmentSearchViewModel.showEquipmentUsers.set(false);

                        loadingViewModel.showLoading.set(true);

                    } else {

                        handleFindEquipment(weakReference.get().equipmentViewPagerAdapter);

                    }

                    mHandler.sendEmptyMessageDelayed(DISCOVERY_TIMEOUT, DISCOVERY_TIMEOUT_TIME);

                    break;

                default:
            }
        }

        private void handleFindEquipment(EquipmentViewPagerAdapter adapter) {
            if (loadingViewModel.showLoading.get()) {
                loadingViewModel.showLoading.set(false);
            }

            if (!hasForceRefresh) {
                hasForceRefresh = true;

                adapter.setForceRefresh();
            }

            equipmentSearchViewModel.showEquipmentViewPager.set(true);
            equipmentSearchViewModel.showEquipmentViewPagerIndicator.set(true);
            equipmentSearchViewModel.showEquipmentUsers.set(true);

            adapter.setEquipments(mUserLoadedEquipments);

            adapter.notifyDataSetChanged();

            if (equipmentSearchView.getCurrentViewPagerItem() == 0 && !hasRefreshFirstEquipmentUsers) {

                refreshEquipment(0);

                hasRefreshFirstEquipmentUsers = true;

            }

        }
    }

    public EquipmentPresenter(LoadingViewModel loadingViewModel, EquipmentSearchViewModel equipmentSearchViewModel, EquipmentSearchView equipmentSearchView,
                              EquipmentSearchManager mEquipmentSearchManager, EquipmentDataSource equipmentDataSource,
                              LoginUseCase loginUseCase, ImageLoader imageLoader) {
        this.loadingViewModel = loadingViewModel;
        this.equipmentSearchViewModel = equipmentSearchViewModel;
        this.equipmentSearchView = equipmentSearchView;
        this.mEquipmentSearchManager = mEquipmentSearchManager;
        this.mEquipmentDataSource = equipmentDataSource;
        this.loginUseCase = loginUseCase;

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

    public void onCreate() {

        startDiscovery();

        // handle for huawei mate9 search result has no ip

 /*       getEquipmentTypeInfo(new Equipment("", Collections.singletonList("192.168.0.81"), 0));

        hasFindEquipment = true;*/

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

            }
        });

        mHandler.sendEmptyMessageDelayed(DISCOVERY_TIMEOUT, DISCOVERY_TIMEOUT_TIME);

    }

    private void stopDiscovery() {

        mEquipmentSearchManager.stopDiscovery();
    }

    private void getEquipmentInfo(final Equipment equipment) {

        if (equipment.getSerialNumber().length() != 0) {

            if (equipmentSerialNumbers.contains(equipment.getSerialNumber())) {

                Log.d(TAG, "getEquipmentTypeInfo: serial number has founded: " + equipment.getSerialNumber());

                return;
            } else {
                equipmentSerialNumbers.add(equipment.getSerialNumber());
            }

        }

        if (equipmentIps.contains(equipment.getHosts().get(0))) {

            Log.d(TAG, "getEquipmentTypeInfo: host has founded: " + equipment.getHosts().get(0));

            return;
        } else
            equipmentIps.add(equipment.getHosts().get(0));

        synchronized (mUserLoadedEquipments) {

            for (Equipment loadedEquipment : mUserLoadedEquipments) {
                if (loadedEquipment.getSerialNumber().equals(equipment.getSerialNumber())) {

                    Log.d(TAG, "getEquipmentTypeInfo: second check in user loaded equipments,serial number has founded: " + equipment.getSerialNumber());

                    return;
                } else if (loadedEquipment.getHosts().contains(equipment.getHosts().get(0))) {

                    Log.d(TAG, "getEquipmentTypeInfo: second check in user loaded equipments,host has founded: " + equipment.getHosts().get(0));

                    return;
                }

            }

            getEquipmentInfoInThread(equipment);

        }

    }

    private void getEquipmentInfoInThread(final Equipment equipment) {
        mEquipmentDataSource.getEquipmentTypeInfo(equipment.getHosts().get(0), new BaseLoadDataCallback<EquipmentTypeInfo>() {
            @Override
            public void onSucceed(List<EquipmentTypeInfo> data, OperationResult operationResult) {

                EquipmentTypeInfo equipmentTypeInfo = data.get(0);

                Log.d(TAG, "onSucceed: equipment info: " + equipmentTypeInfo);

                if (equipmentTypeInfo == null) {
                    equipmentTypeInfo = new EquipmentTypeInfo();
                }

                equipment.setEquipmentTypeInfo(equipmentTypeInfo);

                getUserInThread(equipment);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                Log.d(TAG, "onFail: get equipment info");

                EquipmentTypeInfo equipmentTypeInfo = new EquipmentTypeInfo();

                equipment.setEquipmentTypeInfo(equipmentTypeInfo);

                getUserInThread(equipment);

            }
        });
    }

    private void getUserInThread(final Equipment equipment) {
        mEquipmentDataSource.getUsersInEquipment(equipment, new BaseLoadDataCallback<User>() {
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
            mHandler.sendMessageDelayed(message, RETRY_DELAY_MILLISECOND);
        }
    }

    private void handleRetrieveUsers(List<User> data, Equipment equipment) {
        if (data.isEmpty())
            return;

        synchronized (mUserLoadedEquipments) {

            for (Equipment userLoadedEquipment : mUserLoadedEquipments) {
                if (userLoadedEquipment.getSerialNumber().equals(equipment.getSerialNumber()))
                    return;
                else if (userLoadedEquipment.getHosts().contains(equipment.getHosts().get(0)))
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

        private boolean forceRefresh = false;

        EquipmentViewPagerAdapter() {
            mEquipments = new ArrayList<>();
        }

        void setEquipments(List<Equipment> equipments) {
            mEquipments.clear();
            mEquipments.addAll(equipments);
        }

        public void setForceRefresh() {
            forceRefresh = true;
        }

        @Override
        public int getCount() {
            return mEquipments.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            Log.d(TAG, "instantiateItem: position: " + position);

            EquipmentItemBinding binding = EquipmentItemBinding.inflate(LayoutInflater.from(container.getContext()), container, false);

            container.addView(binding.getRoot());

            EquipmentItemViewModel equipmentItemViewModel = new EquipmentItemViewModel();

            Equipment equipment = mEquipments.get(position);

            if (equipment.equals(Equipment.NULL)) {

                equipmentItemViewModel.showNoEquipment.set(true);
                equipmentItemViewModel.showEquipment.set(false);

//                binding.noEquipmentLayout.setVisibility(View.VISIBLE);
//                binding.equipmentLayout.setVisibility(View.GONE);

                initEquipmentViewModelDefaultBackgroundColor(container, equipmentItemViewModel);

            } else {

                equipmentItemViewModel.showNoEquipment.set(false);
                equipmentItemViewModel.showEquipment.set(true);

//                binding.noEquipmentLayout.setVisibility(View.GONE);
//                binding.equipmentLayout.setVisibility(View.VISIBLE);

                initEquipmentItemViewModel(container, equipmentItemViewModel, equipment);

            }

            binding.setEquipmentItemViewModel(equipmentItemViewModel);

            return binding.getRoot();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            Log.d(TAG, "destroyItem: position: " + position);

            container.removeView((View) object);

        }

        @Override
        public int getItemPosition(Object object) {

            if (forceRefresh) {

                forceRefresh = false;

                return POSITION_NONE;

            }

            return super.getItemPosition(object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private void initEquipmentViewModelDefaultBackgroundColor(ViewGroup container, EquipmentItemViewModel equipmentItemViewModel) {
        equipmentItemViewModel.cardBackgroundColorID.set(ContextCompat.getColor(container.getContext(), R.color.login_ui_blue));

        equipmentItemViewModel.backgroundColorID.set(R.color.equipment_ui_blue);

        equipmentSearchView.setBackgroundColor(R.color.equipment_ui_blue);
    }

    private void initEquipmentItemViewModel(ViewGroup container, EquipmentItemViewModel equipmentItemViewModel, Equipment equipment) {
        EquipmentTypeInfo equipmentTypeInfo = equipment.getEquipmentTypeInfo();

        if (equipmentTypeInfo != null) {

            String type = equipmentTypeInfo.getType();

            equipmentItemViewModel.type.set(type);

            equipmentItemViewModel.label.set(equipmentTypeInfo.getLabel());

            if (type.equals(EquipmentTypeInfo.WS215I)) {

                equipmentItemViewModel.equipmentIconID.set(R.drawable.equipment_215i);

                initEquipmentViewModelDefaultBackgroundColor(container, equipmentItemViewModel);

            } else {

                equipmentItemViewModel.equipmentIconID.set(R.drawable.virtual_machine);

                initEquipmentViewModelDefaultBackgroundColor(container, equipmentItemViewModel);

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

            for (User user : currentUsers) {

                if (!user.isDisabled())
                    mCurrentUsers.add(user);

            }

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

        private UserAvatar userAvatar;
        private TextView mChildName;

        private ViewGroup equipmentUserItemLayout;

        private EquipmentUserItemBinding binding;

        EquipmentUserViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            binding = (EquipmentUserItemBinding) viewDataBinding;

            userAvatar = binding.userAvatar;
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

            userAvatar.setUser(user, imageLoader);

            equipmentUserItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    loginWithUserInThread(user);

                }
            });

        }
    }


    private void loginWithUserInThread(final User user) {

        loginUseCase.loginWithUser(user, new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(final Boolean data, OperationResult result) {

                equipmentSearchView.handleLoginWithUserSucceed(data);

            }

            @Override
            public void onFail(OperationResult result) {

                equipmentSearchView.handleLoginWithUserFail(currentEquipment, user);

            }

        });
    }

}
