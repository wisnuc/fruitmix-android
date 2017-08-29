package com.winsun.fruitmix.file.view.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.databinding.ViewDataBinding;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.command.DeleteDownloadedFileCommand;
import com.winsun.fruitmix.command.NullCommand;
import com.winsun.fruitmix.command.MacroCommand;
import com.winsun.fruitmix.command.ShowSelectModeViewCommand;
import com.winsun.fruitmix.command.ShowUnSelectModeViewCommand;
import com.winsun.fruitmix.databinding.DownloadedFileItemBinding;
import com.winsun.fruitmix.databinding.DownloadingFileItemBinding;
import com.winsun.fruitmix.databinding.FileDownloadGroupItemBinding;
import com.winsun.fruitmix.databinding.FragmentFileDownloadBinding;
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory;
import com.winsun.fruitmix.eventbus.DownloadStateChangedEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.data.download.DownloadState;
import com.winsun.fruitmix.file.data.download.DownloadedFileWrapper;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadManager;
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.file.view.viewmodel.FileDownloadGroupItemViewModel;
import com.winsun.fruitmix.file.view.viewmodel.FileDownloadedItemViewModel;
import com.winsun.fruitmix.file.view.viewmodel.FileDownloadingItemViewModel;
import com.winsun.fruitmix.interfaces.Page;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.interfaces.OnViewSelectListener;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileDownloadFragment implements Page, OnViewSelectListener, IShowHideFragmentListener {

    public static final String TAG = FileDownloadFragment.class.getSimpleName();

    private RecyclerView fileDownloadRecyclerView;

    private FileDownloadRecyclerViewAdapter mAdapter;

    private List<IDownloadItem> downloadItems;

    private List<IDownloadItem> downloadingItems;
    private List<IDownloadItem> downloadedItems;

    private CustomHandler customHandler;

    private static final int DOWNLOAD_STATE_CHANGED = 0x0010;

    private static final int DELAY_TIME_MILLISECOND = 0;

    private boolean selectMode = false;

    private boolean mStartDownloadOrPending = false;

    private Map<String, DownloadedFileWrapper> selectDownloadedItemMap;

    private AbstractCommand showUnSelectModeViewCommand;

    private AbstractCommand showSelectModeViewCommand;

    private AbstractCommand nullCommand;

    private ToolbarViewModel toolbarViewModel;

    private ToolbarViewModel.ToolbarNavigationOnClickListener defaultListener;

    private View view;

    private Activity activity;

    private StationFileRepository stationFileRepository;

    private String currentUserUUID;

    private FileDownloadManager fileDownloadManager;

    private static final int DOWNLOADING_GROUP = 0;
    private static final int DOWNLOADING_CHILD = 1;
    private static final int DOWNLOADED_GROUP = 2;
    private static final int DOWNLOADED_CHILD = 3;

    private interface IDownloadItem {

        int getDownloadItemType();

    }

    private class DownloadingGroupItem implements IDownloadItem {
        @Override
        public int getDownloadItemType() {
            return DOWNLOADING_GROUP;
        }
    }

    private class DownloadingChildItem implements IDownloadItem {

        private FileDownloadItem fileDownloadItem;

        public DownloadingChildItem(FileDownloadItem fileDownloadItem) {
            this.fileDownloadItem = fileDownloadItem;
        }

        public FileDownloadItem getFileDownloadItem() {
            return fileDownloadItem;
        }

        @Override
        public int getDownloadItemType() {
            return DOWNLOADING_CHILD;
        }
    }

    private class DownloadedGroupItem implements IDownloadItem {
        @Override
        public int getDownloadItemType() {
            return DOWNLOADED_GROUP;
        }
    }

    private class DownloadedChildItem implements IDownloadItem {

        private FileDownloadItem fileDownloadItem;

        public DownloadedChildItem(FileDownloadItem fileDownloadItem) {
            this.fileDownloadItem = fileDownloadItem;
        }

        public FileDownloadItem getFileDownloadItem() {
            return fileDownloadItem;
        }

        @Override
        public int getDownloadItemType() {
            return DOWNLOADED_CHILD;
        }
    }

    public void setDefaultListener(ToolbarViewModel.ToolbarNavigationOnClickListener defaultListener) {
        this.defaultListener = defaultListener;
    }

    public void setToolbarViewModel(ToolbarViewModel toolbarViewModel) {
        this.toolbarViewModel = toolbarViewModel;
    }

    public FileDownloadFragment(Activity activity, ToolbarViewModel toolbarViewModel, ToolbarViewModel.ToolbarNavigationOnClickListener defaultListener) {

        this.activity = activity;

        fileDownloadManager = FileDownloadManager.getInstance();

        setToolbarViewModel(toolbarViewModel);
        setDefaultListener(defaultListener);

        mAdapter = new FileDownloadRecyclerViewAdapter();

        downloadingItems = new ArrayList<>();
        downloadedItems = new ArrayList<>();

        downloadItems = new ArrayList<>();

        customHandler = new CustomHandler(this);

        selectDownloadedItemMap = new HashMap<>();

        showUnSelectModeViewCommand = new ShowUnSelectModeViewCommand(this);

        showSelectModeViewCommand = new ShowSelectModeViewCommand(this);

        nullCommand = new NullCommand();

        view = onCreateView(activity.getLayoutInflater());

        currentUserUUID = InjectSystemSettingDataSource.provideSystemSettingDataSource(activity).getCurrentLoginUserUUID();

        stationFileRepository = InjectStationFileRepository.provideStationFileRepository(activity);

        stationFileRepository.getCurrentLoginUserDownloadedFileRecord(currentUserUUID);

        refreshView();

        fileDownloadRecyclerView.smoothScrollToPosition(0);

    }

    private View onCreateView(LayoutInflater inflater) {

        FragmentFileDownloadBinding fragmentFileDownloadBinding = FragmentFileDownloadBinding.inflate(inflater, null, false);

        fileDownloadRecyclerView = fragmentFileDownloadBinding.fileDownloadRecyclerView;

        fileDownloadRecyclerView.setAdapter(mAdapter);

        fileDownloadRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

        fileDownloadRecyclerView.setItemAnimator(new DefaultItemAnimator());

        return fragmentFileDownloadBinding.getRoot();
    }

    public void handleTitle() {

        toolbarViewModel.titleText.set(activity.getString(R.string.file));
        toolbarViewModel.navigationIconResId.set(R.drawable.menu_black);
        toolbarViewModel.setToolbarNavigationOnClickListener(defaultListener);

    }

    @Override
    public void show() {
        MobclickAgent.onPageStart("FileDownloadFragment");
    }

    @Override
    public void hide() {
        MobclickAgent.onPageEnd("FileDownloadFragment");
    }

    public void handleEvent(DownloadStateChangedEvent downloadStateChangedEvent) {

        DownloadState downloadState = downloadStateChangedEvent.getDownloadState();

        if (downloadState == DownloadState.FINISHED || downloadState == DownloadState.ERROR) {

            if (mStartDownloadOrPending) {
                mStartDownloadOrPending = false;
                customHandler.sendEmptyMessageDelayed(DOWNLOAD_STATE_CHANGED, DELAY_TIME_MILLISECOND);
            }

        } else if (downloadState == DownloadState.NO_ENOUGH_SPACE) {

            Toast.makeText(activity, activity.getString(R.string.no_enough_space), Toast.LENGTH_SHORT).show();

        } else if (downloadState == DownloadState.START_DOWNLOAD || downloadState == DownloadState.PENDING) {

            Log.d(TAG, "handleEvent: download state: START_DOWNLOAD,PENDING");

            mStartDownloadOrPending = true;

            refreshView();

        } else if (downloadState == DownloadState.DOWNLOADING) {

            if (!mStartDownloadOrPending)
                mStartDownloadOrPending = true;

            refreshViewBeforeDownloadedGroupItem();
        }

    }

    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        Log.i(TAG, "handleOperationEvent: action:" + action);

        OperationResult result = operationEvent.getOperationResult();

        if (result.getOperationResultType() == OperationResultType.SUCCEED)
            refreshView();
        else
            Toast.makeText(activity, result.getResultMessage(activity), Toast.LENGTH_SHORT).show();

    }

    private static class CustomHandler extends Handler {

        WeakReference<FileDownloadFragment> weakReference = null;

        CustomHandler(FileDownloadFragment fileDownloadFragment) {
            weakReference = new WeakReference<>(fileDownloadFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            weakReference.get().refreshView();
        }
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void refreshView() {
        refreshData();

        mAdapter.setDownloads(downloadItems);
        mAdapter.notifyDataSetChanged();

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
    }

    private void refreshData() {
        filterFileDownloadItems(FileDownloadManager.getInstance().getFileDownloadItems());
    }

    private void filterFileDownloadItems(List<FileDownloadItem> fileDownloadItems) {

        downloadItems.clear();
        downloadingItems.clear();
        downloadedItems.clear();

        for (FileDownloadItem fileDownloadItem : fileDownloadItems) {

            DownloadState downloadState = fileDownloadItem.getDownloadState();

            if (downloadState.equals(DownloadState.FINISHED) || downloadState.equals(DownloadState.ERROR)) {
                downloadedItems.add(new DownloadedChildItem(fileDownloadItem));
            } else {
                downloadingItems.add(new DownloadingChildItem(fileDownloadItem));
            }

        }

        downloadItems.add(new DownloadingGroupItem());
        downloadItems.addAll(downloadingItems);
        downloadItems.add(new DownloadedGroupItem());
        downloadItems.addAll(downloadedItems);

    }

    private void refreshViewBeforeDownloadedGroupItem() {

        int i;

        for (i = 0; i < downloadItems.size(); i++) {

            IDownloadItem downloadItem = downloadItems.get(i);

            if (downloadItem instanceof DownloadedGroupItem) {
                break;
            }
        }

        mAdapter.notifyItemRangeChanged(0, i);
    }

    public void onBackPressed() {

        if (selectMode) {
            selectMode = false;
            refreshSelectMode(selectMode);
        }

    }

    public boolean handleBackPressedOrNot() {
        return selectMode;
    }

    public List<BottomMenuItem> getMainMenuItem() {

        List<BottomMenuItem> bottomMenuItems = new ArrayList<>();

        if (selectMode) {

            BottomMenuItem clearSelectItem = new BottomMenuItem(R.drawable.cancel, activity.getString(R.string.clear_select_item), showUnSelectModeViewCommand);

            bottomMenuItems.add(clearSelectItem);

            AbstractCommand macroCommand = new MacroCommand();

            macroCommand.addCommand(new DeleteDownloadedFileCommand(new ArrayList<>(selectDownloadedItemMap.values()), currentUserUUID, stationFileRepository));
            macroCommand.addCommand(showUnSelectModeViewCommand);

            BottomMenuItem deleteSelectItem = new BottomMenuItem(R.drawable.del_user, activity.getString(R.string.delete_text), macroCommand);

            bottomMenuItems.add(deleteSelectItem);

        } else {

            BottomMenuItem selectItem = new BottomMenuItem(R.drawable.check, activity.getString(R.string.choose_text), showSelectModeViewCommand);

            if (downloadedItems.isEmpty())
                selectItem.setDisable(true);

            bottomMenuItems.add(selectItem);

        }

        BottomMenuItem cancelMenuItem = new BottomMenuItem(R.drawable.close, activity.getString(R.string.cancel), nullCommand);

        bottomMenuItems.add(cancelMenuItem);

        return bottomMenuItems;
    }

    private void refreshSelectMode(boolean selectMode) {
        this.selectMode = selectMode;

        if (!selectMode) {
            selectDownloadedItemMap.clear();
        }

        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void selectMode() {
        selectMode = true;
        refreshSelectMode(selectMode);
    }

    @Override
    public void unSelectMode() {
        selectMode = false;
        refreshSelectMode(selectMode);
    }

    public Dialog getBottomSheetDialog(List<BottomMenuItem> bottomMenuItems) {

        Dialog dialog = new BottomMenuDialogFactory(bottomMenuItems).createDialog(activity);

        for (BottomMenuItem bottomMenuItem : bottomMenuItems) {
            bottomMenuItem.setDialog(dialog);
        }

        return dialog;
    }

    private class FileDownloadRecyclerViewAdapter extends RecyclerView.Adapter<FileDownloadViewHolder> {

        private List<IDownloadItem> mDownloads;

        public FileDownloadRecyclerViewAdapter() {

            mDownloads = new ArrayList<>();
        }

        public void setDownloads(List<IDownloadItem> downloads) {
            mDownloads.clear();
            mDownloads.addAll(downloads);
        }

        @Override
        public FileDownloadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ViewDataBinding viewDataBinding;

            FileDownloadViewHolder fileDownloadViewHolder = null;

            switch (viewType) {
                case DOWNLOADING_GROUP:
                case DOWNLOADED_GROUP:

                    viewDataBinding = FileDownloadGroupItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                    fileDownloadViewHolder = new FileDownloadGroupHolder(viewDataBinding);

                    break;

                case DOWNLOADING_CHILD:

                    viewDataBinding = DownloadingFileItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                    fileDownloadViewHolder = new FileDownloadingItemHolder(viewDataBinding);

                    break;
                case DOWNLOADED_CHILD:

                    viewDataBinding = DownloadedFileItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                    fileDownloadViewHolder = new FileDownloadedItemHolder(viewDataBinding);

                    break;
            }

            return fileDownloadViewHolder;
        }

        @Override
        public void onBindViewHolder(FileDownloadViewHolder holder, int position) {
            holder.refreshDownloadItemView(mDownloads.get(position));
        }

        @Override
        public int getItemCount() {

            return mDownloads.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mDownloads.get(position).getDownloadItemType();
        }

    }

    abstract class FileDownloadViewHolder extends BindingViewHolder {

        public FileDownloadViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        public abstract void refreshDownloadItemView(IDownloadItem downloadItem);
    }

    class FileDownloadGroupHolder extends FileDownloadViewHolder {

        private FileDownloadGroupItemBinding binding;

        FileDownloadGroupHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            binding = (FileDownloadGroupItemBinding) viewDataBinding;
        }

        @Override
        public void refreshDownloadItemView(IDownloadItem downloadItem) {

            FileDownloadGroupItemViewModel fileDownloadGroupItemViewModel = binding.getFileDownloadGroupItemViewModel();

            if (fileDownloadGroupItemViewModel == null) {
                fileDownloadGroupItemViewModel = new FileDownloadGroupItemViewModel();
                binding.setFileDownloadGroupItemViewModel(fileDownloadGroupItemViewModel);
            }

            if (downloadItem instanceof DownloadingGroupItem) {
                fileDownloadGroupItemViewModel.groupTitle.set(activity.getString(R.string.downloading));
            } else if (downloadItem instanceof DownloadedGroupItem) {
                fileDownloadGroupItemViewModel.groupTitle.set(activity.getString(R.string.downloaded));
            }

        }
    }

    class FileDownloadingItemHolder extends FileDownloadViewHolder {

        private int max = 100;

        private DownloadingFileItemBinding binding;

        public FileDownloadingItemHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            binding = (DownloadingFileItemBinding) viewDataBinding;

        }

        @Override
        public void refreshDownloadItemView(IDownloadItem downloadItem) {

            FileDownloadingItemViewModel fileDownloadingItemViewModel = binding.getFileDownloadingItemViewModel();

            if (fileDownloadingItemViewModel == null) {
                fileDownloadingItemViewModel = new FileDownloadingItemViewModel();
                binding.setFileDownloadingItemViewModel(fileDownloadingItemViewModel);
            }

            final FileDownloadItem fileDownloadItem = ((DownloadingChildItem) downloadItem).getFileDownloadItem();

            fileDownloadingItemViewModel.fileName.set(fileDownloadItem.getFileName());
            fileDownloadingItemViewModel.maxProgress.set(max);

            fileDownloadingItemViewModel.currentProgress.set(fileDownloadItem.getCurrentProgress(max));

            binding.downloadingFileItemMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    List<BottomMenuItem> bottomMenuItems = new ArrayList<>();

                    BottomMenuItem cancelDownloadItem = new BottomMenuItem(R.drawable.cancel, activity.getString(R.string.cancel) + activity.getString(R.string.download), new AbstractCommand() {
                        @Override
                        public void execute() {

                            fileDownloadItem.cancelDownloadItem();

                            fileDownloadManager.deleteFileDownloadItem(Collections.singletonList(fileDownloadItem.getFileUUID()));

                            refreshView();

                        }

                        @Override
                        public void unExecute() {

                        }
                    });
                    bottomMenuItems.add(cancelDownloadItem);

                    BottomMenuItem cancelMenuItem = new BottomMenuItem(R.drawable.close, activity.getString(R.string.cancel), nullCommand);
                    bottomMenuItems.add(cancelMenuItem);

                    getBottomSheetDialog(bottomMenuItems).show();

                }
            });

        }
    }

    class FileDownloadedItemHolder extends FileDownloadViewHolder {

        LinearLayout downloadedItemLayout;

        private DownloadedFileItemBinding binding;

        private FileDownloadedItemViewModel fileDownloadedItemViewModel;

        public FileDownloadedItemHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            binding = (DownloadedFileItemBinding) viewDataBinding;

            downloadedItemLayout = binding.downloadedFileItemLayout;
        }

        @Override
        public void refreshDownloadItemView(IDownloadItem downloadItem) {
            final FileDownloadItem fileDownloadItem = ((DownloadedChildItem) downloadItem).getFileDownloadItem();

            final DownloadState downloadState = fileDownloadItem.getDownloadState();

            fileDownloadedItemViewModel = binding.getFileDownloadedItemViewModel();

            if (fileDownloadedItemViewModel == null) {
                fileDownloadedItemViewModel = new FileDownloadedItemViewModel();
                binding.setFileDownloadedItemViewModel(fileDownloadedItemViewModel);
            }

            fileDownloadedItemViewModel.fileName.set(fileDownloadItem.getFileName());

            if (downloadState.equals(DownloadState.FINISHED)) {

                fileDownloadedItemViewModel.fileSize.set(FileUtil.formatFileSize(fileDownloadItem.getFileSize()));

            } else {

                fileDownloadedItemViewModel.fileSize.set(activity.getString(R.string.download_failed));

            }

            toggleFileIconBgResource(fileDownloadItem.getFileUUID(), fileDownloadedItemViewModel);

            if (selectMode) {

                fileDownloadedItemViewModel.fileIconBgVisibility.set(true);

                downloadedItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toggleFileInSelectedFile(fileDownloadItem.getFileUUID(), fileDownloadItem.getFileName());
                        toggleFileIconBgResource(fileDownloadItem.getFileUUID(), fileDownloadedItemViewModel);
                    }
                });

            } else {

                fileDownloadedItemViewModel.fileIconBgVisibility.set(false);

                downloadedItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (downloadState.equals(DownloadState.FINISHED)) {
                            FileUtil.openAbstractRemoteFile(activity, fileDownloadItem.getFileName());
                        }

                    }
                });

            }
        }

        private void toggleFileIconBgResource(String fileUUID, FileDownloadedItemViewModel fileDownloadedItemViewModel) {
            if (selectDownloadedItemMap.containsKey(fileUUID)) {

                fileDownloadedItemViewModel.fileIconBgBackgroundSource.set(R.drawable.check_circle_selected);
                fileDownloadedItemViewModel.fileIconVisibility.set(false);

            } else {

                fileDownloadedItemViewModel.fileIconBgBackgroundSource.set(R.drawable.round_circle);
                fileDownloadedItemViewModel.fileIconVisibility.set(true);

            }
        }

        private void toggleFileInSelectedFile(String fileUUID, String fileName) {
            if (selectDownloadedItemMap.containsKey(fileUUID)) {
                selectDownloadedItemMap.remove(fileUUID);
            } else {
                selectDownloadedItemMap.put(fileUUID, new DownloadedFileWrapper(fileUUID, fileName));
            }
        }
    }

}
