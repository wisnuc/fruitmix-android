package com.winsun.fruitmix.torrent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.ViewDataBinding;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackWrapper;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.databinding.ActivityTorrentDownloadManageBinding;
import com.winsun.fruitmix.databinding.CreateTorrentDownloadTaskViewInDialogBinding;
import com.winsun.fruitmix.databinding.TorrentDownloadedChildItemBinding;
import com.winsun.fruitmix.databinding.TorrentDownloadedGroupItemBinding;
import com.winsun.fruitmix.databinding.TorrentDownloadingChildItemBinding;
import com.winsun.fruitmix.databinding.TorrentDownloadingGroupItemBinding;
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory;
import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.plugin.data.PluginManageDataSource;
import com.winsun.fruitmix.plugin.data.PluginStatus;
import com.winsun.fruitmix.torrent.data.DownloadState;
import com.winsun.fruitmix.torrent.data.TorrentDataRepository;
import com.winsun.fruitmix.torrent.data.TorrentDownloadInfo;
import com.winsun.fruitmix.torrent.data.TorrentRequestParam;
import com.winsun.fruitmix.torrent.viewmodel.TorrentDownloadedChildItemViewModel;
import com.winsun.fruitmix.torrent.viewmodel.TorrentDownloadedGroupItemViewModel;
import com.winsun.fruitmix.torrent.viewmodel.TorrentDownloadingChildItemViewModel;
import com.winsun.fruitmix.torrent.viewmodel.TorrentDownloadingGroupItemViewModel;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/12/13.
 */

public class TorrentDownloadManagePresenter implements ActiveView {

    private static final int DOWNLOADING_GROUP = 0;
    private static final int DOWNLOADING_CHILD = 1;
    private static final int DOWNLOADED_GROUP = 2;
    private static final int DOWNLOADED_CHILD = 3;

    private interface IDownloadItem {

        int getDownloadItemType();

    }

    private class DownloadingGroupItem implements IDownloadItem {

        private int itemCount;
        private double totalSpeed;

        public DownloadingGroupItem(int itemCount, double totalSpeed) {
            this.itemCount = itemCount;
            this.totalSpeed = totalSpeed;
        }

        public double getTotalSpeed() {
            return totalSpeed;
        }

        public int getItemCount() {
            return itemCount;
        }

        @Override
        public int getDownloadItemType() {
            return DOWNLOADING_GROUP;
        }
    }

    private class DownloadedGroupItem implements IDownloadItem {

        private int itemCount;

        private int downloadingItemCount;

        public DownloadedGroupItem(int itemCount, int downloadingItemCount) {
            this.itemCount = itemCount;
            this.downloadingItemCount = downloadingItemCount;
        }

        public int getItemCount() {
            return itemCount;
        }

        public int getDownloadingItemCount() {
            return downloadingItemCount;
        }

        @Override
        public int getDownloadItemType() {
            return DOWNLOADED_GROUP;
        }
    }

    private class DownloadingChildItem implements IDownloadItem {

        private TorrentDownloadInfo mTorrentDownloadInfo;

        public DownloadingChildItem(TorrentDownloadInfo torrentDownloadInfo) {
            mTorrentDownloadInfo = torrentDownloadInfo;
        }

        public TorrentDownloadInfo getTorrentDownloadInfo() {
            return mTorrentDownloadInfo;
        }

        @Override
        public int getDownloadItemType() {
            return DOWNLOADING_CHILD;
        }
    }

    private class DownloadedChildItem implements IDownloadItem {

        private TorrentDownloadInfo mTorrentDownloadInfo;

        public DownloadedChildItem(TorrentDownloadInfo torrentDownloadInfo) {
            mTorrentDownloadInfo = torrentDownloadInfo;
        }

        public TorrentDownloadInfo getTorrentDownloadInfo() {
            return mTorrentDownloadInfo;
        }

        @Override
        public int getDownloadItemType() {
            return DOWNLOADED_CHILD;
        }
    }

    private TorrentDataRepository mTorrentDataRepository;

