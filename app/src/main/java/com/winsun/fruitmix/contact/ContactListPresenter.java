package com.winsun.fruitmix.contact;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateCallbackWrapper;
import com.winsun.fruitmix.component.UserAvatar;
import com.winsun.fruitmix.contact.data.ContactDataSource;
import com.winsun.fruitmix.databinding.ContactListItemBinding;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2018/1/27.
 */

public class ContactListPresenter implements ActiveView {

    private ContactDataSource mContactDataSource;

    private ImageLoader mImageLoader;

    private ContactListView mContactListView;

    private ContactRecyclerViewAdapter mContactRecyclerViewAdapter;

    private LoadingViewModel mLoadingViewModel;
    private NoContentViewModel mNoContentViewModel;

    private GroupRepository mGroupRepository;

    private User mCurrentUser;

    private int mPurpose;
    private String mGroupUUID;

    private List<User> mSelectedUsers;

    private List<User> mAlreadySelectedUsers;

    public ContactListPresenter(ContactDataSource contactDataSource, ImageLoader imageLoader,
                                LoadingViewModel loadingViewModel, NoContentViewModel noContentViewModel,
                                ContactListView contactListView, GroupRepository groupRepository, User currentUser,
                                int purpose, String groupUUID) {
        mContactDataSource = contactDataSource;
        mImageLoader = imageLoader;
        mLoadingViewModel = loadingViewModel;
        mNoContentViewModel = noContentViewModel;
        mContactListView = contactListView;

        mGroupRepository = groupRepository;
        mCurrentUser = currentUser;

        mPurpose = purpose;
        mGroupUUID = groupUUID;

        mContactRecyclerViewAdapter = new ContactRecyclerViewAdapter();

        mSelectedUsers = new ArrayList<>();

        mAlreadySelectedUsers = new ArrayList<>();

    }

    public ContactRecyclerViewAdapter getContactRecyclerViewAdapter() {
        return mContactRecyclerViewAdapter;
    }

    public void onDestroy() {
        mContactListView = null;
    }

