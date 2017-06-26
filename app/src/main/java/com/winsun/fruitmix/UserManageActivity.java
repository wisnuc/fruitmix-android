package com.winsun.fruitmix;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.winsun.fruitmix.databinding.ActivityUserManageBinding;
import com.winsun.fruitmix.databinding.UserManageItemBinding;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.user.manage.UserManagePresenterImpl;
import com.winsun.fruitmix.user.manage.UserMangePresenter;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserManageActivity extends BaseActivity {

    public static final String TAG = "UserManageActivity";

    ListView mUserListView;

    TextView mUserListEmpty;

    private List<User> mUserList;

    private Context mContext;

    private UserListAdapter mUserListAdapter;

    private UserMangePresenter userMangePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityUserManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_user_manage);

        mUserListView = binding.userList;

        mUserListEmpty = binding.userListEmpty;

        mContext = this;

        userMangePresenter = new UserManagePresenterImpl(this);

        binding.setUserPresenter(userMangePresenter);

        binding.setBaseView(this);

        refreshView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;

        userMangePresenter.onDestroy();
    }

    private void refreshView() {

        if (LocalCache.RemoteUserMapKeyIsUUID == null) {
            Log.w(TAG, "refreshUserInNavigationView: RemoteUserMapKeyIsUUID", new NullPointerException());
        }

        if (LocalCache.RemoteUserMapKeyIsUUID != null && !LocalCache.RemoteUserMapKeyIsUUID.isEmpty()) {

            mUserListEmpty.setVisibility(View.GONE);
            mUserListView.setVisibility(View.VISIBLE);

            refreshUserList();

            if (mUserListAdapter == null) {
                mUserListAdapter = new UserListAdapter();
                mUserListView.setAdapter(mUserListAdapter);
            } else {
                mUserListAdapter.notifyDataSetChanged();
            }

        } else {
            mUserListView.setVisibility(View.GONE);
            mUserListEmpty.setVisibility(View.VISIBLE);
        }

    }

    private void refreshUserList() {

        if (mUserList == null)
            mUserList = new ArrayList<>();
        else
            mUserList.clear();

        mUserList.addAll(LocalCache.RemoteUserMapKeyIsUUID.values());

        Collections.sort(mUserList, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return Collator.getInstance(Locale.CHINESE).compare(lhs.getUserName(), (rhs.getUserName()));
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Util.KEY_CREATE_USER_REQUEST_CODE && resultCode == RESULT_OK) {
            refreshView();
        }
    }

    private class UserListAdapter extends BaseAdapter {
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

            UserManageItemBinding binding;

            if (convertView == null) {

                binding = UserManageItemBinding.inflate(LayoutInflater.from(mContext), parent, false);

                convertView = binding.getRoot();

            } else {

                binding = DataBindingUtil.getBinding(convertView);
            }

            binding.setUser(new UserManageWrap(mUserList.get(position)));
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

            return convertView;
        }
    }

    public class UserManageWrap {

        private User user;

        public UserManageWrap(User user) {
            this.user = user;
        }

        public String getUserName() {
            String userName = user.getUserName();

            if (userName.length() > 20) {
                userName = userName.substring(0, 20);
                userName += mContext.getString(R.string.android_ellipsize);
            }

            return userName;
        }

        public String getDefaultAvatar() {
            return Util.getUserNameFirstLetter(user.getUserName());
        }

        public String getEmail() {
            return user.getEmail();
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
