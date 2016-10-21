package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.Media;

import java.util.ArrayList;
import java.util.List;

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
        media.setTitle(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_TITLE)));
        media.setBelongingMediaShareUUID(cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_BELONGING_MEDIASHARE_UUID)));
        media.setUploaded(cursor.getInt(cursor.getColumnIndex(DBHelper.MEDIA_KEY_UPLOADED)) == 1);
        media.setOrientationNumber(cursor.getInt(cursor.getColumnIndex(DBHelper.MEDIA_KEY_ORIENTATION_NUMBER)));

        return media;
    }
}
