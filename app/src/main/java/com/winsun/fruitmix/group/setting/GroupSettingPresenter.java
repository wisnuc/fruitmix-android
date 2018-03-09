package com.winsun.fruitmix.group.setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateCallbackWrapper;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.databinding.AddReduceGroupMemberItemBinding;
import com.winsun.fruitmix.databinding.GroupMemberItemBinding;
import com.winsun.fruitmix.databinding.ModifyGroupNameLayoutBinding;
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.model.ViewItem;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.recyclerview.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

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

    private PrivateGroup mPrivateGroup;

    private User mCurrentUser;

    private ToolbarViewModel mToolbarViewModel;

    public GroupSettingPresenter(GroupSettingViewModel groupSettingViewModel, GroupRepository groupRepository,
                                 String groupUUID, ImageLoader imageLoader, GroupSettingView groupSettingView,
                                 User currentUser, ToolbarViewModel toolbarViewModel) {

        mGroupSettingViewModel = groupSettingViewModel;
        mGroupRepository = groupRepository;
        mImageLoader = imageLoader;
        mGroupSettingView = groupSettingView;
        mGroupUUID = groupUUID;

        mCurrentUser = currentUser;

        mToolbarViewModel = toolbarViewModel;

        mGroupMemberRecyclerViewAdapter = new GroupMemberRecyclerViewAdapter();

    }

    public void setAdapter(RecyclerView recyclerView) {
        recyclerView.setAdapter(mGroupMemberRecyclerViewAdapter);
    }

    public void refreshView() {

        mGroupRepository.getGroupFromMemory(mGroupUUID, new BaseLoadDataCallbackWrapper<>(
                new BaseLoadDataCallback<PrivateGroup>() {
                    @Override
                    public void onSucceed(List<PrivateGroup> data, OperationResult operationResult) {

                        mPrivateGroup = data.get(0);

                        mToolbarViewModel.showMenu.set(true);

                        mToolbarViewModel.setToolbarMenuBtnOnClickListener(new ToolbarViewModel.ToolbarMenuBtnOnClickListener() {
                            @Override
                            public void onClick() {
                                menuBtnOnClick();
                            }
                        });

                        refreshViewAfterGetGroup(mPrivateGroup);

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mGroupSettingView.showToast(operationResult.getResultMessage(mGroupSettingView.getContext()));

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

        mGroupSettingViewModel.deviceName.set(group.getStationName());

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

    private void menuBtnOnClick() {

        List<BottomMenuItem> bottomMenuItems = new ArrayList<>();

        if (checkIsOwner()) {

            BottomMenuItem deleteGroupItem = new BottomMenuItem(R.drawable.delete_download_task, mGroupSettingView.getString(R.string.delete_group), new AbstractCommand() {
                @Override
                public void execute() {

                    deleteGroup();

                }

                @Override
                public void unExecute() {

                }
            });

            bottomMenuItems.add(deleteGroupItem);

        } else {

            BottomMenuItem dropOutGroupItem = new BottomMenuItem(R.drawable.delete_download_task, mGroupSettingView.getString(R.string.quit_group), new AbstractCommand() {
                @Override
                public void execute() {

                    quitGroup();

                }

                @Override
                public void unExecute() {

                }
            });

            bottomMenuItems.add(dropOutGroupItem);
        }


        new BottomMenuDialogFactory(bottomMenuItems).createDialog(mGroupSettingView.getContext()).show();

    }

    private boolean checkIsOwner() {

        return mCurrentUser.getAssociatedWeChatGUID().equals(mPrivateGroup.getOwnerGUID());

    }

    public void modifyGroupName() {

        if (!checkIsOwner()) {

            mGroupSettingView.showToast(mGroupSettingView.getString(R.string.no_operate_group_permission));

            return;

        }

        ModifyGroupNameLayoutBinding binding = ModifyGroupNameLayoutBinding.inflate(LayoutInflater.from(mGroupSettingView.getContext()),null,false);

        final EditText modifyGroupNameEditText = binding.modifyGroupNameEdittext;

        AlertDialog.Builder builder = new AlertDialog.Builder(mGroupSettingView.getContext())
                .setTitle(mGroupSettingView.getString(R.string.modify_group_name))
                .setView(binding.getRoot())
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String newName = modifyGroupNameEditText.getText().toString();

                        modifyGroupName(newName);

                    }
                }).setNegativeButton(R.string.cancel, null);

        builder.create().show();

    }

    private void modifyGroupName(final String newName) {

        mGroupSettingView.showProgressDialog(mGroupSettingView.getString(R.string.operating_title, mGroupSettingView.getString(R.string.modify_group_name)));

        GroupRequestParam groupRequestParam = new GroupRequestParam(mPrivateGroup.getUUID(), mPrivateGroup.getStationID());

        mGroupRepository.updateGroupName(groupRequestParam, newName, new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                mGroupSettingView.dismissDialog();

                mGroupSettingView.showToast(mGroupSettingView.getString(R.string.success, mGroupSettingView.getString(R.string.modify_group_name)));

                mGroupSettingViewModel.groupName.set(newName);

                mGroupSettingView.setResult(GroupSettingActivity.RESULT_MODIFY_GROUP_INFO);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                mGroupSettingView.dismissDialog();

                mGroupSettingView.showToast(operationResult.getResultMessage(mGroupSettingView.getContext()));
            }
        });

    }

    private void deleteGroup() {

        mGroupSettingView.showProgressDialog(mGroupSettingView.getString(R.string.operating_title, mGroupSettingView.getString(R.string.delete_group)));

        mGroupRepository.deleteGroup(new GroupRequestParam(mGroupUUID, mPrivateGroup.getStationID()), new BaseOperateCallbackWrapper(
                new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        mGroupSettingView.dismissDialog();

                        mGroupSettingView.showToast(mGroupSettingView.getString(R.string.success, mGroupSettingView.getString(R.string.delete_group)));

                        mGroupSettingView.setResult(GroupSettingActivity.RESULT_DELETE_OR_QUIT_GROUP);

                        mGroupSettingView.finishView();

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mGroupSettingView.dismissDialog();

                        mGroupSettingView.showToast(operationResult.getResultMessage(mGroupSettingView.getContext()));

                    }
                }, this
        ));

    }

    private void quitGroup() {

        mGroupSettingView.showProgressDialog(mGroupSettingView.getString(R.string.operating_title, mGroupSettingView.getString(R.string.quit_group)));

        mGroupRepository.quitGroup(new GroupRequestParam(mGroupUUID, mPrivateGroup.getStationID()), mCurrentUser.getAssociatedWeChatGUID(), new BaseOperateCallbackWrapper(
                new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        mGroupSettingView.dismissDialog();

                        mGroupSettingView.showToast(mGroupSettingView.getString(R.string.success, mGroupSettingView.getString(R.string.quit_group)));

                        mGroupSettingView.setResult(GroupSettingActivity.RESULT_DELETE_OR_QUIT_GROUP);

                        mGroupSettingView.finishView();

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mGroupSettingView.dismissDialog();

                        mGroupSettingView.showToast(operationResult.getResultMessage(mGroupSettingView.getContext()));

                    }
                }, this
        ));

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

                mAddReduceGroupMemberItemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!checkIsOwner()) {

                            mGroupSettingView.showToast(mGroupSettingView.getString(R.string.no_operate_group_permission));

                            return;

                        }

                        mGroupSettingView.addUserBtnOnClick();

                    }
                });


            } else {

                mAddReduceGroupMemberItemBinding.addReduceImg.setImageResource(R.drawable.reduce_group_member);

                mAddReduceGroupMemberItemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!checkIsOwner()) {

                            mGroupSettingView.showToast(mGroupSettingView.getString(R.string.no_operate_group_permission));

                            return;

                        }

                        mGroupSettingView.deleteUserBtnOnClick();

                    }
                });

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
