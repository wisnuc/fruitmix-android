package com.winsun.fruitmix.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.MediaShareContent;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.parser.LocalDataParser;
import com.winsun.fruitmix.parser.LocalMediaCommentParser;
import com.winsun.fruitmix.parser.LocalMediaParser;
import com.winsun.fruitmix.parser.LocalMediaShareParser;
import com.winsun.fruitmix.parser.LocalUserParser;
import com.winsun.fruitmix.parser.MediaShareContentParser;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        contentValues.put(DBHelper.COMMENT_KEY_CREATOR_UUID, comment.getCreator());
        contentValues.put(DBHelper.COMMENT_KEY_TIME, comment.getTime());
        contentValues.put(DBHelper.COMMENT_KEY_FORMAT_TIME, comment.getFormatTime());
        contentValues.put(DBHelper.COMMENT_KEY_SHARE_UUID, comment.getShareId());
        contentValues.put(DBHelper.COMMENT_KEY_TEXT, comment.getText());
        contentValues.put(DBHelper.COMMENT_IMAGE_UUID, mediaUUID);

        return contentValues;
    }

    private long insertComment(String tableName, Comment comment, String imageUUID) {
        openWritableDB();

        ContentValues contentValues = createCommentContentValues(comment, imageUUID);

        long returnValue = database.insert(tableName, null, contentValues);

        close();

        return returnValue;
    }

    public long insertRemoteComment(Comment comment, String imageUUid) {
        return insertComment(DBHelper.REMOTE_COMMENT_TABLE_NAME, comment, imageUUid);
    }

    public long insertLocalComment(Comment comment, String imageUUid) {

        return insertComment(DBHelper.LOCAL_COMMENT_TABLE_NAME, comment, imageUUid);
    }

    private ContentValues createMediaShareContentContentValues(MediaShareContent mediaShareContent, String mediaShareUUID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.SHARE_CONTENT_KEY_SHARE_UUID, mediaShareUUID);
        contentValues.put(DBHelper.SHARE_CONTENT_KEY_DIGEST, mediaShareContent.getDigest());
        contentValues.put(DBHelper.SHARE_CONTENT_KEY_CREATOR_UUID, mediaShareContent.getAuthor());
        contentValues.put(DBHelper.SHARE_CONTENT_KEY_TIME, mediaShareContent.getTime());

        return contentValues;
    }

    private ContentValues createMediaShareContentValues(MediaShare mediaShare) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.SHARE_KEY_UUID, mediaShare.getUuid());
        contentValues.put(DBHelper.SHARE_KEY_CREATOR_UUID, mediaShare.getCreatorUUID());
        contentValues.put(DBHelper.SHARE_KEY_TIME, mediaShare.getTime());
        contentValues.put(DBHelper.SHARE_KEY_TITLE, mediaShare.getTitle());
        contentValues.put(DBHelper.SHARE_KEY_DESC, mediaShare.getDesc());

        StringBuilder builder = new StringBuilder();

        for (String viewer : mediaShare.getViewers()) {
            builder.append(viewer);
            builder.append(",");
        }

        contentValues.put(DBHelper.SHARE_KEY_VIEWERS, builder.toString());

        builder.setLength(0);
        for (String maintainer : mediaShare.getMaintainers()) {
            builder.append(maintainer);
            builder.append(",");
        }

        contentValues.put(DBHelper.SHARE_KEY_MAINTAINERS, builder.toString());

        contentValues.put(DBHelper.SHARE_KEY_IS_ALBUM, mediaShare.isAlbum() ? 1 : 0);
        contentValues.put(DBHelper.SHARE_KEY_IS_ARCHIVED, mediaShare.isArchived() ? 1 : 0);
        contentValues.put(DBHelper.SHARE_KEY_IS_DATE, mediaShare.getDate());
        contentValues.put(DBHelper.SHARE_KEY_IS_COVER_IMAGE_DIGEST, mediaShare.getCoverImageDigest());
        contentValues.put(DBHelper.SHARE_KEY_IS_LOCKED, mediaShare.isLocal() ? 1 : 0);
        contentValues.put(DBHelper.SHARE_KEY_DIGEST, mediaShare.getShareDigest());
        contentValues.put(DBHelper.SHARE_KEY_IS_STICKY, mediaShare.isSticky() ? 1 : 0);

        return contentValues;
    }

    private long insertMediaShareContent(String mediashareContentTableName,MediaShareContent mediaShareContent,String mediashareUUID){
        openWritableDB();

        ContentValues contentValues = createMediaShareContentContentValues(mediaShareContent,mediashareUUID);

        long returnValue = database.insert(mediashareContentTableName,null,contentValues);

        close();

        return returnValue;
    }

    public long insertLocalMediaShareContent(MediaShareContent mediaShareContent,String mediashareUUID){
        return insertMediaShareContent(DBHelper.LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME,mediaShareContent,mediashareUUID);
    }

    public long insertRemoteMediaShareContent(MediaShareContent mediaShareContent,String mediashareUUID){
        return insertMediaShareContent(DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME,mediaShareContent,mediashareUUID);
    }

    private long insertMediaShare(String mediashareContentTableName,String mediashareTableName, Collection<MediaShare> mediashares) {
        openWritableDB();

        long returnValue = 0;

        ContentValues contentValue;

        for (MediaShare mediashare : mediashares) {

            for (MediaShareContent mediashareContent : mediashare.getMediaShareContents()) {
                contentValue = createMediaShareContentContentValues(mediashareContent, mediashare.getUuid());

                database.insert(mediashareContentTableName, null, contentValue);
            }

            ContentValues contentValues = createMediaShareContentValues(mediashare);

            returnValue = database.insert(mediashareTableName, null, contentValues);
        }

        close();

        return returnValue;
    }

    public long insertLocalShare(MediaShare mediaShare) {

        return insertMediaShare(DBHelper.LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME,DBHelper.LOCAL_SHARE_TABLE_NAME, Collections.singletonList(mediaShare));
    }

    public long insertRemoteMediaShare(MediaShare mediaShare) {

        return insertMediaShare(DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME,DBHelper.REMOTE_SHARE_TABLE_NAME, Collections.singletonList(mediaShare));

    }

    public long insertRemoteMediaShares(ConcurrentMap<String, MediaShare> mediaShares) {

        return insertMediaShare(DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME,DBHelper.REMOTE_SHARE_TABLE_NAME, mediaShares.values());
    }

    private ContentValues createUserContentValues(User user) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.USER_KEY_USERNAME, user.getUserName());
        contentValues.put(DBHelper.USER_KEY_UUID, user.getUuid());
        contentValues.put(DBHelper.USER_KEY_AVATAR, user.getAvatar());
        contentValues.put(DBHelper.USER_KEY_EMAIL, user.getEmail());
        contentValues.put(DBHelper.USER_KEY_DEFAULT_AVATAR, user.getDefaultAvatar());
        contentValues.put(DBHelper.USER_KEY_DEFAULT_AVATAR_BG_COLOR, user.getDefaultAvatarBgColor());
        contentValues.put(DBHelper.USER_KEY_HOME, user.getHome());
        contentValues.put(DBHelper.USER_KEY_LIBRARY, user.getLibrary());

        return contentValues;
    }

    public long insertRemoteUsers(ConcurrentMap<String, User> users) {

        openWritableDB();

        long returnValue = 0;

        ContentValues contentValues;

        for (User user : users.values()) {

            contentValues = createUserContentValues(user);

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

    public long insertLocalMedia(Media media) {

        openWritableDB();

        long returnValue = 0;

        ContentValues contentValues = createMediaContentValues(media);

        returnValue = database.insert(DBHelper.LOCAL_MEDIA_TABLE_NAME, null, contentValues);


        close();

        return returnValue;

    }

    private long deleteAllDataInTable(String tableName) {
        openWritableDB();

        long returnValue = database.delete(tableName, null, null);

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

        return deleteAllDataInTable(DBHelper.REMOTE_COMMENT_TABLE_NAME);
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

        return deleteAllDataInTable(DBHelper.LOCAL_COMMENT_TABLE_NAME);
    }

    private long deleteMediaShareContentByID(String mediashareContentTableName,String id){
        openWritableDB();

        long returnValue = database.delete(mediashareContentTableName,DBHelper.SHARE_CONTENT_KEY_ID + " = ?",new String[]{id});

        close();

        return returnValue;
    }

    public long deleteLocalMediaShareContentByID(String id){
        return deleteMediaShareContentByID(DBHelper.LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME,id);
    }

    public long deleteRemoteMediaShareContentByID(String id){
        return deleteMediaShareContentByID(DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME,id);
    }

    public long deleteLocalShareByUUid(String uuid) {

        openWritableDB();

        database.delete(DBHelper.LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME, DBHelper.SHARE_CONTENT_KEY_SHARE_UUID + " = ?", new String[]{uuid});

        long returnValue = database.delete(DBHelper.LOCAL_SHARE_TABLE_NAME, DBHelper.SHARE_KEY_UUID + " = ?", new String[]{uuid});

        close();

        return returnValue;
    }

    public long deleteAllLocalShare() {

        openWritableDB();

        database.delete(DBHelper.LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME,null,null);
        long returnValue = database.delete(DBHelper.LOCAL_SHARE_TABLE_NAME, null, null);

        close();

        return returnValue;
    }

    public long deleteRemoteShareByUUid(String uuid) {

        openWritableDB();

        database.delete(DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME, DBHelper.SHARE_CONTENT_KEY_SHARE_UUID + " = ?", new String[]{uuid});
        long returnValue = database.delete(DBHelper.REMOTE_SHARE_TABLE_NAME, DBHelper.SHARE_KEY_UUID + " = ?", new String[]{uuid});

        close();

        return returnValue;
    }

    public long deleteAllRemoteShare() {

        openWritableDB();

        database.delete(DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME,null,null);
        long returnValue = database.delete(DBHelper.REMOTE_SHARE_TABLE_NAME, null, null);

        close();

        return returnValue;
    }

    public long deleteAllRemoteUser() {

        return deleteAllDataInTable(DBHelper.REMOTE_USER_TABLE_NAME);
    }

    public long deleteAllRemoteMedia() {

        return deleteAllDataInTable(DBHelper.REMOTE_MEDIA_TABLE_NAME);
    }

    public long deleteAllLocalMedia() {

        return deleteAllDataInTable(DBHelper.LOCAL_MEDIA_TABLE_NAME);
    }

    private List<MediaShareContent> getMediaShareContents(String mediashareContentTableName,String mediashareUUID) {


        List<MediaShareContent> mediashareContents = new ArrayList<>();
        MediaShareContentParser parser = new MediaShareContentParser();
        Cursor cursor = database.rawQuery("select * from " + mediashareContentTableName + " where " + DBHelper.SHARE_CONTENT_KEY_SHARE_UUID + " = ?", new String[]{mediashareUUID});
        while (cursor.moveToNext()) {
            mediashareContents.add(parser.parse(cursor));
        }
        cursor.close();

        return mediashareContents;
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

        for (MediaShare mediaShare:list){

            mediaShare.initMediaShareContents(getMediaShareContents(DBHelper.LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME,mediaShare.getUuid()));
        }

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

        mediaShare.initMediaShareContents(getMediaShareContents(DBHelper.LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME,mediaShare.getUuid()));

        close();

        return mediaShare;
    }

    public List<MediaShare> getAllRemoteShare() {

        openReadableDB();

        List<MediaShare> mediaShares = new ArrayList<>();
        LocalDataParser<MediaShare> parser = new LocalMediaShareParser();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.REMOTE_SHARE_TABLE_NAME, null);
        while (cursor.moveToNext()) {

            mediaShares.add(parser.parse(cursor));
        }
        cursor.close();

        for (MediaShare mediaShare:mediaShares){

            mediaShare.initMediaShareContents(getMediaShareContents(DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME,mediaShare.getUuid()));
        }


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

        mediaShare.initMediaShareContents(getMediaShareContents(DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME,mediaShare.getUuid()));

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
