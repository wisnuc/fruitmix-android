package com.winsun.fruitmix.user.manage;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.CreateUserActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityUserManageBinding;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.user.manage.UserManagePresenterImpl;
import com.winsun.fruitmix.user.manage.UserManageView;
import com.winsun.fruitmix.user.manage.UserMangePresenter;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

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

        userMangePresenter = new UserManagePresenterImpl(this, userManageViewModel, InjectUser.provideRepository(this));

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
