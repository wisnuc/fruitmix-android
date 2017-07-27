package com.winsun.fruitmix.invitation;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.ConfirmInviteUserItemBinding;
import com.winsun.fruitmix.databinding.ConfirmInviteUserItemHeaderBinding;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveTicketOperationEvent;
import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Andy on 2017/7/12.
 */

public class ConfirmInviteUserPresenterImpl implements ConfirmInviteUserPresenter {

    public static final String TAG = ConfirmInviteUserPresenterImpl.class.getSimpleName();

    private InvitationRemoteDataSource mInvitationRemoteDataSource;

    private ThreadManager threadManager;

    private Map<String, List<ConfirmInviteUser>> mConfirmInviteUserMaps;
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

        mConfirmInviteUserMaps = new HashMap<>();

//        createFakeConfirmInviteUser();

        adapter = new ConfirmTicketAdapter();

    }

    public ConfirmTicketAdapter getAdapter() {
        return adapter;
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

                        noContentViewModel.showNoContent.set(false);

                        createMap(data);

                        adapter.setViewItems(createViewItems(mConfirmInviteUserMaps));
                        adapter.notifyDataSetChanged();

                    }
                });

            }

            @Override
            public void onFail(OperationResult operationResult) {

                threadManager.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {

                        loadingViewModel.showLoading.set(false);

                        noContentViewModel.showNoContent.set(true);

                    }
                });

            }
        });
    }

    private void createMap(List<ConfirmInviteUser> data) {

        for (ConfirmInviteUser user : data) {

            if (mConfirmInviteUserMaps.containsKey(user.getTicketUUID())) {

                List<ConfirmInviteUser> users = mConfirmInviteUserMaps.get(user.getTicketUUID());

                users.add(user);

            } else {

                List<ConfirmInviteUser> users = new ArrayList<>();
                users.add(user);

                mConfirmInviteUserMaps.put(user.getTicketUUID(), users);

            }

        }

    }

    private void postOperation(final ConfirmInviteUser confirmInviteUser) {

        baseView.showProgressDialog("正在执行");

        threadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                mInvitationRemoteDataSource.confirmInvitation(confirmInviteUser, new BaseOperateDataCallback<String>() {
                    @Override
                    public void onSucceed(final String data, OperationResult result) {

                        threadManager.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                baseView.dismissDialog();

                                baseView.showToast("执行成功");

                                handleOperateSucceed(confirmInviteUser.getTicketUUID());
                            }
                        });

                    }

                    @Override
                    public void onFail(OperationResult result) {

                        threadManager.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                baseView.dismissDialog();

                                baseView.showToast("执行失败");

                            }
                        });

                    }
                });

            }
        });


    }

    @Override
    public void acceptInviteUser(final ConfirmInviteUser confirmInviteUser) {

        Log.d(TAG, "acceptInviteUser: " + confirmInviteUser.getUserName());

        confirmInviteUser.setOperateType(ConfirmInviteUser.OPERATE_TYPE_ACCEPT);

        postOperation(confirmInviteUser);

    }

    @Override
    public void refuseInviteUser(final ConfirmInviteUser confirmInviteUser) {

        Log.d(TAG, "refuseInviteUser: " + confirmInviteUser.getUserName());

        confirmInviteUser.setOperateType(ConfirmInviteUser.OPERATE_TYPE_REFUSE);

        postOperation(confirmInviteUser);

    }

    private void handleOperateSucceed(String ticketID) {

        if (mConfirmInviteUserMaps.containsKey(ticketID)) {

            mConfirmInviteUserMaps.remove(ticketID);

            adapter.setViewItems(createViewItems(mConfirmInviteUserMaps));
            adapter.notifyDataSetChanged();
        }

    }

    private void createFakeConfirmInviteUser() {

        for (int i = 0; i < 10; i++) {

            ConfirmInviteUser confirmInviteUser = new ConfirmInviteUser();
            confirmInviteUser.setStation("test station " + i);
            confirmInviteUser.setUserName("test username " + i);

            List<ConfirmInviteUser> users = new ArrayList<>();
            users.add(confirmInviteUser);

            mConfirmInviteUserMaps.put("test Ticket" + i, users);
        }

    }

    private List<ViewItem> createViewItems(Map<String, List<ConfirmInviteUser>> map) {

        List<ViewItem> viewItems = new ArrayList<>();

        Set<String> tickets = map.keySet();

        for (String ticket : tickets) {

            ViewHeader viewHeader = new ViewHeader();
            viewHeader.setTicketID(ticket);

            viewItems.add(viewHeader);

            List<ConfirmInviteUser> confirmInviteUsers = map.get(ticket);

            for (ConfirmInviteUser confirmInviteUser : confirmInviteUsers) {
                ViewContent viewContent = new ViewContent();
                viewContent.setConfirmInviteUser(confirmInviteUser);
                viewItems.add(viewContent);
            }

        }

        return viewItems;

    }


    private class ConfirmTicketAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<ViewItem> mViewItems;

        ConfirmTicketAdapter() {
            mViewItems = new ArrayList<>();
        }

        void setViewItems(List<ViewItem> viewItems) {
            mViewItems.clear();
            mViewItems.addAll(viewItems);
        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ViewDataBinding binding;

            if (viewType == VIEW_HEAD) {

                binding = ConfirmInviteUserItemHeaderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                return new BindingViewHolder(binding);

            } else {
                binding = ConfirmInviteUserItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                return new ConfirmTicketContentViewHolder(binding);
            }

        }

        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            ViewDataBinding binding = holder.getViewDataBinding();

            if (mViewItems.get(position).getViewType() == VIEW_HEAD) {

                ViewHeader viewHeader = (ViewHeader) mViewItems.get(position);

                binding.setVariable(BR.ticketID, viewHeader.getTicketID());

                binding.executePendingBindings();

            } else {

                ViewContent viewContent = (ViewContent) mViewItems.get(position);

                ConfirmInviteUser confirmInviteUser = viewContent.getConfirmInviteUser();

                binding.setVariable(BR.confirmInviteUser, confirmInviteUser);
                binding.setVariable(BR.confirmInviteUserPresenter, ConfirmInviteUserPresenterImpl.this);

                binding.executePendingBindings();

                ConfirmTicketContentViewHolder viewHolder = (ConfirmTicketContentViewHolder) holder;

                viewHolder.refreshView(confirmInviteUser);

            }


        }

        @Override
        public int getItemViewType(int position) {
            return mViewItems.get(position).getViewType();
        }

        @Override
        public int getItemCount() {
            return mViewItems.size();
        }
    }


    private class ConfirmTicketContentViewHolder extends BindingViewHolder {

//        NetworkImageView userAvatar;

        public ConfirmTicketContentViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            ConfirmInviteUserItemBinding binding = (ConfirmInviteUserItemBinding) viewDataBinding;
//            userAvatar = binding.userAvatar;

        }

        public void refreshView(final ConfirmInviteUser confirmInviteUser) {

//            retrieveUserAvatar(confirmInviteUser.getUserAvatar(), userAvatar);

        }

    }

    private void retrieveUserAvatar(String userAvatarUrl, NetworkImageView imageView) {

        imageView.setDefaultImageResId(R.drawable.default_place_holder);

        if (userAvatarUrl != null && !userAvatarUrl.isEmpty())
            imageView.setImageUrl(userAvatarUrl, imageLoader);

    }

    @Override
    public void handleOperationEvent(OperationEvent operationEvent) {

        RetrieveTicketOperationEvent ticketOperationEvent = (RetrieveTicketOperationEvent) operationEvent;

        List<ConfirmInviteUser> confirmInviteUsers = ticketOperationEvent.getConfirmInviteUsers();

        filterConfirmInviteUser(confirmInviteUsers);


    }

    public void filterConfirmInviteUser(List<ConfirmInviteUser> newConfirmInviteUsers) {


    }

    public static final int VIEW_HEAD = 1;
    public static final int VIEW_CONTENT = 2;

    private interface ViewItem {

        int getViewType();

    }

    private class ViewHeader implements ViewItem {

        private String ticketID;

        @Override
        public int getViewType() {
            return VIEW_HEAD;
        }

        public String getTicketID() {
            return ticketID;
        }

        public void setTicketID(String ticketID) {
            this.ticketID = ticketID;
        }
    }

    private class ViewContent implements ViewItem {

        private ConfirmInviteUser confirmInviteUser;

        @Override
        public int getViewType() {
            return VIEW_CONTENT;
        }

        public ConfirmInviteUser getConfirmInviteUser() {
            return confirmInviteUser;
        }

        public void setConfirmInviteUser(ConfirmInviteUser confirmInviteUser) {
            this.confirmInviteUser = confirmInviteUser;
        }
    }

}
