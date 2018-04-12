package com.winsun.fruitmix.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.user.User;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2016/4/22.
 */
public class LocalCache {

    private static final String TAG = LocalCache.class.getSimpleName();

    public static ConcurrentMap<String, User> RemoteUserMapKeyIsUUID = null;
    public static ConcurrentMap<String, Media> RemoteMediaMapKeyIsUUID = null;
    public static ConcurrentMap<String, Media> LocalMediaMapKeyIsOriginalPhotoPath = null;
    public static ConcurrentMap<String, AbstractRemoteFile> RemoteFileMapKeyIsUUID = null;
    public static List<AbstractRemoteFile> RemoteFileShareList = null;

    public static String DeviceID = null;

    public static String currentEquipmentName;

    public static List<LoggedInUser> LocalLoggedInUsers = null;

    //optimize get media from db, modify send media info mode: use static list instead of put it into bundle

    // optimize photo list view refresh view when data is too large

    public static void CleanAll(final Context context) {

        DeviceID = null;

        ExecutorServiceInstance instance = ExecutorServiceInstance.SINGLE_INSTANCE;
        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {
                DBUtils dbUtils = DBUtils.getInstance(context);

                dbUtils.deleteAllRemoteUser();
                dbUtils.deleteAllRemoteMedia();
            }
        });

    }

    public static void initCurrentEquipmentName() {

        if (LocalLoggedInUsers == null || FNAS.userUUID == null)
            return;

        for (LoggedInUser loggedInUser : LocalLoggedInUsers) {
            if (loggedInUser.getUser().getUuid().equals(FNAS.userUUID)) {

                currentEquipmentName = loggedInUser.getEquipmentName();
            }
        }

    }

    public static boolean Init() {

        Log.d(TAG, "Init: ");

        if (RemoteUserMapKeyIsUUID == null)
            RemoteUserMapKeyIsUUID = new ConcurrentHashMap<>();
        else
            RemoteUserMapKeyIsUUID.clear();

        Log.d(TAG, "Init: RemoteUserMapKeyIsUUID == null: " + (RemoteUserMapKeyIsUUID == null ? "true" : "false"));

        if (RemoteMediaMapKeyIsUUID == null)
            RemoteMediaMapKeyIsUUID = new ConcurrentHashMap<>();
        else
            RemoteMediaMapKeyIsUUID.clear();

        if (RemoteFileMapKeyIsUUID == null)
            RemoteFileMapKeyIsUUID = new ConcurrentHashMap<>();
        else
            RemoteFileMapKeyIsUUID.clear();

        if (RemoteFileShareList == null)
            RemoteFileShareList = new ArrayList<>();
        else
            RemoteFileShareList.clear();

        if (LocalMediaMapKeyIsOriginalPhotoPath == null)
            LocalMediaMapKeyIsOriginalPhotoPath = new ConcurrentHashMap<>();

        Log.d(TAG, "Init: init LocalMediaMapKeyIsOriginalPhotoPath ");

        if (LocalLoggedInUsers == null)
            LocalLoggedInUsers = new ArrayList<>();
        else
            LocalLoggedInUsers.clear();

        return true;
    }

    public static ConcurrentMap<String, User> BuildRemoteUserMapKeyIsUUID(List<User> users) {

        ConcurrentMap<String, User> userConcurrentMap = new ConcurrentHashMap<>(users.size());
        for (User user : users) {
            userConcurrentMap.put(user.getUuid(), user);
        }
        return userConcurrentMap;
    }

    public static ConcurrentMap<String, Media> BuildMediaMapKeyIsUUID(Collection<Media> medias) {

        ConcurrentMap<String, Media> mediaConcurrentMap = new ConcurrentHashMap<>(medias.size());
        for (Media media : medias) {
            mediaConcurrentMap.put(media.getUuid(), media);
        }
        return mediaConcurrentMap;
    }

    public static <T extends Media >ConcurrentMap<String, T> BuildMediaMapKeyIsThumb(List<T> medias) {

        ConcurrentMap<String, T> mediaConcurrentMap = new ConcurrentHashMap<>(medias.size());
        for (T media : medias) {
            mediaConcurrentMap.put(media.getOriginalPhotoPath(), media);
        }
        return mediaConcurrentMap;
    }

    public static Media findMediaInLocalMediaMap(String key) {

        Collection<Media> collection = LocalMediaMapKeyIsOriginalPhotoPath.values();

        for (Media media : collection) {
            if (media.getUuid().equals(key) || media.getOriginalPhotoPath().equals(key))
                return media;
        }

        return null;
    }

    public static List<Media> PhotoList(Context context, String bucketName) {
        ContentResolver cr;
        String[] fields = {MediaStore.Images.Media._ID, MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION,
                MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE};
        Cursor cursor;
        List<Media> mediaList;
        Media media;
        File f;
        SimpleDateFormat df;
        Calendar date;

        String data = MediaStore.Images.Media.DATA;

        String thumbnailFolderPath = FileUtil.getLocalPhotoMiniThumbnailFolderPath();
        String oldThumbnailFolderPath = FileUtil.getOldLocalPhotoThumbnailFolderPath();
        String thumbnailFolder200Path = FileUtil.getLocalPhotoThumbnailFolderPath();
        String originalFolderPath = FileUtil.getOriginalPhotoFolderPath();

        String selection = data + " not like ? and " + data + " not like ? and " + data + " not like ? and " + data + " not like ?";
        String[] selectionArgs = {thumbnailFolderPath + "%", oldThumbnailFolderPath + "%", thumbnailFolder200Path + "%", originalFolderPath + "%"};

        cr = context.getContentResolver();
//        cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "='" + bucketName + "'", null, null);

        cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, selection, selectionArgs, null);

        mediaList = new ArrayList<>();
        if (cursor == null || !cursor.moveToFirst()) return mediaList;

        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = Calendar.getInstance();

        Log.i(TAG, "PhotoList: cursor count: " + cursor.getCount());

        do {

            String originalPhotoPath = cursor.getString(cursor.getColumnIndexOrThrow(data));

            if (LocalMediaMapKeyIsOriginalPhotoPath == null)
                break;

            if (LocalCache.LocalMediaMapKeyIsOriginalPhotoPath.containsKey(originalPhotoPath)) {
                continue;
            }

            if (originalPhotoPath.contains(FileUtil.getLocalPhotoMiniThumbnailFolderPath())) {
                continue;
            }

            if (originalPhotoPath.contains(FileUtil.getOldLocalPhotoThumbnailFolderPath())) {
                continue;
            }

            if (originalPhotoPath.contains(FileUtil.getLocalPhotoThumbnailFolderPath())) {
                continue;
            }

            if (originalPhotoPath.contains(FileUtil.getOriginalPhotoFolderPath()))
                continue;

            media = new Media();
            media.setOriginalPhotoPath(originalPhotoPath);
            media.setWidth(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)));
            media.setHeight(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)));

            f = new File(originalPhotoPath);
            date.setTimeInMillis(f.lastModified());
            media.setFormattedTime(df.format(date.getTime()));

            int orientation = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION));

            switch (orientation) {
                case 0:
                    media.setOrientationNumber(1);
                    break;
                case 90:
                    media.setOrientationNumber(6);
                    break;
                case 180:
                    media.setOrientationNumber(4);
                    break;
                case 270:
                    media.setOrientationNumber(3);
                    break;
                default:
                    media.setOrientationNumber(1);
            }

            media.setLocal(true);
            media.setSharing(true);
            media.setUuid("");

            String longitude = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));
            String latitude = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));

            Log.d(TAG, "PhotoList: originalPhotoPath: " + media.getOriginalPhotoPath() + " longitude: " + longitude + " latitude: " + latitude);

            media.setLongitude(longitude);
            media.setLatitude(latitude);

            mediaList.add(media);

            Media mapResult = LocalCache.LocalMediaMapKeyIsOriginalPhotoPath.put(media.getOriginalPhotoPath(), media);

