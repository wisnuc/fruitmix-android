package com.winsun.fruitmix.group.view;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityGroupContentBinding;
import com.winsun.fruitmix.group.data.source.FakeGroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.viewmodel.GroupContentViewModel;
import com.winsun.fruitmix.group.presenter.GroupContentPresenter;
import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class GroupContentActivity extends BaseActivity implements BaseView {

    private RecyclerView recyclerView;

    public static final String GROUP_UUID = "group_uuid";
    public static final String GROUP_NAME = "group_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityGroupContentBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_group_content);

        recyclerView = binding.chatRecyclerview;

        String groupUUID = getIntent().getStringExtra(GROUP_UUID);

        GroupContentViewModel groupContentViewModel = new GroupContentViewModel();

        binding.setGroupContentViewModel(groupContentViewModel);

        GroupRepository groupRepository = GroupRepository.getInstance(FakeGroupDataSource.getInstance());

        LoggedInUserDataSource loggedInUserDataSource = InjectLoggedInUser.provideLoggedInUserRepository(this);

        GroupContentPresenter groupContentPresenter = new GroupContentPresenter(groupUUID,loggedInUserDataSource, groupRepository, groupContentViewModel);

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();
        toolbarViewModel.setBaseView(this);

        String groupName = getIntent().getStringExtra(GROUP_NAME);

        toolbarViewModel.titleText.set(groupName);

        binding.setToolbarViewModel(toolbarViewModel);

        binding.setPingToggleListener(groupContentPresenter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(groupContentPresenter.getGroupContentAdapter());

        groupContentPresenter.refreshGroup();

    }


}
