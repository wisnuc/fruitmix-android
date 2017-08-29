package com.winsun.fruitmix.user.manage;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.databinding.UserManageItemBinding;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
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

    private UserDataRepository userDataRepository;

    public UserManagePresenterImpl(UserManageView userManageView, UserManageActivity.UserManageViewModel userManageViewModel, UserDataRepository userDataRepository) {
        this.userManageView = userManageView;
        this.userDataRepository = userDataRepository;
        this.userManageViewModel = userManageViewModel;

        mUserListAdapter = new UserListAdapter();
    }

    @Override
    public BaseAdapter getAdapter() {
        return mUserListAdapter;
    }

    @Override
    public void refreshView() {

        getUserInThread();

    }

    private void getUserInThread() {

        userDataRepository.getUsers(new BaseLoadDataCallback<User>() {
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
