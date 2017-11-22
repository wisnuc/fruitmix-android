package com.winsun.fruitmix.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.widget.CompoundButton;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.SettingActivity;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.upload.media.CheckMediaIsUploadStrategy;
import com.winsun.fruitmix.upload.media.UploadMediaCountChangeListener;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2017/6/22.
 */

public class SettingPresenterImpl implements SettingPresenter {

    private boolean mAutoUploadOrNot = false;

    private boolean mAutoUploadWhenConnectedWithMobileNetwork = false;

    private int mAlreadyUploadMediaCount = -1;

    private int mTotalLocalMediaCount = 0;

    private long mTotalCacheSize = 0;

    private BaseView baseView;

    private SettingActivity.SettingViewModel settingViewModel;

    private SystemSettingDataSource systemSettingDataSource;

    private MediaDataSourceRepository mediaDataSourceRepository;

    private CheckMediaIsUploadStrategy checkMediaIsUploadStrategy;

    private UploadMediaUseCase uploadMediaUseCase;

    private UploadMediaCountChangeListener uploadMediaCountChangeListener;

    private String currentUserUUID;

    private ThreadManager threadManager;

    public SettingPresenterImpl(BaseView baseView, SettingActivity.SettingViewModel settingViewModel, SystemSettingDataSource systemSettingDataSource,
                                MediaDataSourceRepository mediaDataSourceRepository, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy,
                                UploadMediaUseCase uploadMediaUseCase, String currentUserUUID, ThreadManager threadManager) {
        this.baseView = baseView;
        this.settingViewModel = settingViewModel;
        this.systemSettingDataSource = systemSettingDataSource;
        this.mediaDataSourceRepository = mediaDataSourceRepository;
        this.checkMediaIsUploadStrategy = checkMediaIsUploadStrategy;
        this.uploadMediaUseCase = uploadMediaUseCase;
        this.currentUserUUID = currentUserUUID;
        this.threadManager = threadManager;
    }

    @Override
    public void onCreate(final Context context) {

        mAutoUploadOrNot = systemSettingDataSource.getAutoUploadOrNot();
        settingViewModel.autoUploadOrNot.set(mAutoUploadOrNot);

        mAutoUploadWhenConnectedWithMobileNetwork = systemSettingDataSource.getOnlyAutoUploadWhenConnectedWithWifi();
        settingViewModel.onlyAutoUploadWhenConnectedWithWifi.set(mAutoUploadWhenConnectedWithMobileNetwork);

        uploadMediaCountChangeListener = new UploadMediaCountChangeListener() {

            @Override
            public void onStartGetUploadMediaCount() {

            }

            @Override
            public void onUploadMediaCountChanged(int uploadedMediaCount, int totalCount) {

                mAlreadyUploadMediaCount = uploadedMediaCount;
                mTotalLocalMediaCount = totalCount;

                handleUploadMedia();
            }

            @Override
            public void onGetUploadMediaCountFail(int httpErrorCode) {
                if (httpErrorCode != -1)
                    baseView.showCustomErrorCode(Util.CUSTOM_ERROR_CODE_HEAD + Util.CUSTOM_ERROR_CODE_GET_UPLOADED_MEDIA + httpErrorCode);
            }

            @Override
            public void onUploadMediaFail(int httpErrorCode) {
                if (httpErrorCode != -1)
                    baseView.showCustomErrorCode(Util.CUSTOM_ERROR_CODE_HEAD + Util.CUSTOM_ERROR_CODE_UPLOAD_MEDIA + httpErrorCode);
            }

            @Override
            public void onCreateFolderFail(int httpErrorCode) {

                if (httpErrorCode != -1)
                    baseView.showCustomErrorCode(Util.CUSTOM_ERROR_CODE_HEAD + Util.CUSTOM_ERROR_CODE_CREATE_FOLDER + httpErrorCode);
            }

            @Override
            public void onGetFolderFail(int httpErrorCode) {

                if (httpErrorCode != -1)
                    baseView.showCustomErrorCode(Util.CUSTOM_ERROR_CODE_HEAD + Util.CUSTOM_ERROR_CODE_GET_FOLDER + httpErrorCode);
            }
        };

        calcAlreadyUploadMediaCountAndTotalCacheSize(context);

    }

    @Override
    public void onResume() {

//        uploadMediaUseCase.registerUploadMediaCountChangeListener(uploadMediaCountChangeListener);
    }

    @Override
    public void onPause() {

//        uploadMediaUseCase.unregisterUploadMediaCountChangeListener(uploadMediaCountChangeListener);
    }

    @Override
    public void clearCache(final Context context, final SettingActivity.SettingViewModel settingViewModel) {

        if (mTotalCacheSize == 0)
            return;

        new AlertDialog.Builder(context).setMessage(context.getString(R.string.confirm_clear_cache))
                .setPositiveButton(context.getString(R.string.clear), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        FileUtil.clearAllCache(context);
                        dialog.dismiss();

                        mTotalCacheSize = FileUtil.getTotalCacheSize(context);

                        settingViewModel.cacheSizeText.set(FileUtil.formatFileSize(mTotalCacheSize));

                    }
                }).setNegativeButton(context.getString(R.string.cancel), null).create().show();

    }

    private void calcAlreadyUploadMediaCountAndTotalCacheSize(final Context context) {

        settingViewModel.cacheSizeText.set(context.getString(R.string.calculating));

        threadManager.runOnCacheThread(new Runnable() {

            @Override
            public void run() {

                mTotalCacheSize = FileUtil.getTotalCacheSize(context);

                threadManager.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        handleUploadMedia();
                    }
                });

            }

        });

    }

    private void handleUploadMedia() {

/*        settingViewModel.alreadyUploadMediaCountTextViewVisibility.set(true);
        settingViewModel.alreadyUploadMediaCountText.set(String.format(context.getString(R.string.already_upload_media_count_text), mAlreadyUploadMediaCount, mTotalLocalMediaCount));*/

        settingViewModel.cacheSizeText.set(FileUtil.formatFileSize(mTotalCacheSize));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {

            case R.id.auto_upload_photos_switch:
                handleAutoUploadCheckedChange(isChecked);
                break;
            case R.id.mobile_network_upload_switch:
                handleAutoUploadWhenConnectedWithMobileNetwork(isChecked);
                break;

        }

    }

    private void handleAutoUploadCheckedChange(boolean isChecked) {
        if (mAutoUploadOrNot != isChecked) {

            mAutoUploadOrNot = isChecked;

            systemSettingDataSource.setAutoUploadOrNot(isChecked);

            if (isChecked) {
                systemSettingDataSource.setCurrentUploadUserUUID(currentUserUUID);
                EventBus.getDefault().post(new RequestEvent(OperationType.START_UPLOAD, null));

            } else {
                systemSettingDataSource.setCurrentUploadUserUUID("");
                EventBus.getDefault().post(new RequestEvent(OperationType.STOP_UPLOAD, null));
            }
        }
    }

    private void handleAutoUploadWhenConnectedWithMobileNetwork(boolean isChecked) {

        if (mAutoUploadWhenConnectedWithMobileNetwork != isChecked) {

            mAutoUploadWhenConnectedWithMobileNetwork = isChecked;

            systemSettingDataSource.setOnlyAutoUploadWhenConnectedWithWifi(isChecked);

            if (!isChecked) {

                EventBus.getDefault().post(new RequestEvent(OperationType.START_UPLOAD, null));

            }

        }

    }


    @Override
    public void onDestroy(Context context) {

        baseView = null;

    }
}
