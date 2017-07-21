package com.winsun.fruitmix.user.manage;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.UserManageActivity;
import com.winsun.fruitmix.databinding.UserManageItemBinding;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.LocalCache;
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

    public UserManagePresenterImpl(UserManageView userManageView, UserManageActivity.UserManageViewModel userManageViewModel) {
        this.userManageView = userManageView;

        mUserListAdapter = new UserListAdapter();

        this.userManageViewModel = userManageViewModel;
    }

    @Override
    public BaseAdapter getAdapter() {
        return mUserListAdapter;
    }

    @Override
    public void refreshView() {

        if (LocalCache.RemoteUserMapKeyIsUUID == null) {
            Log.w(TAG, "refreshUserInNavigationView: RemoteUserMapKeyIsUUID", new NullPointerException());
        }

        if (LocalCache.RemoteUserMapKeyIsUUID != null && !LocalCache.RemoteUserMapKeyIsUUID.isEmpty()) {

            userManageViewModel.showUserListEmpty.set(false);
            userManageViewModel.showUserListView.set(true);

            refreshUserList();

            mUserListAdapter.notifyDataSetChanged();

        } else {

            userManageViewModel.showUserListEmpty.set(true);
            userManageViewModel.showUserListView.set(false);

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

        public String getUserName(Context context) {
            String userName = user.getUserName();

            if (userName.length() > 20) {
                userName = userName.substring(0, 20);
                userName += context.getString(R.string.android_ellipsize);
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


}
