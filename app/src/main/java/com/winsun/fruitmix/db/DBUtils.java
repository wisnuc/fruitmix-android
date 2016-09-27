package com.winsun.fruitmix.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.parser.LocalDataParser;
import com.winsun.fruitmix.parser.LocalMediaCommentParser;
import com.winsun.fruitmix.parser.LocalMediaParser;
import com.winsun.fruitmix.parser.LocalMediaShareParser;
import com.winsun.fruitmix.parser.LocalUserParser;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2016/7/8.
 */
public enum DBUtils {

    SINGLE_INSTANCE;

    private static final String TAG = DBUtils.class.getSimpleName();

    private DBHelper dbHelper;
    private SQLiteDatabase database;

    private AtomicInteger referenceCount;

    DBUtils() {
        dbHelper = new DBHelper(Util.APPLICATION_CONTEXT);
        referenceCount = new AtomicInteger();
    }

    private void openWritableDB() {

        referenceCount.incrementAndGet();

        database = dbHelper.getWritableDatabase();
    }

    private void openReadableDB() {

        referenceCount.incrementAndGet();

        database = dbHelper.getReadableDatabase();
    }

    private synchronized void close() {

        if (referenceCount.decrementAndGet() == 0) {
            database.close();
        }
    }

    private boolean isOpen() {
        return database.isOpen();
    }

    private ContentValues createCommentContentValues(Comment comment, String mediaUUID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.COMMENT_KEY_CREATOR, comment.getCreator());
        contentValues.put(DBHelper.COMMENT_KEY_TIME, comment.getTime());
        contentValues.put(DBHelper.COMMENT_KEY_FORMAT_TIME, comment.getFormatTime());
        contentValues.put(DBHelper.COMMENT_KEY_SHARE_ID, comment.getShareId());
        contentValues.put(DBHelper.COMMENT_KEY_TEXT, comment.getText());
        contentValues.put(DBHelper.COMMENT_IMAGE_UUID, mediaUUID);

