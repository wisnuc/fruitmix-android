package com.winsun.fruitmix.invitation;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.component.UserAvatar;
import com.winsun.fruitmix.databinding.ConfirmInviteUserItemBinding;
import com.winsun.fruitmix.databinding.ConfirmInviteUserItemHeaderBinding;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveTicketOperationEvent;
import com.winsun.fruitmix.invitation.data.InvitationDataSource;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.parser.HttpErrorBodyParser;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by Andy on 2017/7/12.
 */

public class ConfirmInviteUserPresenterImpl implements ConfirmInviteUserPresenter {

    public static final String TAG = ConfirmInviteUserPresenterImpl.class.getSimpleName();

    private InvitationDataSource mInvitationDataSource;

    private Map<String, List<ConfirmInviteUser>> mConfirmInviteUserMaps;
    private ConfirmTicketAdapter adapter;

    private ImageLoader imageLoader;

    private LoadingViewModel loadingViewModel;
    private NoContentViewModel noContentViewModel;

    private ConfirmInviteUserView confirmInviteUserView;

    private Random random;

    public ConfirmInviteUserPresenterImpl(ConfirmInviteUserView confirmInviteUserView, InvitationDataSource invitationDataSource, ImageLoader imageLoader, final LoadingViewModel loadingViewModel, final NoContentViewModel noContentViewModel) {
        mInvitationDataSource = invitationDataSource;

        this.confirmInviteUserView = confirmInviteUserView;
        this.loadingViewModel = loadingViewModel;
        this.noContentViewModel = noContentViewModel;

        this.imageLoader = imageLoader;

        imageLoader.setShouldCache(true);

        mConfirmInviteUserMaps = new HashMap<>();

//        createFakeConfirmInviteUser();

        adapter = new ConfirmTicketAdapter();

        random = new Random();

    }

    @Override
    public void onDestroy() {

        confirmInviteUserView = null;
    }

    public ConfirmTicketAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void getInvitations() {

        getInvitationInThread();

    }

