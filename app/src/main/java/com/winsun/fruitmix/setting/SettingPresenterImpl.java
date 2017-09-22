package com.winsun.fruitmix.setting;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.SettingActivity;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.upload.media.CheckMediaIsUploadStrategy;
import com.winsun.fruitmix.upload.media.UploadMediaCountChangeListener;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.AbstractCollection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/6/22.
 */

public class SettingPresenterImpl implements SettingPresenter {

    private boolean mAutoUploadOrNot = false;

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

    public SettingPresenterImpl(BaseView baseView, SettingActivity.SettingViewModel settingViewModel, SystemSettingDataSource systemSettingDataSource,
                                MediaDataSourceRepository mediaDataSourceRepository, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy,
                                UploadMediaUseCase uploadMediaUseCase, String currentUserUUID) {
        this.baseView = baseView;
        this.settingViewModel = settingViewModel;
        this.systemSettingDataSource = systemSettingDataSource;
        this.mediaDataSourceRepository = mediaDataSourceRepository;
        this.checkMediaIsUploadStrategy = checkMediaIsUploadStrategy;
        this.uploadMediaUseCase = uploadMediaUseCase;
        this.currentUserUUID = currentUserUUID;
    }

    @Override
    public void onCreate(final Context context) {

        mAutoUploadOrNot = systemSettingDataSource.getAutoUploadOrNot();
        settingViewModel.autoUploadOrNot.set(mAutoUploadOrNot);

        calcAlreadyUploadMediaCountAndTotalCacheSize(context);

        uploadMediaCountChangeListener = new UploadMediaCountChangeListener() {

            @Override
            public void onStartGetUploadMediaCount() {

            }

            @Override
            public void onUploadMediaCountChanged(int uploadedMediaCount, int totalCount) {

                mAlreadyUploadMediaCount = uploadedMediaCount;
                mTotalLocalMediaCount = totalCount;

                handleUploadMedia(context);
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

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

/*                int alreadyUploadMediaCount = 0;
                int totalUploadMediaCount = 0;

                alreadyUploadMediaCount = uploadMediaUseCase.getAlreadyUploadedMediaCount();
                totalUploadMediaCount = uploadMediaUseCase.getLocalMedias().size();

                mAlreadyUploadMediaCount = alreadyUploadMediaCount;
                mTotalLocalMediaCount = totalUploadMediaCount;*/

                mTotalCacheSize = FileUtil.getTotalCacheSize(context);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                handleUploadMedia(context);

            }
        }.execute();

    }

    private void handleUploadMedia(Context context) {

/*        settingViewModel.alreadyUploadMediaCountTextViewVisibility.set(true);
        settingViewModel.alreadyUploadMediaCountText.set(String.format(context.getString(R.string.already_upload_media_count_text), mAlreadyUploadMediaCount, mTotalLocalMediaCount));*/

        settingViewModel.cacheSizeText.set(FileUtil.formatFileSize(mTotalCacheSize));
    }

    @Override
    public void onCheckedChanged(boolean isChecked) {

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

    @Override
    public void onDestroy(Context context) {

        baseView = null;

    }
}
