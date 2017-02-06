package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;

/**
 * Created by Administrator on 2016/10/17.
 */

public class MediaShareContentParser implements LocalDataParser<MediaShareContent> {

    @Override
    public MediaShareContent parse(Cursor cursor) {

        MediaShareContent mediaShareContent = new MediaShareContent();
        mediaShareContent.setId(cursor.getString(cursor.getColumnIndex(DBHelper.SHARE_CONTENT_KEY_ID)));
        mediaShareContent.setMediaUUID(cursor.getString(cursor.getColumnIndex(DBHelper.SHARE_CONTENT_KEY_DIGEST)));
        mediaShareContent.setAuthor(cursor.getString(cursor.getColumnIndex(DBHelper.SHARE_CONTENT_KEY_CREATOR_UUID)));
        mediaShareContent.setTime(cursor.getString(cursor.getColumnIndex(DBHelper.SHARE_CONTENT_KEY_TIME)));

        return mediaShareContent;
    }
}
