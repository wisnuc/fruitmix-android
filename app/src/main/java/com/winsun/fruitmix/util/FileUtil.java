package com.winsun.fruitmix.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.winsun.fruitmix.fileModule.download.FileDownloadErrorState;
import com.winsun.fruitmix.fileModule.download.FileDownloadFinishedState;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadState;
import com.winsun.fruitmix.mediaModule.model.Media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2016/10/25.
 */

public class FileUtil {

    public static final String TAG = FileUtil.class.getSimpleName();

    private static final String DOWNLOAD_FOLDER_NAME = "winsuc";

    private static final String LOCAL_PHOTO_THUMBNAIL_FOLDER_NAME = "thumbnail";

    public static boolean checkExternalStorageState() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean checkExternalDirectoryForDownloadAvailableSizeEnough() {

        StatFs statFs = new StatFs(getExternalDirectoryPathForDownload());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            long availableBlocks = statFs.getAvailableBlocksLong();
            long blockSize = statFs.getBlockSizeLong();

            return availableBlocks * blockSize > 100 * 1024 * 1024;

        } else {
            int availableBlocks = statFs.getAvailableBlocks();
            int blockSize = statFs.getBlockSize();

            return availableBlocks * blockSize > 100 * 1024 * 1024;
        }
    }

    public static String getExternalStorageDirectoryPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    private static String getExternalDirectoryPathForDownload() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

    public static boolean createDownloadFileStoreFolder() {
        return createFolder(getDownloadFileStoreFolderPath());
    }

    public static boolean createLocalPhotoThumbnailFolder() {
        return createFolder(getLocalPhotoThumbnailFolderPath());
    }

    private static boolean createFolder(String path) {
        if (!checkExternalStorageState()) {
            Log.i(TAG, "create folder: External storage not mounted");
            return false;
        }
        File folder = new File(path);

        return folder.mkdirs() || folder.isDirectory();
    }


    public static String getDownloadFileStoreFolderPath() {
        return getExternalDirectoryPathForDownload() + File.separator + DOWNLOAD_FOLDER_NAME + File.separator;
    }

    public static String getLocalPhotoThumbnailFolderPath() {
        return getExternalDirectoryPathForDownload() + File.separator + DOWNLOAD_FOLDER_NAME + File.separator + LOCAL_PHOTO_THUMBNAIL_FOLDER_NAME;
    }

    public static boolean writeBitmapToLocalPhotoThumbnailFolder(Media media) {

        if (!media.getMiniThumb().isEmpty())
            return false;

        String thumb = media.getThumb();

        String miniThumbName = media.getUuid() + ".jpg";

        File file = new File(getLocalPhotoThumbnailFolderPath(), miniThumbName);

        if (file.exists()) {
            media.setMiniThumb(file.getAbsolutePath());
            return true;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();

        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(thumb, decodeOptions);

        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;

        decodeOptions.inJustDecodeBounds = false;
        decodeOptions.inSampleSize = findBestSampleSize(actualWidth, actualHeight, 32, 32);
        Bitmap bitmap = BitmapFactory.decodeFile(thumb, decodeOptions);

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();

            media.setMiniThumb(file.getAbsolutePath());

            Log.d(TAG, "writeBitmapToLocalPhotoThumbnailFolder: media mini thumb:" + media.getMiniThumb());

            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            bitmap = null;

            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return false;

    }

    private static int findBestSampleSize(
            int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }

        return (int) n;
    }

    public static boolean writeResponseBodyToFolder(ResponseBody responseBody, FileDownloadState fileDownloadState) {

        File downloadFile = new File(getDownloadFileStoreFolderPath(), fileDownloadState.getFileName());

        FileDownloadItem fileDownloadItem = fileDownloadState.getFileDownloadItem();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        byte[] fileBuffer = new byte[4096];

        try {

            long contentLength = responseBody.contentLength();

            if (contentLength != 0) {
                fileDownloadState.setFileSize(contentLength);
            }

            long fileDownloadedSize = 0;

            if (downloadFile.createNewFile() || downloadFile.isFile()) {

                inputStream = responseBody.byteStream();

                outputStream = new FileOutputStream(downloadFile);

                while (true) {
                    int read = inputStream.read(fileBuffer);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileBuffer, 0, read);

                    fileDownloadedSize += read;

                    fileDownloadState.setFileCurrentDownloadSize(fileDownloadedSize);
                    fileDownloadState.notifyDownloadStateChanged();
                }

                outputStream.flush();

                fileDownloadItem.setFileDownloadState(new FileDownloadFinishedState(fileDownloadItem));

                return true;

            } else {

                fileDownloadItem.setFileDownloadState(new FileDownloadErrorState(fileDownloadItem));

                return false;

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();

            fileDownloadItem.setFileDownloadState(new FileDownloadErrorState(fileDownloadItem));

            return false;
        } catch (IOException e) {
            e.printStackTrace();

            fileDownloadItem.setFileDownloadState(new FileDownloadErrorState(fileDownloadItem));

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

    public static boolean openAbstractRemoteFile(Context context, String fileName) {

        File file = new File(FileUtil.getDownloadFileStoreFolderPath(), fileName);

        try {
            FileUtil.openFile(context, file);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private static void openFile(Context context, File file) throws Exception {
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

            formatFileSize = decimalFormat.format(fileSize / (float) 1024) + " KB";

        } else if (fileSize < 1024L * 1024L * 1024L) {

            formatFileSize = decimalFormat.format(fileSize / (float) 1024 / 1024) + " MB";

        } else if (fileSize < 1024L * 1024L * 1024L * 1024L) {

            formatFileSize = decimalFormat.format(fileSize / (float) 1024 / 1024 / 1024 / 1024) + " GB";

        }

        return formatFileSize;
    }

    public static long getTotalCacheSize(Context context) {
        long cacheSize = getFolderSize(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            cacheSize += getFolderSize(context.getExternalCacheDir());
        }
        return cacheSize;
    }

    private static long getFolderSize(File file) {
        long size = 0;

        File[] fileList = file.listFiles();
        int fileListLength;
        if (fileList != null) {
            fileListLength = fileList.length;
            for (int i = 0; i < fileListLength; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        }

        return size;
    }

    public static void clearAllCache(Context context) {
        deleteDir(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteDir(context.getExternalCacheDir());
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            int length;
            if (children != null) {
                length = children.length;
                for (int i = 0; i < length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }

        }
        return dir == null || dir.delete();
    }

}
