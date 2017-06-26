package com.winsun.fruitmix;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.winsun.fruitmix.account.manage.AccountManagePresenter;
import com.winsun.fruitmix.account.manage.AccountManagePresenterImpl;
import com.winsun.fruitmix.account.manage.AccountManageView;
import com.winsun.fruitmix.databinding.AccountChildItemBinding;
import com.winsun.fruitmix.databinding.AccountGroupItemBinding;
import com.winsun.fruitmix.databinding.ActivityAccountManageBinding;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccountManageActivity extends BaseActivity implements AccountManageView {

    public static final String TAG = "AccountManageActivity";

    ExpandableListView mAccountExpandableListView;

    private List<String> mEquipmentNames;
    private List<List<LoggedInUser>> mUsers;

    private Context mContext;

    private AccountExpandableListViewAdapter mAdapter;

    public static final int START_EQUIPMENT_SEARCH = 0x1001;

    private boolean mNewUserLoginSucceed = false;
    private boolean mDeleteCurrentUser = false;
    private boolean mDeleteOtherUser = false;

    private AccountManagePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAccountManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_account_manage);

        mAccountExpandableListView = binding.accountExpandableListView;

        presenter = new AccountManagePresenterImpl(this);

        binding.setBaseView(this);

        binding.setAccountManagePresenter(presenter);

        mContext = this;

        mEquipmentNames = new ArrayList<>();
        mUsers = new ArrayList<>();

        fillData();

        mAdapter = new AccountExpandableListViewAdapter(mEquipmentNames, mUsers);
        mAccountExpandableListView.setAdapter(mAdapter);

        mAccountExpandableListView.setGroupIndicator(null);

        for (int i = 0; i < mAdapter.getGroupCount(); i++) {
            mAccountExpandableListView.expandGroup(i);
        }

    }

    @Override
    public void finishView() {
        handleBack();
        super.finishView();
    }

    @Override
    public void onBackPressed() {

        handleBack();

        super.onBackPressed();

    }

    private void handleBack() {
        if (mNewUserLoginSucceed) {
            setResult(NavPagerActivity.RESULT_FINISH_ACTIVITY);
        } else if (mDeleteCurrentUser) {
            setResult(NavPagerActivity.RESULT_LOGOUT);
        } else if (mDeleteOtherUser) {
            setResult(NavPagerActivity.RESULT_REFRESH_LOGGED_IN_USER);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;

        presenter.onDestroy();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == START_EQUIPMENT_SEARCH && resultCode == RESULT_OK) {

            FNAS.handleLogout();

            mNewUserLoginSucceed = true;
            handleBack();
            finish();
        }

    }

    @Override
    public void gotoEquipmentSearchActivity() {
        Intent intent = new Intent(mContext, EquipmentSearchActivity.class);
        intent.putExtra(Util.KEY_SHOULD_STOP_SERVICE, false);
        startActivityForResult(intent, START_EQUIPMENT_SEARCH);
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

                binding = AccountGroupItemBinding.inflate(LayoutInflater.from(mContext), parent, false);

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

                binding = AccountChildItemBinding.inflate(LayoutInflater.from(mContext), parent, false);

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

    class AccountGroupHolder {

        @BindView(R.id.group_item_text_view)
        TextView mGroupItemTextView;

        AccountGroupHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void refreshView(String equipmentName) {
            mGroupItemTextView.setText(equipmentName);
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

        public void deleteUser() {

            DBUtils.getInstance(mContext).deleteLoggerUserByUserUUID(user.getUuid());

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

    class AccountChildHolder {

        @BindView(R.id.user_default_portrait)
        TextView mAvatar;
        @BindView(R.id.user_name)
        TextView mUserName;
        @BindView(R.id.del_user)
        ViewGroup mDelUserLayout;

        AccountChildHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void refreshView(final List<List<LoggedInUser>> users, final int groupPosition, final int childPosition) {

            final LoggedInUser loggedInUser = users.get(groupPosition).get(childPosition);
            final User user = loggedInUser.getUser();

            mAvatar.setText(Util.getUserNameFirstLetter(user.getUserName()));
            mAvatar.setBackgroundResource(user.getDefaultAvatarBgColorResourceId());

            mUserName.setText(user.getUserName());
            mDelUserLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DBUtils.getInstance(mContext).deleteLoggerUserByUserUUID(user.getUuid());

                    users.get(groupPosition).remove(childPosition);

                    LocalCache.LocalLoggedInUsers.remove(loggedInUser);

                    mAdapter.notifyDataSetChanged();

                    if (user.getUuid().equals(FNAS.userUUID)) {
                        mDeleteCurrentUser = true;
                    } else {
                        mDeleteOtherUser = true;
                    }
                }
            });
        }
    }

}
