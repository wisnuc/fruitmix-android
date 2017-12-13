package com.winsun.fruitmix.torrent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.databinding.ActivityTorrentDownloadManageBinding;
import com.winsun.fruitmix.databinding.CreateTorrentDownloadTaskViewInDialogBinding;
import com.winsun.fruitmix.databinding.DeleteRecordViewInDialogBinding;
import com.winsun.fruitmix.databinding.DownloadedFileItemBinding;
import com.winsun.fruitmix.databinding.DownloadingFileItemBinding;
import com.winsun.fruitmix.databinding.FileDownloadGroupItemBinding;
import com.winsun.fruitmix.databinding.TorrentDownloadedChildItemBinding;
import com.winsun.fruitmix.databinding.TorrentDownloadedGroupItemBinding;
import com.winsun.fruitmix.databinding.TorrentDownloadingChildItemBinding;
import com.winsun.fruitmix.databinding.TorrentDownloadingGroupItemBinding;
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory;
import com.winsun.fruitmix.file.view.fragment.FileDownloadFragment;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.torrent.data.TorrentDataRepository;
import com.winsun.fruitmix.torrent.data.TorrentDownloadInfo;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;

import java.util.ArrayList;
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

        public void setTotalSpeed(double totalSpeed) {
            this.totalSpeed = totalSpeed;
        }

        @Override
        public int getDownloadItemType() {
            return DOWNLOADING_GROUP;
        }
    }

    private class DownloadedGroupItem implements IDownloadItem {

        private int itemCount;

        public DownloadedGroupItem(int itemCount) {
            this.itemCount = itemCount;
        }

        @Override
        public int getDownloadItemType() {
            return DOWNLOADED_GROUP;
        }
    }

    private class DownloadingChildItem implements IDownloadItem {

        private List<TorrentDownloadInfo> mTorrentDownloadInfos;

        public DownloadingChildItem(List<TorrentDownloadInfo> torrentDownloadInfos) {
            mTorrentDownloadInfos = torrentDownloadInfos;
        }

        @Override
        public int getDownloadItemType() {
            return DOWNLOADING_CHILD;
        }
    }

    private class DownloadedChildItem implements IDownloadItem {

        private List<TorrentDownloadInfo> mTorrentDownloadInfos;

        @Override
        public int getDownloadItemType() {
            return DOWNLOADED_CHILD;
        }
    }

    private TorrentDataRepository mTorrentDataRepository;

    private ActivityTorrentDownloadManageBinding mBinding;

    private User currentUser;

    private LoadingViewModel mLoadingViewModel;

    private TorrentDownloadItemAdapter mTorrentDownloadItemAdapter;

    public TorrentDownloadManagePresenter(TorrentDataRepository torrentDataRepository,
                                          ActivityTorrentDownloadManageBinding binding, User currentUser,
                                          LoadingViewModel loadingViewModel) {
        mTorrentDataRepository = torrentDataRepository;
        mBinding = binding;
        this.currentUser = currentUser;
        mLoadingViewModel = loadingViewModel;
    }


    public void refreshView() {

        mTorrentDataRepository.getAllTorrentDownloadInfo(new BaseLoadDataCallbackWrapper<>(
                new BaseLoadDataCallback<TorrentDownloadInfo>() {
                    @Override
                    public void onSucceed(List<TorrentDownloadInfo> data, OperationResult operationResult) {

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mLoadingViewModel.showLoading.set(false);


                    }
                }
                , this));

    }

    @Override
    public boolean isActive() {
        return false;
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
    }


    abstract class DownloadViewHolder extends BindingViewHolder {

        DownloadViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        public abstract void refreshDownloadItemView(IDownloadItem downloadItem);
    }

    private class DownloadingGroupViewHolder extends DownloadViewHolder {

        private DownloadingGroupItem mDownloadingGroupItem;

        public DownloadingGroupViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void refreshDownloadItemView(IDownloadItem downloadItem) {

            mDownloadingGroupItem = (DownloadingGroupItem) downloadItem;


        }
    }

    private class DownloadingChildViewHolder extends DownloadViewHolder {


        DownloadingChildViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void refreshDownloadItemView(IDownloadItem downloadItem) {

            DownloadingChildItem downloadingChildItem = (DownloadingChildItem) downloadItem;

        }
    }

    private class DownloadedGroupViewHolder extends DownloadViewHolder {


        DownloadedGroupViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void refreshDownloadItemView(IDownloadItem downloadItem) {

            DownloadedGroupItem downloadedGroupItem = (DownloadedGroupItem) downloadItem;

        }
    }

    private class DownloadedChildViewHolder extends DownloadViewHolder {


        DownloadedChildViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void refreshDownloadItemView(IDownloadItem downloadItem) {

            DownloadedChildItem downloadedChildItem = (DownloadedChildItem) downloadItem;

        }
    }

    public void showClearRecordDialog(Context context) {

        final DeleteRecordViewInDialogBinding binding = DeleteRecordViewInDialogBinding.inflate(LayoutInflater.from(context), null, false);

        new AlertDialog.Builder(context).setTitle(context.getString(R.string.clear_record))
                .setView(binding.getRoot())
                .setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (binding.checkbox.isChecked()) {
                            //TODO:delete record and file
                        } else {
                            //TODO:delete record
                        }

                    }
                }).setNegativeButton(context.getString(R.string.cancel), null)
                .create().show();

    }

    public void showCreateDownloadTaskDialog(Context context) {

        final CreateTorrentDownloadTaskViewInDialogBinding binding = CreateTorrentDownloadTaskViewInDialogBinding
                .inflate(LayoutInflater.from(context), null, false);

        new AlertDialog.Builder(context).setTitle(context.getString(R.string.create_new_download_task))
                .setView(binding.getRoot())
                .setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String url = binding.editText.getText().toString();

                        //TODO:create task

                    }
                }).setNegativeButton(context.getString(R.string.cancel), null)
                .create().show();


    }


    private Dialog getBottomSheetDialog(List<BottomMenuItem> bottomMenuItems, Context context) {

        Dialog dialog = new BottomMenuDialogFactory(bottomMenuItems).createDialog(context);

        for (BottomMenuItem bottomMenuItem : bottomMenuItems) {
            bottomMenuItem.setDialog(dialog);
        }

        return dialog;
    }


}
