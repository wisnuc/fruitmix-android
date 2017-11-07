package com.winsun.fruitmix.media.local.media;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.model.operationResult.OperationMediaDataChanged;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.MediaUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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

        List<String> currentAllMediaPathInSystemDB = new ArrayList<>();

        List<String> currentAllVideoPathInSystemDB = new ArrayList<>();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Calendar date = Calendar.getInstance();

        List<Media> medias = getAllMedias(currentLocalMediaOriginalPaths, currentAllMediaPathInSystemDB, df, date);

        List<Video> videos = getAllVideos(df, date, currentLocalMediaOriginalPaths, currentAllVideoPathInSystemDB);

        if (medias.isEmpty() && videos.isEmpty())
            callback.onSucceed(currentAllMediaPathInSystemDB, currentAllVideoPathInSystemDB, medias, videos, new OperationSuccess());
        else
            callback.onSucceed(currentAllMediaPathInSystemDB, currentAllVideoPathInSystemDB, medias, videos, new OperationMediaDataChanged());

    }

    private List<Media> getAllMedias(Collection<String> currentLocalMediaOriginalPaths, List<String> currentAllMediaPathInSystemDB, SimpleDateFormat df, Calendar date) {

        List<Media> medias = new ArrayList<>();

        Media media;
        File f;

        Cursor cursor;

        String queryData = MediaStore.Images.Media.DATA;
        String queryWidth = MediaStore.Images.Media.WIDTH;
        String queryHeight = MediaStore.Images.Media.HEIGHT;
        String queryOrientation = MediaStore.Images.Media.ORIENTATION;
        String queryLatitude = MediaStore.Images.Media.LATITUDE;
        String queryLongitude = MediaStore.Images.Media.LONGITUDE;
        String queryType = MediaStore.Images.Media.MIME_TYPE;

        String[] fields = {MediaStore.Images.Media._ID, queryHeight,
                queryWidth, queryData, queryOrientation,
                queryLatitude, queryLongitude, queryType};

        String miniThumbnailFolderPath = FileUtil.getLocalPhotoMiniThumbnailFolderPath();
        String oldThumbnailFolderPath = FileUtil.getOldLocalPhotoThumbnailFolderPath();
        String thumbnailFolderPath = FileUtil.getLocalPhotoThumbnailFolderPath();
        String originalPhotoFolderPath = FileUtil.getOriginalPhotoFolderPath();

        String selection = queryData + " not like ? and " + queryData + " not like ? and " + queryData + " not like ? and " + queryData + " not like ?";
        String[] selectionArgs = {miniThumbnailFolderPath + "%", oldThumbnailFolderPath + "%", thumbnailFolderPath + "%", originalPhotoFolderPath + "%"};

//        cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "='" + bucketName + "'", null, null);

        cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, selection, selectionArgs, null);

        if (cursor == null || !cursor.moveToFirst()) {
            return medias;
        }

        Log.i(TAG, "getAllMedias: cursor count: " + cursor.getCount());

        do {

            String originalPhotoPath = cursor.getString(cursor.getColumnIndexOrThrow(queryData));

            if (originalPhotoPath.contains(miniThumbnailFolderPath) || originalPhotoPath.contains(oldThumbnailFolderPath)
                    || originalPhotoPath.contains(thumbnailFolderPath)
                    || originalPhotoPath.contains(originalPhotoFolderPath)) {
                continue;
            }

            currentAllMediaPathInSystemDB.add(originalPhotoPath);

            if (currentLocalMediaOriginalPaths.contains(originalPhotoPath))
                continue;

            media = new Media();
            media.setOriginalPhotoPath(originalPhotoPath);
            media.setWidth(cursor.getString(cursor.getColumnIndexOrThrow(queryWidth)));
            media.setHeight(cursor.getString(cursor.getColumnIndexOrThrow(queryHeight)));

            f = new File(originalPhotoPath);
            date.setTimeInMillis(f.lastModified());
            media.setTime(df.format(date.getTime()));
            media.setSelected(false);
            media.setLoaded(false);

            int orientation = cursor.getInt(cursor.getColumnIndexOrThrow(queryOrientation));
            setMediaOrientationNumber(orientation, media);

            media.setLocal(true);
            media.setSharing(true);
            media.setUuid("");

            String longitude = cursor.getString(cursor.getColumnIndex(queryLongitude));
            String latitude = cursor.getString(cursor.getColumnIndex(queryLatitude));

            Log.d(TAG, "getAllMedias: originalPhotoPath: " + media.getOriginalPhotoPath() + " longitude: " + longitude + " latitude: " + latitude);

            media.setLongitude(longitude);
            media.setLatitude(latitude);

            String type = cursor.getString(cursor.getColumnIndex(queryType));

            media.setType(type);

            Log.d(TAG, "getAllMedias: media type:" + type);

            medias.add(media);

//            Log.i(TAG, "insert local media to map key is originalPhotoPath result:" + (mapResult != null ? "true" : "false"));

        }
        while (cursor.moveToNext());

        cursor.close();

        return medias;
    }

    private void setMediaOrientationNumber(int orientation, Media media) {

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


    private List<Video> getAllVideos(SimpleDateFormat df, Calendar date, Collection<String> currentLocalMediaOriginalPaths, List<String> currentAllVideoPathInSystemDB) {

        List<Video> videos = new ArrayList<>();

        String queryName = MediaStore.Video.Media.DISPLAY_NAME;
        String queryPath = MediaStore.Video.Media.DATA;
        String queryTitle = MediaStore.Video.Media.TITLE;
        String queryAlbum = MediaStore.Video.Media.ALBUM;
        String queryArtist = MediaStore.Video.Media.ARTIST;
        String queryTakeDate = MediaStore.Video.Media.DATE_TAKEN;
        String queryDescription = MediaStore.Video.Media.DESCRIPTION;
        String queryDuration = MediaStore.Video.Media.DURATION;
        String queryLanguage = MediaStore.Video.Media.LANGUAGE;
        String queryResolution = MediaStore.Video.Media.RESOLUTION;
        String querySize = MediaStore.Video.Media.SIZE;
        String queryType = MediaStore.Video.Media.MIME_TYPE;

        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{queryName, queryPath, queryTitle, queryAlbum, queryArtist, queryTakeDate, queryDescription, queryDuration,
                        queryLanguage, queryResolution, querySize, queryType}, null, null, null);

        if (cursor == null || !cursor.moveToFirst()) {
            return videos;
        }

        Log.d(TAG, "getAllVideos: cursor count: " + cursor.getCount());

        do {

            String path = cursor.getString(cursor.getColumnIndex(queryPath));

            if (!path.toLowerCase().contains("camera"))
                continue;

            currentAllVideoPathInSystemDB.add(path);

            if (currentLocalMediaOriginalPaths.contains(path))
                continue;

            Video video = new Video();

            video.setName(cursor.getString(cursor.getColumnIndex(queryName)));
            video.setOriginalPhotoPath(path);

            date.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(queryTakeDate)));
            video.setTime(df.format(date.getTime()));

            long duration = cursor.getLong(cursor.getColumnIndex(queryDuration));

            video.setDuration(duration);

            long size = cursor.getLong(cursor.getColumnIndex(querySize));

            video.setSize(size);

            video.setSelected(false);
            video.setLoaded(false);

            video.setLocal(true);
            video.setUuid("");

            String type = cursor.getString(cursor.getColumnIndex(queryType));

            video.setType(type);

            videos.add(video);

        } while (cursor.moveToNext());

        cursor.close();

        return videos;
    }


}
