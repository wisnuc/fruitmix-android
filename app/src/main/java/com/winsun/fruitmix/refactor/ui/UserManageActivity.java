package com.winsun.fruitmix.refactor.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.refactor.common.BaseActivity;
import com.winsun.fruitmix.refactor.common.Injection;
import com.winsun.fruitmix.refactor.contract.UserManageContract;
import com.winsun.fruitmix.refactor.presenter.UserManagePresenterImpl;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserManageActivity extends BaseActivity implements UserManageContract.UserManageView, View.OnClickListener {

    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.user_list)
    ListView mUserListView;
    @BindView(R.id.user_list_empty)
    TextView mUserListEmpty;
    @BindView(R.id.add_user)
    FloatingActionButton mAddUserBtn;

    private Context mContext;

    private UserListAdapter mUserListAdapter;

    private UserManageContract.UserManagePresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);

        ButterKnife.bind(this);

        mContext = this;

        mBack.setOnClickListener(this);
        mAddUserBtn.setOnClickListener(this);

        mUserListAdapter = new UserListAdapter();
        mUserListView.setAdapter(mUserListAdapter);

        mPresenter = new UserManagePresenterImpl(Injection.injectDataRepository(mContext));
        mPresenter.attachView(this);
        mPresenter.initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;
    }

    @Override
    public void showContentUI() {
        super.showContentUI();

        mUserListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissContentUI() {
        super.dismissContentUI();

        mUserListView.setVisibility(View.GONE);
    }

    @Override
    public void showNoContentUI() {
        super.showNoContentUI();

        mUserListEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissNoContentUI() {
        super.dismissNoContentUI();

        mUserListEmpty.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                mPresenter.handleBackEvent();
                break;
            case R.id.add_user:

                mPresenter.addUserBtnClick();

                Intent intent = new Intent(mContext, CreateUserActivity.class);
                startActivityForResult(intent, Util.KEY_CREATE_USER_REQUEST_CODE);

                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mPresenter.handleOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showUsers(List<User> users) {

        mUserListAdapter.setUserList(users);
        mUserListAdapter.notifyDataSetChanged();
    }

    @Override
    public void finishActivity() {
        finish();
    }

    private class UserListAdapter extends BaseAdapter {

        private List<User> mUserList;

        UserListAdapter() {
            mUserList = new ArrayList<>();
        }

        void setUserList(List<User> mUserList) {
            this.mUserList = mUserList;
        }

        @Override
        public int getCount() {
            return mUserList == null ? 0 : mUserList.size();
        }

        @Override
        public Object getItem(int position) {
            return mUserListEmpty == null ? null : mUserList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.user_manage_item, parent, false);

                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.refreshView(mUserList.get(position));

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

            return convertView;
        }
    }

    class ViewHolder {
        @BindView(R.id.user_default_portrait)
        TextView mUserDefaultPortrait;
        @BindView(R.id.user_name)
        TextView mUserName;
        @BindView(R.id.user_email)
        TextView mUserEmail;
        @BindView(R.id.del_user)
        LinearLayout mDelUser;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void refreshView(User user) {

            String userName = user.getUserName();

            if (userName.length() > 20) {
                userName = userName.substring(0, 20);
                userName += mContext.getString(R.string.android_ellipsize);
            }

            mUserName.setText(userName);
            if (user.getEmail().length() > 0) {
                mUserEmail.setVisibility(View.VISIBLE);
                mUserEmail.setText(user.getEmail());
            } else {
                mUserEmail.setVisibility(View.GONE);
            }

            mUserDefaultPortrait.setText(Util.getUserNameFirstLetter(user.getUserName()));
        }

        void setDelUserVisibility(int visibility) {
            mDelUser.setVisibility(visibility);
        }

        int getDelUserVisibility() {
            return mDelUser.getVisibility();
        }

    }

}
