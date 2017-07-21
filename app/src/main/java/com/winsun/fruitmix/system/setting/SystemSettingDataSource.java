package com.winsun.fruitmix.system.setting;

import android.content.Context;
import android.content.SharedPreferences;

import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/7/18.
 */

public class SystemSettingDataSource {

    private SharedPreferences sharedPreferences;

    private String showAutoUploadDialogKey = "showAutoUploadDialog";

    public SystemSettingDataSource(Context context) {

        sharedPreferences = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);

    }

    public String getCurrentUploadDeviceID() {

        return sharedPreferences.getString(Util.CURRENT_UPLOAD_DEVICE_ID, "");
    }

    public void setCurrentUploadDeviceID(String currentUploadDeviceID) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putString(Util.CURRENT_UPLOAD_DEVICE_ID, currentUploadDeviceID);
        editor.apply();
    }

    public boolean getAutoUploadOrNot() {

        return sharedPreferences.getBoolean(Util.AUTO_UPLOAD_OR_NOT, true);
    }

    public void setAutoUploadOrNot(boolean autoUploadOrNot) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putBoolean(Util.AUTO_UPLOAD_OR_NOT, autoUploadOrNot);
        editor.apply();
    }

    public void setShowAutoUploadDialog(boolean showAutoUploadDialog) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putBoolean(showAutoUploadDialogKey, showAutoUploadDialog);
        editor.apply();

    }

    public boolean getShowAutoUploadDialog() {

        return sharedPreferences.getBoolean(showAutoUploadDialogKey, false);

    }

}
