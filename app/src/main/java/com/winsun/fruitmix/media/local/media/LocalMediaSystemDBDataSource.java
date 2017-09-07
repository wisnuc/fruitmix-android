package com.winsun.fruitmix.media.local.media;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationMediaDataChanged;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/18.
 */

public class LocalMediaSystemDBDataSource {

    public static final String TAG = LocalMediaSystemDBDataSource.class.getSimpleName();

    private ContentResolver contentResolver;

    private static LocalMediaSystemDBDataSource instance;

    public static LocalMediaSystemDBDataSource getInstance(Context context) {

        if (instance == null)
            instance = new LocalMediaSystemDBDataSource(context);

        return instance;
    }

    private LocalMediaSystemDBDataSource(Context context) {

        contentResolver = context.getContentResolver();

    }

    public void getMedia(Collection<String> currentLocalMediaOriginalPaths, MediaInSystemDBLoadCallback callback) {

        ContentResolver cr = contentResolver;

        Cursor cursor;

        List<String> currentAllMediaPathInSystemDB = new ArrayList<>();

        List<Media> newMediaList = new ArrayList<>();
        Media media;
        File f;
        SimpleDateFormat df;
        Calendar date;

        String[] fields = {MediaStore.Images.Media._ID, MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION,
                MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE};

        String data = MediaStore.Images.Media.DATA;

        String thumbnailFolderPath = FileUtil.getLocalPhotoThumbnailFolderPath();
        String oldThumbnailFolderPath = FileUtil.getOldLocalPhotoThumbnailFolderPath();
        String thumbnailFolder200Path = FileUtil.getFolderPathForLocalPhotoThumbnailFolderName200();
        String originalPhotoFolderPath = FileUtil.getOriginalPhotoFolderPath();

        String selection = data + " not like ? and " + data + " not like ? and " + data + " not like ? and " + data + " not like ?";
        String[] selectionArgs = {thumbnailFolderPath + "%", oldThumbnailFolderPath + "%", thumbnailFolder200Path + "%", originalPhotoFolderPath + "%"};

//        cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "='" + bucketName + "'", null, null);

        cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, selection, selectionArgs, null);

        if (cursor == null || !cursor.moveToFirst()) {
            callback.onSucceed(currentAllMediaPathInSystemDB, newMediaList, new OperationSuccess());
            return;
        }
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = Calendar.getInstance();

        Log.i(TAG, "PhotoList: cursor count: " + cursor.getCount());

        do {

            String originalPhotoPath = cursor.getString(cursor.getColumnIndexOrThrow(data));

            if (originalPhotoPath.contains(thumbnailFolderPath) || originalPhotoPath.contains(oldThumbnailFolderPath)
                    || originalPhotoPath.contains(thumbnailFolder200Path)
                    || originalPhotoPath.contains(originalPhotoFolderPath)) {
                continue;
            }

            currentAllMediaPathInSystemDB.add(originalPhotoPath);

            if (currentLocalMediaOriginalPaths.contains(originalPhotoPath))
                continue;

            media = new Media();
            media.setOriginalPhotoPath(originalPhotoPath);
            media.setWidth(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)));
            media.setHeight(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)));

            f = new File(originalPhotoPath);
            date.setTimeInMillis(f.lastModified());
            media.setTime(df.format(date.getTime()));
            media.setSelected(false);
            media.setLoaded(false);

            setMediaOrientationNumber(cursor, media);

            media.setLocal(true);
            media.setSharing(true);
            media.setUuid("");

            String longitude = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));
            String latitude = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));

            Log.d(TAG, "PhotoList: originalPhotoPath: " + media.getOriginalPhotoPath() + " longitude: " + longitude + " latitude: " + latitude);

            media.setLongitude(longitude);
            media.setLatitude(latitude);

            newMediaList.add(media);

//            Log.i(TAG, "insert local media to map key is originalPhotoPath result:" + (mapResult != null ? "true" : "false"));

        }
        while (cursor.moveToNext());

        cursor.close();

        if (newMediaList.isEmpty())
            callback.onSucceed(currentAllMediaPathInSystemDB, newMediaList, new OperationSuccess());
        else
            callback.onSucceed(currentAllMediaPathInSystemDB, newMediaList, new OperationMediaDataChanged());

    }

    private void setMediaOrientationNumber(Cursor cursor, Media media) {
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
    }

}
