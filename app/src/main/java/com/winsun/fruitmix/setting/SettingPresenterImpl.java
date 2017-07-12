package com.winsun.fruitmix.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.SettingActivity;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.LocalCache;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017/6/22.
 */

public class SettingPresenterImpl implements SettingPresenter {

    private boolean mAutoUploadOrNot = false;

    private int mAlreadyUploadMediaCount = -1;

    private int mTotalLocalMediaCount = 0;

    private long mTotalCacheSize = 0;

    private SettingActivity.SettingViewModel settingViewModel;

    public SettingPresenterImpl(SettingActivity.SettingViewModel settingViewModel) {
        this.settingViewModel = settingViewModel;
    }

    @Override
    public void onCreate(Context context) {

        mAutoUploadOrNot = LocalCache.getAutoUploadOrNot(context);
        settingViewModel.autoUploadOrNot.set(mAutoUploadOrNot);

        calcAlreadyUploadMediaCountAndTotalCacheSize(context);

    }

    @Override
    public void clearCache(final Context context, final SettingActivity.SettingViewModel settingViewModel) {

        new AlertDialog.Builder(context).setMessage(context.getString(R.string.confirm_clear_cache))
                .setPositiveButton(context.getString(R.string.clear), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        FileUtil.clearAllCache(context);
                        dialog.dismiss();

                        settingViewModel.cacheSizeText.set(FileUtil.formatFileSize(FileUtil.getTotalCacheSize(context)));

                    }
                }).setNegativeButton(context.getString(R.string.cancel), null).create().show();

    }

    private void calcAlreadyUploadMediaCountAndTotalCacheSize(final Context context) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                int alreadyUploadMediaCount = 0;
                int totalUploadMediaCount = 0;

                for (Media media : LocalCache.LocalMediaMapKeyIsOriginalPhotoPath.values()) {

                    if (media.getUploadedDeviceIDs().contains(LocalCache.DeviceID)) {
                        alreadyUploadMediaCount++;
                    }

                    totalUploadMediaCount++;
                }

                mAlreadyUploadMediaCount = alreadyUploadMediaCount;
                mTotalLocalMediaCount = totalUploadMediaCount;

                mTotalCacheSize = FileUtil.getTotalCacheSize(context);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                settingViewModel.alreadyUploadMediaCountTextViewVisibility.set(true);
                settingViewModel.alreadyUploadMediaCountText.set(String.format(context.getString(R.string.already_upload_media_count_text), mAlreadyUploadMediaCount, mTotalLocalMediaCount));

                settingViewModel.cacheSizeText.set(FileUtil.formatFileSize(mTotalCacheSize));

            }
        }.execute();

    }

    @Override
    public void onDestroy(Context context) {
        boolean isChecked = settingViewModel.autoUploadOrNot.get();

        if (mAutoUploadOrNot != isChecked) {
            LocalCache.setAutoUploadOrNot(context, isChecked);

            if (isChecked) {
                LocalCache.setCurrentUploadDeviceID(context, LocalCache.DeviceID);
                EventBus.getDefault().post(new RequestEvent(OperationType.START_UPLOAD, null));
            } else {
                LocalCache.setCurrentUploadDeviceID(context, "");
                EventBus.getDefault().post(new RequestEvent(OperationType.STOP_UPLOAD, null));
            }
        }
    }
}
