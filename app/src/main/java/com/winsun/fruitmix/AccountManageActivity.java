package com.winsun.fruitmix;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccountManageActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.back)
    ImageView mBackImageView;
    @BindView(R.id.account_expandable_list_view)
    ExpandableListView mAccountExpandableListView;
    @BindView(R.id.add_account)
    FloatingActionButton mAddAccountBtn;

    private List<String> mEquipmentNames;
    private List<List<LoggedInUser>> mUsers;

    private Context mContext;

    private AccountExpandableListViewAdapter mAdapter;

    public static final int START_EQUIPMENT_SEARCH = 0x1001;

    private boolean mNewUserLoginSucceed = false;
    private boolean mDeleteCurrentUser = false;
    private boolean mDeleteOtherUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manage);

        ButterKnife.bind(this);

        mContext = this;

        mBackImageView.setOnClickListener(this);
        mAddAccountBtn.setOnClickListener(this);

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
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back:
                handleBack();
                finish();
                break;
            case R.id.add_account:
                Intent intent = new Intent(mContext, EquipmentSearchActivity.class);
                intent.putExtra(Util.KEY_SHOULD_STOP_SERVICE, false);
                startActivityForResult(intent, START_EQUIPMENT_SEARCH);
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == START_EQUIPMENT_SEARCH && resultCode == RESULT_OK) {

            EventBus.getDefault().post(new RequestEvent(OperationType.STOP_UPLOAD, null));

            ButlerService.stopTimingRetrieveMediaShare();

            Util.setRemoteMediaLoaded(false);
            Util.setRemoteMediaShareLoaded(false);

            mNewUserLoginSucceed = true;
            handleBack();
            finish();
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

            AccountGroupHolder groupViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.account_group_item, parent, false);

                groupViewHolder = new AccountGroupHolder(convertView);
                convertView.setTag(groupViewHolder);
            } else {
                groupViewHolder = (AccountGroupHolder) convertView.getTag();
            }

            groupViewHolder.refreshView(equipmentNames.get(groupPosition));

            return convertView;

        }


        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            AccountChildHolder groupViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.account_child_item, parent, false);

                groupViewHolder = new AccountChildHolder(convertView);
                convertView.setTag(groupViewHolder);
            } else {
                groupViewHolder = (AccountChildHolder) convertView.getTag();
            }

            groupViewHolder.refreshView(mUsers, groupPosition, childPosition);

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