        return contentValues;
    }

    public long insertRemoteComment(Comment comment, String imageUUid) {

        openWritableDB();

        ContentValues contentValues = createCommentContentValues(comment, imageUUid);

        long returnValue = database.insert(DBHelper.REMOTE_COMMENT_TABLE_NAME, null, contentValues);

        close();

        return returnValue;
    }

    public long insertLocalComment(Comment comment, String imageUUid) {

        openWritableDB();

        ContentValues contentValues = createCommentContentValues(comment, imageUUid);

        long returnValue = database.insert(DBHelper.LOCAL_COMMENT_TABLE_NAME, null, contentValues);

        close();

        return returnValue;
    }

    private ContentValues createMediaShareContentValues(MediaShare mediaShare) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.SHARE_KEY_UUID, mediaShare.getUuid());
        contentValues.put(DBHelper.SHARE_KEY_CREATOR_UUID, mediaShare.getCreatorUUID());
        contentValues.put(DBHelper.SHARE_KEY_TIME, mediaShare.getTime());
        contentValues.put(DBHelper.SHARE_KEY_TITLE, mediaShare.getTitle());
        contentValues.put(DBHelper.SHARE_KEY_DESC, mediaShare.getDesc());

        StringBuilder builder = new StringBuilder();
        for (String image : mediaShare.getImageDigests()) {
            builder.append(image);
            builder.append(",");
        }

        contentValues.put(DBHelper.SHARE_KEY__IMAGE_DIGESTS, builder.toString());

        builder.setLength(0);
        for (String viewer : mediaShare.getViewer()) {
            builder.append(viewer);
            builder.append(",");
        }

        contentValues.put(DBHelper.SHARE_KEY_VIEWERS, builder.toString());

        builder.setLength(0);
        for (String maintainer : mediaShare.getMaintainer()) {
            builder.append(maintainer);
            builder.append(",");
        }

        contentValues.put(DBHelper.SHARE_KEY_MAINTAINERS, builder.toString());

        contentValues.put(DBHelper.SHARE_KEY_IS_ALBUM, mediaShare.isAlbum() ? 1 : 0);
        contentValues.put(DBHelper.SHARE_KEY_IS_ARCHIVED, mediaShare.isArchived() ? 1 : 0);
        contentValues.put(DBHelper.SHARE_KEY_IS_DATE, mediaShare.getDate());
        contentValues.put(DBHelper.SHARE_KEY_IS_COVER_IMAGE_DIGEST, mediaShare.getCoverImageDigest());
        contentValues.put(DBHelper.SHARE_KEY_IS_LOCKED, mediaShare.isLocked() ? 1 : 0);

        return contentValues;
    }

    public long insertLocalShare(MediaShare mediaShare) {

        openWritableDB();

        ContentValues contentValues = createMediaShareContentValues(mediaShare);

        long returnValue = database.insert(DBHelper.LOCAL_SHARE_TABLE_NAME, null, contentValues);

        close();

        return returnValue;
    }

    public long insertRemoteMediaShare(MediaShare mediaShare) {

        openWritableDB();

        long returnValue = 0;

        ContentValues contentValues = createMediaShareContentValues(mediaShare);

        returnValue = database.insert(DBHelper.REMOTE_SHARE_TABLE_NAME, null, contentValues);

        close();

        return returnValue;

    }

    public long insertRemoteMediaShares(ConcurrentMap<String, MediaShare> mediaShares) {

        openWritableDB();

        long returnValue = 0;
        ContentValues contentValues;

        for (MediaShare mediaShare : mediaShares.values()) {

            contentValues = createMediaShareContentValues(mediaShare);

            returnValue = database.insert(DBHelper.REMOTE_SHARE_TABLE_NAME, null, contentValues);
        }

        close();

        return returnValue;
    }

    private ContentValues createUesrContentValues(User user) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.USER_KEY_USERNAME, user.getUserName());
        contentValues.put(DBHelper.USER_KEY_UUID, user.getUuid());
        contentValues.put(DBHelper.USER_KEY_AVATAR, user.getAvatar());
        contentValues.put(DBHelper.USER_KEY_EMAIL, user.getEmail());
        contentValues.put(DBHelper.USER_KEY_DEFAULT_AVATAR, user.getDefaultAvatar());
        contentValues.put(DBHelper.USER_KEY_DEFAULT_AVATAR_BG_COLOR, user.getDefaultAvatarBgColor());

        return contentValues;
    }

    public long insertRemoteUsers(ConcurrentMap<String, User> users) {

        openWritableDB();

        long returnValue = 0;

        ContentValues contentValues;

        for (User user : users.values()) {

            contentValues = createUesrContentValues(user);

            returnValue = database.insert(DBHelper.REMOTE_USER_TABLE_NAME, null, contentValues);
        }

        close();

        return returnValue;
    }

    private ContentValues createMediaContentValues(Media media) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.MEDIA_KEY_UUID, media.getUuid());
        contentValues.put(DBHelper.MEDIA_KEY_TIME, media.getTime());
        contentValues.put(DBHelper.MEDIA_KEY_WIDTH, media.getWidth());
        contentValues.put(DBHelper.MEDIA_KEY_HEIGHT, media.getHeight());
        contentValues.put(DBHelper.MEDIA_KEY_THUMB, media.getThumb());
        contentValues.put(DBHelper.MEDIA_KEY_LOCAL, media.isLocal() ? 1 : 0);
        contentValues.put(DBHelper.MEDIA_KEY_TITLE, media.getTitle());
        contentValues.put(DBHelper.MEDIA_KEY_BELONGING_MEDIASHARE_UUID, media.getBelongingMediaShareUUID());
        contentValues.put(DBHelper.MEDIA_KEY_UPLOADED, media.isUploaded() ? 1 : 0);

        return contentValues;
    }

    public long insertRemoteMedias(ConcurrentMap<String, Media> medias) {

        openWritableDB();

        long returnValue = 0;

        ContentValues contentValues;

        for (Media media : medias.values()) {

            contentValues = createMediaContentValues(media);

            returnValue = database.insert(DBHelper.REMOTE_MEDIA_TABLE_NAME, null, contentValues);
        }

        close();

        return returnValue;

    }

    public long insertLocalMediaMap(ConcurrentMap<String, Media> medias) {

        openWritableDB();

        long returnValue = 0;

        ContentValues contentValues;

        for (Media media : medias.values()) {
            contentValues = createMediaContentValues(media);

            returnValue = database.insert(DBHelper.LOCAL_MEDIA_TABLE_NAME, null, contentValues);
        }

        close();

        return returnValue;

    }

    public long insertLocalMedia(Media media) {

        openWritableDB();

        long returnValue = 0;

        ContentValues contentValues = createMediaContentValues(media);

        returnValue = database.insert(DBHelper.LOCAL_MEDIA_TABLE_NAME, null, contentValues);


        close();

        return returnValue;

    }

    public long deleteTask(int id) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.TASK_TABLE_NAME, DBHelper.TASK_KEY_ID + " = ?", new String[]{String.valueOf(id)});

        close();

        return returnValue;
    }

    public long deleteRemoteComment(int id) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.REMOTE_COMMENT_TABLE_NAME, DBHelper.COMMENT_KEY_ID + " = ?", new String[]{String.valueOf(id)});

        close();

        return returnValue;
    }

    public long deleteRemoteCommentByUUid(String uuid) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.REMOTE_COMMENT_TABLE_NAME, DBHelper.COMMENT_IMAGE_UUID + " = ?", new String[]{uuid});

        close();

        return returnValue;
    }

    public long deleteAllRemoteComment() {

        openWritableDB();

        long returnValue = database.delete(DBHelper.REMOTE_COMMENT_TABLE_NAME, null, null);

        close();

        return returnValue;
    }

    public long deleteLocalComment(long id) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOCAL_COMMENT_TABLE_NAME, DBHelper.COMMENT_KEY_ID + " = ?", new String[]{String.valueOf(id)});

        close();

        return returnValue;
    }

    public long deleteLocalCommentByUUid(String uuid) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOCAL_COMMENT_TABLE_NAME, DBHelper.COMMENT_IMAGE_UUID + " = ?", new String[]{uuid});

        close();

        return returnValue;
    }

    public long deleteAllLocalComment() {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOCAL_COMMENT_TABLE_NAME, null, null);

        close();

        return returnValue;
    }

    public long deleteLocalShare(int id) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOCAL_SHARE_TABLE_NAME, DBHelper.SHARE_KEY_ID + " = ?", new String[]{String.valueOf(id)});

        close();

        return returnValue;

    }


    public long deleteLocalShareByUUid(String UUid) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOCAL_SHARE_TABLE_NAME, DBHelper.SHARE_KEY_UUID + " = ?", new String[]{UUid});

        close();

        return returnValue;
    }

    public long deleteAllLocalShare() {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOCAL_SHARE_TABLE_NAME, null, null);

        close();

        return returnValue;
    }

    public long deleteRemoteShare(int id) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.REMOTE_SHARE_TABLE_NAME, DBHelper.SHARE_KEY_ID + " = ?", new String[]{String.valueOf(id)});

        close();

        return returnValue;

    }


    public long deleteRemoteShareByUUid(String UUid) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.REMOTE_SHARE_TABLE_NAME, DBHelper.SHARE_KEY_UUID + " = ?", new String[]{UUid});

        close();

        return returnValue;
    }

    public long deleteAllRemoteShare() {

        openWritableDB();

        long returnValue = database.delete(DBHelper.REMOTE_SHARE_TABLE_NAME, null, null);

        close();

        return returnValue;
    }

    public long deleteAllRemoteUser() {

        openWritableDB();

        long returnValue = database.delete(DBHelper.REMOTE_USER_TABLE_NAME, null, null);

        close();

        return returnValue;
    }

    public long deleteAllRemoteMedia() {

        openWritableDB();

        long returnValue = database.delete(DBHelper.REMOTE_MEDIA_TABLE_NAME, null, null);

        close();

        return returnValue;
    }

    public long deleteAllLocalMedia() {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOCAL_MEDIA_TABLE_NAME, null, null);

        close();

        return returnValue;
    }

    public Map<String, List<Comment>> getAllLocalImageCommentKeyIsImageUUID() {

        openReadableDB();

        Map<String, List<Comment>> map = new HashMap<>();

        LocalDataParser<Comment> parser = new LocalMediaCommentParser();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.LOCAL_COMMENT_TABLE_NAME, null);
        while (cursor.moveToNext()) {
            String imageUuid = cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_IMAGE_UUID));
            List<Comment> commentList;
            if (map.containsKey(imageUuid)) {

                commentList = map.get(imageUuid);

                commentList.add(parser.parse(cursor));

            } else {
                commentList = new ArrayList<>();

                commentList.add(parser.parse(cursor));

                map.put(imageUuid, commentList);
            }
        }
        cursor.close();

        close();

        return map;
    }

    public List<Comment> getLocalImageCommentByUUid(String uuid) {

        openReadableDB();

        List<Comment> commentList = new ArrayList<>();
        LocalDataParser<Comment> parser = new LocalMediaCommentParser();
        Cursor cursor = database.rawQuery("select * from " + DBHelper.LOCAL_COMMENT_TABLE_NAME + " where " + DBHelper.COMMENT_IMAGE_UUID + " = ?", new String[]{uuid});

        while (cursor.moveToNext()) {

            commentList.add(parser.parse(cursor));

        }
        cursor.close();

        close();

        return commentList;
    }

    public List<Comment> getRemoteImageCommentByUUid(String uuid) {

        openReadableDB();

        List<Comment> commentList = new ArrayList<>();
        LocalDataParser<Comment> parser = new LocalMediaCommentParser();
        Cursor cursor = database.rawQuery("select * from " + DBHelper.REMOTE_COMMENT_TABLE_NAME + " where " + DBHelper.COMMENT_IMAGE_UUID + " = ?", new String[]{uuid});
        while (cursor.moveToNext()) {

            commentList.add(parser.parse(cursor));

        }
        cursor.close();

        close();

        return commentList;
    }

    public Map<String, List<Comment>> getAllRemoteImageCommentKeyIsImageUUID() {

        openReadableDB();

        Map<String, List<Comment>> map = new HashMap<>();
        LocalDataParser<Comment> parser = new LocalMediaCommentParser();
        Cursor cursor = database.rawQuery("select * from " + DBHelper.REMOTE_COMMENT_TABLE_NAME, null);
        while (cursor.moveToNext()) {
            String imageUuid = cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_IMAGE_UUID));
            List<Comment> commentList;
            if (map.containsKey(imageUuid)) {

                commentList = map.get(imageUuid);
                commentList.add(parser.parse(cursor));

            } else {
                commentList = new ArrayList<>();
                commentList.add(parser.parse(cursor));

                map.put(imageUuid, commentList);
            }
        }
        cursor.close();

        close();

        return map;
    }

    public List<MediaShare> getAllLocalShare() {

        openReadableDB();

        List<MediaShare> list = new ArrayList<>();
        LocalDataParser<MediaShare> parser = new LocalMediaShareParser();
        Cursor cursor = database.rawQuery("select * from " + DBHelper.LOCAL_SHARE_TABLE_NAME, null);
        while (cursor.moveToNext()) {

            list.add(parser.parse(cursor));
        }
        cursor.close();

        close();

        return list;
    }

    public MediaShare getLocalShareByUuid(String uuid) {

        openReadableDB();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.LOCAL_SHARE_TABLE_NAME + " where " + DBHelper.SHARE_KEY_UUID + " = ?", new String[]{uuid});
        MediaShare mediaShare = new MediaShare();
        LocalDataParser<MediaShare> parser = new LocalMediaShareParser();
        while (cursor.moveToNext()) {
            mediaShare = parser.parse(cursor);
        }
        cursor.close();

        close();

        return mediaShare;
    }

    public List<MediaShare> getAllRemoteShare() {

        openReadableDB();

        List<MediaShare> mediaShares = new ArrayList<>();
        LocalDataParser<MediaShare> parser = new LocalMediaShareParser();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.REMOTE_SHARE_TABLE_NAME, null);
        while (cursor.moveToNext()) {

            MediaShare mediaShare = parser.parse(cursor);

            mediaShares.add(mediaShare);
        }
        cursor.close();

        close();

        return mediaShares;
    }

    public MediaShare getRemoteShareByUuid(String uuid) {

        openReadableDB();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.REMOTE_SHARE_TABLE_NAME + " where " + DBHelper.SHARE_KEY_UUID + " = ?", new String[]{uuid});
        MediaShare mediaShare = new MediaShare();

        LocalDataParser<MediaShare> parser = new LocalMediaShareParser();

        while (cursor.moveToNext()) {
            mediaShare = parser.parse(cursor);
        }
        cursor.close();

        close();

        return mediaShare;
    }

    public List<User> getAllRemoteUser() {
        openReadableDB();

        List<User> users = new ArrayList<>();

        LocalDataParser<User> parser = new LocalUserParser();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.REMOTE_USER_TABLE_NAME, null);

        while (cursor.moveToNext()) {

            User user = parser.parse(cursor);

            users.add(user);
        }

        cursor.close();

        close();

        return users;
    }

    public List<Media> getAllRemoteMedia() {
        openReadableDB();

        List<Media> medias = new ArrayList<>();

        LocalDataParser<Media> parser = new LocalMediaParser();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.REMOTE_MEDIA_TABLE_NAME, null);

        while (cursor.moveToNext()) {

            Media media = parser.parse(cursor);

            medias.add(media);
        }

        cursor.close();

        close();

        return medias;
    }

    public List<Media> getAllLocalMedia() {
        openReadableDB();

        List<Media> medias = new ArrayList<>();

        LocalDataParser<Media> parser = new LocalMediaParser();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.LOCAL_MEDIA_TABLE_NAME, null);

        while (cursor.moveToNext()) {

            Media media = parser.parse(cursor);

            medias.add(media);
        }

        cursor.close();

        close();

        return medias;
    }

    public long updateLocalShare(MediaShare mediaShare) {

        openWritableDB();

        ContentValues contentValues = createMediaShareContentValues(mediaShare);

        long returnValue = database.update(DBHelper.LOCAL_SHARE_TABLE_NAME, contentValues, DBHelper.SHARE_KEY_UUID + " = ?", new String[]{mediaShare.getUuid()});

        close();

        return returnValue;
    }

    public long updateRemoteShare(MediaShare mediaShare) {

        openWritableDB();

        ContentValues contentValues = createMediaShareContentValues(mediaShare);

        long returnValue = database.update(DBHelper.REMOTE_SHARE_TABLE_NAME, contentValues, DBHelper.SHARE_KEY_UUID + " = ?", new String[]{mediaShare.getUuid()});

        close();

        return returnValue;
    }


    public long updateRemoteMedia(Media media) {

        openWritableDB();

        ContentValues contentValues = createMediaContentValues(media);

        long returnValue = database.update(DBHelper.REMOTE_MEDIA_TABLE_NAME, contentValues, DBHelper.MEDIA_KEY_UUID + " = ?", new String[]{media.getUuid()});

        close();

        return returnValue;
    }

    public long updateLocalMedia(Media media) {

        openWritableDB();

        ContentValues contentValues = createMediaContentValues(media);

        long returnValue = database.update(DBHelper.LOCAL_MEDIA_TABLE_NAME, contentValues, DBHelper.MEDIA_KEY_UUID + " = ?", new String[]{media.getUuid()});

        Log.i(TAG, "update local media media uuid:" + media.getUuid());

        close();

        return returnValue;
    }

}