    private PluginManageDataSource mPluginManageDataSource;

    private ActivityTorrentDownloadManageBinding mBinding;

    private LoadingViewModel mLoadingViewModel;

    private NoContentViewModel mNoContentViewModel;

    private TorrentDownloadItemAdapter mTorrentDownloadItemAdapter;

    private List<TorrentDownloadInfo> mDownloadingTorrentDownloadInfo;
    private List<TorrentDownloadInfo> mDownloadedTorrentDownloadInfo;

    private BaseView mBaseView;

    private CreateTorrentDownloadTaskViewInDialogBinding mCreateTorrentDownloadTaskViewInDialogBinding;

    private static final int REFRESH_VIEW = 0x1001;

    public static final String TAG = TorrentDownloadManagePresenter.class.getSimpleName();

    private static class RefreshViewTimelyHandler extends Handler {

        private WeakReference<TorrentDownloadManagePresenter> mWeakReference;

        /**
         * Use the provided {@link Looper} instead of the default one.
         *
         * @param looper The looper, must not be null.
         */
        public RefreshViewTimelyHandler(Looper looper, TorrentDownloadManagePresenter torrentDownloadManagePresenter) {
            super(looper);
            mWeakReference = new WeakReference<>(torrentDownloadManagePresenter);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {

                case REFRESH_VIEW:

                    Log.d(TAG, "handleMessage: refreshView after 2 seconds");

                    TorrentDownloadManagePresenter presenter = mWeakReference.get();

                    if (presenter != null)
                        presenter.refreshView();

                    break;
            }
        }
    }

    private RefreshViewTimelyHandler mRefreshViewTimelyHandler;

    private boolean allPauseFlag = true;

    public TorrentDownloadManagePresenter(TorrentDataRepository torrentDataRepository, PluginManageDataSource pluginManageDataSource,
                                          ActivityTorrentDownloadManageBinding binding,
                                          LoadingViewModel loadingViewModel, NoContentViewModel noContentViewModel, BaseView baseView) {
        mTorrentDataRepository = torrentDataRepository;
        mPluginManageDataSource = pluginManageDataSource;
        mBinding = binding;
        mLoadingViewModel = loadingViewModel;
        mNoContentViewModel = noContentViewModel;
        mBaseView = baseView;

        mDownloadedTorrentDownloadInfo = new ArrayList<>();
        mDownloadingTorrentDownloadInfo = new ArrayList<>();

        mTorrentDownloadItemAdapter = new TorrentDownloadItemAdapter();

        mBinding.torrentDownloadRecyclerview.setAdapter(mTorrentDownloadItemAdapter);

        mRefreshViewTimelyHandler = new RefreshViewTimelyHandler(Looper.getMainLooper(), this);

    }

    public void initView(final Context context) {

        mPluginManageDataSource.getBTStatus(new BaseLoadDataCallbackWrapper<>(
                new BaseLoadDataCallback<PluginStatus>() {
                    @Override
                    public void onSucceed(List<PluginStatus> data, OperationResult operationResult) {

                        if (!data.get(0).isActive()) {

                            mLoadingViewModel.showLoading.set(false);

                            mNoContentViewModel.showNoContent.set(true);

                            mBinding.addUser.setVisibility(View.GONE);

                            return;

                        }

                        refreshView();

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        refreshView();

                    }
                }, this
        ));

    }

    public void refreshView() {

        mTorrentDataRepository.getAllTorrentDownloadInfo(new BaseLoadDataCallbackWrapper<>(
                new BaseLoadDataCallback<TorrentDownloadInfo>() {
                    @Override
                    public void onSucceed(List<TorrentDownloadInfo> data, OperationResult operationResult) {

                        mLoadingViewModel.showLoading.set(false);

                        handleGetAllTorrentDownloadInfo(data);

                        if (mBaseView != null)
                            mRefreshViewTimelyHandler.sendEmptyMessageDelayed(REFRESH_VIEW, 2 * 1000);

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mLoadingViewModel.showLoading.set(false);

                        handleGetAllTorrentDownloadInfo(Collections.<TorrentDownloadInfo>emptyList());

                        if (mBaseView != null)
                            mRefreshViewTimelyHandler.sendEmptyMessageDelayed(REFRESH_VIEW, 2 * 1000);

                    }
                }, this));

    }

