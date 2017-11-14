package com.winsun.fruitmix.parser;

import android.database.Cursor;
import android.util.Log;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.util.FileUtil;

/**
 * Created by Administrator on 2016/8/31.
 */
public class LocalMediaParser implements LocalDataParser<Media> {

    public static final String TAG = LocalMediaParser.class.getSimpleName();

    @Override
    public Media parse(Cursor cursor) {

        String path = cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_ORIGINAL_PHOTO_PATH));

        Media media;

        if (FileUtil.checkFileIsVideo(path)) {

            return null;

        } else
            media = new Media();

        setMediaValue(cursor, path, media);

        return media;
    }

    void setMediaValue(Cursor cursor, String path, Media media) {
        media.setUuid(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_UUID)));

        media.setTime(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_TIME)));
        media.setWidth(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_WIDTH)));
        media.setHeight(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_HEIGHT)));
        media.setThumb(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_THUMB)));
        media.setLocal(cursor.getInt(cursor.getColumnIndex(DBHelper.MEDIA_KEY_LOCAL)) == 1);

        if (!cursor.isNull(cursor.getColumnIndex(DBHelper.MEDIA_KEY_UPLOADED_USER_UUID))) {
            media.setUploadedUserUUIDs(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_UPLOADED_USER_UUID)));
        }

        media.setOrientationNumber(cursor.getInt(cursor.getColumnIndex(DBHelper.MEDIA_KEY_ORIENTATION_NUMBER)));
        media.setSharing(cursor.getInt(cursor.getColumnIndex(DBHelper.MEDIA_KEY_SHARING)) == 1);
        media.setType(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_TYPE)));
        media.setMiniThumbPath(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_MINI_THUMB)));
        media.setOriginalPhotoPath(path);

        media.setLongitude(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_LONGITUDE)));
        media.setLatitude(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_LATITUDE)));

        Log.d(TAG, "setMediaValue: " + media);

    }

}
