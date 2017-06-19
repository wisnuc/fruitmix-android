package com.winsun.fruitmix.fileModule.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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
import com.winsun.fruitmix.fileModule.download.DownloadState;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.fileModule.interfaces.OnFileInteractionListener;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.interfaces.OnViewSelectListener;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileDownloadFragment extends Fragment implements OnViewSelectListener, IShowHideFragmentListener {

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

    private List<String> selectDownloadedItemUUID;

    private OnFileInteractionListener onFileInteractionListener;

    private AbstractCommand showUnSelectModeViewCommand;

    private AbstractCommand showSelectModeViewCommand;

    private AbstractCommand nullCommand;

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

    public FileDownloadFragment() {
        // Required empty public constructor
    }

    public void setOnFileInteractionListener(OnFileInteractionListener onFileInteractionListener) {
        this.onFileInteractionListener = onFileInteractionListener;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileDownloadFragment.
     */
    public static FileDownloadFragment newInstance(OnFileInteractionListener onFileInteractionListener) {
        FileDownloadFragment fragment = new FileDownloadFragment();
        fragment.setOnFileInteractionListener(onFileInteractionListener);

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new FileDownloadRecyclerViewAdapter();

        downloadingItems = new ArrayList<>();
        downloadedItems = new ArrayList<>();

        downloadItems = new ArrayList<>();

        customHandler = new CustomHandler(this);

        selectDownloadedItemUUID = new ArrayList<>();

        showUnSelectModeViewCommand = new ShowUnSelectModeViewCommand(this);

        showSelectModeViewCommand = new ShowSelectModeViewCommand(this);

        nullCommand = new NullCommand();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentFileDownloadBinding fragmentFileDownloadBinding = FragmentFileDownloadBinding.inflate(inflater, container, false);

        fileDownloadRecyclerView = fragmentFileDownloadBinding.fileDownloadRecyclerView;

        fileDownloadRecyclerView.setAdapter(mAdapter);

        fileDownloadRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fileDownloadRecyclerView.setItemAnimator(new DefaultItemAnimator());

        return fragmentFileDownloadBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        FNAS.retrieveDownloadedFile();
    }

    public void handleTitle() {
        onFileInteractionListener.setToolbarTitle(getString(R.string.file));
        onFileInteractionListener.setNavigationIcon(R.drawable.menu_black);
        onFileInteractionListener.setDefaultNavigationOnClickListener();
    }

    @Override
    public void onStop() {
        super.onStop();
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
            customHandler.sendEmptyMessageDelayed(DOWNLOAD_STATE_CHANGED, DELAY_TIME_MILLISECOND);
        } else if (downloadState == DownloadState.NO_ENOUGH_SPACE) {

            Toast.makeText(getActivity(), getString(R.string.no_enough_space), Toast.LENGTH_SHORT).show();

        } else if (downloadState == DownloadState.START_DOWNLOAD || downloadState == DownloadState.PENDING) {

            Log.d(TAG, "handleEvent: download state: START_DOWNLOAD,PENDING");

            refreshView();

        } else if (downloadState == DownloadState.DOWNLOADING) {
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

    private void refreshView() {
        refreshData();

        mAdapter.setDownloads(downloadItems);
        mAdapter.notifyDataSetChanged();

        fileDownloadRecyclerView.smoothScrollToPosition(0);

    }

    private void refreshData() {
        filterFileDownloadItems(FileDownloadManager.INSTANCE.getFileDownloadItems());
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

    private int getPosition(String fileUUID) {

        for (int i = 0; i < downloadItems.size(); i++) {

            IDownloadItem downloadItem = downloadItems.get(i);

            if (downloadItem instanceof DownloadedGroupItem) {
                return i;
            }
        }

        return -1;

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

            BottomMenuItem clearSelectItem = new BottomMenuItem(getString(R.string.clear_select_item), showUnSelectModeViewCommand);

            bottomMenuItems.add(clearSelectItem);

            AbstractCommand macroCommand = new MacroCommand();
            macroCommand.addCommand(new DeleteDownloadedFileCommand(selectDownloadedItemUUID));
            macroCommand.addCommand(showUnSelectModeViewCommand);

            BottomMenuItem deleteSelectItem = new BottomMenuItem(getString(R.string.delete_text), macroCommand);

            bottomMenuItems.add(deleteSelectItem);

        } else {

            BottomMenuItem selectItem = new BottomMenuItem(getString(R.string.choose_text), showSelectModeViewCommand);

            if (downloadedItems.isEmpty())
                selectItem.setDisable(true);

            bottomMenuItems.add(selectItem);

        }

        BottomMenuItem cancelMenuItem = new BottomMenuItem(getString(R.string.cancel), nullCommand);

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

        Dialog dialog = new BottomMenuDialogFactory(bottomMenuItems).createDialog(getActivity());

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

                    view = LayoutInflater.from(getContext()).inflate(R.layout.file_download_group_item, parent, false);

                    fileDownloadViewHolder = new FileDownloadGroupHolder(view);

                    break;

                case DOWNLOADING_CHILD:

                    view = LayoutInflater.from(getContext()).inflate(R.layout.downloading_file_item, parent, false);

                    fileDownloadViewHolder = new FileDownloadingItemHolder(view);

                    break;
                case DOWNLOADED_CHILD:

                    view = LayoutInflater.from(getContext()).inflate(R.layout.downloaded_file_item, parent, false);

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

        @BindView(R.id.file_icon_bg)
        ImageView fileIconBg;
        @BindView(R.id.file_icon)
        ImageView fileIcon;
        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.file_size)
        TextView fileSize;
        @BindView(R.id.remote_file_item_layout)
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
                fileSize.setText(getString(R.string.download_failed));
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
                            FileUtil.openAbstractRemoteFile(getActivity(), fileDownloadItem.getFileName());
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
