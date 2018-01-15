package com.winsun.fruitmix.invitation;

import android.content.res.Resources;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackWrapper;
import com.winsun.fruitmix.component.UserAvatar;
import com.winsun.fruitmix.databinding.ConfirmInviteUserItemBinding;
import com.winsun.fruitmix.databinding.ConfirmInviteUserItemHeaderBinding;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.invitation.data.InvitationDataSource;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.parser.HttpErrorBodyParser;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.wxapi.MiniProgram;
import com.winsun.fruitmix.wxapi.WXEntryActivity;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by Andy on 2017/7/12.
 */

public class ConfirmInviteUserPresenterImpl implements ConfirmInviteUserPresenter, ActiveView {

    public static final String TAG = ConfirmInviteUserPresenterImpl.class.getSimpleName();

    private InvitationDataSource mInvitationDataSource;

    private Map<String, List<ConfirmInviteUser>> mConfirmInviteUserMaps;
    private ConfirmTicketAdapter adapter;

    private ImageLoader imageLoader;

    private LoadingViewModel loadingViewModel;
    private NoContentViewModel noContentViewModel;

    private ConfirmInviteUserView confirmInviteUserView;

    private Random random;

    private Resources resources;

    private IWXAPI iwxapi;

    private boolean isSendingMiniProgram = false;

    ConfirmInviteUserPresenterImpl(ConfirmInviteUserView confirmInviteUserView, InvitationDataSource invitationDataSource, ImageLoader imageLoader, final LoadingViewModel loadingViewModel, final NoContentViewModel noContentViewModel) {
        mInvitationDataSource = invitationDataSource;

        this.confirmInviteUserView = confirmInviteUserView;
        this.loadingViewModel = loadingViewModel;
        this.noContentViewModel = noContentViewModel;

        this.imageLoader = imageLoader;

        imageLoader.setShouldCache(true);

        mConfirmInviteUserMaps = new HashMap<>();

        adapter = new ConfirmTicketAdapter();

        random = new Random();

        iwxapi = MiniProgram.registerToWX(confirmInviteUserView.getContext());

        resources = confirmInviteUserView.getContext().getResources();

//        createFakeConfirmInviteUser();

        WXEntryActivity.setWxEntrySendMiniProgramCallback(new WXEntryActivity.WXEntrySendMiniProgramCallback() {
            @Override
            public void succeed() {

                Log.d(TAG, "succeed: set isSendingMiniProgram to false");

                isSendingMiniProgram = false;
            }

            @Override
            public void fail() {

                Log.d(TAG, "fail: set isSendingMiniProgram to false");

                isSendingMiniProgram = false;
            }
        });

    }

    @Override
    public void onDestroy() {

        confirmInviteUserView = null;
    }

    @Override
    public boolean isActive() {
        return confirmInviteUserView != null;
    }

    public ConfirmTicketAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void createInvitation() {
        createInvitationInThread();
    }

    private void createInvitationInThread() {

        Log.d(TAG, "createInvitationInThread: isSendingMiniProgram: " + isSendingMiniProgram);

        if (isSendingMiniProgram)
            return;

        confirmInviteUserView.showProgressDialog(String.format(confirmInviteUserView.getString(R.string.operating_title),
                confirmInviteUserView.getString(R.string.create_invitation)));

        mInvitationDataSource.createInvitation(new BaseOperateDataCallbackWrapper<>(new BaseOperateDataCallback<String>() {
            @Override
            public void onSucceed(final String data, OperationResult result) {

                Log.d(TAG, "onSucceed: create invitation,ticket: " + data);

                confirmInviteUserView.dismissDialog();

                isSendingMiniProgram = true;

                Log.d(TAG, "onSucceed: isSendingMiniProgram: " + isSendingMiniProgram);

                MiniProgram.shareMiniWXApp(confirmInviteUserView.getContext(), iwxapi, resources, data);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                confirmInviteUserView.dismissDialog();

                isSendingMiniProgram = false;

                Log.d(TAG, "onFail: isSendingMiniProgram: " + isSendingMiniProgram);

                confirmInviteUserView.showToast(operationResult.getResultMessage(confirmInviteUserView.getContext()));

            }
        }, this));
    }

    @Override
    public void getInvitations() {

        getInvitationInThread();

    }

