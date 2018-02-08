package com.winsun.fruitmix.group.setting;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.BottomSheetDialog;
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
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.contact.ContactListActivity;
import com.winsun.fruitmix.databinding.ActivityGroupSettingBinding;
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import java.util.Collections;

public class GroupSettingActivity extends BaseToolbarActivity implements GroupSettingView {

    public static final int RESULT_MODIFY_GROUP_INFO = 0x1001;

    public static final int RESULT_DELETE_OR_QUIT_GROUP = 0x1002;

    private ActivityGroupSettingBinding mActivityGroupSettingBinding;

    private static final int MEMBER_SPAN_COUNT = 4;

    private GroupSettingPresenter mGroupSettingPresenter;

    public static void start(String groupUUID, Context context) {

        Intent intent = new Intent(context, GroupSettingActivity.class);
        intent.putExtra(Util.KEY_GROUP_UUID, groupUUID);
        context.startActivity(intent);

    }

    private String mGroupUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGroupUUID = getIntent().getStringExtra(Util.KEY_GROUP_UUID);

        GroupSettingViewModel groupSettingViewModel = new GroupSettingViewModel();

        mActivityGroupSettingBinding.setGroupSettingViewModel(groupSettingViewModel);

        RecyclerView membersRecyclerView = mActivityGroupSettingBinding.memberRecyclerView;

        membersRecyclerView.setLayoutManager(new GridLayoutManager(this, MEMBER_SPAN_COUNT));
        membersRecyclerView.setItemAnimator(new DefaultItemAnimator());

        User currentUser = InjectUser.provideRepository(this).
                getUserByUUID(InjectSystemSettingDataSource.provideSystemSettingDataSource(this).getCurrentLoginUserUUID());

        mGroupSettingPresenter = new GroupSettingPresenter(groupSettingViewModel,
                InjectGroupDataSource.provideGroupRepository(this), mGroupUUID,
                InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this),
                this,currentUser,toolbarViewModel);

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

    @Override
    public void addUserBtnOnClick() {

        ContactListActivity.start(this, ContactListActivity.ADD_USER, mGroupUUID);

    }

    @Override
    public void deleteUserBtnOnClick() {

        ContactListActivity.start(this, ContactListActivity.DELETE_USER, mGroupUUID);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ContactListActivity.MODIFY_GROUP_USERS_REQUEST_CODE && resultCode == RESULT_OK) {

            mGroupSettingPresenter.refreshView();

            setResult(RESULT_MODIFY_GROUP_INFO);

        }

    }


}
