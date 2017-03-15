package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.mediaModule.model.Media;

/**
 * Created by Administrator on 2016/8/31.
 */
public class LocalMediaParser implements LocalDataParser<Media> {

    @Override
    public Media parse(Cursor cursor) {

        Media media = new Media();
        media.setUuid(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_UUID)));
        media.setTime(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_TIME)));
        media.setWidth(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_WIDTH)));
        media.setHeight(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_HEIGHT)));
        media.setThumb(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_THUMB)));
        media.setLocal(cursor.getInt(cursor.getColumnIndex(DBHelper.MEDIA_KEY_LOCAL)) == 1);

        if(!cursor.isNull(cursor.getColumnIndex(DBHelper.MEDIA_KEY_UPLOADED_DEVICE_ID))){
            media.setUploadedDeviceIDs(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_UPLOADED_DEVICE_ID)));
        }

        media.setOrientationNumber(cursor.getInt(cursor.getColumnIndex(DBHelper.MEDIA_KEY_ORIENTATION_NUMBER)));
        media.setSharing(cursor.getInt(cursor.getColumnIndex(DBHelper.MEDIA_KEY_SHARING)) == 1);
        media.setType(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_TYPE)));
        media.setMiniThumb(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_MINI_THUMB)));

        return media;
    }
}
