package com.winsun.fruitmix.retrieve.file.from.other.app;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.HandleTorrentDialogViewBinding;
import com.winsun.fruitmix.file.data.model.FileTaskManager;
import com.winsun.fruitmix.file.data.upload.InjectUploadFileCase;
import com.winsun.fruitmix.file.view.FileDownloadActivity;
import com.winsun.fruitmix.network.InjectNetworkStateManager;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.torrent.view.TorrentDownloadManageActivity;
import com.winsun.fruitmix.util.FileUtil;

/**
 * Created by Administrator on 2017/12/21.
 */

public class RetrieveFilePresenter {

    public static final String TAG = RetrieveFilePresenter.class.getSimpleName();

    public void handleUploadFilePath(String uploadFilePath, Activity context) {

        Log.d(TAG, "handleUploadFilePath: uploadFilePath: " + uploadFilePath);

        SystemSettingDataSource mSystemSettingDataSource = InjectSystemSettingDataSource.provideSystemSettingDataSource(context);

        if (FileUtil.checkFileIsTorrent(uploadFilePath)) {

            int behavior = mSystemSettingDataSource.getOpenTorrentFileBehavior();

            if (behavior == SystemSettingDataSource.OPEN_TORRENT_FILE_BEHAVIOR_CREATE_DOWNLOAD_TASK) {
                startTorrentDownloadManageActivity(uploadFilePath,context);
            } else if (behavior == SystemSettingDataSource.OPEN_TORRENT_FILE_BEHAVIOR_UPLOAD_FILE) {
                startTaskManageActivity(uploadFilePath,context);
            } else
                showHandleTorrentDialog(mSystemSettingDataSource,context, uploadFilePath);

        } else {

            startTaskManageActivity(uploadFilePath,context);

        }

    }

    private void showHandleTorrentDialog(final SystemSettingDataSource systemSettingDataSource, final Activity context, final String uploadFilePath) {

        final HandleTorrentDialogViewBinding binding = HandleTorrentDialogViewBinding.inflate(LayoutInflater.from(context),
                null, false);

        new AlertDialog.Builder(context).setTitle(context.getString(R.string.upload_torrent_file_title))
                .setView(binding.getRoot())
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        boolean isAlways = binding.alwaysCheckbox.isChecked();

                        if (binding.createNewDownloadTask.isChecked()) {

                            if (isAlways)
                                systemSettingDataSource.setOpenTorrentFileDefaultBehavior(SystemSettingDataSource.OPEN_TORRENT_FILE_BEHAVIOR_CREATE_DOWNLOAD_TASK);

                            startTorrentDownloadManageActivity(uploadFilePath,context);

                        } else if (binding.uploadFile.isChecked()) {

                            if (isAlways)
                                systemSettingDataSource.setOpenTorrentFileDefaultBehavior(SystemSettingDataSource.OPEN_TORRENT_FILE_BEHAVIOR_UPLOAD_FILE);

                            startTaskManageActivity(uploadFilePath,context);

                        }


                    }
                }).create().show();

    }

    private void startTorrentDownloadManageActivity(String uploadFilePath, Activity activity) {

        startTorrentDownloadTask(uploadFilePath, activity);

        activity.finish();

    }

    private void startTorrentDownloadTask(String uploadFilePath, Context context) {

        Intent intent = new Intent(context, TorrentDownloadManageActivity.class);
        intent.putExtra(TorrentDownloadManageActivity.KEY_TORRENT_FILE_PATH, uploadFilePath);
        context.startActivity(intent);

    }

    private void startTaskManageActivity(String uploadFilePath,Activity activity) {

        Log.d(TAG, "startTaskManageActivity: uploadFilePath: " + uploadFilePath);

        startUploadFileTask(uploadFilePath, activity);

        activity.finish();

    }

    private void startUploadFileTask(String uploadFilePath, Context context) {

        Intent gotoTaskManageActivityIntent = new Intent(context, FileDownloadActivity.class);
        context.startActivity(gotoTaskManageActivityIntent);

        FileTaskManager fileTaskManager = FileTaskManager.getInstance();

        fileTaskManager.addFileUploadItem(uploadFilePath, InjectUploadFileCase.provideInstance(context),
                InjectNetworkStateManager.provideNetworkStateManager(context), true);

    }


}
