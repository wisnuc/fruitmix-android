package com.winsun.fruitmix;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
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
import com.winsun.fruitmix.user.manage.UserManageView;
import com.winsun.fruitmix.user.manage.UserMangePresenter;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserManageActivity extends BaseActivity implements UserManageView {

    public static final String TAG = "UserManageActivity";

    ListView mUserListView;

    TextView mUserListEmpty;

    private UserMangePresenter userMangePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityUserManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_user_manage);

        UserManageViewModel userManageViewModel = new UserManageViewModel();

        mUserListView = binding.userList;

        mUserListEmpty = binding.userListEmpty;

        binding.setUserManageViewModel(userManageViewModel);

        userMangePresenter = new UserManagePresenterImpl(this, userManageViewModel);

        binding.setUserPresenter(userMangePresenter);

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set(getString(R.string.user_manage));

        toolbarViewModel.setBaseView(this);

        binding.setToolbarViewModel(toolbarViewModel);

        mUserListView.setAdapter(userMangePresenter.getAdapter());

        userMangePresenter.refreshView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        userMangePresenter.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Util.KEY_CREATE_USER_REQUEST_CODE && resultCode == RESULT_OK) {
            userMangePresenter.refreshView();
        }
    }

    @Override
    public void gotoCreateUserActivity() {
        Intent intent = new Intent(this, CreateUserActivity.class);
        startActivityForResult(intent, Util.KEY_CREATE_USER_REQUEST_CODE);
    }

    public class UserManageViewModel {

        public final ObservableBoolean showUserListEmpty = new ObservableBoolean(false);

        public final ObservableBoolean showUserListView = new ObservableBoolean(true);

    }

}
