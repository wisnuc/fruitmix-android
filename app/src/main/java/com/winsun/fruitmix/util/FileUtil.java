package com.winsun.fruitmix.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.file.data.download.FileDownloadErrorState;
import com.winsun.fruitmix.file.data.download.FileDownloadFinishedState;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.download.FileDownloadingState;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2016/10/25.
 */

public class FileUtil {

    public static final String TAG = FileUtil.class.getSimpleName();

    private static final String DOWNLOAD_FOLDER_NAME = "winsuc";

    private static final String LOCAL_PHOTO_THUMBNAIL_FOLDER_NAME = "thumbnail_200";

    private static final String LOCAL_PHOTO_MINI_THUMBNAIL_FOLDER_NAME = "thumbnail_64";

    private static final String OLD_LOCAL_PHOTO_THUMBNAIL_FOLDER_NAME = "thumbnail";

    private static final String ORIGINAL_PHOTO_FOLDER_NAME = "originalPhoto";

    private static final String AUDIO_RECORD_FOLDER_NAME = "audioRecord";

    private static final String NO_MEDIA = ".nomedia";

    public static boolean checkExternalStorageState() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean checkExternalDirectoryForDownloadAvailableSizeEnough() {

        StatFs statFs = new StatFs(getExternalDirectoryPathForDownload());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            long availableBlocks = statFs.getAvailableBlocksLong();
            long blockSize = statFs.getBlockSizeLong();

            return availableBlocks * blockSize > 0;

        } else {
            long availableBlocks = statFs.getAvailableBlocks();
            long blockSize = statFs.getBlockSize();

            return availableBlocks * blockSize > 0;
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

    public static boolean createLocalPhotoMiniThumbnailFolder() {
        return createFolder(getLocalPhotoMiniThumbnailFolderPath());
    }

    public static boolean createLocalPhotoMiniThumbnailFolderNoMedia() {
        return createFolder(getLocalPhotoMiniThumbnailFolderPath() + File.separator + NO_MEDIA);
    }

    public static boolean createLocalPhotoThumbnailFolder() {
        return createFolder(getLocalPhotoThumbnailFolderPath());
    }

    public static boolean createLocalPhotoThumbnailFolderNoMedia() {
        return createFolder(getLocalPhotoThumbnailFolderPath() + File.separator + NO_MEDIA);
    }

    public static boolean createOriginalPhotoFolder() {
        return createFolder(getOriginalPhotoFolderPath());
    }

    public static boolean createOriginalPhotoFolderNoMedia() {
        return createFolder(getOriginalPhotoFolderPath() + File.separator + NO_MEDIA);
    }

    public static boolean createOldLocalPhotoThumbnailFolderNoMedia() {
        return createFolder(getOldLocalPhotoThumbnailFolderPath() + File.separator + NO_MEDIA);
    }

    public static boolean createAudioRecordFolder() {
        return createFolder(getAudioRecordFolderPath());
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

    public static String getLocalPhotoMiniThumbnailFolderPath() {
        return getDownloadFileStoreFolderPath() + LOCAL_PHOTO_MINI_THUMBNAIL_FOLDER_NAME;
    }

    public static String getLocalPhotoThumbnailFolderPath() {
        return getDownloadFileStoreFolderPath() + LOCAL_PHOTO_THUMBNAIL_FOLDER_NAME;
    }

    public static String getOldLocalPhotoThumbnailFolderPath() {
        return getDownloadFileStoreFolderPath() + OLD_LOCAL_PHOTO_THUMBNAIL_FOLDER_NAME;
    }

    public static String getOriginalPhotoFolderPath() {
        return getDownloadFileStoreFolderPath() + ORIGINAL_PHOTO_FOLDER_NAME;
    }

    public static String getAudioRecordFolderPath() {
        return getDownloadFileStoreFolderPath() + AUDIO_RECORD_FOLDER_NAME;
    }

    public static boolean writeBitmapToLocalPhotoMiniThumbnailFolder(Media media) {

        if (!media.getMiniThumbPath().isEmpty())
            return false;

        checkIfNoExistThenCreateDownloadFileStoreFolder();

        File miniThumbnailFolder = new File(getLocalPhotoMiniThumbnailFolderPath());

        if (!miniThumbnailFolder.exists())
            createLocalPhotoMiniThumbnailFolder();

        String thumb = media.getOriginalPhotoPath();

        if (media.getUuid().isEmpty()) {
            media.setUuid(Util.calcSHA256OfFile(thumb));
        }

        String miniThumbName = media.getUuid() + ".jpg";

        File file = new File(getLocalPhotoMiniThumbnailFolderPath(), miniThumbName);

        if (file.exists()) {

            Log.d(TAG, "writeBitmapToLocalPhotoMiniThumbnailFolder: exist mini thumb: " + file.getAbsolutePath());

            media.setMiniThumbPath(file.getAbsolutePath());
            return true;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();

        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(thumb, decodeOptions);

        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;

        decodeOptions.inJustDecodeBounds = false;
        decodeOptions.inSampleSize = findBestSampleSize(actualWidth, actualHeight, 64, 64);
        Bitmap bitmap = BitmapFactory.decodeFile(thumb, decodeOptions);

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();

            media.setMiniThumbPath(file.getAbsolutePath());

            Log.d(TAG, "writeBitmapToLocalPhotoMiniThumbnailFolder: media mini thumb:" + media.getMiniThumbPath());

            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

            boolean result = file.delete();

            Log.d(TAG, "writeBitmapToLocalPhotoMiniThumbnailFolder: io exception occur,delete file: " + result);

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

    private static void checkIfNoExistThenCreateDownloadFileStoreFolder() {
        File root = new File(getDownloadFileStoreFolderPath());

        if (!root.exists())
            createDownloadFileStoreFolder();
    }

    public static boolean writeBitmapToFolder(Bitmap bitmap, File file) {

        FileOutputStream outputStream = null;

        try {

            if (file.createNewFile() || file.isFile()) {

                outputStream = new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();

                Log.d(TAG, "writeBitmapToFolder fileName:" + file.getName());

                return true;

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

            boolean result = file.delete();

            Log.d(TAG, "writeBitmapToLocalPhotoThumbnailFolder: io exception occur,delete file: " + result);

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

    public static boolean generateLocalVideoMiniThumbnail(Video video) {

        checkIfNoExistThenCreateDownloadFileStoreFolder();

        File miniThumbnailFolder = new File(getLocalPhotoMiniThumbnailFolderPath());

        if (!miniThumbnailFolder.exists()) {
            createLocalPhotoMiniThumbnailFolder();
        }

        if (video.getMiniThumbPath().length() != 0)
            return false;

        String originalPhotoPath = video.getOriginalPhotoPath();

        if (video.getUuid().isEmpty()) {
            video.setUuid(Util.calcSHA256OfFile(originalPhotoPath));
        }

        String thumbName = video.getUuid() + ".jpg";

        File file = new File(getLocalPhotoMiniThumbnailFolderPath(), thumbName);

        if (file.exists()) {

            Log.d(TAG, "writeBitmapToLocalVideoMiniThumbnailFolder: exist mini thumb: " + file.getAbsolutePath());

            video.setMiniThumbPath(file.getAbsolutePath());
            return true;
        }

        Bitmap originalBitmap = ThumbnailUtils.createVideoThumbnail(originalPhotoPath, -1);

        Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(originalBitmap, 64, 64, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        boolean result = writeBitmapToFolder(thumbBitmap, file);

        if (result)
            video.setMiniThumbPath(file.getAbsolutePath());

        return result;

    }


    public static boolean generateLocalVideoThumbnail(Video video) {

        checkIfNoExistThenCreateDownloadFileStoreFolder();

        File thumbnailFolder = new File(getLocalPhotoThumbnailFolderPath());

        if (!thumbnailFolder.exists()) {
            createLocalPhotoThumbnailFolder();
        }

        if (video.getThumb().length() != 0)
            return false;

        String originalPhotoPath = video.getOriginalPhotoPath();

        if (video.getUuid().isEmpty()) {
            video.setUuid(Util.calcSHA256OfFile(originalPhotoPath));
        }

        String thumbName = video.getUuid() + ".jpg";

        File file = new File(getLocalPhotoThumbnailFolderPath(), thumbName);

        if (file.exists()) {

            Log.d(TAG, "writeBitmapToLocalVideoThumbnailFolder: exist thumb: " + file.getAbsolutePath());

            video.setThumb(file.getAbsolutePath());
            return true;
        }

        Bitmap originalBitmap = ThumbnailUtils.createVideoThumbnail(originalPhotoPath, -1);

        Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(originalBitmap, 200, 200, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        boolean result = writeBitmapToFolder(thumbBitmap, file);

        if (result)
            video.setThumb(file.getAbsolutePath());

        return result;

    }


    public static boolean writeBitmapToLocalPhotoThumbnailFolder(Media media) {

        checkIfNoExistThenCreateDownloadFileStoreFolder();

        File thumbnailFolder = new File(getLocalPhotoThumbnailFolderPath());

        if (!thumbnailFolder.exists()) {
            createLocalPhotoThumbnailFolder();
        }

        if (!media.getThumb().isEmpty())
            return false;

        String thumb = media.getOriginalPhotoPath();

        if (media.getUuid().isEmpty()) {
            media.setUuid(Util.calcSHA256OfFile(thumb));
        }

        String thumbName = media.getUuid() + ".jpg";

        File file = new File(getLocalPhotoThumbnailFolderPath(), thumbName);

        if (file.exists()) {

            Log.d(TAG, "writeBitmapToLocalPhotoThumbnailFolder: exist thumb: " + file.getAbsolutePath());

            media.setThumb(file.getAbsolutePath());
            return true;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();

        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(thumb, decodeOptions);

        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;

        decodeOptions.inJustDecodeBounds = false;
        decodeOptions.inSampleSize = findBestSampleSize(actualWidth, actualHeight, 200, 200);
        Bitmap bitmap = BitmapFactory.decodeFile(thumb, decodeOptions);

        if (bitmap == null)
            return false;

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();

            media.setThumb(file.getAbsolutePath());

            Log.d(TAG, "writeBitmapToLocalPhotoThumbnailFolder: media thumb:" + media.getThumb());

            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

            boolean result = file.delete();

            Log.d(TAG, "writeBitmapToLocalPhotoThumbnailFolder: io exception occur,delete file: " + result);

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

    public static boolean downloadMediaToOriginalPhotoFolder(ResponseBody responseBody, Media media) {

        checkIfNoExistThenCreateDownloadFileStoreFolder();

        File originalPhotoFolder = new File(getOriginalPhotoFolderPath());

        if (!originalPhotoFolder.exists())
            createOriginalPhotoFolder();

        if (responseBody == null)
            return false;

        File file = new File(getOriginalPhotoFolderPath(), media.getUuid() + ".jpg");

        if (file.exists()) {
            media.setOriginalPhotoPath(file.getAbsolutePath());
            return true;
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;

        byte[] fileBuffer = new byte[4096];

        try {
            if (file.createNewFile() || file.isFile()) {

                inputStream = responseBody.byteStream();

                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileBuffer);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileBuffer, 0, read);

                }

                outputStream.flush();

                media.setOriginalPhotoPath(file.getAbsolutePath());

                Log.d(TAG, "downloadMediaToOriginalPhotoFolder: original photo path: " + media.getOriginalPhotoPath());

                return true;

            }
        } catch (IOException e) {
            e.printStackTrace();

            boolean result = file.delete();

            Log.d(TAG, "downloadMediaToOriginalPhotoFolder: io exception occur,delete file: " + result);

        } finally {

            try {

                responseBody.close();

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

        return false;

    }

    public static boolean writeResponseBodyToFolder(ResponseBody responseBody, FileDownloadState fileDownloadState) {

        checkIfNoExistThenCreateDownloadFileStoreFolder();

        File downloadFolder = new File(getDownloadFileStoreFolderPath());

        if (!downloadFolder.exists())
            createDownloadFileStoreFolder();

        File downloadFile = new File(getDownloadFileStoreFolderPath(), fileDownloadState.getFileName());

        FileDownloadItem fileDownloadItem = fileDownloadState.getFileDownloadItem();

        FileDownloadState newFileDownloadState = new FileDownloadingState(fileDownloadItem);

        fileDownloadItem.setFileDownloadState(newFileDownloadState);

        InputStream inputStream = null;
        OutputStream outputStream = null;

        byte[] fileBuffer = new byte[4096];

        try {

            long contentLength = responseBody.contentLength();

            Log.d(TAG, "writeResponseBodyToFolder: contentLength: " + contentLength);

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

                    newFileDownloadState.setFileCurrentDownloadSize(fileDownloadedSize);

                    Log.d(TAG, "writeResponseBodyToFolder: fileDownloadedSize: " + fileDownloadedSize);

                    newFileDownloadState.notifyDownloadStateChanged();
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

            if (downloadFile.exists()) {

                boolean result = downloadFile.delete();

                Log.d(TAG, "writeResponseBodyToFolder: io exception occur,delete file: " + result);

            }

            return false;
        } finally {

            try {

                if (responseBody != null)
                    responseBody.close();

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

    public static void sendShareToOtherApp(Context context, List<String> filePaths) {
        ArrayList<Uri> uris = new ArrayList<>();

        List<String> types = new ArrayList<>();

        for (String filePath : filePaths) {

            File file = new File(filePath);

            Uri uri = Uri.fromFile(file);
            uris.add(uri);

            String type = getMIMEType(file);

            int index = type.indexOf("/");
            type = type.substring(0, index) + "/*";

            if (!types.contains(type))
                types.add(type);

        }

        StringBuilder builder = new StringBuilder();
        for (String type : types) {
            builder.append(type);
            builder.append(";");
        }

        String shareType = builder.toString();

        Log.d(TAG, "sendShareToOtherApp: shareType: " + shareType);

        Intent intent = new Intent();

        if (uris.size() == 1) {
            intent.setAction(Intent.ACTION_SEND);

            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));

        } else {
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);

            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }

        intent.setType(shareType);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_text)));

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

            formatFileSize = decimalFormat.format(fileSize / (float) 1024 / 1024 / 1024) + " GB";

        } else if (fileSize < 1024L * 1024L * 1024L * 1024L * 1024L) {

            formatFileSize = decimalFormat.format(fileSize / (float) 1024 / 1024 / 1024 / 1024) + " TB";

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

    public static boolean checkFileIsVideo(String fileName) {

        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex < 0)
            return false;

        String end = fileName.substring(dotIndex, fileName.length()).toLowerCase();

        if (end.isEmpty())
            return false;

        String type = "";
        for (String[] aMIME_MapTable : MIME_MapTable) {
            if (end.equals(aMIME_MapTable[0]))
                type = aMIME_MapTable[1];
        }

        return type.startsWith("video");

    }

    public static int getFileTypeResID(String fileName) {

        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex < 0)
            return R.drawable.file_icon;

        String end = fileName.substring(dotIndex, fileName.length()).toLowerCase();

        if (end.isEmpty())
            return R.drawable.file_icon;

        String type = "";
        for (String[] aMIME_MapTable : MIME_MapTable) {
            if (end.equals(aMIME_MapTable[0]))
                type = aMIME_MapTable[1];
        }

        if (end.startsWith(".xls"))
            return R.drawable.excel;
        else if (end.startsWith(".doc"))
            return R.drawable.word;
        else if (end.equals(".pdf"))
            return R.drawable.pdf;
        else if (end.startsWith(".ppt"))
            return R.drawable.power_point;
        else if (end.equals(".txt")) {
            return R.drawable.txt;
        } else if (type.startsWith("video")) {

            if (type.contains("mp4")) {
                return R.drawable.mp4;
            } else if (type.contains("quicktime")) {
                return R.drawable.mov;
            } else
                return R.drawable.video;

        } else if (type.startsWith("audio")) {
            return R.drawable.audio;
        } else if (type.contains("gif")) {
            return R.drawable.gif;
        } else if (type.contains("jpeg")) {
            return R.drawable.jpg;
        } else if (type.contains("png")) {
            return R.drawable.png;
        } else
            return R.drawable.file_icon;

    }

}
