package com.winsun.fruitmix.group.presenter;

import android.databinding.ViewDataBinding;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.databinding.GroupPingItemBinding;
import com.winsun.fruitmix.group.data.model.Ping;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.TextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.model.UserCommentShowStrategy;
import com.winsun.fruitmix.group.data.model.UserCommentViewFactory;
import com.winsun.fruitmix.group.data.source.FakeGroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.viewmodel.GroupContentViewModel;
import com.winsun.fruitmix.group.view.GroupContentView;
import com.winsun.fruitmix.group.view.customview.CustomArrowToggleButton;
import com.winsun.fruitmix.group.view.customview.UserCommentView;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.viewholder.BindingViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/21.
 */

public class GroupContentPresenter implements CustomArrowToggleButton.PingToggleListener {

    private GroupRepository groupRepository;

    private GroupContentViewModel groupContentViewModel;

    private GroupContentAdapter groupContentAdapter;

    private PingViewPageAdapter pingViewPageAdapter;

    private User currentLoggedInUser;

    private String groupUUID;

    private PrivateGroup currentPrivateGroup;

    private GroupContentView groupContentView;

    public GroupContentPresenter(GroupContentView groupContentView, String groupUUID, LoggedInUserDataSource loggedInUserDataSource, GroupRepository groupRepository, GroupContentViewModel groupContentViewModel) {

        this.groupContentView = groupContentView;
        this.groupUUID = groupUUID;
        this.groupRepository = groupRepository;
        this.groupContentViewModel = groupContentViewModel;

//        currentLoggedInUser = loggedInUserDataSource.getCurrentLoggedInUser().getUser();

        currentLoggedInUser = new User();
        currentLoggedInUser.setUuid(FakeGroupDataSource.MYSELF_UUID);

        groupContentAdapter = new GroupContentAdapter();

        pingViewPageAdapter = new PingViewPageAdapter();
    }

    public GroupContentAdapter getGroupContentAdapter() {
        return groupContentAdapter;
    }

    public PingViewPageAdapter getPingViewPageAdapter() {
        return pingViewPageAdapter;
    }

    public void refreshGroup() {

        currentPrivateGroup = groupRepository.getGroup(groupUUID);

        groupContentAdapter.setUserComments(currentPrivateGroup.getUserComments());
        groupContentAdapter.notifyDataSetChanged();

        pingViewPageAdapter.setPings(currentPrivateGroup.getPings());
        pingViewPageAdapter.notifyDataSetChanged();

    }

    public void onDestroy() {

        groupContentView = null;

    }

    @Override
    public void onPingToggleArrowToDown() {

        groupContentViewModel.showPing.set(true);

    }

    @Override
    public void onPingToggleArrowToUp() {

        groupContentViewModel.showPing.set(false);

    }

    private class GroupContentAdapter extends RecyclerView.Adapter<UserCommentViewHolder> {

        private List<UserComment> mUserComments;

        private UserCommentViewFactory factory;

        GroupContentAdapter() {
            mUserComments = new ArrayList<>();

            factory = UserCommentViewFactory.getInstance();
        }

        void setUserComments(List<UserComment> userComments) {
            mUserComments.clear();
            mUserComments.addAll(userComments);
        }

        @Override
        public UserCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            UserCommentView userCommentView = factory.createUserCommentView(viewType);

            ViewDataBinding viewDataBinding = userCommentView.getViewDataBinding(parent.getContext());

            return new UserCommentViewHolder(viewDataBinding.getRoot(), userCommentView);
        }


        @Override
        public void onBindViewHolder(UserCommentViewHolder holder, int position) {

            UserComment preUserComment;
            UserComment currentUserComment;

            if (position == 0)
                preUserComment = null;
            else
                preUserComment = mUserComments.get(position - 1);

            currentUserComment = mUserComments.get(position);

            UserCommentShowStrategy userCommentShowStrategy = new UserCommentShowStrategy(preUserComment, currentUserComment, currentLoggedInUser.getUuid());

            holder.userCommentView.refreshCommentView(userCommentShowStrategy, currentUserComment);

        }

        @Override
        public int getItemCount() {
            return mUserComments.size();
        }

        @Override
        public int getItemViewType(int position) {

            return factory.getUserCommentViewType(mUserComments.get(position));

        }
    }


    private class UserCommentViewHolder extends RecyclerView.ViewHolder {

        private UserCommentView userCommentView;

        UserCommentViewHolder(View itemView, UserCommentView userCommentView) {
            super(itemView);
            this.userCommentView = userCommentView;
        }

    }

    private class PingViewPageAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<Ping> mPings;

        PingViewPageAdapter() {
            mPings = new ArrayList<>();
        }

        void setPings(List<Ping> pings) {
            mPings.clear();
            mPings.addAll(pings);
        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            GroupPingItemBinding binding = GroupPingItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

            return new BindingViewHolder(binding);
        }


        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            holder.getViewDataBinding().setVariable(BR.ping, mPings.get(position));

            holder.getViewDataBinding().executePendingBindings();

        }

        @Override
        public int getItemCount() {
            return mPings.size();
        }

    }


    public void sendTxt(String text) {

        TextComment textComment = new TextComment(currentLoggedInUser, System.currentTimeMillis(), text);

        groupRepository.insertUserComment(groupUUID, textComment, new BaseOperateDataCallbackImpl<UserComment>() {
            @Override
            public void onSucceed(UserComment data, OperationResult result) {
                super.onSucceed(data, result);

                List<UserComment> userComments = currentPrivateGroup.getUserComments();

                int lastPosition = userComments.size() - 1;

                groupContentAdapter.setUserComments(userComments);
                groupContentAdapter.notifyItemInserted(lastPosition);

                groupContentView.smoothToChatListPosition(lastPosition);

                groupContentView.clearEditText();

            }
        });

    }


}