    private void getInvitationInThread() {
        mInvitationDataSource.getInvitation(new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<ConfirmInviteUser>() {
            @Override
            public void onSucceed(final List<ConfirmInviteUser> data, OperationResult operationResult) {

                loadingViewModel.showLoading.set(false);

                createMap(data);

                handleInvitation();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                loadingViewModel.showLoading.set(false);

                noContentViewModel.showNoContent.set(true);

                confirmInviteUserView.setInviteUserFabVisibility(View.VISIBLE);

            }
        }, this));
    }

    private void handleInvitation() {
        List<ViewItem> viewItems = createViewItems(mConfirmInviteUserMaps);

        if (viewItems.size() == 0) {

            noContentViewModel.showNoContent.set(true);

            confirmInviteUserView.setInviteUserFabVisibility(View.VISIBLE);

        } else {

            noContentViewModel.showNoContent.set(false);

            confirmInviteUserView.setInviteUserFabVisibility(View.VISIBLE);

            adapter.setViewItems(viewItems);
            adapter.notifyDataSetChanged();

        }
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

    @Override
    public void acceptInviteUser(final ConfirmInviteUser confirmInviteUser) {

        Log.d(TAG, "acceptInviteUser: " + confirmInviteUser.getUserName());

        postOperation(confirmInviteUser, true, R.string.accept_invitation);

    }

    @Override
    public void refuseInviteUser(final ConfirmInviteUser confirmInviteUser) {

        Log.d(TAG, "refuseInviteUser: " + confirmInviteUser.getUserName());

        postOperation(confirmInviteUser, false, R.string.refuse_invitation);

    }

    private void postOperation(final ConfirmInviteUser confirmInviteUser, final boolean isAccepted, final int resID) {

        confirmInviteUserView.showProgressDialog(String.format(confirmInviteUserView.getString(R.string.operating_title),
                confirmInviteUserView.getString(R.string.invitation)));

        mInvitationDataSource.confirmInvitation(confirmInviteUser, isAccepted,new BaseOperateDataCallback<String>() {
            @Override
            public void onSucceed(final String data, OperationResult result) {

                confirmInviteUserView.dismissDialog();

                confirmInviteUserView.showToast(String.format(confirmInviteUserView.getString(R.string.success),
                        confirmInviteUserView.getString(resID)));

                handleOperateSucceed(confirmInviteUser, isAccepted);

            }

            @Override
            public void onFail(OperationResult result) {

                confirmInviteUserView.dismissDialog();

                if (result instanceof OperationNetworkException) {

                    HttpErrorBodyParser parser = new HttpErrorBodyParser();

                    try {
                        String messageInBody = parser.parse(((OperationNetworkException) result).getHttpResponseData());

                        confirmInviteUserView.showToast(messageInBody);

                    } catch (JSONException e) {
                        e.printStackTrace();

                        confirmInviteUserView.showToast(result.getResultMessage(confirmInviteUserView.getContext()));
                    }

                } else {

                    confirmInviteUserView.showToast(result.getResultMessage(confirmInviteUserView.getContext()));

                }

            }
        });


    }

    private void handleOperateSucceed(ConfirmInviteUser confirmInviteUser, boolean isAccepted) {

        if (isAccepted)
            confirmInviteUser.setOperateType(ConfirmInviteUser.OPERATE_TYPE_ACCEPT);
        else
            confirmInviteUser.setOperateType(ConfirmInviteUser.OPERATE_TYPE_REFUSE);

        handleInvitation();

    }

    private void createFakeConfirmInviteUser() {

        List<ConfirmInviteUser> users = new ArrayList<>();

        String ticketID = "test Ticket" + 0;

        for (int i = 0; i < 10; i++) {

            ConfirmInviteUser confirmInviteUser = new ConfirmInviteUser();
            confirmInviteUser.setStation("test station " + i);
            confirmInviteUser.setUserName("test username " + i);
            confirmInviteUser.setCreateFormatTime("");

            int type = random.nextInt(3);

            confirmInviteUser.setOperateType(type);
            confirmInviteUser.setUserAvatar(User.DEFAULT_AVATAR);
            confirmInviteUser.setTicketUUID(ticketID);

            users.add(confirmInviteUser);

        }

        mConfirmInviteUserMaps.put(ticketID, users);

    }

    private List<ViewItem> createViewItems(Map<String, List<ConfirmInviteUser>> map) {

        List<ViewContent> viewContents = new ArrayList<>();

        Set<String> tickets = map.keySet();

        for (String ticket : tickets) {

            List<ViewContent> temporaryViewItems = null;

            List<ConfirmInviteUser> confirmInviteUsers = map.get(ticket);

            for (ConfirmInviteUser confirmInviteUser : confirmInviteUsers) {

                if (temporaryViewItems == null)
                    temporaryViewItems = new ArrayList<>();

                ViewContent viewContent = new ViewContent();
                viewContent.setConfirmInviteUser(confirmInviteUser);

                temporaryViewItems.add(viewContent);

            }

            if (temporaryViewItems != null) {
/*                ViewHeader viewHeader = new ViewHeader();
                viewHeader.setTicketID(ticket);

                viewItems.add(viewHeader);*/

                viewContents.addAll(temporaryViewItems);
            }

        }

        sortViewItems(viewContents);

        List<ViewItem> viewItems = new ArrayList<>();
        viewItems.addAll(viewContents);

        return viewItems;

    }

    private void sortViewItems(List<ViewContent> viewContents) {

        Collections.sort(viewContents, new Comparator<ViewContent>() {
            @Override
            public int compare(ViewContent lhs, ViewContent rhs) {

                int lnsType = lhs.getConfirmInviteUser().getOperateType();
                int rhsType = rhs.getConfirmInviteUser().getOperateType();

                if (lnsType > rhsType) {
                    return 1;
                } else if (lnsType == rhsType)
                    return 0;
                else
                    return -1;

            }
        });

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

            ViewItem viewItem = mViewItems.get(position);

            if (viewItem.getViewType() == VIEW_HEAD) {

                ViewHeader viewHeader = (ViewHeader) viewItem;

                binding.setVariable(BR.ticketID, viewHeader.getTicketID());

                binding.executePendingBindings();

            } else {

                ViewContent viewContent = (ViewContent) viewItem;

                ConfirmInviteUser confirmInviteUser = viewContent.getConfirmInviteUser();

                binding.setVariable(BR.confirmInviteUser, confirmInviteUser);
                binding.setVariable(BR.confirmInviteUserPresenter, ConfirmInviteUserPresenterImpl.this);

                ConfirmInviteUserItemBinding confirmInviteUserItemBinding = (ConfirmInviteUserItemBinding) binding;

                ConfirmInviteUserViewModel confirmInviteUserViewModel = confirmInviteUserItemBinding.getConfirmInviteUserViewModel();

                if (confirmInviteUserViewModel == null) {
                    confirmInviteUserViewModel = new ConfirmInviteUserViewModel();
                }

                switch (confirmInviteUser.getOperateType()) {
                    case ConfirmInviteUser.OPERATE_TYPE_ACCEPT:
                        confirmInviteUserViewModel.showOperateBtn.set(false);

                        confirmInviteUserViewModel.operateResult.set(confirmInviteUserView.getString(R.string.accepted));

                        break;
                    case ConfirmInviteUser.OPERATE_TYPE_PENDING:
                        confirmInviteUserViewModel.showOperateBtn.set(true);
                        break;
                    case ConfirmInviteUser.OPERATE_TYPE_REFUSE:
                        confirmInviteUserViewModel.showOperateBtn.set(false);

                        confirmInviteUserViewModel.operateResult.set(confirmInviteUserView.getString(R.string.refused));

                        break;
                }

                confirmInviteUserItemBinding.setConfirmInviteUserViewModel(confirmInviteUserViewModel);

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

        private UserAvatar userAvatar;

        public ConfirmTicketContentViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            ConfirmInviteUserItemBinding binding = (ConfirmInviteUserItemBinding) viewDataBinding;
            userAvatar = binding.userAvatar;

        }

        public void refreshView(final ConfirmInviteUser confirmInviteUser) {

            userAvatar.setUser(confirmInviteUser.generateUser(random), imageLoader);

        }

    }

    @Override
    public void handleOperationEvent(OperationEvent operationEvent) {

/*        RetrieveTicketOperationEvent ticketOperationEvent = (RetrieveTicketOperationEvent) operationEvent;

        List<ConfirmInviteUser> confirmInviteUsers = ticketOperationEvent.getConfirmInviteUsers();

        mConfirmInviteUserMaps.clear();
        createMap(confirmInviteUsers);

        handleInvitation();*/

    }

    public void filterConfirmInviteUser(List<ConfirmInviteUser> newConfirmInviteUsers) {


    }

    private static final int VIEW_HEAD = 1;
    private static final int VIEW_CONTENT = 2;

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
