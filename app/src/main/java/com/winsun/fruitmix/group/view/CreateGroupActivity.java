package com.winsun.fruitmix.group.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.BaseToolbarActivity;
import com.winsun.fruitmix.R;
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

import java.util.Collections;

public class CreateGroupActivity extends BaseToolbarActivity implements CreateGroupPresenter {

    private GroupRepository groupRepository;

    private User currentUser;

    private ActivityCreateGroupBinding mActivityCreateGroupBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupRepository = InjectGroupDataSource.provideGroupRepository();

        currentUser = InjectUser.provideRepository(this).getUserByUUID(InjectSystemSettingDataSource.provideSystemSettingDataSource(this).getCurrentLoginUserUUID());

        CreateGroupViewModel createGroupViewModel = new CreateGroupViewModel();

        mActivityCreateGroupBinding.setCreateGroupViewModel(createGroupViewModel);

    }

    @Override
    protected View generateContent() {

        mActivityCreateGroupBinding = ActivityCreateGroupBinding.inflate(LayoutInflater.from(this),
                null,false);

        return mActivityCreateGroupBinding.getRoot();
    }

    @Override
    protected String getToolbarTitle() {
        return "创建群";
    }


    @Override
    public void createGroup(CreateGroupViewModel createGroupViewModel) {
        PrivateGroup group = new PrivateGroup(Util.createLocalUUid(), createGroupViewModel.getGroupName(), Collections.singletonList(currentUser));

        showProgressDialog("正在创建群");

        groupRepository.addGroup(group, new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {
                dismissDialog();

                CreateGroupActivity.this.setResult(RESULT_OK);

                finishView();
            }

            @Override
            public void onFail(OperationResult result) {
                dismissDialog();

                showToast("创建失败");
            }
        });

    }
}
