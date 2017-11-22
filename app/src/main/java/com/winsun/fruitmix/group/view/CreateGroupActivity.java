package com.winsun.fruitmix.group.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.winsun.fruitmix.BaseActivity;
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

public class CreateGroupActivity extends BaseActivity implements CreateGroupPresenter {

    private GroupRepository groupRepository;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCreateGroupBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_create_group);

        groupRepository = InjectGroupDataSource.provideGroupRepository();

        currentUser = InjectUser.provideRepository(this).getUserByUUID(InjectSystemSettingDataSource.provideSystemSettingDataSource(this).getCurrentLoginUserUUID());

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set("创建群");

        toolbarViewModel.setBaseView(this);

        binding.setToolbarViewModel(toolbarViewModel);

        CreateGroupViewModel createGroupViewModel = new CreateGroupViewModel();

        binding.setCreateGroupViewModel(createGroupViewModel);

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
