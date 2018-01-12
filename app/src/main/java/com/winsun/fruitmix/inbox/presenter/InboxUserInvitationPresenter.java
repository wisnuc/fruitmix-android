package com.winsun.fruitmix.inbox.presenter;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.databinding.InboxUserInvitationBinding;
import com.winsun.fruitmix.databinding.InboxUserInvitationItemBinding;
import com.winsun.fruitmix.invitation.ConfirmInviteUser;
import com.winsun.fruitmix.viewholder.BindingViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/11.
 */

public class InboxUserInvitationPresenter {

    private InboxUserInvitationBinding mInboxUserInvitationBinding;

    private List<ConfirmInviteUser> mConfirmInviteUsers;

    private ImageLoader mImageLoader;

    public InboxUserInvitationPresenter(InboxUserInvitationBinding inboxUserInvitationBinding, List<ConfirmInviteUser> confirmInviteUsers, ImageLoader imageLoader) {
        mInboxUserInvitationBinding = inboxUserInvitationBinding;
        mConfirmInviteUsers = confirmInviteUsers;
        mImageLoader = imageLoader;
    }


    private class InboxUserInvitationRecyclerViewAdapter extends RecyclerView.Adapter<InboxUserInvitationViewHolder>{

        private List<ConfirmInviteUser> mConfirmInviteUsers;

        public InboxUserInvitationRecyclerViewAdapter() {
            mConfirmInviteUsers = new ArrayList<>();
        }

        @Override
        public InboxUserInvitationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(InboxUserInvitationViewHolder holder, int position) {

        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return 0;
        }
    }


    private class InboxUserInvitationViewHolder extends BindingViewHolder{


        public InboxUserInvitationViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        void refreshView(ConfirmInviteUser confirmInviteUser){

            InboxUserInvitationItemBinding binding = (InboxUserInvitationItemBinding) getViewDataBinding();


        }

    }


}
