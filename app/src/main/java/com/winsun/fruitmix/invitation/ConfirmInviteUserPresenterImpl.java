package com.winsun.fruitmix.invitation;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.ConfirmInviteUserItemBinding;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveTicketOperationEvent;
import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy on 2017/7/12.
 */

public class ConfirmInviteUserPresenterImpl implements ConfirmInviteUserPresenter {

    public static final String TAG = ConfirmInviteUserPresenterImpl.class.getSimpleName();

    private InvitationRemoteDataSource mInvitationRemoteDataSource;

    private ThreadManager threadManager;

    private List<ConfirmInviteUser> mConfirmInviteUsers;

    private ConfirmTicketAdapter adapter;

    private ImageLoader imageLoader;

    private LoadingViewModel loadingViewModel;
    private NoContentViewModel noContentViewModel;

    private BaseView baseView;

    public ConfirmInviteUserPresenterImpl(BaseView baseView, InvitationRemoteDataSource invitationRemoteDataSource, ImageLoader imageLoader, final LoadingViewModel loadingViewModel, final NoContentViewModel noContentViewModel) {
        mInvitationRemoteDataSource = invitationRemoteDataSource;

        this.baseView = baseView;
        this.loadingViewModel = loadingViewModel;
        this.noContentViewModel = noContentViewModel;

        this.imageLoader = imageLoader;

        imageLoader.setShouldCache(true);

        threadManager = ThreadManager.getInstance();

        mConfirmInviteUsers = new ArrayList<>();

        createFakeConfirmInviteUser();

        adapter = new ConfirmTicketAdapter();

    }

    @Override
    public void getInvitations() {

        threadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                getInvitationInThread();

            }
        });

    }

    private void getInvitationInThread() {
        mInvitationRemoteDataSource.getInvitation(new BaseLoadDataCallback<ConfirmInviteUser>() {
            @Override
            public void onSucceed(final List<ConfirmInviteUser> data, OperationResult operationResult) {

                threadManager.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {

                        loadingViewModel.showLoading.set(false);

                        adapter.setConfirmInviteUsers(data);
                        adapter.notifyDataSetChanged();

                    }
                });

            }

            @Override
            public void onFail(OperationResult operationResult) {

                threadManager.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {

                        noContentViewModel.showNoContent.set(true);
                        loadingViewModel.showLoading.set(false);


                    }
                });

            }
        });
    }

    public ConfirmTicketAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void acceptInviteUser(final ConfirmInviteUser confirmInviteUser) {

        Log.d(TAG, "acceptInviteUser: " + confirmInviteUser.getUserName());

        threadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                baseView.showProgressDialog("正在执行");

                mInvitationRemoteDataSource.acceptInvitation(confirmInviteUser, new BaseOperateDataCallback<ConfirmInviteUser>() {
                    @Override
                    public void onSucceed(ConfirmInviteUser data, OperationResult result) {

                        baseView.dismissDialog();

                        baseView.showToast("执行成功");

                        handleOperateSucceed(confirmInviteUser);
                    }

                    @Override
                    public void onFail(OperationResult result) {

                        baseView.dismissDialog();

                        baseView.showToast("执行失败");

                    }
                });

            }
        });
    }

    @Override
    public void refuseInviteUser(final ConfirmInviteUser confirmInviteUser) {

        Log.d(TAG, "refuseInviteUser: " + confirmInviteUser.getUserName());

        threadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                baseView.showProgressDialog("正在执行");

                mInvitationRemoteDataSource.refuseInvitation(confirmInviteUser, new BaseOperateDataCallback<ConfirmInviteUser>() {
                    @Override
                    public void onSucceed(ConfirmInviteUser data, OperationResult result) {

                        baseView.dismissDialog();

                        baseView.showToast("执行成功");

                        handleOperateSucceed(confirmInviteUser);
                    }

                    @Override
                    public void onFail(OperationResult result) {

                        baseView.dismissDialog();

                        baseView.showToast("执行失败");
                    }
                });

            }
        });

    }

    private void handleOperateSucceed(ConfirmInviteUser confirmInviteUser) {

        int position = mConfirmInviteUsers.indexOf(confirmInviteUser);

        if (position != -1) {
            mConfirmInviteUsers.remove(position);

            adapter.setConfirmInviteUsers(mConfirmInviteUsers);
            adapter.notifyItemRemoved(position);
        }

    }

    private void retrieveUserAvatar(String userAvatarUrl, NetworkImageView imageView) {

        imageView.setDefaultImageResId(R.drawable.default_place_holder);

        if (userAvatarUrl != null && !userAvatarUrl.isEmpty())
            imageView.setImageUrl(userAvatarUrl, imageLoader);

    }

    private void createFakeConfirmInviteUser() {

        for (int i = 0; i < 10; i++) {

            ConfirmInviteUser confirmInviteUser = new ConfirmInviteUser();
            confirmInviteUser.setStation("test station " + i);
            confirmInviteUser.setUserName("test username " + i);
            mConfirmInviteUsers.add(confirmInviteUser);
        }

    }


    private class ConfirmTicketAdapter extends RecyclerView.Adapter<ConfirmTicketViewHolder> {

        private List<ConfirmInviteUser> confirmInviteUsers;

        public ConfirmTicketAdapter() {
            confirmInviteUsers = new ArrayList<>();
        }

        public void setConfirmInviteUsers(List<ConfirmInviteUser> confirmInviteUsers) {
            this.confirmInviteUsers.clear();
            this.confirmInviteUsers.addAll(confirmInviteUsers);
        }

        @Override
        public ConfirmTicketViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ConfirmInviteUserItemBinding binding = ConfirmInviteUserItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

            return new ConfirmTicketViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(ConfirmTicketViewHolder holder, int position) {

            ViewDataBinding binding = holder.getViewDataBinding();

            ConfirmInviteUser confirmInviteUser = confirmInviteUsers.get(position);

            binding.setVariable(BR.confirmInviteUser, confirmInviteUser);
            binding.setVariable(BR.confirmInviteUserPresenter, ConfirmInviteUserPresenterImpl.this);

            binding.executePendingBindings();

            holder.refreshView(confirmInviteUser);

        }

        @Override
        public int getItemCount() {
            return confirmInviteUsers.size();
        }
    }

    private class ConfirmTicketViewHolder extends BindingViewHolder {

        NetworkImageView userAvatar;


        public ConfirmTicketViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            ConfirmInviteUserItemBinding binding = (ConfirmInviteUserItemBinding) viewDataBinding;
            userAvatar = binding.userAvatar;

        }

        public void refreshView(final ConfirmInviteUser confirmInviteUser) {

            retrieveUserAvatar(confirmInviteUser.getUserAvatar(), userAvatar);

        }

    }

    @Override
    public void handleOperationEvent(OperationEvent operationEvent) {

        RetrieveTicketOperationEvent ticketOperationEvent = (RetrieveTicketOperationEvent) operationEvent;

        List<ConfirmInviteUser> confirmInviteUsers = ticketOperationEvent.getConfirmInviteUsers();

        filterConfirmInviteUser(mConfirmInviteUsers, confirmInviteUsers);


    }

    public void filterConfirmInviteUser(List<ConfirmInviteUser> currentConfirmInviteUsers, List<ConfirmInviteUser> newConfirmInviteUsers) {


    }

}
