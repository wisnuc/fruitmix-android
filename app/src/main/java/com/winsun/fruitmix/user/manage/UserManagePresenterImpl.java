package com.winsun.fruitmix.user.manage;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.component.UserAvatar;
import com.winsun.fruitmix.databinding.UserManageItemBinding;
import com.winsun.fruitmix.equipment.search.EquipmentItemViewModel;
import com.winsun.fruitmix.equipment.search.data.EquipmentDataSource;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.equipment.search.data.EquipmentTypeInfo;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.stations.Station;
import com.winsun.fruitmix.stations.StationsDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.Util;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2017/6/21.
 */

public class UserManagePresenterImpl implements UserMangePresenter, ActiveView {

    public static final String TAG = UserManagePresenterImpl.class.getSimpleName();

    private UserManageView userManageView;

    private List<User> mUserList;

    private UserListAdapter mUserListAdapter;

    private UserManageActivity.UserManageViewModel userManageViewModel;

    private EquipmentItemViewModel equipmentItemViewModel;

    private UserDataRepository userDataRepository;

    private SystemSettingDataSource systemSettingDataSource;

    private StationsDataSource stationsDataSource;

    private ImageLoader imageLoader;

    private EquipmentDataSource equipmentDataSource;
    private String currentIP;
    private String currentLoginUserUUID;

    public UserManagePresenterImpl(UserManageView userManageView, EquipmentItemViewModel equipmentItemViewModel, UserManageActivity.UserManageViewModel userManageViewModel, UserDataRepository userDataRepository,
                                   EquipmentDataSource equipmentDataSource, SystemSettingDataSource systemSettingDataSource, StationsDataSource stationsDataSource,
                                   ImageLoader imageLoader) {
        this.userManageView = userManageView;
        this.userDataRepository = userDataRepository;
        this.userManageViewModel = userManageViewModel;
        this.equipmentItemViewModel = equipmentItemViewModel;
        this.equipmentDataSource = equipmentDataSource;
        this.systemSettingDataSource = systemSettingDataSource;
        this.stationsDataSource = stationsDataSource;

        this.imageLoader = imageLoader;

        this.currentLoginUserUUID = systemSettingDataSource.getCurrentLoginUserUUID();

        mUserListAdapter = new UserListAdapter();
    }

    @Override
    public BaseAdapter getAdapter() {
        return mUserListAdapter;
    }

    @Override
    public void refreshView() {

        getEquipmentInfoInThread();

        getUser(true);

    }

    @Override
    public void refreshUserFromCache() {

        getUser(false);

    }

    private void getEquipmentInfoInThread() {
        String currentIPWithHttpHead = systemSettingDataSource.getCurrentEquipmentIp();

        if (currentIPWithHttpHead.equals(HttpRequestFactory.CLOUD_IP)) {

            stationsDataSource.getStationsByWechatGUID(systemSettingDataSource.getCurrentLoginUserGUID(), new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<Station>() {
                @Override
                public void onSucceed(List<Station> data, OperationResult operationResult) {

                    String currentStationID = systemSettingDataSource.getCurrentLoginStationID();

                    for (Station station : data) {

                        if (station.getId().equals(currentStationID)) {

                            currentIP = station.getFirstIp();

                            getEquipmentInfo(station);

                        }

                    }

                }

                @Override
                public void onFail(OperationResult operationResult) {

                    handleGetEquipmentInfoFail();

                }
            }, this));


        } else {

            if (currentIPWithHttpHead.contains(Util.HTTP)) {
                String[] result = currentIPWithHttpHead.split(Util.HTTP);

                currentIP = result[1];

            } else {
                currentIP = currentIPWithHttpHead;
            }

            getEquipmentInfo();

        }
    }

    private void getEquipmentInfo(final Station station) {

        equipmentDataSource.getEquipmentTypeInfo(currentIP, new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<EquipmentTypeInfo>() {
            @Override
            public void onSucceed(List<EquipmentTypeInfo> data, OperationResult operationResult) {

                EquipmentTypeInfo equipmentTypeInfo = data.get(0);

                equipmentTypeInfo.setLabel(station.getLabel());

                setEquipmentInfo(equipmentTypeInfo);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                handleGetEquipmentInfoFail();

            }
        }, this));

    }

    private void getEquipmentInfo() {
        equipmentDataSource.getEquipmentTypeInfo(currentIP, new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<EquipmentTypeInfo>() {
            @Override
            public void onSucceed(List<EquipmentTypeInfo> data, OperationResult operationResult) {

                EquipmentTypeInfo equipmentTypeInfo = data.get(0);

                setEquipmentInfo(equipmentTypeInfo);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                handleGetEquipmentInfoFail();

            }
        }, this));
    }

    private void handleGetEquipmentInfoFail() {
        EquipmentTypeInfo equipmentTypeInfo = new EquipmentTypeInfo();

        setEquipmentInfo(equipmentTypeInfo);
    }

    private void setEquipmentInfo(EquipmentTypeInfo equipmentTypeInfo) {

        String type = equipmentTypeInfo.getType(userManageView.getContext());

        if (type.equals(EquipmentTypeInfo.WS215I)) {

            equipmentItemViewModel.equipmentIconID.set(R.drawable.equipment_215i);
        } else {

            equipmentItemViewModel.equipmentIconID.set(R.drawable.virtual_machine);
        }

        equipmentItemViewModel.type.set(type);

        equipmentItemViewModel.label.set(equipmentTypeInfo.getFormatLabel(userManageView.getContext()));

        equipmentItemViewModel.ip.set(currentIP);
    }

    private void getUser(boolean setCacheDirty) {

        if (setCacheDirty)
            userDataRepository.setCacheDirty();

        userDataRepository.getUsers(currentLoginUserUUID, new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(final List<User> data, OperationResult operationResult) {

                userManageViewModel.showUserListEmpty.set(false);
                userManageViewModel.showUserListView.set(true);

                refreshUserList(data);

                mUserListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                userManageViewModel.showUserListEmpty.set(true);
                userManageViewModel.showUserListView.set(false);

            }
        }, this));

    }

    private void refreshUserList(List<User> users) {

        if (mUserList == null)
            mUserList = new ArrayList<>();
        else
            mUserList.clear();

/*        for (User user : users) {

            if (!user.isDisabled())
                mUserList.add(user);

        }*/

        mUserList.addAll(users);

        Collections.sort(mUserList, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return Collator.getInstance(Locale.CHINESE).compare(lhs.getUserName(), (rhs.getUserName()));
            }
        });

    }


    @Override
    public void onDestroy() {
        userManageView = null;
    }

    @Override
    public boolean isActive() {
        return userManageView != null;
    }

    @Override
    public void addUser() {
        userManageView.gotoCreateUserActivity();
    }

    private class UserListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mUserList == null ? 0 : mUserList.size();
        }

        @Override
        public Object getItem(int position) {
            return mUserList == null ? null : mUserList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            UserManageItemBinding binding;

            if (convertView == null) {

                binding = UserManageItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                convertView = binding.getRoot();

            } else {

                binding = DataBindingUtil.getBinding(convertView);
            }

            User user = mUserList.get(position);

            binding.setUser(user);

            binding.setUserManageView(userManageView);

            binding.executePendingBindings();

/*            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewHolder.getDelUserVisibility() != View.VISIBLE) {
                        viewHolder.setDelUserVisibility(View.VISIBLE);
                    } else {
                        viewHolder.setDelUserVisibility(View.INVISIBLE);
                    }
                }
            });*/

            UserAvatar userAvatar = binding.userAvatar;

            userAvatar.setUser(user, imageLoader);

            return convertView;
        }
    }


}
