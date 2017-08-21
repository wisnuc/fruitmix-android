package com.winsun.fruitmix.file.view.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.command.ChangeToDownloadPageCommand;
import com.winsun.fruitmix.command.NullCommand;
import com.winsun.fruitmix.command.DownloadFileCommand;
import com.winsun.fruitmix.command.MacroCommand;
import com.winsun.fruitmix.command.OpenFileCommand;
import com.winsun.fruitmix.command.ShowSelectModeViewCommand;
import com.winsun.fruitmix.command.ShowUnSelectModeViewCommand;
import com.winsun.fruitmix.databinding.FragmentFileBinding;
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory;
import com.winsun.fruitmix.eventbus.DownloadStateChangedEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveFileOperationEvent;

import com.winsun.fruitmix.file.data.download.DownloadState;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadManager;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.file.presenter.FilePresenter;
import com.winsun.fruitmix.file.view.FileDownloadActivity;
import com.winsun.fruitmix.file.view.interfaces.HandleTitleCallback;
import com.winsun.fruitmix.file.view.viewmodel.FileItemViewModel;
import com.winsun.fruitmix.file.view.viewmodel.FileViewModel;
import com.winsun.fruitmix.interfaces.Page;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.interfaces.OnViewSelectListener;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BaseRecyclerViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FileFragment implements Page, IShowHideFragmentListener {

    public static final String TAG = FileFragment.class.getSimpleName();

    private RecyclerView fileRecyclerView;

    private Activity activity;

    private View view;

    private NoContentViewModel noContentViewModel;
    private LoadingViewModel loadingViewModel;

    private FileViewModel fileViewModel;

    private FilePresenter filePresenter;

    public FileFragment(final Activity activity, HandleTitleCallback handleTitleCallback) {

        this.activity = activity;

        view = onCreateView();

        filePresenter = new FilePresenter(activity, InjectStationFileRepository.provideStationFileRepository(activity),
                noContentViewModel, loadingViewModel, fileViewModel, handleTitleCallback,
                InjectLoggedInUser.provideLoggedInUserRepository(activity), InjectSystemSettingDataSource.provideSystemSettingDataSource(activity));

        fileRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        fileRecyclerView.setItemAnimator(new DefaultItemAnimator());
        fileRecyclerView.setAdapter(filePresenter.getFileRecyclerViewAdapter());

        refreshView();

    }

    private View onCreateView() {
        // Inflate the layout for this fragment

        FragmentFileBinding binding = FragmentFileBinding.inflate(LayoutInflater.from(activity), null, false);

        noContentViewModel = new NoContentViewModel();
        noContentViewModel.setNoContentText(activity.getString(R.string.no_files));
        noContentViewModel.setNoContentImgResId(R.drawable.no_file);

        loadingViewModel = new LoadingViewModel();

        fileViewModel = new FileViewModel();

        binding.setLoadingViewModel(loadingViewModel);
        binding.setNoContentViewModel(noContentViewModel);

        binding.setFileViewModel(fileViewModel);

        fileRecyclerView = binding.fileRecyclerview;

        return binding.getRoot();
    }

    @Override
    public void refreshView() {

        filePresenter.refreshView();
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

}
