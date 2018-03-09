package com.winsun.fruitmix.group.presenter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.databinding.AddFriendItemBinding;
import com.winsun.fruitmix.group.data.source.FakeGroupDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.recyclerview.BindingViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/10.
 */

public class AddFriendPresenterImpl implements AddFriendPresenter {

    private List<User> users;

    private List<User> selectUsers;

    private AddFriendAdapter addFriendAdapter;

    private String groupUUID;

    public AddFriendPresenterImpl(String groupUUID) {

        this.groupUUID = groupUUID;

        users = new ArrayList<>();

        selectUsers = new ArrayList<>();

        addFriendAdapter = new AddFriendAdapter();

    }

    public AddFriendAdapter getAddFriendAdapter() {
        return addFriendAdapter;
    }

    public void refreshUser() {

        User aimi = new User();
        aimi.setUserName("Aimi");
        aimi.setUuid(FakeGroupDataSource.AIMI_UUID);
        users.add(aimi);

        User naomi = new User();
        naomi.setUserName("Naomi");
        naomi.setUuid(FakeGroupDataSource.NAOMI_UUID);

        users.add(naomi);

        addFriendAdapter.setUsers(users);
        addFriendAdapter.notifyDataSetChanged();

    }

    public void addFriend() {


    }

    @Override
    public void userCheckChanged(User user, boolean isChecked) {

        if (isChecked && !selectUsers.contains(user))
            selectUsers.add(user);
        else if (!isChecked && selectUsers.contains(user))
            selectUsers.remove(user);

    }

    class AddFriendAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<User> mUsers;

        public AddFriendAdapter() {
            mUsers = new ArrayList<>();
        }

        public void setUsers(List<User> users) {
            mUsers.clear();
            mUsers.addAll(users);
        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            AddFriendItemBinding binding = AddFriendItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

            return new BindingViewHolder(binding);
        }


        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            AddFriendItemBinding binding = (AddFriendItemBinding) holder.getViewDataBinding();

            User user = mUsers.get(position);

            binding.addFriendCheckbox.setChecked(checkFriend(user));

            holder.getViewDataBinding().setVariable(BR.user, user);

            holder.getViewDataBinding().setVariable(BR.addFriendPresenter, AddFriendPresenterImpl.this);

            holder.getViewDataBinding().executePendingBindings();

        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }

    }

    private boolean checkFriend(User user) {
        return selectUsers.contains(user);
    }

}
