package com.winsun.fruitmix.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2016/10/25.
 */

public class FileUtil {

    public static boolean checkExternalStorageState(){
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    public static String getExternalStorageDirectoryPath(){
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }


}
