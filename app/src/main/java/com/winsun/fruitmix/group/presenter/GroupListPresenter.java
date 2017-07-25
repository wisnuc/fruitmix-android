package com.winsun.fruitmix.group.presenter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.databinding.GroupListItemBinding;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.TextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.viewmodel.GroupListViewModel;
import com.winsun.fruitmix.group.view.GroupListPageView;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/21.
 */

public class GroupListPresenter {

    private GroupRepository groupRepository;

    private GroupListAdapter groupListAdapter;

    private ThreadManager threadManager;

    private LoadingViewModel loadingViewModel;
    private NoContentViewModel noContentViewModel;
    private GroupListViewModel groupListViewModel;

    private GroupListPageView groupListPageView;

    public GroupListPresenter(GroupListPageView groupListPageView, GroupRepository groupRepository, LoadingViewModel loadingViewModel, NoContentViewModel noContentViewModel, GroupListViewModel groupListViewModel) {
        this.groupRepository = groupRepository;
        this.loadingViewModel = loadingViewModel;
        this.noContentViewModel = noContentViewModel;
        this.groupListViewModel = groupListViewModel;

        this.groupListPageView = groupListPageView;

        groupListAdapter = new GroupListAdapter();

        threadManager = ThreadManager.getInstance();

    }

    public void onDestroyView() {
        groupListPageView = null;
    }

    public GroupListAdapter getGroupListAdapter() {
        return groupListAdapter;
    }

    public void refreshGroups() {

        threadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                groupRepository.getGroupList(new BaseLoadDataCallback<PrivateGroup>() {
                    @Override
                    public void onSucceed(final List<PrivateGroup> data, OperationResult operationResult) {

                        threadManager.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {

                                loadingViewModel.showLoading.set(false);
                                noContentViewModel.showNoContent.set(false);

                                groupListViewModel.showRecyclerView.set(true);
                                groupListViewModel.showAddFriendsFAB.set(true);

                                groupListAdapter.setPrivateGroups(data);
                                groupListAdapter.notifyDataSetChanged();

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

                                groupListViewModel.showRecyclerView.set(false);
                                groupListViewModel.showAddFriendsFAB.set(true);

                            }
                        });

                    }
                });

            }
        });


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

                    groupListPageView.gotoGroupContentActivity(privateGroup.getUUID(), privateGroup.getName());

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

        if (userComment instanceof TextComment) {

            TextComment textComment = (TextComment) userComment;

            return textComment.getCreator().getUserName() + ":" + textComment.getText();
        } else {
            return "";
        }

    }


}
