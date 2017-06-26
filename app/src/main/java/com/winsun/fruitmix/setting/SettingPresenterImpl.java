package com.winsun.fruitmix.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.SettingActivity;
import com.winsun.fruitmix.util.FileUtil;

/**
 * Created by Administrator on 2017/6/22.
 */

public class SettingPresenterImpl implements SettingPresenter {

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



}