    public void onDestroy() {

        mBaseView = null;

    }

    private void handleGetAllTorrentDownloadInfo(List<TorrentDownloadInfo> torrentDownloadInfos) {

        mDownloadingTorrentDownloadInfo.clear();
        mDownloadedTorrentDownloadInfo.clear();

        List<IDownloadItem> downloadItems = new ArrayList<>();

        List<DownloadingChildItem> downloadingChildItems = new ArrayList<>();
        List<DownloadedChildItem> downloadedChildItems = new ArrayList<>();

        double totalSpeed = 0;

        for (TorrentDownloadInfo torrentDownloadInfo : torrentDownloadInfos) {

            if (torrentDownloadInfo.getState() == DownloadState.DOWNLOADED) {

                mDownloadedTorrentDownloadInfo.add(torrentDownloadInfo);

                downloadedChildItems.add(new DownloadedChildItem(torrentDownloadInfo));

            } else {

                mDownloadingTorrentDownloadInfo.add(torrentDownloadInfo);

                downloadingChildItems.add(new DownloadingChildItem(torrentDownloadInfo));

                if (!torrentDownloadInfo.isPause())
                    totalSpeed += torrentDownloadInfo.getDownloadedSpeed();

            }

        }

        DownloadingGroupItem downloadingGroupItem = new DownloadingGroupItem(downloadingChildItems.size(), totalSpeed);
        DownloadedGroupItem downloadedGroupItem = new DownloadedGroupItem(downloadedChildItems.size(), downloadingChildItems.size());

        downloadItems.add(downloadingGroupItem);
        downloadItems.addAll(downloadingChildItems);
        downloadItems.add(downloadedGroupItem);
        downloadItems.addAll(downloadedChildItems);

        mTorrentDownloadItemAdapter.setIDownloadItems(downloadItems);
        mTorrentDownloadItemAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean isActive() {
        return mBaseView != null;
    }

    private class TorrentDownloadItemAdapter extends RecyclerView.Adapter<DownloadViewHolder> {

        private List<IDownloadItem> mIDownloadItems;

        public TorrentDownloadItemAdapter() {
            mIDownloadItems = new ArrayList<>();
        }

        public void setIDownloadItems(List<IDownloadItem> IDownloadItems) {
            mIDownloadItems.clear();
            mIDownloadItems.addAll(IDownloadItems);
        }

        @Override
        public DownloadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewDataBinding viewDataBinding;

            DownloadViewHolder downloadViewHolder = null;

            switch (viewType) {
                case DOWNLOADING_GROUP:

                    viewDataBinding = TorrentDownloadingGroupItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent, false);

                    downloadViewHolder = new DownloadingGroupViewHolder(viewDataBinding);

                    break;

                case DOWNLOADED_GROUP:

                    viewDataBinding = TorrentDownloadedGroupItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent, false);

                    downloadViewHolder = new DownloadedGroupViewHolder(viewDataBinding);

                    break;

                case DOWNLOADING_CHILD:

                    viewDataBinding = TorrentDownloadingChildItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent, false);

                    downloadViewHolder = new DownloadingChildViewHolder(viewDataBinding);

                    break;
                case DOWNLOADED_CHILD:

