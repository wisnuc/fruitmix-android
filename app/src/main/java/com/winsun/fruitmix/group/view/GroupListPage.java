package com.winsun.fruitmix.group.view;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityGroupListBinding;
import com.winsun.fruitmix.group.data.source.FakeGroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.group.data.viewmodel.GroupListViewModel;
import com.winsun.fruitmix.group.presenter.GroupListPresenter;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.interfaces.Page;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.List;
import java.util.Map;

public class GroupListPage implements Page, IShowHideFragmentListener, GroupListPageView {

    private View view;

    private RecyclerView recyclerView;

    private GroupListPresenter groupListPresenter;

    private Activity containerActivity;

    public GroupListPage(Activity activity) {

        containerActivity = activity;

        ActivityGroupListBinding binding = ActivityGroupListBinding.inflate(LayoutInflater.from(activity), null, false);

        LoadingViewModel loadingViewModel = new LoadingViewModel();

        binding.setLoadingViewModel(loadingViewModel);

        NoContentViewModel noContentViewModel = new NoContentViewModel();
        noContentViewModel.setNoContentImgResId(R.drawable.no_file);
        noContentViewModel.setNoContentText("没有内容");

        binding.setNoContentViewModel(noContentViewModel);

        GroupListViewModel groupListViewModel = new GroupListViewModel();

        binding.setGroupListViewModel(groupListViewModel);

        view = binding.getRoot();

        GroupRepository groupRepository = InjectGroupDataSource.provideGroupRepository();

        groupRepository.setCurrentUser(InjectLoggedInUser.provideLoggedInUserRepository(containerActivity).getCurrentLoggedInUser().getUser());

        groupListPresenter = new GroupListPresenter(this, groupRepository, loadingViewModel, noContentViewModel, groupListViewModel);

        recyclerView = binding.groupRecyclerview;

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(groupListPresenter.getGroupListAdapter());

        groupListPresenter.refreshGroups();
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void refreshView() {

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

    }

    @Override
    public void onDestroy() {
        groupListPresenter.onDestroyView();
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void gotoGroupContentActivity(String groupUUID, String groupName) {

        Intent intent = new Intent(containerActivity, GroupContentActivity.class);
        intent.putExtra(GroupContentActivity.GROUP_UUID, groupUUID);
        intent.putExtra(GroupContentActivity.GROUP_NAME, groupName);

        containerActivity.startActivity(intent);

    }
}
