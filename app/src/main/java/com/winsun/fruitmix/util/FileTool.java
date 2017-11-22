package com.winsun.fruitmix.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2017/11/21.
 */

public class FileTool {

    public static final String TAG = FileTool.class.getSimpleName();

    private static final FileTool ourInstance = new FileTool();

    public static FileTool getInstance() {
        return ourInstance;
    }

    private FileTool() {
    }

    public boolean copyFileToDir(String srcFile, String destDir) {
        File fileDir = new File(destDir);

        if (!fileDir.exists()) {
            boolean result = fileDir.mkdir();
            if (result)
                return false;
        }

        String destFile = destDir + File.separator + new File(srcFile).getName();

        File file = new File(destFile);

        if (file.exists())
            return true;

        try {
            InputStream streamFrom = new FileInputStream(srcFile);
            OutputStream streamTo = new FileOutputStream(destFile);
            byte buffer[] = new byte[1024];
            int len;
            while ((len = streamFrom.read(buffer)) > 0) {
                streamTo.write(buffer, 0, len);
            }
            streamFrom.close();
            streamTo.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public void deleteFile(String filePath) {

        File file = new File(filePath);
        boolean result = file.delete();

        Log.d(TAG, "delete file path:" + filePath + " result: " + result);

    }


}
