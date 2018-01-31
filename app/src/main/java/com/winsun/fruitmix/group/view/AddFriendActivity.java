package com.winsun.fruitmix.group.view;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.winsun.fruitmix.BaseToolbarActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityAddFriendBinding;
import com.winsun.fruitmix.group.presenter.AddFriendPresenterImpl;
import com.winsun.fruitmix.util.Util;

public class AddFriendActivity extends BaseToolbarActivity {

    private ActivityAddFriendBinding binding;

    private AddFriendPresenterImpl addFriendPresenter;

    private String groupUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupUUID = getIntent().getStringExtra(Util.KEY_GROUP_UUID);

        RecyclerView friendRecyclerView = binding.friendRecyclerview;

        friendRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendRecyclerView.setItemAnimator(new DefaultItemAnimator());

        addFriendPresenter = new AddFriendPresenterImpl(groupUUID);

        friendRecyclerView.setAdapter(addFriendPresenter.getAddFriendAdapter());

        addFriendPresenter.refreshUser();
    }

    @Override
    protected View generateContent(ViewGroup root) {

        binding = ActivityAddFriendBinding.inflate(LayoutInflater.from(this), root, false);

        return binding.getRoot();
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.add_friend);
    }


}
