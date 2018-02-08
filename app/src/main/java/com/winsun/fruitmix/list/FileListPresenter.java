package com.winsun.fruitmix.list;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.ViewDataBinding;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.command.ChangeToDownloadPageCommand;
import com.winsun.fruitmix.command.DownloadFileCommand;
import com.winsun.fruitmix.command.MacroCommand;
import com.winsun.fruitmix.command.NullCommand;
import com.winsun.fruitmix.command.OpenFileCommand;
import com.winsun.fruitmix.databinding.RemoteFileItemLayoutBinding;
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory;
import com.winsun.fruitmix.eventbus.TaskStateChangedEvent;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.TaskState;
import com.winsun.fruitmix.file.data.download.param.FileFromBoxDownloadParam;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.FileTaskItem;
import com.winsun.fruitmix.file.data.model.FileTaskManager;
import com.winsun.fruitmix.file.data.model.FinishedTaskItemWrapper;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.file.view.FileDownloadActivity;
import com.winsun.fruitmix.file.view.viewmodel.FileItemViewModel;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.video.PlayVideoActivity;
import com.winsun.fruitmix.viewholder.BindingViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FileListPresenter {

    private List<AbstractFile> mAbstractFiles;

    private FileTaskManager mFileTaskManager;

    private Activity mActivity;

    private AbstractCommand nullCommand;

    private ChangeToDownloadPageCommand.ChangeToDownloadPageCallback changeToDownloadPageCallback;

    private StationFileRepository mStationFileRepository;

    private String currentUserUUID;

    private ProgressDialog currentDownloadFileProgressDialog;

    private int progressMax = 100;

    private boolean cancelDownload = false;

    private NetworkStateManager mNetworkStateManager;

    private String groupUUID;

    private String stationID;

    private DownloadFileCommand mCurrentDownloadFileCommand;

    private String cloudToken;

    public FileListPresenter(FileComment fileComment, FileTaskManager fileTaskManager, Activity activity,
                             StationFileRepository stationFileRepository, NetworkStateManager networkStateManager,
                             String currentUserUUID,String cloudToken) {
        mAbstractFiles = fileComment.getFiles();

        groupUUID = fileComment.getGroupUUID();

        stationID = fileComment.getStationID();

        this.cloudToken = cloudToken;

        mFileTaskManager = fileTaskManager;

        mNetworkStateManager = networkStateManager;

        mStationFileRepository = stationFileRepository;

        this.currentUserUUID = currentUserUUID;

        mActivity = activity;

        nullCommand = new NullCommand();

        changeToDownloadPageCallback = new ChangeToDownloadPageCommand.ChangeToDownloadPageCallback() {
            @Override
            public void changeToDownloadPage() {
                mActivity.startActivity(new Intent(mActivity, FileDownloadActivity.class));
            }
        };

    }

    public void refreshView(RecyclerView recyclerView) {

        FileListAdapter fileListAdapter = new FileListAdapter();

        recyclerView.setAdapter(fileListAdapter);

        fileListAdapter.setAbstractFiles(mAbstractFiles);
        fileListAdapter.notifyDataSetChanged();
    }

    public void onDestroy() {

        mActivity = null;

    }

    private class FileListAdapter extends RecyclerView.Adapter<FileListViewHolder> {

        private List<AbstractFile> mAbstractFiles;

        public FileListAdapter() {
            mAbstractFiles = new ArrayList<>();
        }

        public void setAbstractFiles(List<AbstractFile> abstractFiles) {
            mAbstractFiles.clear();
            mAbstractFiles.addAll(abstractFiles);
        }

        @Override
        public FileListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            RemoteFileItemLayoutBinding binding = RemoteFileItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),
                    parent, false);

            return new FileListViewHolder(binding);

        }

        @Override
        public void onBindViewHolder(FileListViewHolder holder, int position) {

            AbstractFile file = mAbstractFiles.get(position);

            holder.getViewDataBinding().setVariable(BR.file, file);

            holder.refreshView(file);

            holder.getViewDataBinding().executePendingBindings();

        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return mAbstractFiles.size();
        }
    }

    private class FileListViewHolder extends BindingViewHolder {

        public FileListViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        void refreshView(AbstractFile file) {

            RemoteFileItemLayoutBinding binding = (RemoteFileItemLayoutBinding) getViewDataBinding();

            final Context context = binding.getRoot().getContext();

            binding.setFile(file);

            final RemoteFile abstractRemoteFile = (RemoteFile) file;

            binding.fileSize.setText(Formatter.formatFileSize(context, abstractRemoteFile.getSize()));

            FileItemViewModel fileItemViewModel = binding.getFileItemViewModel();

            if (fileItemViewModel == null)
                fileItemViewModel = new FileItemViewModel();

            fileItemViewModel.selectMode.set(false);

            fileItemViewModel.showFileIcon.set(true);

            binding.setFileItemViewModel(fileItemViewModel);

            binding.itemMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    List<BottomMenuItem> bottomMenuItems = new ArrayList<>();

                    BottomMenuItem menuItem;

                    FileTaskItem fileTaskItem = mFileTaskManager.getFileTaskItem(abstractRemoteFile.getName(), abstractRemoteFile.getFileHash());

                    boolean result = false;

                    if (fileTaskItem != null) {

                        TaskState taskState = fileTaskItem.getTaskState();

                        result = taskState != TaskState.START_DOWNLOAD_OR_UPLOAD && taskState != TaskState.PENDING && taskState != TaskState.DOWNLOADING_OR_UPLOADING;

                    }

                    if (FileUtil.checkFileExistInDownloadFolder(abstractRemoteFile.getName()) && result) {

                        bottomMenuItems.add(new BottomMenuItem(R.drawable.download, context.getString(R.string.re_download_the_item), new ReDownloadCommand(abstractRemoteFile)));

                        menuItem = new BottomMenuItem(abstractRemoteFile.getFileTypeResID(), context.getString(R.string.open_the_item), new OpenFileCommand(context, abstractRemoteFile.getName()));

                    } else {
                        AbstractCommand macroCommand = new MacroCommand();
                        AbstractCommand downloadFileCommand = new DownloadFileCommand(mFileTaskManager,
                                abstractRemoteFile.getFileHash(),abstractRemoteFile, mStationFileRepository,
                                mNetworkStateManager, currentUserUUID, groupUUID,stationID,cloudToken);
                        macroCommand.addCommand(downloadFileCommand);
                        macroCommand.addCommand(new ChangeToDownloadPageCommand(changeToDownloadPageCallback));

                        menuItem = new BottomMenuItem(R.drawable.download, context.getString(R.string.download_the_item), macroCommand);
                    }
                    bottomMenuItems.add(menuItem);

                    BottomMenuItem cancelMenuItem = new BottomMenuItem(R.drawable.close, context.getString(R.string.cancel), nullCommand);
                    bottomMenuItems.add(cancelMenuItem);

                    getBottomSheetDialog(bottomMenuItems).show();
                }
            });

            binding.remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (FileUtil.checkFileExistInDownloadFolder(abstractRemoteFile.getName())) {

                        if (!abstractRemoteFile.openAbstractRemoteFile(context, "")) {
                            Toast.makeText(context, context.getString(R.string.open_file_failed), Toast.LENGTH_SHORT).show();
                        }

                    } else {

                        if (FileUtil.checkFileIsVideo(abstractRemoteFile.getName())) {

                            PlayVideoActivity.startPlayVideoActivity(mActivity,
                                    new FileFromBoxDownloadParam(groupUUID, stationID,abstractRemoteFile.getFileHash(),cloudToken));

                            return;
                        }

                        checkWriteExternalStoragePermission(abstractRemoteFile, mActivity);
                    }

                }
            });

        }

    }

    private Dialog getBottomSheetDialog(List<BottomMenuItem> bottomMenuItems) {

        Dialog dialog = new BottomMenuDialogFactory(bottomMenuItems).createDialog(mActivity);

        for (BottomMenuItem bottomMenuItem : bottomMenuItems) {
            bottomMenuItem.setDialog(dialog);
        }

        return dialog;
    }


    private void checkWriteExternalStoragePermission(AbstractRemoteFile abstractRemoteFile, Activity activity) {

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.WRITE_EXTERNAL_STORAGE_REQUEST_CODE);

        } else {
            openFileWhenOnClick((RemoteFile) abstractRemoteFile, activity);
        }

    }

    private void openFileWhenOnClick(RemoteFile abstractRemoteFile, Activity activity) {

        FileTaskItem fileTaskItem = mFileTaskManager.getFileTaskItem(abstractRemoteFile.getName(), abstractRemoteFile.getFileHash());

        if (fileTaskItem != null &&
                (fileTaskItem.getTaskState().equals(TaskState.PENDING) || fileTaskItem.getTaskState().equals(TaskState.DOWNLOADING_OR_UPLOADING) || fileTaskItem.getTaskState().equals(TaskState.START_DOWNLOAD_OR_UPLOAD))) {

            AbstractCommand command = new ChangeToDownloadPageCommand(changeToDownloadPageCallback);
            command.execute();

            return;

        }

        mCurrentDownloadFileCommand = new DownloadFileCommand(mFileTaskManager, abstractRemoteFile.getFileHash(),abstractRemoteFile, mStationFileRepository,
                mNetworkStateManager, currentUserUUID, groupUUID,stationID,cloudToken);

        currentDownloadFileProgressDialog = new ProgressDialog(activity);

        currentDownloadFileProgressDialog.setTitle(activity.getString(R.string.downloading));

        currentDownloadFileProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        currentDownloadFileProgressDialog.setIndeterminate(false);

        currentDownloadFileProgressDialog.setProgressNumberFormat(null);

        currentDownloadFileProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getText(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (mCurrentDownloadFileCommand != null) {

                    mCurrentDownloadFileCommand.unExecute();

                    mCurrentDownloadFileCommand = null;

                    cancelDownload = true;

                    currentDownloadFileProgressDialog.dismiss();

                }

            }
        });

        currentDownloadFileProgressDialog.setMax(progressMax);

        currentDownloadFileProgressDialog.setCancelable(false);

        currentDownloadFileProgressDialog.show();

        mCurrentDownloadFileCommand.execute();
    }

    private class ReDownloadCommand extends AbstractCommand {

        private AbstractRemoteFile abstractRemoteFile;

        ReDownloadCommand(AbstractRemoteFile abstractRemoteFile) {
            this.abstractRemoteFile = abstractRemoteFile;
        }

        @Override
        public void execute() {

            File file = new File(FileUtil.getDownloadFileStoreFolderPath(), abstractRemoteFile.getName());

            if (file.exists()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

                builder.setMessage(mActivity.getString(R.string.need_download_and_cover_original_file, abstractRemoteFile.getName())).setPositiveButton(mActivity.getString(R.string.cover),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                FinishedTaskItemWrapper finishedTaskItemWrapper = new FinishedTaskItemWrapper(abstractRemoteFile.getUuid(), abstractRemoteFile.getName());

                                mStationFileRepository.deleteFileFinishedTaskItems(Collections.singleton(finishedTaskItemWrapper), currentUserUUID, new BaseOperateDataCallback<Void>() {
                                    @Override
                                    public void onSucceed(Void data, OperationResult result) {

                                        MacroCommand macroCommand = createDownloadFileMarcoCommand((RemoteFile) abstractRemoteFile);

                                        macroCommand.execute();
                                    }

                                    @Override
                                    public void onFail(OperationResult result) {

                                        Toast.makeText(mActivity, mActivity.getString(R.string.delete_original_file_fail, abstractRemoteFile.getName()), Toast.LENGTH_SHORT).show();

                                    }
                                });


                            }
                        }).setNegativeButton(mActivity.getString(R.string.cancel), null)
                        .setCancelable(false).create().show();

            } else {

                MacroCommand macroCommand = createDownloadFileMarcoCommand((RemoteFile) abstractRemoteFile);

                macroCommand.execute();
            }

        }

        @Override
        public void unExecute() {

        }
    }

    private MacroCommand createDownloadFileMarcoCommand(RemoteFile abstractRemoteFile) {

        MacroCommand macroCommand = new MacroCommand();

        AbstractCommand downloadFileCommand = new DownloadFileCommand(mFileTaskManager, abstractRemoteFile.getFileHash(),abstractRemoteFile, mStationFileRepository,
                mNetworkStateManager, currentUserUUID, groupUUID,stationID,cloudToken);
        macroCommand.addCommand(downloadFileCommand);
        macroCommand.addCommand(new ChangeToDownloadPageCommand(changeToDownloadPageCallback));

        return macroCommand;
    }

    public void handleEvent(TaskStateChangedEvent taskStateChangedEvent) {

        TaskState taskState = taskStateChangedEvent.getTaskState();

        switch (taskState) {
            case START_DOWNLOAD_OR_UPLOAD:
            case PENDING:
                break;
            case DOWNLOADING_OR_UPLOADING:

                if (mCurrentDownloadFileCommand != null) {

                    FileDownloadItem fileDownloadItem = mCurrentDownloadFileCommand.getFileDownloadItem();
                    currentDownloadFileProgressDialog.setProgress(fileDownloadItem.getCurrentProgress(progressMax));

                }

                break;
            case FINISHED:

                if (mCurrentDownloadFileCommand != null) {

                    FileDownloadItem fileDownloadItem = mCurrentDownloadFileCommand.getFileDownloadItem();

                    if (fileDownloadItem.getTaskState() == TaskState.FINISHED) {

                        mCurrentDownloadFileCommand = null;

                        currentDownloadFileProgressDialog.dismiss();

                        OpenFileCommand openFileCommand = new OpenFileCommand(mActivity, fileDownloadItem.getFileName());
                        openFileCommand.execute();

                    }


                }

                break;
            case ERROR:

                if (mCurrentDownloadFileCommand != null && mCurrentDownloadFileCommand.getFileDownloadItem().getTaskState() == TaskState.ERROR) {

                    mCurrentDownloadFileCommand.unExecute();

                    mCurrentDownloadFileCommand = null;

                    currentDownloadFileProgressDialog.dismiss();

                    if (cancelDownload)
                        cancelDownload = false;
                    else
                        Toast.makeText(mActivity, mActivity.getString(R.string.fail, mActivity.getString(R.string.download)), Toast.LENGTH_SHORT).show();

                }

                break;
            case NO_ENOUGH_SPACE:

                if (mCurrentDownloadFileCommand != null) {

                    mCurrentDownloadFileCommand.unExecute();

                    mCurrentDownloadFileCommand = null;

                    currentDownloadFileProgressDialog.dismiss();

                    Toast.makeText(mActivity, mActivity.getString(R.string.no_enough_space), Toast.LENGTH_SHORT).show();

                }

                break;
        }

    }


}
