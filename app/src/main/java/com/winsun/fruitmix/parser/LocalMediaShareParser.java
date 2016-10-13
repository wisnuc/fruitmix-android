package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.model.MediaShare;

import java.util.Arrays;

/**
 * Created by Administrator on 2016/8/31.
 */
public class LocalMediaShareParser implements LocalDataParser<MediaShare>{

    @Override
    public MediaShare parse(Cursor cursor) {

        MediaShare mediaShare = new MediaShare();

        mediaShare.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.SHARE_KEY_ID)));
        mediaShare.setUuid(cursor.getString(cursor.getColumnIndex(DBHelper.SHARE_KEY_UUID)));
        mediaShare.setCreatorUUID(cursor.getString(cursor.getColumnIndex(DBHelper.SHARE_KEY_CREATOR_UUID)));
        mediaShare.setTime(cursor.getString(cursor.getColumnIndex(DBHelper.SHARE_KEY_TIME)));
        mediaShare.setTitle(cursor.getString(cursor.getColumnIndex(DBHelper.SHARE_KEY_TITLE)));
        mediaShare.setDesc(cursor.getString(cursor.getColumnIndex(DBHelper.SHARE_KEY_DESC)));
        mediaShare.setImageDigests(Arrays.asList(cursor.getString(cursor.getColumnIndex(DBHelper.SHARE_KEY__IMAGE_DIGESTS)).split(",")));
        mediaShare.setViewers(Arrays.asList(cursor.getString(cursor.getColumnIndex(DBHelper.SHARE_KEY_VIEWERS)).split(",")));
        mediaShare.setMaintainers(Arrays.asList(cursor.getString(cursor.getColumnIndex(DBHelper.SHARE_KEY_MAINTAINERS)).split(",")));
        mediaShare.setAlbum(cursor.getInt(cursor.getColumnIndex(DBHelper.SHARE_KEY_IS_ALBUM)) == 1);
        mediaShare.setArchived(cursor.getInt(cursor.getColumnIndex(DBHelper.SHARE_KEY_IS_ARCHIVED)) == 1);
        mediaShare.setDate(cursor.getString(cursor.getColumnIndex(DBHelper.SHARE_KEY_IS_DATE)));
        mediaShare.setCoverImageDigest(cursor.getString(cursor.getColumnIndex(DBHelper.SHARE_KEY_IS_COVER_IMAGE_DIGEST)));
        mediaShare.setLocal(cursor.getInt(cursor.getColumnIndex(DBHelper.SHARE_KEY_IS_LOCKED)) == 1);

        return mediaShare;
    }
}
