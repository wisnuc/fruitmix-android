package com.winsun.fruitmix.file.view.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.command.DeleteDownloadedFileCommand;
import com.winsun.fruitmix.command.NullCommand;
import com.winsun.fruitmix.command.MacroCommand;
import com.winsun.fruitmix.command.ShowSelectModeViewCommand;
import com.winsun.fruitmix.command.ShowUnSelectModeViewCommand;
import com.winsun.fruitmix.databinding.FragmentFileDownloadBinding;
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory;
import com.winsun.fruitmix.eventbus.DownloadStateChangedEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.data.download.DownloadState;
import com.winsun.fruitmix.file.data.download.DownloadedItem;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadManager;
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.interfaces.Page;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.interfaces.OnViewSelectListener;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileDownloadFragment implements Page, OnViewSelectListener, IShowHideFragmentListener {

    public static final String TAG = FileDownloadFragment.class.getSimpleName();

    private RecyclerView fileDownloadRecyclerView;

    private FileDownloadRecyclerViewAdapter mAdapter;

    private List<IDownloadItem> downloadItems;

    private List<IDownloadItem> downloadingItems;
    private List<IDownloadItem> downloadedItems;

    private CustomHandler customHandler;

    public static final int DOWNLOAD_STATE_CHANGED = 0x0010;

    public static final int DELAY_TIME_MILLISECOND = 0;

    private boolean selectMode = false;

    private boolean mStartDownloadOrPending = false;

    private List<String> selectDownloadedItemUUID;

    private AbstractCommand showUnSelectModeViewCommand;

    private AbstractCommand showSelectModeViewCommand;

    private AbstractCommand nullCommand;

    private ToolbarViewModel toolbarViewModel;

    private ToolbarViewModel.ToolbarNavigationOnClickListener defaultListener;

    private View view;

    private Activity activity;

    private StationFileRepository stationFileRepository;

    private String currentUserUUID;

    public static final int DOWNLOADING_GROUP = 0;
    public static final int DOWNLOADING_CHILD = 1;
    public static final int DOWNLOADED_GROUP = 2;
    public static final int DOWNLOADED_CHILD = 3;

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

        setToolbarViewModel(toolbarViewModel);
        setDefaultListener(defaultListener);

        mAdapter = new FileDownloadRecyclerViewAdapter();

        downloadingItems = new ArrayList<>();
        downloadedItems = new ArrayList<>();

        downloadItems = new ArrayList<>();

        customHandler = new CustomHandler(this);

        selectDownloadedItemUUID = new ArrayList<>();

        showUnSelectModeViewCommand = new ShowUnSelectModeViewCommand(this);

        showSelectModeViewCommand = new ShowSelectModeViewCommand(this);

        nullCommand = new NullCommand();

        view = onCreateView(activity.getLayoutInflater(), null);

        currentUserUUID = InjectLoggedInUser.provideLoggedInUserRepository(activity).getCurrentLoggedInUserUUID();

        stationFileRepository = InjectStationFileRepository.provideStationFileRepository(activity);

        stationFileRepository.getCurrentLoginUserDownloadedFileRecord(currentUserUUID);

        refreshView();

    }

    private View onCreateView(LayoutInflater inflater, ViewGroup container) {

        FragmentFileDownloadBinding fragmentFileDownloadBinding = FragmentFileDownloadBinding.inflate(inflater, container, false);

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

        refreshView();

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

        fileDownloadRecyclerView.smoothScrollToPosition(0);

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
            macroCommand.addCommand(new DeleteDownloadedFileCommand(selectDownloadedItemUUID,currentUserUUID,stationFileRepository));
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
            selectDownloadedItemUUID.clear();
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

            View view;

            FileDownloadViewHolder fileDownloadViewHolder = null;

            switch (viewType) {
                case DOWNLOADING_GROUP:
                case DOWNLOADED_GROUP:

                    view = LayoutInflater.from(activity).inflate(R.layout.file_download_group_item, parent, false);

                    fileDownloadViewHolder = new FileDownloadGroupHolder(view);

                    break;

                case DOWNLOADING_CHILD:

                    view = LayoutInflater.from(activity).inflate(R.layout.downloading_file_item, parent, false);

                    fileDownloadViewHolder = new FileDownloadingItemHolder(view);

                    break;
                case DOWNLOADED_CHILD:

                    view = LayoutInflater.from(activity).inflate(R.layout.downloaded_file_item, parent, false);

                    fileDownloadViewHolder = new FileDownloadedItemHolder(view);

                    break;
            }

            return fileDownloadViewHolder;
        }

        @Override
        public void onBindViewHolder(FileDownloadViewHolder holder, int position) {
            holder.refreshView(mDownloads.get(position));
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

    abstract class FileDownloadViewHolder extends RecyclerView.ViewHolder {

        public FileDownloadViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public abstract void refreshView(IDownloadItem downloadItem);
    }

    class FileDownloadGroupHolder extends FileDownloadViewHolder {

        @BindView(R.id.file_download_group_text_view)
        TextView mFileDownloadGroupTextView;

        FileDownloadGroupHolder(View view) {
            super(view);
        }

        @Override
        public void refreshView(IDownloadItem downloadItem) {

            if (downloadItem instanceof DownloadingGroupItem) {
                mFileDownloadGroupTextView.setText(R.string.downloading);
            } else if (downloadItem instanceof DownloadedGroupItem) {
                mFileDownloadGroupTextView.setText(R.string.downloaded);
            }

        }
    }

    class FileDownloadingItemHolder extends FileDownloadViewHolder {

        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.downloading_progressbar)
        ProgressBar downloadingProgressBar;

        private int max = 100;

        FileDownloadingItemHolder(View view) {
            super(view);

            downloadingProgressBar.setMax(max);
        }

        @Override
        public void refreshView(IDownloadItem downloadItem) {

            FileDownloadItem fileDownloadItem = ((DownloadingChildItem) downloadItem).getFileDownloadItem();

            fileName.setText(fileDownloadItem.getFileName());

            downloadingProgressBar.setProgress(fileDownloadItem.getCurrentProgress(max));
        }
    }

    class FileDownloadedItemHolder extends FileDownloadViewHolder {

        @BindView(R.id.select_file_icon_bg)
        ImageView fileIconBg;
        @BindView(R.id.file_icon)
        ImageView fileIcon;
        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.file_size)
        TextView fileSize;
        @BindView(R.id.downloaded_file_item_layout)
        LinearLayout downloadedItemLayout;

        FileDownloadedItemHolder(View view) {
            super(view);
        }

        @Override
        public void refreshView(IDownloadItem downloadItem) {
            final FileDownloadItem fileDownloadItem = ((DownloadedChildItem) downloadItem).getFileDownloadItem();

            final DownloadState downloadState = fileDownloadItem.getDownloadState();

            fileName.setText(fileDownloadItem.getFileName());

            if (downloadState.equals(DownloadState.FINISHED)) {
                fileSize.setText(FileUtil.formatFileSize(fileDownloadItem.getFileSize()));
            } else {
                fileSize.setText(activity.getString(R.string.download_failed));
            }

            toggleFileIconBgResource(fileDownloadItem.getFileUUID());

            if (selectMode) {
                fileIconBg.setVisibility(View.VISIBLE);

                downloadedItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toggleFileInSelectedFile(fileDownloadItem.getFileUUID());
                        toggleFileIconBgResource(fileDownloadItem.getFileUUID());
                    }
                });

            } else {
                fileIconBg.setVisibility(View.INVISIBLE);

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

        private void toggleFileIconBgResource(String fileUUID) {
            if (selectDownloadedItemUUID.contains(fileUUID)) {
                fileIconBg.setBackgroundResource(R.drawable.check_circle_selected);
                fileIcon.setVisibility(View.INVISIBLE);
            } else {
                fileIconBg.setBackgroundResource(R.drawable.round_circle);
                fileIcon.setVisibility(View.VISIBLE);
            }
        }

        private void toggleFileInSelectedFile(String fileUUID) {
            if (selectDownloadedItemUUID.contains(fileUUID)) {
                selectDownloadedItemUUID.remove(fileUUID);
            } else {
                selectDownloadedItemUUID.add(fileUUID);
            }
        }
    }

}
