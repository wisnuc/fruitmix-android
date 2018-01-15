package com.winsun.fruitmix.inbox.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.InboxListPageBinding;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.inbox.data.source.InboxDataRepository;
import com.winsun.fruitmix.inbox.presenter.InboxListPresenter;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.interfaces.Page;
import com.winsun.fruitmix.invitation.data.FakeInvitationDataSource;
import com.winsun.fruitmix.invitation.data.InjectInvitationDataSource;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/1/10.
 */

public class InboxListPage implements Page, IShowHideFragmentListener, InboxView {

    private BaseActivity mActivity;

    private View mView;

    private InboxListPresenter mInboxListPresenter;

    public InboxListPage(BaseActivity baseActivity) {

        mActivity = baseActivity;

        InboxListPageBinding binding = InboxListPageBinding.inflate(LayoutInflater.from(mActivity), null, false);

        mView = binding.getRoot();

        LoadingViewModel loadingViewModel = new LoadingViewModel();

        binding.setLoadingViewModel(loadingViewModel);

        NoContentViewModel noContentViewModel = new NoContentViewModel();
        noContentViewModel.setNoContentImgResId(R.drawable.no_file);
        noContentViewModel.setNoContentText("没有内容");

        binding.setNoContentViewModel(noContentViewModel);

        String currentLoginUserUUID = InjectSystemSettingDataSource.provideSystemSettingDataSource(mActivity).getCurrentLoginUserUUID();

        User currentUser = InjectUser.provideRepository(mActivity).getUserByUUID(currentLoginUserUUID);

        mInboxListPresenter = new InboxListPresenter(new InboxDataRepository(InjectGroupDataSource.provideGroupRepository(mActivity)),
                new FakeInvitationDataSource(), loadingViewModel, noContentViewModel,
                currentUser, this);


        RecyclerView recyclerView = binding.inboxRecyclerview;

        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(mInboxListPresenter.getInboxListAdapter());

    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public void refreshView() {

        mInboxListPresenter.refreshView();

    }

    @Override
    public void refreshViewForce() {

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

    }

    @Override
    public void onDestroy() {

        mInboxListPresenter.onDestroy();

        mActivity = null;

    }

    @Override
    public boolean canEnterSelectMode() {
        return false;
    }


    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }


    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public void finishView() {

    }

    @Override
    public void setResult(int resultCode) {

    }

    @Override
    public Dialog showProgressDialog(String message) {
        return mActivity.showProgressDialog(message);
    }

    @Override
    public void dismissDialog() {
        mActivity.dismissDialog();
    }

    @Override
    public void showToast(String text) {
        mActivity.showToast(text);
    }

    @Override
    public void showCustomErrorCode(String text) {

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public String getQuantityString(int resID, int quantity) {
        return mActivity.getResources().getQuantityString(resID,quantity);
    }

    @Override
    public String getQuantityString(int resID, int quantity, Object... formatArgs) {
        return mActivity.getResources().getQuantityString(resID,quantity,formatArgs);
    }

    @Override
    public String getString(int resID) {
        return mActivity.getString(resID);
    }

    @Override
    public String getString(int resID, Object... formatArgs) {
        return mActivity.getString(resID, formatArgs);
    }


}
