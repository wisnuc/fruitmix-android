package com.winsun.fruitmix.group.presenter;

import android.databinding.BindingAdapter;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.model.UserCommentShowStrategy;
import com.winsun.fruitmix.group.data.model.UserCommentViewFactory;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.viewmodel.GroupContentViewModel;
import com.winsun.fruitmix.group.view.customview.CustomArrowToggleButton;
import com.winsun.fruitmix.group.view.customview.UserCommentView;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.logged.in.user.LoggedInUserRepository;
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

    private String currentLoggedInUserUUID;

    private String groupUUID;

    public GroupContentPresenter(String groupUUID, LoggedInUserDataSource loggedInUserDataSource, GroupRepository groupRepository, GroupContentViewModel groupContentViewModel) {

        this.groupUUID = groupUUID;
        this.groupRepository = groupRepository;
        this.groupContentViewModel = groupContentViewModel;

        currentLoggedInUserUUID = loggedInUserDataSource.getCurrentLoggedInUser().getUser().getUuid();

        groupContentAdapter = new GroupContentAdapter();

    }

    public GroupContentAdapter getGroupContentAdapter() {
        return groupContentAdapter;
    }

    public void refreshGroup(){

        PrivateGroup currentPrivateGroup = groupRepository.getGroup(groupUUID);

        groupContentAdapter.setUserComments(currentPrivateGroup.getUserComments());
        groupContentAdapter.notifyDataSetChanged();
    }


    @Override
    public void onPingToggleArrowToDown() {

        groupContentViewModel.showPing.set(false);

    }

    @Override
    public void onPingToggleArrowToUp() {

        groupContentViewModel.showPing.set(true);

    }

    private class GroupContentAdapter extends RecyclerView.Adapter<UserCommentViewHolder>{

        private List<UserComment> mUserComments;

        private UserCommentViewFactory factory;

        public GroupContentAdapter() {
            mUserComments = new ArrayList<>();

            factory = UserCommentViewFactory.getInstance();
        }

        public void setUserComments(List<UserComment> userComments) {
            mUserComments.clear();
            mUserComments.addAll(userComments);
        }

        @Override
        public UserCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            UserCommentView userCommentView = factory.createUserCommentView(viewType);

            ViewDataBinding viewDataBinding = userCommentView.getViewDataBinding(parent.getContext());

            return new UserCommentViewHolder(viewDataBinding.getRoot(),userCommentView);
        }


        @Override
        public void onBindViewHolder(UserCommentViewHolder holder, int position) {

            UserComment preUserComment;
            UserComment currentUserComment;

            if(position == 0)
                preUserComment = null;
            else
                preUserComment = mUserComments.get(position - 1);

            currentUserComment = mUserComments.get(position);

            UserCommentShowStrategy userCommentShowStrategy = new UserCommentShowStrategy(preUserComment,currentUserComment,currentLoggedInUserUUID);

            holder.userCommentView.refreshCommentView(userCommentShowStrategy,currentUserComment);

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


    private class UserCommentViewHolder extends RecyclerView.ViewHolder{

        private UserCommentView userCommentView;

        public UserCommentViewHolder(View itemView, UserCommentView userCommentView) {
            super(itemView);
            this.userCommentView = userCommentView;
        }

    }

}
