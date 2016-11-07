package com.winsun.fruitmix.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.DecimalFormat;

import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2016/10/25.
 */

public class FileUtil {

    private static final String DOWNLOAD_FOLDER_NAME = "winsuc";

    public static boolean checkExternalStorageState() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    public static String getExternalStorageDirectoryPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    private static String getExternalDirectoryPathForDownload() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

    public static boolean createDownloadFileStoreFolder() {
        if (!checkExternalStorageState()) return false;
        File downloadFileStoreFolder = new File(getDownloadFileStoreFolderPath());

        return downloadFileStoreFolder.mkdirs() || downloadFileStoreFolder.isDirectory();
    }

    public static String getDownloadFileStoreFolderPath() {
        return getExternalDirectoryPathForDownload() + File.separator;
    }

    public static boolean writeResponseBodyToFolder(ResponseBody responseBody, String fileName) {

        File downloadFile = new File(getDownloadFileStoreFolderPath(), fileName);

        InputStream inputStream = null;
        OutputStream outputStream = null;

        byte[] fileBuffer = new byte[4096];

        try {

            if (downloadFile.createNewFile() || downloadFile.isFile()) {

                inputStream = responseBody.byteStream();

                outputStream = new FileOutputStream(downloadFile);

                while (true) {
                    int read = inputStream.read(fileBuffer);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileBuffer, 0, read);

                }

                outputStream.flush();

                return true;

            } else {

                return false;

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {

            try {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

    }

    public static void openFile(Context context, File file) throws Exception {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = getMIMEType(file);
        intent.setDataAndType(Uri.fromFile(file), type);

        context.startActivity(intent);
    }

    private static String getMIMEType(File file) {

        String type = "*/*";
        String fName = file.getName();
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end.equals("")) return type;
        for (String[] aMIME_MapTable : MIME_MapTable) {
            if (end.equals(aMIME_MapTable[0]))
                type = aMIME_MapTable[1];
        }
        return type;
    }

    private static String[][] MIME_MapTable = {
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };

    public static String formatFileSize(long fileSize) {

        String formatFileSize = "";

        DecimalFormat decimalFormat = new DecimalFormat("####.00");

        if (fileSize < 0) {

            formatFileSize = "0 B";

        } else if (fileSize < 1024L) {

            formatFileSize = fileSize + " B";

        } else if (fileSize < 1024L * 1024L) {

            formatFileSize = decimalFormat.format(fileSize / 1024) + " KB";

        } else if (fileSize < 1024L * 1024L * 1024L) {

            formatFileSize = decimalFormat.format(fileSize / 1024 / 1024) + " MB";

        } else if (fileSize < 1024L * 1024L * 1024L * 1024L) {

            formatFileSize = decimalFormat.format(fileSize / 1024 / 1024 / 1024 / 1024) + " GB";

        }

        return formatFileSize;
    }

}