//            Log.i(TAG, "insert local media to map key is originalPhotoPath result:" + (mapResult != null ? "true" : "false"));

        }
        while (cursor.moveToNext());

        cursor.close();

        return mediaList;
    }

    private static List<Map<String, String>> getAllLocalMedia() {

        if (!FileUtil.checkExternalStorageState()) {
            return Collections.emptyList();
        }

        File file = new File(FileUtil.getExternalStorageDirectoryPath());

        SimpleDateFormat df;

        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        checkFile(file, df);

        return fileList;
    }

    private static List<Map<String, String>> fileList = new ArrayList<>();

    private static void checkFile(File file, SimpleDateFormat format) {

        if (file.isDirectory()) {

            File[] files = file.listFiles();

            if (files != null) {

                for (File f : files) {

                    checkFile(f, format);

                }

            }

        } else if (file.isFile()) {

            int dot = file.getName().lastIndexOf(".");

            if (dot > -1 && dot < file.getName().length()) {

                String extriName = file.getName().substring(dot, file.getName().length());

                if (extriName.equals(".gif")) {

                    Map<String, String> map = new HashMap<>();

                    map.put("thumb", file.getAbsolutePath());
                    map.put("width", "200");
                    map.put("height", "200");

                    Calendar date = Calendar.getInstance();
                    date.setTimeInMillis(file.lastModified());

                    map.put("lastModified", format.format(date.getTime()));

                    fileList.add(map);
                }

            }

        }

    }





}
