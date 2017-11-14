package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;

/**
 * Created by Administrator on 2017/10/31.
 */

public class LocalVideoParser extends LocalMediaParser {

    @Override
    public Media parse(Cursor cursor) {

        String path = cursor.getString(cursor.getColumnIndex(DBHelper.MEDIA_KEY_ORIGINAL_PHOTO_PATH));

        Video video = new Video();

        video.setName(cursor.getString(cursor.getColumnIndex(DBHelper.VIDEO_KEY_NAME)));
        video.setSize(cursor.getLong(cursor.getColumnIndex(DBHelper.VIDEO_KEY_SIZE)));
        video.setDuration(cursor.getLong(cursor.getColumnIndex(DBHelper.VIDEO_KEY_DURATION)));

        setMediaValue(cursor,path,video);

        return video;

    }
}
