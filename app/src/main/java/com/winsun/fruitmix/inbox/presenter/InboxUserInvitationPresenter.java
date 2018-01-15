package com.winsun.fruitmix.inbox.presenter;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackWrapper;
import com.winsun.fruitmix.databinding.InboxUserInvitationBinding;
import com.winsun.fruitmix.databinding.InboxUserInvitationItemBinding;
import com.winsun.fruitmix.inbox.view.InboxView;
import com.winsun.fruitmix.invitation.ConfirmInviteUser;
import com.winsun.fruitmix.invitation.data.InvitationDataSource;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.parser.HttpErrorBodyParser;
import com.winsun.fruitmix.viewholder.BindingViewHolder;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/1/11.
 */

public class InboxUserInvitationPresenter implements ActiveView {

    private InboxUserInvitationBinding mInboxUserInvitationBinding;

    private List<ConfirmInviteUser> mConfirmInviteUsers;

    private InvitationDataSource mInvitationDataSource;

    private ImageLoader mImageLoader;

    private Random mRandom;

    private InboxUserInvitationRecyclerViewAdapter mInboxUserInvitationRecyclerViewAdapter;

    private InboxView mInboxView;

    public InboxUserInvitationPresenter(InboxUserInvitationBinding inboxUserInvitationBinding, List<ConfirmInviteUser> confirmInviteUsers,
                                        InvitationDataSource invitationDataSource, ImageLoader imageLoader, InboxView inboxView) {

        mInboxUserInvitationBinding = inboxUserInvitationBinding;
        mConfirmInviteUsers = confirmInviteUsers;
        mInvitationDataSource = invitationDataSource;
        mImageLoader = imageLoader;

        mInboxView = inboxView;

        mRandom = new Random();

        RecyclerView recyclerView = mInboxUserInvitationBinding.inboxUserInvitationRecyclerView;

        Context context = mInboxUserInvitationBinding.getRoot().getContext();

        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mInboxUserInvitationRecyclerViewAdapter = new InboxUserInvitationRecyclerViewAdapter();

        recyclerView.setAdapter(mInboxUserInvitationRecyclerViewAdapter);

        mInboxUserInvitationRecyclerViewAdapter.setConfirmInviteUsers(confirmInviteUsers);
        mInboxUserInvitationRecyclerViewAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean isActive() {
        return mInboxView != null;
    }


    private class InboxUserInvitationRecyclerViewAdapter extends RecyclerView.Adapter<InboxUserInvitationViewHolder> {

        private List<ConfirmInviteUser> mConfirmInviteUsers;

        public InboxUserInvitationRecyclerViewAdapter() {
            mConfirmInviteUsers = new ArrayList<>();
        }

        public void setConfirmInviteUsers(List<ConfirmInviteUser> confirmInviteUsers) {
            mConfirmInviteUsers.clear();

            mConfirmInviteUsers.addAll(confirmInviteUsers);
        }

        @Override
        public InboxUserInvitationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            InboxUserInvitationItemBinding binding = InboxUserInvitationItemBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false);

            return new InboxUserInvitationViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(InboxUserInvitationViewHolder holder, int position) {

            holder.refreshView(mConfirmInviteUsers.get(position));

        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return mConfirmInviteUsers.size();
        }
    }


    private class InboxUserInvitationViewHolder extends BindingViewHolder {

        InboxUserInvitationViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        void refreshView(ConfirmInviteUser confirmInviteUser) {

            InboxUserInvitationItemBinding binding = (InboxUserInvitationItemBinding) getViewDataBinding();

            binding.setConfirmInviteUser(confirmInviteUser);

            binding.executePendingBindings();

            binding.userAvatar2.setUser(confirmInviteUser.generateUser(mRandom), mImageLoader);

            binding.setInboxUserInvitationPresenter(InboxUserInvitationPresenter.this);

        }

    }

    public void acceptInvitation(ConfirmInviteUser confirmInviteUser) {

        postOperation(confirmInviteUser, true, R.string.accept_invitation);

    }

    public void refuseInvitation(ConfirmInviteUser confirmInviteUser) {

        postOperation(confirmInviteUser, false, R.string.refuse_invitation);

    }


    private void postOperation(final ConfirmInviteUser confirmInviteUser, final boolean isAccepted, final int resID) {

        mInboxView.showProgressDialog(String.format(mInboxView.getString(R.string.operating_title),
                mInboxView.getString(R.string.invitation)));

        mInvitationDataSource.confirmInvitation(confirmInviteUser, isAccepted, new BaseOperateDataCallbackWrapper<>(
                new BaseOperateDataCallback<String>() {
                    @Override
                    public void onSucceed(final String data, OperationResult result) {

                        mInboxView.dismissDialog();

                        mInboxView.showToast(String.format(mInboxView.getString(R.string.success),
                                mInboxView.getString(resID)));

                        handleOperateSucceed(confirmInviteUser, isAccepted);

                    }

                    @Override
                    public void onFail(OperationResult result) {

                        mInboxView.dismissDialog();

                        if (result instanceof OperationNetworkException) {

                            HttpErrorBodyParser parser = new HttpErrorBodyParser();

                            try {
                                String messageInBody = parser.parse(((OperationNetworkException) result).getHttpResponseData());

                                mInboxView.showToast(messageInBody);

                            } catch (JSONException e) {
                                e.printStackTrace();

                                mInboxView.showToast(result.getResultMessage(mInboxView.getContext()));
                            }

                        } else {

                            mInboxView.showToast(result.getResultMessage(mInboxView.getContext()));

                        }

                    }
                }, this
        ));


    }

    private void handleOperateSucceed(ConfirmInviteUser confirmInviteUser, boolean isAccepted) {

        if (isAccepted)
            confirmInviteUser.setOperateType(ConfirmInviteUser.OPERATE_TYPE_ACCEPT);
        else
            confirmInviteUser.setOperateType(ConfirmInviteUser.OPERATE_TYPE_REFUSE);

        int position = mConfirmInviteUsers.indexOf(confirmInviteUser);

        mConfirmInviteUsers.remove(confirmInviteUser);

        mInboxUserInvitationRecyclerViewAdapter.setConfirmInviteUsers(mConfirmInviteUsers);

        mInboxUserInvitationRecyclerViewAdapter.notifyItemRemoved(position);

    }


}
