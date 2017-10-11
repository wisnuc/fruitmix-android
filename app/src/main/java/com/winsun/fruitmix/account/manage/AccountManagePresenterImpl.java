package com.winsun.fruitmix.account.manage;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.winsun.fruitmix.AccountManageActivity;
import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.databinding.AccountChildItemBinding;
import com.winsun.fruitmix.databinding.AccountGroupItemBinding;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.logged.in.user.LoggedInWeChatUser;
import com.winsun.fruitmix.logout.LogoutUseCase;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.usecase.GetAllBindingLocalUserUseCase;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2017/6/22.
 */

public class AccountManagePresenterImpl implements AccountManagePresenter {

    private AccountManageView view;
    private LoggedInUserDataSource loggedInUserDataSource;

    private SystemSettingDataSource systemSettingDataSource;

    private GetAllBindingLocalUserUseCase getAllBindingLocalUserUseCase;

    private List<String> mEquipmentNames;
    private List<List<LoggedInUser>> mUsers;

    private boolean mNewUserLoginSucceed = false;
    private boolean mDeleteCurrentUser = false;
    private boolean mDeleteOtherUser = false;

    private AccountExpandableListViewAdapter mAdapter;

    private List<LoggedInUser> loggedInUsers;

    private String currentUserUUID;

    private LogoutUseCase logoutUseCase;

    public AccountManagePresenterImpl(AccountManageView view, LoggedInUserDataSource loggedInUserDataSource, SystemSettingDataSource systemSettingDataSource,
                                      GetAllBindingLocalUserUseCase getAllBindingLocalUserUseCase,LogoutUseCase logoutUseCase) {
        this.view = view;
        this.loggedInUserDataSource = loggedInUserDataSource;
        this.systemSettingDataSource = systemSettingDataSource;
        this.getAllBindingLocalUserUseCase = getAllBindingLocalUserUseCase;

        this.logoutUseCase = logoutUseCase;

        mEquipmentNames = new ArrayList<>();
        mUsers = new ArrayList<>();

        loggedInUsers = new ArrayList<>();
        currentUserUUID = systemSettingDataSource.getCurrentLoginUserUUID();

        mAdapter = new AccountExpandableListViewAdapter();

        refreshData();
    }

    private void refreshData() {

        mEquipmentNames.clear();
        mUsers.clear();

        loggedInUsers.clear();
        loggedInUsers.addAll(loggedInUserDataSource.getAllLoggedInUsers());

        final String guid = systemSettingDataSource.getCurrentLoginUserGUID();

        if (guid == null || guid.isEmpty()) {

            fillData();
            mAdapter.setData(mEquipmentNames, mUsers);
            mAdapter.notifyDataSetChanged();

        } else {

            String token = systemSettingDataSource.getCurrentLoginToken();

            getAllBindingLocalUserUseCase.getAllBindingLocalUser(guid, token, new BaseLoadDataCallback<LoggedInWeChatUser>() {
                @Override
                public void onSucceed(List<LoggedInWeChatUser> data, OperationResult operationResult) {

                    loggedInUsers.addAll(data);

                    fillData();
                    mAdapter.setData(mEquipmentNames, mUsers);
                    mAdapter.notifyDataSetChanged();

                }

                @Override
                public void onFail(OperationResult operationResult) {

                    fillData();
                    mAdapter.setData(mEquipmentNames, mUsers);
                    mAdapter.notifyDataSetChanged();

                }
            });

        }

    }

    @Override
    public BaseExpandableListAdapter getAdapter() {
        return mAdapter;
    }

    private void fillData() {

        LoggedInUser loggedInUser;

        for (int i = 0; i < loggedInUsers.size(); i++) {

            loggedInUser = loggedInUsers.get(i);
            String equipmentName = loggedInUser.getEquipmentName();

            if (mEquipmentNames.contains(equipmentName)) {

                mUsers.get(mEquipmentNames.indexOf(equipmentName)).add(loggedInUser);

            } else {

                List<LoggedInUser> users = new ArrayList<>();
                users.add(loggedInUser);

                mEquipmentNames.add(equipmentName);
                mUsers.add(users);
            }

        }

    }

    @Override
    public void onDestroy() {
        view = null;
    }

    @Override
    public void addAccount() {
        view.gotoEquipmentSearchActivity();
    }

    @Override
    public void handleBack() {

        if (mNewUserLoginSucceed) {
            view.setResultCode(NavPagerActivity.RESULT_FINISH_ACTIVITY);
        } else if (mDeleteCurrentUser) {
            view.setResultCode(NavPagerActivity.RESULT_LOGOUT);
        } else if (mDeleteOtherUser) {
            view.setResultCode(NavPagerActivity.RESULT_REFRESH_LOGGED_IN_USER);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == AccountManageActivity.START_EQUIPMENT_SEARCH && resultCode == RESULT_OK) {

            logoutUseCase.changeLoginUser();

            mNewUserLoginSucceed = true;
            handleBack();
            view.finishView();
        }

    }

    private class AccountExpandableListViewAdapter extends BaseExpandableListAdapter {

        private List<String> mEquipmentNames;
        private List<List<LoggedInUser>> mUsers;

        AccountExpandableListViewAdapter() {
            mEquipmentNames = new ArrayList<>();
            mUsers = new ArrayList<>();
        }

        void setData(List<String> equipmentNames, List<List<LoggedInUser>> users) {
            mEquipmentNames.clear();
            mEquipmentNames.addAll(equipmentNames);
            mUsers.clear();
            mUsers.addAll(users);
        }

        @Override
        public int getGroupCount() {
            return mEquipmentNames.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mUsers.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mEquipmentNames.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mUsers.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }


        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            AccountGroupItemBinding binding;

            if (convertView == null) {

                binding = AccountGroupItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                convertView = binding.getRoot();
            } else {
                binding = DataBindingUtil.getBinding(convertView);
            }

            binding.setEquipmentName(mEquipmentNames.get(groupPosition));
            binding.executePendingBindings();

            return convertView;

        }


        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            AccountChildItemBinding binding;

            if (convertView == null) {

                binding = AccountChildItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                convertView = binding.getRoot();

            } else {
                binding = DataBindingUtil.getBinding(convertView);
            }

            AccountChildViewModel model = new AccountChildViewModel(mUsers, groupPosition, childPosition);

            binding.setAccountChildViewModel(model);
            binding.executePendingBindings();

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }


    public class AccountChildViewModel {

        private LoggedInUser loggedInUser;

        private User user;

        private int groupPosition;
        private int childPosition;
        private List<List<LoggedInUser>> users;

        public AccountChildViewModel(List<List<LoggedInUser>> users, int groupPosition, int childPosition) {
            this.users = users;
            this.groupPosition = groupPosition;
            this.childPosition = childPosition;

            loggedInUser = users.get(groupPosition).get(childPosition);
            user = loggedInUser.getUser();
        }

        public String getAvatarName() {
            return Util.getUserNameFirstLetter(user.getUserName());
        }

        public int getBackgroundResource() {
            return user.getDefaultAvatarBgColorResourceId();
        }

        public String getUserName() {
            return user.getUserName();
        }

        public void deleteUser(Context context) {

            loggedInUserDataSource.deleteLoggedInUsers(Collections.singletonList(loggedInUser));

            refreshData();

            if (user.getUuid().equals(currentUserUUID)) {
                mDeleteCurrentUser = true;
            } else {
                mDeleteOtherUser = true;
            }

        }

    }


}
