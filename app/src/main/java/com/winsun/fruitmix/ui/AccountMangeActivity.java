package com.winsun.fruitmix.ui;

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

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.common.BaseActivity;
import com.winsun.fruitmix.common.Injection;
import com.winsun.fruitmix.contract.AccountManageContract;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.presenter.AccountManagePresenterImpl;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccountMangeActivity extends BaseActivity implements AccountManageContract.AccountManageView, View.OnClickListener {
    @BindView(R.id.back)
    ImageView mBackImageView;
    @BindView(R.id.account_expandable_list_view)
    ExpandableListView mAccountExpandableListView;
    @BindView(R.id.add_account)
    FloatingActionButton mAddAccountBtn;

    private Context mContext;

    private AccountExpandableListViewAdapter mAdapter;

    private AccountManageContract.AccountManagePresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manage);

        ButterKnife.bind(this);

        mContext = this;

        mBackImageView.setOnClickListener(this);
        mAddAccountBtn.setOnClickListener(this);

        mPresenter = new AccountManagePresenterImpl(Injection.injectDataRepository(mContext));

        mPresenter.attachView(this);

        mAdapter = new AccountExpandableListViewAdapter();
        mAccountExpandableListView.setAdapter(mAdapter);

        mAccountExpandableListView.setGroupIndicator(null);

        mPresenter.initView();

        for (int i = 0; i < mAdapter.getGroupCount(); i++) {
            mAccountExpandableListView.expandGroup(i);
        }


    }

    @Override
    public void onBackPressed() {

        mPresenter.handleBackEvent();

        super.onBackPressed();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPresenter.detachView();

        mContext = null;

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back:
                mPresenter.handleBackEvent();
                break;
            case R.id.add_account:
                Intent intent = new Intent(mContext, EquipmentSearchActivity.class);
                intent.putExtra(Util.KEY_SHOULD_CALL_LOGOUT, true);
                startActivityForResult(intent, AccountManagePresenterImpl.START_EQUIPMENT_SEARCH);
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mPresenter.handleOnActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void setData(List<String> equipmentNames, List<List<LoggedInUser>> users) {
        mAdapter.setData(equipmentNames, users);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void finishActivity() {
        finish();
    }

    private class AccountExpandableListViewAdapter extends BaseExpandableListAdapter {

        private List<String> equipmentNames;
        private List<List<LoggedInUser>> users;

        AccountExpandableListViewAdapter() {
            equipmentNames = new ArrayList<>();
            users = new ArrayList<>();
        }

        void setData(List<String> equipmentNames, List<List<LoggedInUser>> users) {

            this.equipmentNames.clear();
            this.users.clear();

            this.equipmentNames.addAll(equipmentNames);
            this.users.addAll(users);
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

            groupViewHolder.refreshView(users, groupPosition, childPosition);

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

                    mPresenter.deleteUserOnClick(groupPosition, childPosition);

                }
            });
        }
    }

}
