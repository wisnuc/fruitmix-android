package com.winsun.fruitmix.invitation;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityConfirmInviteUserBinding;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class ConfirmInviteUserActivity extends BaseActivity {

    private RecyclerView recyclerView;

    private ConfirmInviteUserPresenter confirmInviteUserPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityConfirmInviteUserBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_confirm_invite_user);

        recyclerView = binding.confirmTicketRecyclerview;

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();
        toolbarViewModel.titleText.set("确认邀请");
        toolbarViewModel.setBaseView(this);

        binding.setToolbarViewModel(toolbarViewModel);

        LoadingViewModel loadingViewModel = new LoadingViewModel();

        binding.setLoadingViewModel(loadingViewModel);

        NoContentViewModel noContentViewModel = new NoContentViewModel();
        noContentViewModel.setNoContentText("没有内容");
        noContentViewModel.setNoContentImgResId(R.drawable.no_file);

        binding.setNoContentViewModel(noContentViewModel);

        InvitationRemoteDataSource invitationRemoteDataSource = new InvitationRemoteDataSource(InjectHttp.provideIHttpUtil(this),InjectHttp.provideHttpRequestFactory());

        ImageLoader imageLoader = InjectHttp.provideImageGifLoaderIntance().getImageLoader(this);

        confirmInviteUserPresenter = new ConfirmInviteUserPresenterImpl(this, invitationRemoteDataSource, imageLoader, loadingViewModel, noContentViewModel);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(((ConfirmInviteUserPresenterImpl) confirmInviteUserPresenter).getAdapter());

        confirmInviteUserPresenter.getInvitations();
    }

    @Override
    public void handleOperationEvent(OperationEvent operationEvent) {
        super.handleOperationEvent(operationEvent);

        if (operationEvent.getAction().equals(Util.REMOTE_CONFIRM_INVITE_USER_RETRIEVED))
            confirmInviteUserPresenter.handleOperationEvent(operationEvent);

    }
}
