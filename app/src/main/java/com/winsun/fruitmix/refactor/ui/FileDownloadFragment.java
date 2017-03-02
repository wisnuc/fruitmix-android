package com.winsun.fruitmix.refactor.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.command.DeleteDownloadedFileCommand;
import com.winsun.fruitmix.command.MacroCommand;
import com.winsun.fruitmix.command.NullCommand;
import com.winsun.fruitmix.command.ShowSelectModeViewCommand;
import com.winsun.fruitmix.command.ShowUnSelectModeViewCommand;
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory;
import com.winsun.fruitmix.eventbus.DownloadStateChangedEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.fileModule.download.DownloadState;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.interfaces.OnFileInteractionListener;
import com.winsun.fruitmix.fileModule.model.BottomMenuItem;
import com.winsun.fruitmix.interfaces.OnViewSelectListener;
import com.winsun.fruitmix.refactor.common.BaseActivity;
import com.winsun.fruitmix.refactor.common.Injection;
import com.winsun.fruitmix.refactor.contract.FileDownloadFragmentContract;
import com.winsun.fruitmix.refactor.contract.FileMainFragmentContract;
import com.winsun.fruitmix.refactor.presenter.FileDownloadFragmentPresenterImpl;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileDownloadFragment implements FileDownloadFragmentContract.FileDownloadFragmentView {

    public static final String TAG = FileDownloadFragment.class.getSimpleName();

    @BindView(R.id.downloading_recyclerview)
    RecyclerView fileDownloadingRecyclerView;
    @BindView(R.id.downloaded_recyclerview)
    RecyclerView fileDownloadedRecyclerView;

    private DownloadingFileAdapter downloadingFileAdapter;
    private DownloadedFileAdapter downloadedFileAdapter;

    private View view;

    private FileDownloadFragmentContract.FileDownloadFragmentPresenter mPresenter;

    private BaseActivity baseActivity;

    public FileDownloadFragment(BaseActivity activity, FileMainFragmentContract.FileMainFragmentPresenter fileMainFragmentPresenter) {

        view = LayoutInflater.from(activity).inflate(R.layout.fragment_file_download, null);

        ButterKnife.bind(this, view);

        baseActivity = activity;

        downloadingFileAdapter = new DownloadingFileAdapter();
        downloadedFileAdapter = new DownloadedFileAdapter();

        fileDownloadingRecyclerView.setAdapter(downloadingFileAdapter);
        fileDownloadingRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

        fileDownloadedRecyclerView.setAdapter(downloadedFileAdapter);
        fileDownloadedRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

        mPresenter = new FileDownloadFragmentPresenterImpl(Injection.injectDataRepository(), fileMainFragmentPresenter);
        mPresenter.attachView(this);
        mPresenter.loadDownloadedFile();

        mPresenter.registerFileDownloadStateChangedListener();
    }

    public FileDownloadFragmentContract.FileDownloadFragmentPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public String getString(int resID) {
        return baseActivity.getString(resID);
    }

    @Override
    public void showDownloadingContent(List<FileDownloadItem> fileDownloadItems) {

        downloadingFileAdapter.setData(fileDownloadItems);
        downloadingFileAdapter.notifyDataSetChanged();
    }

    @Override
    public void showDownloadedContent(List<FileDownloadItem> fileDownloadItems, boolean selectMode) {

        downloadedFileAdapter.setData(fileDownloadItems, selectMode);
        downloadedFileAdapter.notifyDataSetChanged();
    }

    @Override
    public void showNoEnoughSpaceToast() {
        Toast.makeText(baseActivity, getString(R.string.no_enough_space), Toast.LENGTH_SHORT).show();

    }

    public Dialog getBottomSheetDialog(List<BottomMenuItem> bottomMenuItems) {

        Dialog dialog = new BottomMenuDialogFactory(bottomMenuItems).createDialog(baseActivity);

        for (BottomMenuItem bottomMenuItem : bottomMenuItems) {
            bottomMenuItem.setDialog(dialog);
        }

        return dialog;
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void onDestroyView() {

        baseActivity = null;

        mPresenter.unregisterFileDownloadStateChangedListener();
        mPresenter.detachView();
    }

    private class DownloadingFileAdapter extends RecyclerView.Adapter<DownloadingFileAdapterViewHolder> {

        private List<FileDownloadItem> downloadingItems;

        void setData(List<FileDownloadItem> downloadingItems) {
            this.downloadingItems = downloadingItems;
        }

        @Override
        public DownloadingFileAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(baseActivity).inflate(R.layout.downloading_file_item, parent, false);

            return new DownloadingFileAdapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DownloadingFileAdapterViewHolder holder, int position) {
            holder.refreshView(downloadingItems.get(position));
        }

        @Override
        public int getItemCount() {
            return downloadingItems.size();
        }
    }


    class DownloadingFileAdapterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.downloading_progressbar)
        ProgressBar downloadingProgressBar;

        DownloadingFileAdapterViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            downloadingProgressBar.setMax(100);
        }

        void refreshView(FileDownloadItem fileDownloadItem) {

            fileName.setText(fileDownloadItem.getFileName());

            Log.d(TAG, "refreshView: currentDownloadSize:" + fileDownloadItem.getFileCurrentDownloadSize() + " fileSize:" + fileDownloadItem.getFileSize());

            float currentProgress = fileDownloadItem.getFileCurrentDownloadSize() * 100 / fileDownloadItem.getFileSize();

            Log.d(TAG, "refreshView: currentProgress:" + currentProgress);

            downloadingProgressBar.setProgress((int) currentProgress);

        }

    }

    private class DownloadedFileAdapter extends RecyclerView.Adapter<DownloadedFileAdapterViewHolder> {

        private List<FileDownloadItem> downloadedItems;
        private boolean selectMode;

        void setData(List<FileDownloadItem> downloadedItems, boolean selectMode) {
            this.downloadedItems = downloadedItems;
            this.selectMode = selectMode;
        }

        @Override
        public DownloadedFileAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(baseActivity).inflate(R.layout.downloaded_file_item, parent, false);

            return new DownloadedFileAdapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DownloadedFileAdapterViewHolder holder, int position) {
            holder.refreshView(downloadedItems.get(position), selectMode);
        }

        @Override
        public int getItemCount() {
            return downloadedItems.size();
        }
    }

    class DownloadedFileAdapterViewHolder extends RecyclerView.ViewHolder {

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

        DownloadedFileAdapterViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        void refreshView(final FileDownloadItem fileDownloadItem, boolean selectMode) {

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

                        mPresenter.toggleFileInSelectedFile(fileDownloadItem.getFileUUID());

                        toggleFileIconBgResource(fileDownloadItem.getFileUUID());
                    }
                });

            } else {
                fileIconBg.setVisibility(View.INVISIBLE);

                downloadedItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mPresenter.openFileIfDownloaded(baseActivity, fileDownloadItem);
                    }
                });

            }

        }

        private void toggleFileIconBgResource(String fileUUID) {
            if (mPresenter.checkIsInSelectedFiles(fileUUID)) {
                fileIconBg.setBackgroundResource(R.drawable.check_circle_selected);
                fileIcon.setVisibility(View.INVISIBLE);
            } else {
                fileIconBg.setBackgroundResource(R.drawable.round_circle);
                fileIcon.setVisibility(View.VISIBLE);
            }
        }

    }

}
