package com.winsun.fruitmix.user.manage;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.TextView;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.CreateUserActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityUserManageBinding;
import com.winsun.fruitmix.equipment.search.EquipmentItemViewModel;
import com.winsun.fruitmix.equipment.search.data.InjectEquipment;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.stations.InjectStation;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class UserManageActivity extends BaseActivity implements UserManageView {

    public static final String TAG = "UserManageActivity";

    ListView mUserListView;

    TextView mUserListEmpty;

    private UserMangePresenter userMangePresenter;

    public static final int MODIFY_USER_STATE_REQUEST_CODE = 0x1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityUserManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_user_manage);

        Toolbar mToolbar = binding.toolbarLayout.toolbar;

        if (Util.checkRunningOnLollipopOrHigher()) {
            mToolbar.setElevation(0f);
        }

        binding.toolbarLayout.title.setTextColor(ContextCompat.getColor(this, R.color.white));

        mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.login_ui_blue));

        Util.setStatusBarColor(this, R.color.login_ui_blue);

        UserManageViewModel userManageViewModel = new UserManageViewModel();

        mUserListView = binding.userList;

        mUserListEmpty = binding.userListEmpty;

        binding.setUserManageViewModel(userManageViewModel);

        EquipmentItemViewModel equipmentItemViewModel = new EquipmentItemViewModel();

        equipmentItemViewModel.showEquipment.set(true);

        binding.setEquipmentItemViewModel(equipmentItemViewModel);

        userMangePresenter = new UserManagePresenterImpl(this, equipmentItemViewModel, userManageViewModel,
                InjectUser.provideRepository(this), InjectEquipment.provideEquipmentDataSource(this),
                InjectSystemSettingDataSource.provideSystemSettingDataSource(this), InjectStation.provideStationDataSource(this),
                InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this));

        binding.setUserPresenter(userMangePresenter);

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set(getString(R.string.user_manage));

        toolbarViewModel.navigationIconResId.set(R.drawable.ic_back);

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
        }else if(requestCode == MODIFY_USER_STATE_REQUEST_CODE){
            userMangePresenter.refreshUserFromCache();
        }

    }

    @Override
    public void gotoCreateUserActivity() {
        Intent intent = new Intent(this, CreateUserActivity.class);
        startActivityForResult(intent, Util.KEY_CREATE_USER_REQUEST_CODE);
    }

    @Override
    public void gotoModifyUserStateActivity(User user) {

        Intent intent = new Intent(this, ModifyUserStateActivity.class);
        intent.putExtra(ModifyUserStateActivity.MODIFY_USER_UUID_KEY, user.getUuid());

        startActivityForResult(intent,MODIFY_USER_STATE_REQUEST_CODE);

    }

    @Override
    public Context getContext() {
        return this;
    }

    public class UserManageViewModel {

        public final ObservableBoolean showUserListEmpty = new ObservableBoolean(false);

        public final ObservableBoolean showUserListView = new ObservableBoolean(true);

    }

}
