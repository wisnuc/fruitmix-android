package com.winsun.fruitmix.util;

import android.content.Context;
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

        return copyFileToDir(srcFile, new File(srcFile).getName(), destDir);

    }

    public boolean copyFileToDir(String srcFile, String newFileName, String destDir) {
        File fileDir = new File(destDir);

        if (!fileDir.exists()) {
            boolean result = fileDir.mkdir();
            if (result)
                return false;
        }

        String destFile = destDir + File.separator + newFileName;

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

    public boolean deleteFile(String filePath) {

        File file = new File(filePath);
        boolean result = file.delete();

        Log.d(TAG, "delete file path:" + filePath + " result: " + result);

        return result;

    }

    public boolean deleteDir(String folderPath) {
        return FileUtil.deleteDir(new File(folderPath));
    }

    public String getTemporaryUploadFolderPath(String temporaryDataFolderPath, String currentUserUUID) {

        String temporaryUserFolderPath = temporaryDataFolderPath + File.separator + currentUserUUID;

        if (FileUtil.createFolderIfNotExist(temporaryDataFolderPath)) {

            String temporaryUploadFolderPath = temporaryUserFolderPath + File.separator + "upload";

            if (FileUtil.createFolderIfNotExist(temporaryUploadFolderPath))
                return temporaryUploadFolderPath;
            else
                return "";

        } else
            return "";

    }

    public boolean checkTemporaryUploadFolderNotEmpty(Context context, String currentUserUUID) {

        File file = new File(getTemporaryUploadFolderPath(FileUtil.getTemporaryDataFolderParentFolderPath(context), currentUserUUID));

        return file.exists() && file.list().length > 0;

    }


}