    private void getInvitationInThread() {
        mInvitationDataSource.getInvitation(new BaseLoadDataCallback<ConfirmInviteUser>() {
            @Override
            public void onSucceed(final List<ConfirmInviteUser> data, OperationResult operationResult) {

                loadingViewModel.showLoading.set(false);

                noContentViewModel.showNoContent.set(false);

                createMap(data);

                adapter.setViewItems(createViewItems(mConfirmInviteUserMaps));
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                loadingViewModel.showLoading.set(false);

                noContentViewModel.showNoContent.set(true);

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

    @Override
    public void acceptInviteUser(final ConfirmInviteUser confirmInviteUser) {

        Log.d(TAG, "acceptInviteUser: " + confirmInviteUser.getUserName());

        confirmInviteUser.setOperateType(ConfirmInviteUser.OPERATE_TYPE_ACCEPT);

        postOperation(confirmInviteUser, R.string.accept_invitation);

    }

    @Override
    public void refuseInviteUser(final ConfirmInviteUser confirmInviteUser) {

        Log.d(TAG, "refuseInviteUser: " + confirmInviteUser.getUserName());

        confirmInviteUser.setOperateType(ConfirmInviteUser.OPERATE_TYPE_REFUSE);

        postOperation(confirmInviteUser, R.string.refuse_invitation);

    }

    private void postOperation(final ConfirmInviteUser confirmInviteUser, final int resID) {

        confirmInviteUserView.showProgressDialog(String.format(confirmInviteUserView.getString(R.string.operating_title),
                confirmInviteUserView.getString(R.string.confirm_invitation)));

        mInvitationDataSource.confirmInvitation(confirmInviteUser, new BaseOperateDataCallback<String>() {
            @Override
            public void onSucceed(final String data, OperationResult result) {

                confirmInviteUserView.dismissDialog();

                confirmInviteUserView.showToast(String.format(confirmInviteUserView.getString(R.string.success),
                        confirmInviteUserView.getString(resID)));

                handleOperateSucceed(confirmInviteUser);

            }

            @Override
            public void onFail(OperationResult result) {

                confirmInviteUserView.dismissDialog();

                if (result instanceof OperationNetworkException) {

                    HttpErrorBodyParser parser = new HttpErrorBodyParser();

                    try {
                        String messageInBody = parser.parse(((OperationNetworkException) result).getHttpResponseBody());

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

    private void handleOperateSucceed(ConfirmInviteUser confirmInviteUser) {

        String ticketUUID = confirmInviteUser.getTicketUUID();

        if (mConfirmInviteUserMaps.containsKey(ticketUUID)) {

            List<ConfirmInviteUser> confirmInviteUsers = mConfirmInviteUserMaps.get(ticketUUID);

            confirmInviteUsers.remove(confirmInviteUser);

            if (confirmInviteUsers.isEmpty())
                mConfirmInviteUserMaps.remove(ticketUUID);

            adapter.setViewItems(createViewItems(mConfirmInviteUserMaps));
            adapter.notifyDataSetChanged();
        }

    }

    private void createFakeConfirmInviteUser() {

        List<ConfirmInviteUser> users = new ArrayList<>();

        String ticketID = "test Ticket" + 0;

        for (int i = 0; i < 10; i++) {

            ConfirmInviteUser confirmInviteUser = new ConfirmInviteUser();
            confirmInviteUser.setStation("test station " + i);
            confirmInviteUser.setUserName("test username " + i);
            confirmInviteUser.setOperateType(ConfirmInviteUser.OPERATE_TYPE_PENDING);
            confirmInviteUser.setUserAvatar(User.DEFAULT_AVATAR);
            confirmInviteUser.setTicketUUID(ticketID);

            users.add(confirmInviteUser);

        }

        mConfirmInviteUserMaps.put(ticketID, users);

    }

    private List<ViewItem> createViewItems(Map<String, List<ConfirmInviteUser>> map) {

        List<ViewItem> viewItems = new ArrayList<>();

        Set<String> tickets = map.keySet();

        for (String ticket : tickets) {

            List<ViewItem> temporaryViewItems = null;

            List<ConfirmInviteUser> confirmInviteUsers = map.get(ticket);

            for (ConfirmInviteUser confirmInviteUser : confirmInviteUsers) {

                if (confirmInviteUser.getOperateType().equals(ConfirmInviteUser.OPERATE_TYPE_PENDING)) {

                    if (temporaryViewItems == null)
                        temporaryViewItems = new ArrayList<>();

                    ViewContent viewContent = new ViewContent();
                    viewContent.setConfirmInviteUser(confirmInviteUser);

                    temporaryViewItems.add(viewContent);

                }

            }

            if (temporaryViewItems != null) {
                ViewHeader viewHeader = new ViewHeader();
                viewHeader.setTicketID(ticket);

                viewItems.add(viewHeader);

                viewItems.addAll(temporaryViewItems);
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

        private UserAvatar userAvatar;

        public ConfirmTicketContentViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            ConfirmInviteUserItemBinding binding = (ConfirmInviteUserItemBinding) viewDataBinding;
            userAvatar = binding.userAvatar;

        }

        public void refreshView(final ConfirmInviteUser confirmInviteUser) {

            User user = new User();
            user.setUserName(confirmInviteUser.getUserName());
            user.setAvatar(confirmInviteUser.getUserAvatar());

            user.setDefaultAvatar(Util.getUserNameFirstLetter(user.getUserName()));
            user.setDefaultAvatarBgColor(random.nextInt(3) + 1);

            userAvatar.setUser(user, imageLoader);

        }

    }

    @Override
    public void handleOperationEvent(OperationEvent operationEvent) {

        RetrieveTicketOperationEvent ticketOperationEvent = (RetrieveTicketOperationEvent) operationEvent;

        List<ConfirmInviteUser> confirmInviteUsers = ticketOperationEvent.getConfirmInviteUsers();

        mConfirmInviteUserMaps.clear();
        createMap(confirmInviteUsers);

        adapter.setViewItems(createViewItems(mConfirmInviteUserMaps));
        adapter.notifyDataSetChanged();

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
