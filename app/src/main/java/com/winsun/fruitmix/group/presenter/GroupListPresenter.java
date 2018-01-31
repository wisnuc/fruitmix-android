package com.winsun.fruitmix.group.presenter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.databinding.GroupListItemBinding;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.TextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.viewmodel.GroupListViewModel;
import com.winsun.fruitmix.group.view.GroupListPageView;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.token.TokenDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/21.
 */

public class GroupListPresenter implements ActiveView {

    private GroupRepository groupRepository;

    private User mCurrentUser;

    private TokenDataSource mTokenDataSource;

    private GroupListAdapter groupListAdapter;

    private LoadingViewModel loadingViewModel;
    private NoContentViewModel noContentViewModel;
    private GroupListViewModel groupListViewModel;

    private GroupListPageView groupListPageView;

    private UserDataRepository mUserDataRepository;

    private SystemSettingDataSource mSystemSettingDataSource;

    public GroupListPresenter(GroupListPageView groupListPageView, User currentUser,
                              TokenDataSource tokenDataSource, GroupRepository groupRepository,
                              LoadingViewModel loadingViewModel, NoContentViewModel noContentViewModel,
                              GroupListViewModel groupListViewModel, UserDataRepository userDataRepository,
                              SystemSettingDataSource systemSettingDataSource) {
        this.groupRepository = groupRepository;
        mCurrentUser = currentUser;
        mTokenDataSource = tokenDataSource;
        this.loadingViewModel = loadingViewModel;
        this.noContentViewModel = noContentViewModel;
        this.groupListViewModel = groupListViewModel;

        this.groupListPageView = groupListPageView;

        mUserDataRepository = userDataRepository;

        mSystemSettingDataSource = systemSettingDataSource;

        groupListAdapter = new GroupListAdapter();

    }

    public void onDestroyView() {
        groupListPageView = null;
    }

    public GroupListAdapter getGroupListAdapter() {
        return groupListAdapter;
    }

    public void refreshView() {

        if(mSystemSettingDataSource.getLoginWithWechatCodeOrNot()){

            groupRepository.setCloudToken(mSystemSettingDataSource.getCurrentWAToken());

            refreshGroups();

        }else {

            mTokenDataSource.getWATokenThroughStationToken(mCurrentUser.getAssociatedWeChatGUID(),
                    new BaseLoadDataCallbackWrapper<>(
                            new BaseLoadDataCallback<String>() {
                                @Override
                                public void onSucceed(List<String> data, OperationResult operationResult) {

                                    groupRepository.setCloudToken(data.get(0));

                                    refreshGroups();

                                }

                                @Override
                                public void onFail(OperationResult operationResult) {

                                    loadingViewModel.showLoading.set(false);
                                    noContentViewModel.showNoContent.set(true);

                                    groupListViewModel.showRecyclerView.set(false);
                                    groupListViewModel.showAddFriendsFAB.set(false);

                                }
                            }, this
                    )

            );

        }


    }

    public void refreshGroups() {

        groupRepository.getGroupList(new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<PrivateGroup>() {
            @Override
            public void onSucceed(final List<PrivateGroup> data, OperationResult operationResult) {

                loadingViewModel.showLoading.set(false);

                if (data.size() > 0) {

                    for (PrivateGroup group : data) {

                        List<User> users = group.getUsers();
                        List<User> usersWithInfo = new ArrayList<>(users.size());

                        for (User user : users) {

                            User userWithInfo = mUserDataRepository.getUserByGUID(user.getAssociatedWeChatGUID());
                            usersWithInfo.add(userWithInfo);

                        }

                        group.clearUsers();
                        group.addUsers(usersWithInfo);

                    }

                    noContentViewModel.showNoContent.set(false);

                    groupListViewModel.showRecyclerView.set(true);

                    groupListAdapter.setPrivateGroups(data);
                    groupListAdapter.notifyDataSetChanged();

                } else {

                    noContentViewModel.showNoContent.set(true);

                    groupListViewModel.showRecyclerView.set(false);
                }

                groupListViewModel.showAddFriendsFAB.set(false);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                loadingViewModel.showLoading.set(false);
                noContentViewModel.showNoContent.set(true);

                groupListViewModel.showRecyclerView.set(false);
                groupListViewModel.showAddFriendsFAB.set(false);


            }
        }, this));

    }

    @Override
    public boolean isActive() {
        return groupListPageView != null;
    }

    public class GroupListAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<PrivateGroup> mPrivateGroups;

        GroupListAdapter() {
            mPrivateGroups = new ArrayList<>();
        }

        void setPrivateGroups(List<PrivateGroup> privateGroups) {

            mPrivateGroups.clear();

            mPrivateGroups.addAll(privateGroups);

        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            GroupListItemBinding binding = GroupListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

            return new BindingViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            final PrivateGroup privateGroup = mPrivateGroups.get(position);

            holder.getViewDataBinding().setVariable(BR.privateGroup, privateGroup);
            holder.getViewDataBinding().executePendingBindings();

            GroupListItemBinding binding = (GroupListItemBinding) holder.getViewDataBinding();
            binding.lastCommentContent.setText(getLastCommentContent(privateGroup));

            holder.getViewDataBinding().getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String groupName = privateGroup.getName();

                    if (groupName.isEmpty()) {
                        groupName = groupListPageView.getString(R.string.group_chat, privateGroup.getUsers().size());
                    }

                    groupListPageView.gotoGroupContentActivity(privateGroup.getUUID(), groupName);

                }
            });

        }

        @Override
        public int getItemCount() {
            return mPrivateGroups.size();
        }

    }

    public String getLastCommentContent(PrivateGroup privateGroup) {

        UserComment userComment = privateGroup.getLastComment();

        if (userComment != null && userComment instanceof TextComment) {

            TextComment textComment = (TextComment) userComment;

            return textComment.getCreator().getUserName() + ":" + textComment.getText();
        } else {
            return "";
        }

    }


}
