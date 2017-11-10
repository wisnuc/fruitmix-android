package com.winsun.fruitmix.invitation;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.ActivityConfirmInviteUserBinding;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.invitation.data.InjectInvitationDataSource;
import com.winsun.fruitmix.invitation.data.InvitationDataSource;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import java.util.Collections;

public class ConfirmInviteUserActivity extends BaseActivity implements ConfirmInviteUserView {

    private RecyclerView recyclerView;

    private FloatingActionButton inviteUserFab;

    private ConfirmInviteUserPresenter confirmInviteUserPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityConfirmInviteUserBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_confirm_invite_user);

        recyclerView = binding.confirmTicketRecyclerview;

        inviteUserFab = binding.inviteUser;

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();
        toolbarViewModel.titleText.set(getString(R.string.confirm_invitation));
        toolbarViewModel.setBaseView(this);

        binding.setToolbarViewModel(toolbarViewModel);

        LoadingViewModel loadingViewModel = new LoadingViewModel();

        binding.setLoadingViewModel(loadingViewModel);

        NoContentViewModel noContentViewModel = new NoContentViewModel();
        noContentViewModel.setNoContentText(getString(R.string.no_invitation));
        noContentViewModel.setNoContentImgResId(R.drawable.no_invitation);

        binding.setNoContentViewModel(noContentViewModel);

        InvitationDataSource invitationDataSource = InjectInvitationDataSource.provideInvitationDataSource(this);

//        InvitationDataSource invitationDataSource = new FakeInvitationDataSource();

        ImageLoader imageLoader = InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this);

        confirmInviteUserPresenter = new ConfirmInviteUserPresenterImpl(this, invitationDataSource, imageLoader, loadingViewModel, noContentViewModel);

        binding.setConfirmInviteUserPresenter(confirmInviteUserPresenter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(((ConfirmInviteUserPresenterImpl) confirmInviteUserPresenter).getAdapter());

        confirmInviteUserPresenter.getInvitations();

        ButlerService.startRetrieveTicketTask();
    }

    @Override
    public void handleOperationEvent(OperationEvent operationEvent) {
        super.handleOperationEvent(operationEvent);

        if (operationEvent.getAction().equals(Util.REMOTE_CONFIRM_INVITE_USER_RETRIEVED))
            confirmInviteUserPresenter.handleOperationEvent(operationEvent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        confirmInviteUserPresenter.onDestroy();

        ButlerService.stopRetrieveTicketTask();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void setInviteUserFabVisibility(int visibility) {
        inviteUserFab.setVisibility(visibility);
    }

    private class FakeInvitationDataSource implements InvitationDataSource {

        @Override
        public void createInvitation(BaseOperateDataCallback<String> callback) {

        }

        @Override
        public void getInvitation(BaseLoadDataCallback<ConfirmInviteUser> callback) {
            callback.onSucceed(Collections.<ConfirmInviteUser>emptyList(), new OperationSuccess());
        }

        @Override
        public void confirmInvitation(ConfirmInviteUser confirmInviteUser, BaseOperateDataCallback<String> callback) {
            callback.onSucceed("", new OperationSuccess());
        }

    }

}