    public void createGroup() {

        List<User> groupUser = new ArrayList<>(mSelectedUsers);
        groupUser.add(mCurrentUser);

        PrivateGroup group = new PrivateGroup(Util.createLocalUUid(), "", mCurrentUser.getUuid(), groupUser);

        mContactListView.showProgressDialog(mContactListView.getString(R.string.operating_title, mContactListView.getString(R.string.create_group)));

        mGroupRepository.addGroup(group, new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                mContactListView.dismissDialog();

                mContactListView.setResult(RESULT_OK);

                mContactListView.finishView();

            }

            @Override
            public void onFail(OperationResult result) {

                mContactListView.dismissDialog();

                mContactListView.showToast(result.getResultMessage(mContactListView.getContext()));
            }
        });

    }

    public void addUser() {

        mContactListView.showProgressDialog(mContactListView.getString(R.string.operating_title, mContactListView.getString(R.string.add_contact)));

        mGroupRepository.addUsersToGroup(mGroupUUID, mSelectedUsers, new BaseOperateCallbackWrapper(
                new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        mContactListView.dismissDialog();

                        mContactListView.setResult(RESULT_OK);

                        mContactListView.finishView();

                        mContactListView.showToast(mContactListView.getString(R.string.success, mContactListView.getString(R.string.add_contact)));

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mContactListView.dismissDialog();

                        mContactListView.showToast(operationResult.getResultMessage(mContactListView.getContext()));

                    }
                }, this
        ));

    }

    public void deleteUser() {

        mContactListView.showProgressDialog(mContactListView.getString(R.string.operating_title, mContactListView.getString(R.string.delete_contact)));

        mGroupRepository.deleteUsersToGroup(mGroupUUID, mSelectedUsers, new BaseOperateCallbackWrapper(
                new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        mContactListView.dismissDialog();

                        mContactListView.setResult(RESULT_OK);

                        mContactListView.finishView();

                        mContactListView.showToast(mContactListView.getString(R.string.success, mContactListView.getString(R.string.delete_contact)));

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mContactListView.dismissDialog();

                        mContactListView.showToast(operationResult.getResultMessage(mContactListView.getContext()));

                    }
                }, this
        ));

    }

    public void refreshView() {

        if (mPurpose == ContactListActivity.DELETE_USER) {

            mLoadingViewModel.showLoading.set(false);

            mGroupRepository.getGroupFromMemory(mGroupUUID, new BaseLoadDataCallbackWrapper<>(
                    new BaseLoadDataCallback<PrivateGroup>() {
                        @Override
                        public void onSucceed(List<PrivateGroup> data, OperationResult operationResult) {

                            PrivateGroup group = data.get(0);

                            List<User> users = new ArrayList<>(group.getUsers());

                            Iterator<User> userIterator = users.iterator();
                            while (userIterator.hasNext()) {
                                User user = userIterator.next();

                                if (user.getUuid().equals(mCurrentUser.getUuid())) {
                                    userIterator.remove();
                                    break;
                                }

                            }

                            mContactRecyclerViewAdapter.setUsers(users);
                            mContactRecyclerViewAdapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onFail(OperationResult operationResult) {

                            mNoContentViewModel.showNoContent.set(true);

                        }
                    }, this
            ));

        } else {

            if (mPurpose == ContactListActivity.ADD_USER) {

                mGroupRepository.getGroupFromMemory(mGroupUUID, new BaseLoadDataCallbackWrapper<>(
                        new BaseLoadDataCallback<PrivateGroup>() {
                            @Override
                            public void onSucceed(List<PrivateGroup> data, OperationResult operationResult) {

                                PrivateGroup group = data.get(0);

                                List<User> users = group.getUsers();

                                mAlreadySelectedUsers.addAll(users);

                                getContact();

                            }

                            @Override
                            public void onFail(OperationResult operationResult) {

                                mNoContentViewModel.showNoContent.set(true);

                            }
                        }, this
                ));

            } else if (mPurpose == ContactListActivity.CREATE_GROUP) {

                getContact();

            }

        }

    }

    private void getContact() {
        mContactDataSource.getContacts(new BaseLoadDataCallbackWrapper<>(

                new BaseLoadDataCallback<User>() {
                    @Override
                    public void onSucceed(List<User> data, OperationResult operationResult) {

                        mLoadingViewModel.showLoading.set(false);

                        List<User> availableUsers = new ArrayList<>();

                        for (User user : data) {

                            if (user.isBoundedWeChat() && !user.getUuid().equals(mCurrentUser.getUuid()))
                                availableUsers.add(user);

                        }

                        if (availableUsers.size() != 0) {

                            mNoContentViewModel.showNoContent.set(false);

                            mContactRecyclerViewAdapter.setUsers(availableUsers);
                            mContactRecyclerViewAdapter.notifyDataSetChanged();

                        } else
                            mNoContentViewModel.showNoContent.set(true);

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mLoadingViewModel.showLoading.set(false);

                        mNoContentViewModel.showNoContent.set(true);

                        mContactListView.showToast(operationResult.getResultMessage(mContactListView.getContext()));

                    }
                }, this

        ));
    }

    public List<User> getSelectedUsers() {
        return mSelectedUsers;
    }

    @Override
    public boolean isActive() {
        return mContactListView != null;
    }

    private class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactItemViewHolder> {

        private List<User> mUsers;

        public ContactRecyclerViewAdapter() {
            mUsers = new ArrayList<>();
        }

        public void setUsers(List<User> users) {
            mUsers.clear();
            mUsers.addAll(users);
        }

        @Override
        public ContactItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ContactListItemBinding contactListItemBinding = ContactListItemBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false);

            return new ContactItemViewHolder(contactListItemBinding);
        }


        @Override
        public void onBindViewHolder(ContactItemViewHolder holder, int position) {

            holder.refreshView(mUsers.get(position));

        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return mUsers.size();
        }
    }


    private class ContactItemViewHolder extends BindingViewHolder {

        ContactItemViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        void refreshView(final User user) {

            ContactListItemBinding contactListItemBinding = (ContactListItemBinding) getViewDataBinding();

            contactListItemBinding.setUser(user);

            UserAvatar userAvatar = contactListItemBinding.userAvatar;

            userAvatar.setUser(user, mImageLoader);

            final ImageView selectImg = contactListItemBinding.selectImg;

            if (checkInAlreadyUserList(user)) {

                selectImg.setImageResource(R.drawable.already_checked_contact);

                contactListItemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

            } else {

                toggleSelectImgState(user, selectImg);

                contactListItemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mSelectedUsers.contains(user)) {

                            mSelectedUsers.remove(user);

                            toggleSelectImgState(user, selectImg);

                        } else {

                            mSelectedUsers.add(user);

                            toggleSelectImgState(user, selectImg);

                        }

                        mContactListView.onSelectItemChanged(mSelectedUsers.size());

                    }
                });


            }


        }


        private boolean checkInAlreadyUserList(User user) {

            for (User user1 : mAlreadySelectedUsers) {
                if (user.getAssociatedWeChatGUID().equals(user1.getAssociatedWeChatGUID()))
                    return true;
            }

            return false;
        }


        private void toggleSelectImgState(User user, ImageView selectImg) {
            if (mSelectedUsers.contains(user)) {
                selectImg.setImageResource(R.drawable.checked_contact);
            } else
                selectImg.setImageResource(R.drawable.unchecked_contact);
        }

    }


}
