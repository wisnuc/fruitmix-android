package com.winsun.fruitmix.user.manage;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.component.UserAvatar;
import com.winsun.fruitmix.databinding.UserManageItemBinding;
import com.winsun.fruitmix.equipment.EquipmentItemViewModel;
import com.winsun.fruitmix.equipment.data.EquipmentDataSource;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.model.EquipmentInfo;
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

public class UserManagePresenterImpl implements UserMangePresenter {

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

        String currentIPWithHttpHead = systemSettingDataSource.getCurrentEquipmentIp();

        if(currentIPWithHttpHead.equals(HttpRequestFactory.CLOUD_IP)){

            stationsDataSource.getStationsByWechatGUID(systemSettingDataSource.getCurrentLoginUserGUID(), new BaseLoadDataCallback<Station>() {
                @Override
                public void onSucceed(List<Station> data, OperationResult operationResult) {

                    String currentStationID = systemSettingDataSource.getCurrentLoginStationID();

                    for (Station station:data){

                        if(station.getId().equals(currentStationID)){

                            currentIP = station.getFirstIp();

                            getEquipmentInfo(station);

                        }

                    }

                }

                @Override
                public void onFail(OperationResult operationResult) {

                    handleGetEquipmentInfoFail();

                }
            });


        }else {

            if (currentIPWithHttpHead.contains(Util.HTTP)) {
                String[] result = currentIPWithHttpHead.split(Util.HTTP);

                currentIP = result[1];

            } else {
                currentIP = currentIPWithHttpHead;
            }

            getEquipmentInfo();

        }

        getUserInThread();

    }

    private void getEquipmentInfo(final Station station){

        equipmentDataSource.getEquipmentInfo(currentIP, new BaseLoadDataCallback<EquipmentInfo>() {
            @Override
            public void onSucceed(List<EquipmentInfo> data, OperationResult operationResult) {

                EquipmentInfo equipmentInfo = data.get(0);

                equipmentInfo.setLabel(station.getLabel());

                setEquipmentInfo(equipmentInfo);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                handleGetEquipmentInfoFail();

            }
        });

    }

    private void getEquipmentInfo() {
        equipmentDataSource.getEquipmentInfo(currentIP, new BaseLoadDataCallback<EquipmentInfo>() {
            @Override
            public void onSucceed(List<EquipmentInfo> data, OperationResult operationResult) {

                EquipmentInfo equipmentInfo = data.get(0);

                setEquipmentInfo(equipmentInfo);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                handleGetEquipmentInfoFail();

            }
        });
    }

    private void handleGetEquipmentInfoFail() {
        EquipmentInfo equipmentInfo = new EquipmentInfo();

        setEquipmentInfo(equipmentInfo);
    }

    private void setEquipmentInfo(EquipmentInfo equipmentInfo) {

        if (equipmentInfo.getType().equals(EquipmentInfo.WS215I))
            equipmentItemViewModel.equipmentIconID.set(R.drawable.equipment_215i);
        else
            equipmentItemViewModel.equipmentIconID.set(R.drawable.virtual_machine);

        equipmentItemViewModel.type.set(equipmentInfo.getType());
        equipmentItemViewModel.label.set(equipmentInfo.getLabel());
        equipmentItemViewModel.ip.set(currentIP);
    }

    private void getUserInThread() {

        userDataRepository.getUsers(currentLoginUserUUID,new BaseLoadDataCallback<User>() {
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
        });

    }

    private void refreshUserList(List<User> users) {

        if (mUserList == null)
            mUserList = new ArrayList<>();
        else
            mUserList.clear();

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

            binding.setUser(new UserManageWrap(user));

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

            UserAvatar userAvatar =binding.userAvatar;

            userAvatar.setUser(user,imageLoader);

            return convertView;
        }
    }

    public class UserManageWrap {

        private User user;

        public UserManageWrap(User user) {
            this.user = user;
        }

        public String getUserName(Context context) {
            String userName = user.getUserName();

            if (userName.length() > 20) {
                userName = userName.substring(0, 20);
                userName += context.getString(R.string.android_ellipsize);
            }

            return userName;
        }

        public String getEmail() {
            return user.getEmail();
        }
    }


}
