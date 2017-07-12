package com.winsun.fruitmix;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.winsun.fruitmix.account.manage.AccountManagePresenter;
import com.winsun.fruitmix.account.manage.AccountManagePresenterImpl;
import com.winsun.fruitmix.account.manage.AccountManageView;
import com.winsun.fruitmix.databinding.AccountChildItemBinding;
import com.winsun.fruitmix.databinding.AccountGroupItemBinding;
import com.winsun.fruitmix.databinding.ActivityAccountManageBinding;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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

        presenter = new AccountManagePresenterImpl(this);

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set(getString(R.string.account_manage));

        toolbarViewModel.setBaseView(this);

        binding.setToolbarViewModel(toolbarViewModel);

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
