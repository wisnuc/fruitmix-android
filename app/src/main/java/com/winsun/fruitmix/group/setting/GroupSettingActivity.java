package com.winsun.fruitmix.group.setting;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.BaseToolbarActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityGroupSettingBinding;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

public class GroupSettingActivity extends BaseToolbarActivity implements GroupSettingView {

    public static final int RESULT_MODIFY_GROUP_NAME = 0x1001;

    private ActivityGroupSettingBinding mActivityGroupSettingBinding;

    private static final int MEMBER_SPAN_COUNT = 4;

    private GroupSettingPresenter mGroupSettingPresenter;

    public static void start(String groupUUID, Context context) {

        Intent intent = new Intent(context,GroupSettingActivity.class);
        intent.putExtra(Util.KEY_GROUP_UUID,groupUUID);
        context.startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String groupUUID = getIntent().getStringExtra(Util.KEY_GROUP_UUID);

        GroupSettingViewModel groupSettingViewModel = new GroupSettingViewModel();

        mActivityGroupSettingBinding.setGroupSettingViewModel(groupSettingViewModel);

        RecyclerView membersRecyclerView = mActivityGroupSettingBinding.memberRecyclerView;

        membersRecyclerView.setLayoutManager(new GridLayoutManager(this, MEMBER_SPAN_COUNT));
        membersRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mGroupSettingPresenter = new GroupSettingPresenter(groupSettingViewModel,
                InjectGroupDataSource.provideGroupRepository(this), groupUUID,
                InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this),
                this);

        mActivityGroupSettingBinding.setGroupSettingPresenter(mGroupSettingPresenter);

        mGroupSettingPresenter.setAdapter(membersRecyclerView);

        mGroupSettingPresenter.refreshView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mGroupSettingPresenter.onDestroy();

    }

    @Override
    protected View generateContent(ViewGroup root) {

        mActivityGroupSettingBinding = ActivityGroupSettingBinding.inflate(LayoutInflater.from(this),
                root, false);

        return mActivityGroupSettingBinding.getRoot();
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.group_setting);
    }

    @Override
    public Context getContext() {
        return this;
    }
}
