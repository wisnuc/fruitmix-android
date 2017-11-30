package com.winsun.fruitmix;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import com.winsun.fruitmix.account.manage.AccountManagePresenter;
import com.winsun.fruitmix.account.manage.AccountManagePresenterImpl;
import com.winsun.fruitmix.account.manage.AccountManageView;
import com.winsun.fruitmix.databinding.ActivityAccountManageBinding;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.logout.InjectLogoutUseCase;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.usecase.InjectGetAllBindingLocalUserUseCase;
import com.winsun.fruitmix.util.FileTool;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class AccountManageActivity extends BaseActivity implements AccountManageView {

    public static final String TAG = "AccountManageActivity";

    ExpandableListView mAccountExpandableListView;

    private Context mContext;

    private AccountManagePresenter presenter;

    public static final int START_EQUIPMENT_SEARCH = 0x1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAccountManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_account_manage);

        mAccountExpandableListView = binding.accountExpandableListView;

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set(getString(R.string.account_manage));

        toolbarViewModel.setBaseView(this);

        binding.setToolbarViewModel(toolbarViewModel);

        LoadingViewModel loadingViewModel = new LoadingViewModel();

        binding.setLoadingViewModel(loadingViewModel);

        presenter = new AccountManagePresenterImpl(this,loadingViewModel, InjectLoggedInUser.provideLoggedInUserRepository(this),
                InjectSystemSettingDataSource.provideSystemSettingDataSource(this),
                InjectGetAllBindingLocalUserUseCase.provideInstance(this), InjectLogoutUseCase.provideLogoutUseCase(this),
                FileTool.getInstance());

        binding.setAccountManagePresenter(presenter);

        mContext = this;

        BaseExpandableListAdapter mAdapter = presenter.getAdapter();

        mAccountExpandableListView.setAdapter(mAdapter);

        mAccountExpandableListView.setGroupIndicator(null);

        for (int i = 0; i < mAdapter.getGroupCount(); i++) {
            mAccountExpandableListView.expandGroup(i);
        }

    }

    @Override
    public void onBackPressed() {

        presenter.handleBack();

        super.onBackPressed();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;

        presenter.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        presenter.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void gotoEquipmentSearchActivity() {
        Intent intent = new Intent(mContext, EquipmentSearchActivity.class);
        intent.putExtra(Util.KEY_SHOULD_STOP_SERVICE, false);
        startActivityForResult(intent, START_EQUIPMENT_SEARCH);
    }

}
