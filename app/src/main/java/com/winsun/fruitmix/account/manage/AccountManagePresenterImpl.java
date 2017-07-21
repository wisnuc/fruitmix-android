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
import com.winsun.fruitmix.databinding.AccountChildItemBinding;
import com.winsun.fruitmix.databinding.AccountGroupItemBinding;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2017/6/22.
 */

public class AccountManagePresenterImpl implements AccountManagePresenter {

    private AccountManageView view;

    private List<String> mEquipmentNames;
    private List<List<LoggedInUser>> mUsers;

    private boolean mNewUserLoginSucceed = false;
    private boolean mDeleteCurrentUser = false;
    private boolean mDeleteOtherUser = false;

    private AccountExpandableListViewAdapter mAdapter;

    public AccountManagePresenterImpl(AccountManageView view) {
        this.view = view;

        mEquipmentNames = new ArrayList<>();
        mUsers = new ArrayList<>();

        fillData();

        mAdapter = new AccountExpandableListViewAdapter(mEquipmentNames, mUsers);
    }

    @Override
    public BaseExpandableListAdapter getAdapter() {
        return mAdapter;
    }

    private void fillData() {

        LoggedInUser loggedInUser;
        for (int i = 0; i < LocalCache.LocalLoggedInUsers.size(); i++) {

            loggedInUser = LocalCache.LocalLoggedInUsers.get(i);
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

            FNAS.handleLogout();

            mNewUserLoginSucceed = true;
            handleBack();
            view.finishView();
        }

    }

    private class AccountExpandableListViewAdapter extends BaseExpandableListAdapter {

        private List<String> equipmentNames;
        private List<List<LoggedInUser>> users;

        AccountExpandableListViewAdapter(List<String> equipmentNames, List<List<LoggedInUser>> users) {
            this.equipmentNames = equipmentNames;
            this.users = users;
        }

        @Override
        public int getGroupCount() {
            return equipmentNames.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return users.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return equipmentNames.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return users.get(groupPosition).get(childPosition);
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

            binding.setEquipmentName(equipmentNames.get(groupPosition));
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

            AccountChildViewModel model = new AccountChildViewModel(users, groupPosition, childPosition);

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

            DBUtils.getInstance(context).deleteLoggerUserByUserUUID(user.getUuid());

            users.get(groupPosition).remove(childPosition);

            LocalCache.LocalLoggedInUsers.remove(loggedInUser);

            mAdapter.notifyDataSetChanged();

            if (user.getUuid().equals(FNAS.userUUID)) {
                mDeleteCurrentUser = true;
            } else {
                mDeleteOtherUser = true;
            }

        }

    }


}
