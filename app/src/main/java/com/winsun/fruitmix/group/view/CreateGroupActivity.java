package com.winsun.fruitmix.group.view;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.BaseToolbarActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.ActivityCreateGroupBinding;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.group.data.viewmodel.CreateGroupViewModel;
import com.winsun.fruitmix.group.presenter.CreateGroupPresenter;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateGroupActivity extends BaseToolbarActivity implements CreateGroupPresenter {

    private GroupRepository groupRepository;

    private User currentUser;

    private ActivityCreateGroupBinding mActivityCreateGroupBinding;

    private static List<User> mUsers;

    public static void start(List<User> users, Context context){

        mUsers = users;
        context.startActivity(new Intent(context,CreateGroupActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupRepository = InjectGroupDataSource.provideGroupRepository(this);

        currentUser = InjectUser.provideRepository(this).getUserByUUID(InjectSystemSettingDataSource.provideSystemSettingDataSource(this).getCurrentLoginUserUUID());

        CreateGroupViewModel createGroupViewModel = new CreateGroupViewModel();

        mActivityCreateGroupBinding.setCreateGroupViewModel(createGroupViewModel);

        mActivityCreateGroupBinding.setCreateGroupPresenter(this);

        if(mUsers == null)
            mUsers = new ArrayList<>();

    }

    @Override
    protected View generateContent(ViewGroup root) {

        mActivityCreateGroupBinding = ActivityCreateGroupBinding.inflate(LayoutInflater.from(this),
                root,false);

        return mActivityCreateGroupBinding.getRoot();
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.create_group);
    }


    @Override
    public void createGroup(CreateGroupViewModel createGroupViewModel) {

        List<User> groupUser = new ArrayList<>(mUsers);
        groupUser.add(currentUser);

        PrivateGroup group = new PrivateGroup(Util.createLocalUUid(), createGroupViewModel.getGroupName(),currentUser.getAssociatedWeChatGUID(),
                InjectSystemSettingDataSource.provideSystemSettingDataSource(this).getCurrentLoginStationID(),groupUser);

        showProgressDialog(getString(R.string.operating_title,getString(R.string.create_group)));

        groupRepository.addGroup(group, new BaseOperateCallback() {
            @Override
            public void onSucceed() {
                dismissDialog();

                CreateGroupActivity.this.setResult(RESULT_OK);

                finishView();
            }

            @Override
            public void onFail(OperationResult result) {
                dismissDialog();

                showToast(result.getResultMessage(CreateGroupActivity.this));
            }
        });

    }
}
