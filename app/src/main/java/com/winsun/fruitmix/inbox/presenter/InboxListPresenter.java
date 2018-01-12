package com.winsun.fruitmix.inbox.presenter;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.databinding.InboxCommentTitleBinding;
import com.winsun.fruitmix.databinding.InboxMediaItemBinding;
import com.winsun.fruitmix.databinding.InboxUserInvitationBinding;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.inbox.data.model.GroupMediaComment;
import com.winsun.fruitmix.inbox.data.model.GroupUserComment;
import com.winsun.fruitmix.inbox.data.source.InboxDataSource;
import com.winsun.fruitmix.inbox.view.InboxView;
import com.winsun.fruitmix.invitation.ConfirmInviteUser;
import com.winsun.fruitmix.model.ViewItemType;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.viewholder.BaseBindingViewHolder;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.List;

/**
 * Created by Administrator on 2018/1/10.
 */

public class InboxListPresenter implements ActiveView {

    private InboxDataSource mInboxDataSource;

    private LoadingViewModel mLoadingViewModel;
    private NoContentViewModel mNoContentViewModel;

    private User currentUser;

    private ImageLoader mImageLoader;

    private InboxView mInboxView;

    public InboxListPresenter(InboxDataSource inboxDataSource, LoadingViewModel loadingViewModel, NoContentViewModel noContentViewModel,
                              User currentUser, InboxView inboxView) {
        mInboxDataSource = inboxDataSource;
        mLoadingViewModel = loadingViewModel;
        mNoContentViewModel = noContentViewModel;

        this.currentUser = currentUser;

        mInboxView = inboxView;

        mImageLoader = InjectHttp.provideImageGifLoaderInstance(inboxView.getContext()).getImageLoader(inboxView.getContext());
    }

    public void refreshView() {

        mInboxDataSource.getAllGroupInfoAboutUser(currentUser.getUuid(), new BaseLoadDataCallbackWrapper<GroupUserComment>(
                new BaseLoadDataCallback<GroupUserComment>() {
                    @Override
                    public void onSucceed(List<GroupUserComment> data, OperationResult operationResult) {

                        if (mLoadingViewModel.showLoading.get())
                            mLoadingViewModel.showLoading.set(false);

                        mNoContentViewModel.showNoContent.set(false);
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        if (mLoadingViewModel.showLoading.get())
                            mLoadingViewModel.showLoading.set(false);

                        mNoContentViewModel.showNoContent.set(true);

                    }
                }, this
        ));


    }

    @Override
    public boolean isActive() {
        return true;
    }

    public void onDestroy() {

        mInboxView = null;

    }

    public static final int VIEW_MEDIA = 1;
    public static final int VIEW_USER_INVITATION = 2;

    private class InboxListAdapter extends RecyclerView.Adapter<BindingViewHolder> {


        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }


        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

    private class MediaItem implements ViewItemType {

        private GroupUserComment mGroupUserComment;

        public MediaItem(GroupUserComment groupUserComment) {
            mGroupUserComment = groupUserComment;
        }

        public GroupUserComment getGroupUserComment() {
            return mGroupUserComment;
        }

        @Override
        public int getType() {
            return VIEW_MEDIA;
        }
    }

    private class UserInvitationItem implements ViewItemType {

        private List<ConfirmInviteUser> mConfirmInviteUsers;

        public UserInvitationItem(List<ConfirmInviteUser> confirmInviteUsers) {
            mConfirmInviteUsers = confirmInviteUsers;
        }

        public List<ConfirmInviteUser> getConfirmInviteUsers() {
            return mConfirmInviteUsers;
        }

        @Override
        public int getType() {
            return VIEW_USER_INVITATION;
        }
    }


    private class InboxMediaViewHolder extends BindingViewHolder {


        public InboxMediaViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        void refreshView(GroupUserComment groupUserComment) {

            InboxMediaItemBinding binding = (InboxMediaItemBinding) getViewDataBinding();

            refreshTitle(groupUserComment, binding);

            new InboxMediaPresenter(binding, (GroupMediaComment) groupUserComment,mImageLoader);


        }

        private void refreshTitle(GroupUserComment groupUserComment, InboxMediaItemBinding binding) {
            binding.setGroupUserComment(groupUserComment);

            InboxCommentTitleBinding inboxCommentTitleBinding = binding.inboxCommentTitle;

            inboxCommentTitleBinding.userAvatar.setUser(groupUserComment.getUserComment().getCreator(),
                    mImageLoader);

            inboxCommentTitleBinding.groupInfo.setText(mInboxView.getString(R.string.group_come_from, groupUserComment.getGroupName()));

            inboxCommentTitleBinding.time.setText(groupUserComment.getUserComment().getDate(mInboxView.getContext()));
        }


    }

    private class InboxUserInvitationViewHolder extends BindingViewHolder {


        public InboxUserInvitationViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        void refreshView(List<ConfirmInviteUser> confirmInviteUsers){

            InboxUserInvitationBinding binding = (InboxUserInvitationBinding) getViewDataBinding();




        }


    }


}
