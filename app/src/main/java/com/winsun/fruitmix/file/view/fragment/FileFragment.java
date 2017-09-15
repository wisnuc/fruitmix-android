package com.winsun.fruitmix.file.view.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.FragmentFileBinding;
import com.winsun.fruitmix.eventbus.DownloadStateChangedEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;

import com.winsun.fruitmix.file.data.download.FileDownloadManager;
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.file.presenter.FilePresenter;
import com.winsun.fruitmix.file.view.interfaces.FileListSelectModeListener;
import com.winsun.fruitmix.file.view.interfaces.FileView;
import com.winsun.fruitmix.file.view.interfaces.HandleFileListOperateCallback;
import com.winsun.fruitmix.file.view.viewmodel.FileViewModel;
import com.winsun.fruitmix.interfaces.Page;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.List;
import java.util.Map;


public class FileFragment implements Page, IShowHideFragmentListener, FileView {

    public static final String TAG = FileFragment.class.getSimpleName();

    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView fileRecyclerView;

    private Activity activity;

    private View view;

    private NoContentViewModel noContentViewModel;
    private LoadingViewModel loadingViewModel;

    private FileViewModel fileViewModel;

    private FilePresenter filePresenter;

    private boolean initFileRecyclerView = false;

    public FileFragment(final Activity activity, FileListSelectModeListener fileListSelectModeListener, HandleFileListOperateCallback handleFileListOperateCallback) {

        this.activity = activity;

        view = onCreateView();

        filePresenter = new FilePresenter(activity, this, fileListSelectModeListener, InjectStationFileRepository.provideStationFileRepository(activity),
                noContentViewModel, loadingViewModel, fileViewModel, handleFileListOperateCallback,
                InjectUser.provideRepository(activity),
                InjectSystemSettingDataSource.provideSystemSettingDataSource(activity), FileDownloadManager.getInstance());

        initSwipeRefreshLayout();

    }

    private View onCreateView() {

        FragmentFileBinding binding = FragmentFileBinding.inflate(LayoutInflater.from(activity), null, false);

        noContentViewModel = new NoContentViewModel();
        noContentViewModel.setNoContentText(activity.getString(R.string.no_files));
        noContentViewModel.setNoContentImgResId(R.drawable.no_file);

        loadingViewModel = new LoadingViewModel();

        fileViewModel = new FileViewModel();

        binding.setLoadingViewModel(loadingViewModel);
        binding.setNoContentViewModel(noContentViewModel);

        binding.setFileViewModel(fileViewModel);

        swipeRefreshLayout = binding.swipeRefreshLayout;

        fileRecyclerView = binding.fileRecyclerview;

        return binding.getRoot();
    }


    private void initFileRecyclerView(Activity activity) {
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        fileRecyclerView.setItemAnimator(new DefaultItemAnimator());
        fileRecyclerView.setAdapter(filePresenter.getFileRecyclerViewAdapter());
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                filePresenter.refreshCurrentFolder();

            }
        });
    }

    @Override
    public void setSwipeRefreshing(boolean refreshing) {
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    public void refreshViewForce() {
        filePresenter.refreshView(true);
    }

    @Override
    public void refreshView() {

        if (!initFileRecyclerView) {
            initFileRecyclerView = true;
            initFileRecyclerView(activity);
        }

        filePresenter.refreshView(false);
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

    }

    @Override
    public void onDestroy() {

        activity = null;

        filePresenter.onDestroy();
    }

    @Override
    public void show() {
        MobclickAgent.onPageStart("FileFragment");
    }

    @Override
    public void hide() {
        MobclickAgent.onPageEnd("FileFragment");
    }

    public void handleEvent(DownloadStateChangedEvent downloadStateChangedEvent) {

        filePresenter.handleEvent(downloadStateChangedEvent);

    }

    public void handleOperationEvent(OperationEvent operationEvent) {

        filePresenter.handleOperationEvent(operationEvent);

    }

    public String getCurrentFolderName() {
        return filePresenter.getCurrentFolderName();
    }

    public boolean handleBackPressedOrNot() {
        return filePresenter.handleBackPressedOrNot();
    }

    public void onBackPressed() {

        filePresenter.onBackPressed();

    }

    public Dialog getBottomSheetDialog(List<BottomMenuItem> bottomMenuItems) {

        return filePresenter.getBottomSheetDialog(bottomMenuItems);
    }

    public List<BottomMenuItem> getMainMenuItem() {

        return filePresenter.getMainMenuItem();
    }

    public void requestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        filePresenter.requestPermissionsResult(requestCode, permissions, grantResults);

    }

    public void downloadSelectItems() {
        filePresenter.downloadSelectItems();
    }

    @Override
    public boolean canEnterSelectMode() {

        return filePresenter.canEnterSelectMode();
    }

    public void enterSelectMode() {
        filePresenter.enterSelectMode();
    }

    public void quitSelectMode() {
        filePresenter.quitSelectMode();
    }

    public void shareSelectFilesToOtherApp(){

        filePresenter.shareSelectFilesToOtherApp(activity);


    }

}
