package com.winsun.fruitmix.inbox.presenter;

import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.databinding.InboxCommentTitleBinding;
import com.winsun.fruitmix.databinding.InboxMediaItemBinding;
import com.winsun.fruitmix.databinding.InboxUserInvitationBinding;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.inbox.data.model.GroupFileComment;
import com.winsun.fruitmix.inbox.data.model.GroupMediaComment;
import com.winsun.fruitmix.inbox.data.model.GroupUserComment;
import com.winsun.fruitmix.inbox.data.source.InboxDataSource;
import com.winsun.fruitmix.inbox.view.InboxView;
import com.winsun.fruitmix.invitation.ConfirmInviteUser;
import com.winsun.fruitmix.invitation.data.InvitationDataSource;
import com.winsun.fruitmix.model.ViewItem;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.ArrayList;
import java.util.Collections;
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

    private InvitationDataSource mInvitationDataSource;

    private InboxListAdapter mInboxListAdapter;

    private List<ViewItem> mViewItems;

    public InboxListPresenter(InboxDataSource inboxDataSource, InvitationDataSource invitationDataSource,
                              LoadingViewModel loadingViewModel, NoContentViewModel noContentViewModel,
                              User currentUser, InboxView inboxView) {
        mInboxDataSource = inboxDataSource;
        mInvitationDataSource = invitationDataSource;
        mLoadingViewModel = loadingViewModel;
        mNoContentViewModel = noContentViewModel;

        this.currentUser = currentUser;

        mInboxView = inboxView;

        mImageLoader = InjectHttp.provideImageGifLoaderInstance(inboxView.getContext()).getImageLoader(inboxView.getContext());

        mInboxListAdapter = new InboxListAdapter();

    }

    public InboxListAdapter getInboxListAdapter() {
        return mInboxListAdapter;
    }

    public void refreshView() {

        mInvitationDataSource.getInvitation(new BaseLoadDataCallback<ConfirmInviteUser>() {
            @Override
            public void onSucceed(List<ConfirmInviteUser> data, OperationResult operationResult) {

                getInboxComment(filterConfirmInviteUsers(data));

            }

            @Override
            public void onFail(OperationResult operationResult) {

                getInboxComment(Collections.<ConfirmInviteUser>emptyList());

            }
        });

    }

    private List<ConfirmInviteUser> filterConfirmInviteUsers(List<ConfirmInviteUser> data) {

        List<ConfirmInviteUser> confirmInviteUsers = new ArrayList<>();

        for (ConfirmInviteUser confirmInviteUser : data) {

            if (confirmInviteUser.getOperateType() == ConfirmInviteUser.OPERATE_TYPE_PENDING)
                confirmInviteUsers.add(confirmInviteUser);

        }

        return confirmInviteUsers;

    }

    private void getInboxComment(final List<ConfirmInviteUser> confirmInviteUsers) {

        mInboxDataSource.getAllGroupInfoAboutUser(currentUser.getUuid(), new BaseLoadDataCallbackWrapper<>(
                new BaseLoadDataCallback<GroupUserComment>() {
                    @Override
                    public void onSucceed(List<GroupUserComment> data, OperationResult operationResult) {

                        if (mLoadingViewModel.showLoading.get())
                            mLoadingViewModel.showLoading.set(false);

                        mViewItems = createViewItem(confirmInviteUsers, data);

                        if (mViewItems.size() == 0) {

                            mNoContentViewModel.showNoContent.set(true);

                        } else {

                            mNoContentViewModel.showNoContent.set(false);

                            mInboxListAdapter.setViewItems(mViewItems);
                            mInboxListAdapter.notifyDataSetChanged();

                        }

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

    private List<ViewItem> createViewItem(List<ConfirmInviteUser> confirmInviteUsers, List<GroupUserComment> groupUserComments) {

        List<ViewItem> viewItems = new ArrayList<>();

        if (confirmInviteUsers.size() > 0)
            viewItems.add(new UserInvitationItem(confirmInviteUsers));

        for (GroupUserComment groupUserComment : groupUserComments) {

            if (groupUserComment instanceof GroupMediaComment) {

                MediaItem mediaItem = new MediaItem((GroupMediaComment) groupUserComment);

                viewItems.add(mediaItem);
            } else if (groupUserComment instanceof GroupFileComment) {

                FileItem fileItem = new FileItem((GroupFileComment) groupUserComment);

                viewItems.add(fileItem);

            }

        }

        return viewItems;

    }


    @Override
    public boolean isActive() {
        return true;
    }

    public void onDestroy() {

        mInboxView = null;

    }

    private static final int VIEW_MEDIA = 1;
    private static final int VIEW_USER_INVITATION = 2;
    private static final int VIEW_FILE = 3;

    public static final String TAG = InboxListAdapter.class.getSimpleName();

    private class InboxListAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<ViewItem> mViewItems;

        public InboxListAdapter() {
            mViewItems = new ArrayList<>();
        }

        public void setViewItems(List<ViewItem> viewItems) {

            mViewItems.clear();
            mViewItems.addAll(viewItems);

        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            BindingViewHolder bindingViewHolder;

            switch (viewType) {

                case VIEW_MEDIA:

                    InboxMediaItemBinding binding = InboxMediaItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent, false);

                    bindingViewHolder = new InboxMediaViewHolder(binding);

                    break;
                case VIEW_USER_INVITATION:

                    InboxUserInvitationBinding inboxUserInvitationBinding = InboxUserInvitationBinding
                            .inflate(LayoutInflater.from(parent.getContext()), parent, false);

                    bindingViewHolder = new InboxUserInvitationViewHolder(inboxUserInvitationBinding);

                    break;

                case VIEW_FILE:

                    InboxCommentTitleBinding inboxCommentTitleBinding = InboxCommentTitleBinding
                            .inflate(LayoutInflater.from(parent.getContext()), parent, false);

                    bindingViewHolder = new InboxFileViewHolder(inboxCommentTitleBinding);

                    break;

                default:
                    bindingViewHolder = null;
                    Log.e(TAG, "onCreateViewHolder: bindingViewHolder is null,error");

            }

            return bindingViewHolder;
        }


        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            if (holder == null)
                return;

            ViewItem viewItem = mViewItems.get(position);

            if (viewItem instanceof MediaItem) {

                InboxMediaViewHolder inboxMediaViewHolder = (InboxMediaViewHolder) holder;
                inboxMediaViewHolder.refreshView(((MediaItem) viewItem).getGroupMediaComment());


            } else if (viewItem instanceof UserInvitationItem) {

                InboxUserInvitationViewHolder inboxUserInvitationViewHolder = (InboxUserInvitationViewHolder) holder;

                inboxUserInvitationViewHolder.refreshView(((UserInvitationItem) viewItem).getConfirmInviteUsers());

            } else if (viewItem instanceof FileItem) {

                InboxFileViewHolder viewHolder = (InboxFileViewHolder) holder;

                viewHolder.refreshView(((FileItem) viewItem).getGroupFileComment());

            }

        }

        @Override
        public int getItemCount() {
            return mViewItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mViewItems.get(position).getType();
        }
    }

    private class MediaItem implements ViewItem {

        private GroupMediaComment mGroupMediaComment;

        public MediaItem(GroupMediaComment groupMediaComment) {
            mGroupMediaComment = groupMediaComment;
        }

        public GroupMediaComment getGroupMediaComment() {
            return mGroupMediaComment;
        }

        @Override
        public int getType() {
            return VIEW_MEDIA;
        }
    }

    private class UserInvitationItem implements ViewItem {

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

    private class FileItem implements ViewItem {

        private GroupFileComment mGroupFileComment;

        public FileItem(GroupFileComment groupFileComment) {
            mGroupFileComment = groupFileComment;
        }

        public GroupFileComment getGroupFileComment() {
            return mGroupFileComment;
        }

        @Override
        public int getType() {
            return VIEW_FILE;
        }
    }


    private class InboxMediaViewHolder extends BindingViewHolder {


        public InboxMediaViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        void refreshView(GroupMediaComment groupMediaComment) {

            InboxMediaItemBinding binding = (InboxMediaItemBinding) getViewDataBinding();

            refreshTitle(groupMediaComment, binding);

            new InboxMediaPresenter(binding, groupMediaComment, mImageLoader);


        }

        private void refreshTitle(GroupMediaComment groupMediaComment, InboxMediaItemBinding binding) {

            InboxCommentTitleBinding inboxCommentTitleBinding = binding.inboxCommentTitle;

            String from = mInboxView.getString(R.string.group_come_from, groupMediaComment.getGroupName());

            int mediaSize = groupMediaComment.getMedias().size();

            String share = mInboxView.getString(R.string.share_something,
                    mInboxView.getQuantityString(R.plurals.photo, mediaSize, mediaSize));

            String text = from + share;

            inboxCommentTitleBinding.shareText.setVisibility(View.GONE);

            refreshCommentTitle(groupMediaComment, inboxCommentTitleBinding, text);

        }

    }

    private class InboxFileViewHolder extends BindingViewHolder {

        public InboxFileViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        void refreshView(GroupFileComment groupFileComment) {

            InboxCommentTitleBinding binding = (InboxCommentTitleBinding) getViewDataBinding();

            AbstractFile file = groupFileComment.getAbstractFiles().get(0);

            String from = mInboxView.getString(R.string.group_come_from, groupFileComment.getGroupName());

            refreshCommentTitle(groupFileComment, binding, from);

            binding.sharePre.setVisibility(View.VISIBLE);
            binding.shareText.setVisibility(View.VISIBLE);

            SpannableString spannableString = new SpannableString("default" + file.getName());

            Drawable drawable = mInboxView.getContext().getResources().getDrawable(file.getFileTypeResID());

            drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());

            ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);

            spannableString.setSpan(imageSpan, 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            binding.shareText.setText(spannableString);

        }

    }

    private void refreshCommentTitle(GroupUserComment groupUserComment, InboxCommentTitleBinding binding,
                                     String groupInfoText) {

        binding.setGroupUserComment(groupUserComment);

        UserComment userComment = groupUserComment.getUserComment();

        binding.userAvatar.setUser(userComment.getCreator(),
                mImageLoader);

        binding.groupInfo.setText(groupInfoText);

        binding.time.setText(userComment.getDate(mInboxView.getContext()));

    }

    private class InboxUserInvitationViewHolder extends BindingViewHolder {


        public InboxUserInvitationViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        void refreshView(List<ConfirmInviteUser> confirmInviteUsers) {

            InboxUserInvitationBinding binding = (InboxUserInvitationBinding) getViewDataBinding();

            new InboxUserInvitationPresenter(binding, confirmInviteUsers, mInvitationDataSource, mImageLoader, mInboxView);

        }

    }


}
