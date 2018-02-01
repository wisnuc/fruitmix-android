package com.winsun.fruitmix.group.setting;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.databinding.AddReduceGroupMemberItemBinding;
import com.winsun.fruitmix.databinding.GroupMemberItemBinding;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.model.ViewItem;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.viewholder.BindingViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/31.
 */

public class GroupSettingPresenter implements ActiveView {

    private GroupSettingViewModel mGroupSettingViewModel;

    private GroupRepository mGroupRepository;

    private ImageLoader mImageLoader;

    private GroupSettingView mGroupSettingView;

    private String mGroupUUID;

    private static final int GROUP_MEMBER = 0;
    private static final int GROUP_ADD_MEMBER = 1;
    private static final int GROUP_DELETE_MEMBER = 2;

    private GroupMemberRecyclerViewAdapter mGroupMemberRecyclerViewAdapter;

    public GroupSettingPresenter(GroupSettingViewModel groupSettingViewModel, GroupRepository groupRepository,
                                 String groupUUID, ImageLoader imageLoader, GroupSettingView groupSettingView) {

        mGroupSettingViewModel = groupSettingViewModel;
        mGroupRepository = groupRepository;
        mImageLoader = imageLoader;
        mGroupSettingView = groupSettingView;
        mGroupUUID = groupUUID;

        mGroupMemberRecyclerViewAdapter = new GroupMemberRecyclerViewAdapter();

    }

    public void setAdapter(RecyclerView recyclerView) {
        recyclerView.setAdapter(mGroupMemberRecyclerViewAdapter);
    }

    public void refreshView() {

        mGroupRepository.getGroupFromMemory(mGroupUUID, new BaseLoadDataCallbackWrapper<PrivateGroup>(
                new BaseLoadDataCallback<PrivateGroup>() {
                    @Override
                    public void onSucceed(List<PrivateGroup> data, OperationResult operationResult) {

                        refreshViewAfterGetGroup(data.get(0));

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                    }
                }, this
        ));


    }

    private void refreshViewAfterGetGroup(PrivateGroup group) {
        if (group.getName().isEmpty()) {
            mGroupSettingViewModel.groupName.set(mGroupSettingView.getString(R.string.unnamed));
        } else {
            mGroupSettingViewModel.groupName.set(group.getName());
        }

        List<User> users = group.getUsers();

        int userSize = users.size();

        List<ViewItem> viewItems = new ArrayList<>(userSize + 2);

        if (userSize > 10)
            mGroupSettingViewModel.showCheckMoreMembers.set(true);
        else
            mGroupSettingViewModel.showCheckMoreMembers.set(false);

        for (User user : users) {
            viewItems.add(new GroupMemberViewItem(user));
        }

        viewItems.add(new GroupAddMemberViewItem());
        viewItems.add(new GroupDeleteMemberViewItem());

        mGroupMemberRecyclerViewAdapter.setViewItems(viewItems);
        mGroupMemberRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void onDestroy() {

        mGroupSettingView = null;

    }

    public void modifyGroupName() {


    }

    @Override
    public boolean isActive() {
        return mGroupSettingView != null;
    }

    private class GroupMemberRecyclerViewAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<ViewItem> mViewItems;

        public GroupMemberRecyclerViewAdapter() {
            mViewItems = new ArrayList<>();
        }

        public void setViewItems(List<ViewItem> viewItems) {
            mViewItems.clear();
            mViewItems.addAll(viewItems);
        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ViewDataBinding binding;

            switch (viewType) {
                case GROUP_ADD_MEMBER:
                case GROUP_DELETE_MEMBER:

                    binding = AddReduceGroupMemberItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent, false);

                    return new GroupAddDeleteMemberViewHolder(binding);


                case GROUP_MEMBER:

                    binding = GroupMemberItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent, false);

                    return new GroupMemberViewHolder(binding);


                default:

                    throw new IllegalStateException("create group member layout error");

            }

        }


        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            if (holder instanceof GroupAddDeleteMemberViewHolder) {

                ((GroupAddDeleteMemberViewHolder) holder).refreshView(mViewItems.get(position));

            } else if (holder instanceof GroupMemberViewHolder) {

                GroupMemberViewItem groupMemberViewItem = (GroupMemberViewItem) mViewItems.get(position);

                ((GroupMemberViewHolder) holder).refreshView(groupMemberViewItem.getUser());

            }

        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return mViewItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mViewItems.get(position).getType();
        }
    }

    private class GroupMemberViewHolder extends BindingViewHolder {

        private GroupMemberItemBinding mGroupMemberItemBinding;

        public GroupMemberViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            mGroupMemberItemBinding = (GroupMemberItemBinding) viewDataBinding;
        }

        void refreshView(User user) {

            Context context = mGroupMemberItemBinding.getRoot().getContext();

            GroupMemberItemViewModel groupMemberItemViewModel = mGroupMemberItemBinding.getGroupMemberItemViewModel();

            if (groupMemberItemViewModel == null)
                groupMemberItemViewModel = new GroupMemberItemViewModel();

            groupMemberItemViewModel.userName.set(user.getFormatUserName(context, 4));

            mGroupMemberItemBinding.setGroupMemberItemViewModel(groupMemberItemViewModel);

            mGroupMemberItemBinding.userAvatar3.setUser(user, mImageLoader);

        }

    }

    private class GroupAddDeleteMemberViewHolder extends BindingViewHolder {

        private AddReduceGroupMemberItemBinding mAddReduceGroupMemberItemBinding;

        public GroupAddDeleteMemberViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            mAddReduceGroupMemberItemBinding = (AddReduceGroupMemberItemBinding) viewDataBinding;
        }

        void refreshView(ViewItem viewItem) {

            if (viewItem.getType() == GROUP_ADD_MEMBER) {

                mAddReduceGroupMemberItemBinding.addReduceImg.setImageResource(R.drawable.add_group_member);


            } else {

                mAddReduceGroupMemberItemBinding.addReduceImg.setImageResource(R.drawable.reduce_group_member);

            }

        }

    }


    private class GroupMemberViewItem implements ViewItem {

        private User mUser;

        public GroupMemberViewItem(User user) {
            mUser = user;
        }

        public User getUser() {
            return mUser;
        }

        @Override
        public int getType() {
            return GROUP_MEMBER;
        }

    }

    private class GroupAddMemberViewItem implements ViewItem {

        @Override
        public int getType() {
            return GROUP_ADD_MEMBER;
        }

    }

    private class GroupDeleteMemberViewItem implements ViewItem {

        @Override
        public int getType() {
            return GROUP_DELETE_MEMBER;
        }

    }


}