                    viewDataBinding = TorrentDownloadedChildItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent, false);

                    downloadViewHolder = new DownloadedChildViewHolder(viewDataBinding);

                    break;
            }

            return downloadViewHolder;
        }

        @Override
        public void onBindViewHolder(DownloadViewHolder holder, int position) {
            holder.refreshDownloadItemView(mIDownloadItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mIDownloadItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mIDownloadItems.get(position).getDownloadItemType();
        }
    }


    abstract class DownloadViewHolder extends BindingViewHolder {

        DownloadViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        public abstract void refreshDownloadItemView(IDownloadItem downloadItem);
    }

    private class DownloadingGroupViewHolder extends DownloadViewHolder {

        private DownloadingGroupItem mDownloadingGroupItem;

        private TextView allPause;

        public DownloadingGroupViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            TorrentDownloadingGroupItemBinding binding = (TorrentDownloadingGroupItemBinding) viewDataBinding;

            allPause = binding.allPause;

        }

        @Override
        public void refreshDownloadItemView(IDownloadItem downloadItem) {

            mDownloadingGroupItem = (DownloadingGroupItem) downloadItem;

            final TorrentDownloadingGroupItemViewModel viewModel = new TorrentDownloadingGroupItemViewModel();
            viewModel.setItemCount(mDownloadingGroupItem.getItemCount());
            viewModel.setTotalSpeed(mDownloadingGroupItem.getTotalSpeed());

            viewModel.allPause.set(allPauseFlag);

            getViewDataBinding().setVariable(BR.viewmodel, viewModel);

            getViewDataBinding().setVariable(BR.presenter, TorrentDownloadManagePresenter.this);

            allPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    allPauseBtnClick(viewModel);

                    getViewDataBinding().executePendingBindings();
                }
            });

            getViewDataBinding().executePendingBindings();

        }
    }

    private void allPauseBtnClick(TorrentDownloadingGroupItemViewModel viewModel) {

        if (mDownloadingTorrentDownloadInfo.isEmpty())
            return;

        for (TorrentDownloadInfo torrentDownloadInfo : mDownloadingTorrentDownloadInfo) {

            if (!torrentDownloadInfo.isPause() && allPauseFlag) {

                mTorrentDataRepository.pauseTorrentDownloadTask(new TorrentRequestParam(torrentDownloadInfo.getHash()), new BaseOperateDataCallbackWrapper<Void>(
                        new BaseOperateDataCallbackImpl<Void>(), this
                ));

            } else if (torrentDownloadInfo.isPause() && !allPauseFlag) {

                mTorrentDataRepository.resumeTorrentDownloadTask(new TorrentRequestParam(torrentDownloadInfo.getHash()), new BaseOperateDataCallbackWrapper<Void>(
                        new BaseOperateDataCallbackImpl<Void>(), this
                ));

            }

        }

        allPauseFlag = !allPauseFlag;

        viewModel.allPause.set(allPauseFlag);

    }


    private class DownloadingChildViewHolder extends DownloadViewHolder {


        DownloadingChildViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void refreshDownloadItemView(IDownloadItem downloadItem) {

            DownloadingChildItem downloadingChildItem = (DownloadingChildItem) downloadItem;

            TorrentDownloadingChildItemViewModel viewModel = new TorrentDownloadingChildItemViewModel(downloadingChildItem.getTorrentDownloadInfo());

            getViewDataBinding().setVariable(BR.viewmodel, viewModel);

            getViewDataBinding().setVariable(BR.presenter, TorrentDownloadManagePresenter.this);

            getViewDataBinding().executePendingBindings();

        }
    }

    private class DownloadedGroupViewHolder extends DownloadViewHolder {


        DownloadedGroupViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void refreshDownloadItemView(IDownloadItem downloadItem) {

            DownloadedGroupItem downloadedGroupItem = (DownloadedGroupItem) downloadItem;

            final TorrentDownloadedGroupItemViewModel viewModel = new TorrentDownloadedGroupItemViewModel();
            viewModel.setItemCount(downloadedGroupItem.getItemCount());
            viewModel.setDownloadingItemCount(downloadedGroupItem.getDownloadingItemCount());

            getViewDataBinding().setVariable(BR.viewmodel, viewModel);

            getViewDataBinding().executePendingBindings();

            final TorrentDownloadedGroupItemBinding binding = (TorrentDownloadedGroupItemBinding) getViewDataBinding();

            binding.clearAllRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (viewModel.getItemCount() == 0)
                        return;

                    showClearRecordDialog(binding.getRoot().getContext(), mDownloadedTorrentDownloadInfo);

                }
            });


        }
    }

    private class DownloadedChildViewHolder extends DownloadViewHolder {

        DownloadedChildViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void refreshDownloadItemView(IDownloadItem downloadItem) {

            final TorrentDownloadInfo torrentDownloadInfo = ((DownloadedChildItem) downloadItem).getTorrentDownloadInfo();

            TorrentDownloadedChildItemViewModel viewModel = new TorrentDownloadedChildItemViewModel(torrentDownloadInfo);

            getViewDataBinding().setVariable(BR.viewmodel, viewModel);

            getViewDataBinding().executePendingBindings();

            final TorrentDownloadedChildItemBinding binding = (TorrentDownloadedChildItemBinding) getViewDataBinding();

            binding.downloadingFileItemMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showClearRecordBottomDialog(binding.getRoot().getContext(), Collections.singletonList(torrentDownloadInfo));

                }
            });

        }
    }

    private void showClearRecordBottomDialog(final Context context, final List<TorrentDownloadInfo> torrentDownloadInfos) {

//        final DeleteRecordViewInDialogBinding binding = DeleteRecordViewInDialogBinding.inflate(LayoutInflater.from(context), null, false);

        BottomMenuItem bottomMenuItem = new BottomMenuItem(R.drawable.delete_download_task, context.getString(R.string.delete_task), new AbstractCommand() {
            @Override
            public void execute() {

                showClearRecordDialog(context, torrentDownloadInfos);

            }

            @Override
            public void unExecute() {

            }
        });

        getBottomSheetDialog(Collections.singletonList(bottomMenuItem), context).show();

    }

    private void showClearRecordDialog(final Context context, final List<TorrentDownloadInfo> torrentDownloadInfos) {
        new AlertDialog.Builder(context).setTitle(context.getString(R.string.clear_record))
                .setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        for (TorrentDownloadInfo torrentDownloadInfo : torrentDownloadInfos) {

                            mTorrentDataRepository.deleteTorrentDownloadTask(new TorrentRequestParam(torrentDownloadInfo.getHash()),
                                    new BaseOperateDataCallbackWrapper<>(new BaseOperateDataCallbackImpl<Void>(), TorrentDownloadManagePresenter.this));

                        }

                        mBaseView.showToast(context.getString(R.string.success, context.getString(R.string.clear_record)));


                    }
                }).setNegativeButton(context.getString(R.string.cancel), null)
                .create().show();
    }

    public void showCreateDownloadTaskDialog(final Context context) {

        mCreateTorrentDownloadTaskViewInDialogBinding = CreateTorrentDownloadTaskViewInDialogBinding
                .inflate(LayoutInflater.from(context), null, false);

        mCreateTorrentDownloadTaskViewInDialogBinding.setPresenter(this);

        new AlertDialog.Builder(context)
                .setView(mCreateTorrentDownloadTaskViewInDialogBinding.getRoot())
                .setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String url = mCreateTorrentDownloadTaskViewInDialogBinding.editText.getText().toString();

                        createDownloadTask(context, url);


                    }
                }).setNegativeButton(context.getString(R.string.cancel), null)
                .create().show();

    }

    public void pasteContentInTheClipboard(Context context) {

        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboardManager == null || !clipboardManager.hasPrimaryClip())
            return;

        ClipData clipData = clipboardManager.getPrimaryClip();

        String text = clipData.getItemAt(0).coerceToText(context).toString();

        mCreateTorrentDownloadTaskViewInDialogBinding.editText.setText(text);

    }


    private void createDownloadTask(final Context context, String url) {

        if (url.length() < 60 || url.startsWith("magnet:\\?xt=urn:btih:")) {

            mBaseView.showToast(context.getString(R.string.magnet_illegal));
            return;
        }

        mBaseView.showProgressDialog(context.getString(R.string.operating_title, context.getString(R.string.create_new_download_task)));

        mTorrentDataRepository.postTorrentDownloadTask(url, new BaseOperateDataCallback<TorrentRequestParam>() {
            @Override
            public void onSucceed(TorrentRequestParam data, OperationResult result) {

                mBaseView.dismissDialog();

                mBaseView.showToast(context.getString(R.string.success, context.getString(R.string.create_new_download_task)));


            }

            @Override
            public void onFail(OperationResult operationResult) {

                mBaseView.dismissDialog();

                mBaseView.showToast(operationResult.getResultMessage(context));
            }
        });

    }

    public void showOperateTorrentDownloadingItemBottomDialog(final Context context, final TorrentDownloadingChildItemViewModel viewModel) {

        String pauseOrResume = viewModel.isPause() ? context.getString(R.string.resume) : context.getString(R.string.pause);

        int resID = viewModel.isPause() ? R.drawable.pause_download_task : R.drawable.resume_download_task;

        BottomMenuItem bottomMenuItem = new BottomMenuItem(resID, pauseOrResume, new AbstractCommand() {
            @Override
            public void execute() {

                if (viewModel.isPause()) {

                    mTorrentDataRepository.resumeTorrentDownloadTask(new TorrentRequestParam(viewModel.getHash()), new BaseOperateDataCallbackWrapper<Void>(
                            new BaseOperateDataCallback<Void>() {
                                @Override
                                public void onSucceed(Void data, OperationResult result) {

                                    mBaseView.showToast(context.getString(R.string.success, context.getString(R.string.resume)));

                                }

                                @Override
                                public void onFail(OperationResult operationResult) {

                                    mBaseView.showToast(operationResult.getResultMessage(context));

                                }
                            }, TorrentDownloadManagePresenter.this
                    ));

                } else {

                    mTorrentDataRepository.pauseTorrentDownloadTask(new TorrentRequestParam(viewModel.getHash()), new BaseOperateDataCallbackWrapper<Void>(
                            new BaseOperateDataCallback<Void>() {
                                @Override
                                public void onSucceed(Void data, OperationResult result) {

                                    mBaseView.showToast(context.getString(R.string.success, context.getString(R.string.pause)));

                                }

                                @Override
                                public void onFail(OperationResult operationResult) {

                                    mBaseView.showToast(operationResult.getResultMessage(context));

                                }
                            }, TorrentDownloadManagePresenter.this
                    ));

                }

            }

            @Override
            public void unExecute() {

            }
        });

        BottomMenuItem deleteTaskItem = new BottomMenuItem(R.drawable.delete_download_task, context.getString(R.string.delete_task), new AbstractCommand() {
            @Override
            public void execute() {

                mTorrentDataRepository.deleteTorrentDownloadTask(new TorrentRequestParam(viewModel.getHash()), new BaseOperateDataCallbackWrapper<Void>(
                        new BaseOperateDataCallback<Void>() {
                            @Override
                            public void onSucceed(Void data, OperationResult result) {

                                mBaseView.showToast(context.getString(R.string.success, context.getString(R.string.delete_task)));

                            }

                            @Override
                            public void onFail(OperationResult operationResult) {

                                mBaseView.showToast(operationResult.getResultMessage(context));
                            }
                        }, TorrentDownloadManagePresenter.this
                ));

            }

            @Override
            public void unExecute() {

            }
        });

        List<BottomMenuItem> bottomMenuItems = new ArrayList<>();
        bottomMenuItems.add(bottomMenuItem);
        bottomMenuItems.add(deleteTaskItem);

        getBottomSheetDialog(bottomMenuItems, context).show();

    }


    private Dialog getBottomSheetDialog(List<BottomMenuItem> bottomMenuItems, Context context) {

        Dialog dialog = new BottomMenuDialogFactory(bottomMenuItems).createDialog(context);

        for (BottomMenuItem bottomMenuItem : bottomMenuItems) {
            bottomMenuItem.setDialog(dialog);
        }

        return dialog;
    }


}
