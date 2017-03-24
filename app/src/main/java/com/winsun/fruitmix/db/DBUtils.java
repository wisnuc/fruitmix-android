package com.winsun.fruitmix.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.util.Log;

import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.parser.LocalDataParser;
import com.winsun.fruitmix.parser.LocalMediaCommentParser;
import com.winsun.fruitmix.parser.LocalMediaParser;
import com.winsun.fruitmix.parser.LocalMediaShareParser;
import com.winsun.fruitmix.parser.LocalUserParser;
import com.winsun.fruitmix.parser.MediaShareContentParser;
import com.winsun.fruitmix.util.FNAS;

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
public class DBUtils {

    private static final String TAG = DBUtils.class.getSimpleName();

    private DBHelper dbHelper;
    private SQLiteDatabase database;

    private AtomicInteger referenceCount;

    private static DBUtils dbUtils;

    private DBUtils(Context context) {
        dbHelper = new DBHelper(context.getApplicationContext());
        referenceCount = new AtomicInteger();
    }

    public static DBUtils getInstance(Context context) {
        if (dbUtils == null) {
            dbUtils = new DBUtils(context);
        }

        return dbUtils;
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

    public boolean isOpen() {
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
        contentValues.put(DBHelper.SHARE_CONTENT_KEY_DIGEST, mediaShareContent.getMediaUUID());
        contentValues.put(DBHelper.SHARE_CONTENT_KEY_CREATOR_UUID, mediaShareContent.getAuthor());
        contentValues.put(DBHelper.SHARE_CONTENT_KEY_TIME, mediaShareContent.getTime());

        return contentValues;
    }

    private void bindMediaShareContent(SQLiteStatement sqLiteStatement, MediaShareContent mediaShareContent, String mediaShareUUID) {
        sqLiteStatement.bindString(1, mediaShareUUID);
        sqLiteStatement.bindString(2, mediaShareContent.getMediaUUID());
        sqLiteStatement.bindString(3, mediaShareContent.getAuthor());
        sqLiteStatement.bindString(4, mediaShareContent.getTime());
    }

    @NonNull
    private String createInsertMediaShareContentSql(String mediaShareContentDBName) {
        return "insert into " + mediaShareContentDBName + "(" +
                DBHelper.SHARE_CONTENT_KEY_SHARE_UUID + "," +
                DBHelper.SHARE_CONTENT_KEY_DIGEST + "," +
                DBHelper.SHARE_CONTENT_KEY_CREATOR_UUID + "," +
                DBHelper.SHARE_CONTENT_KEY_TIME + ")" +
                "values(?,?,?,?)";
    }

    private long insertMediaShareContent(String mediashareContentTableName, MediaShareContent mediaShareContent, String mediashareUUID) {

        long returnValue = 0;

        try {
            openWritableDB();

            String sql = createInsertMediaShareContentSql(mediashareContentTableName);

            SQLiteStatement sqLiteStatement = database.compileStatement(sql);
            database.beginTransaction();

            bindMediaShareContent(sqLiteStatement, mediaShareContent, mediashareUUID);

            returnValue = sqLiteStatement.executeInsert();

            database.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            database.endTransaction();

            close();
        }

        return returnValue;
    }

    public long insertLocalMediaShareContent(MediaShareContent mediaShareContent, String mediashareUUID) {
        return insertMediaShareContent(DBHelper.LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME, mediaShareContent, mediashareUUID);
    }

    public long insertRemoteMediaShareContent(MediaShareContent mediaShareContent, String mediashareUUID) {
        return insertMediaShareContent(DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME, mediaShareContent, mediashareUUID);
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
        contentValues.put(DBHelper.SHARE_KEY_IS_COVER_IMAGE_DIGEST, mediaShare.getCoverImageUUID());
        contentValues.put(DBHelper.SHARE_KEY_IS_LOCAL, mediaShare.isLocal() ? 1 : 0);
        contentValues.put(DBHelper.SHARE_KEY_DIGEST, mediaShare.getShareDigest());
        contentValues.put(DBHelper.SHARE_KEY_IS_STICKY, mediaShare.isSticky() ? 1 : 0);

        return contentValues;
    }

    private void bindMediaShare(SQLiteStatement sqLiteStatement, MediaShare mediaShare) {
        sqLiteStatement.bindString(1, mediaShare.getUuid());
        sqLiteStatement.bindString(2, mediaShare.getCreatorUUID());
        sqLiteStatement.bindString(3, mediaShare.getTime());
        sqLiteStatement.bindString(4, mediaShare.getTitle());
        sqLiteStatement.bindString(5, mediaShare.getDesc());

        StringBuilder builder = new StringBuilder();

        for (String viewer : mediaShare.getViewers()) {
            builder.append(viewer);
            builder.append(",");
        }

        sqLiteStatement.bindString(6, builder.toString());

        builder.setLength(0);
        for (String maintainer : mediaShare.getMaintainers()) {
            builder.append(maintainer);
            builder.append(",");
        }

        sqLiteStatement.bindString(7, builder.toString());

        sqLiteStatement.bindLong(8, mediaShare.isAlbum() ? 1 : 0);
        sqLiteStatement.bindLong(9, mediaShare.isArchived() ? 1 : 0);
        sqLiteStatement.bindString(10, mediaShare.getDate());
        sqLiteStatement.bindString(11, mediaShare.getCoverImageUUID());
        sqLiteStatement.bindLong(12, mediaShare.isLocal() ? 1 : 0);
        sqLiteStatement.bindString(13, mediaShare.getShareDigest());
        sqLiteStatement.bindLong(14, mediaShare.isSticky() ? 1 : 0);

    }

    @NonNull
    private String createInsertMediaShareSql(String mediaShareDBName) {
        return "insert into " + mediaShareDBName + "(" +
                DBHelper.SHARE_KEY_UUID + "," +
                DBHelper.SHARE_KEY_CREATOR_UUID + "," +
                DBHelper.SHARE_KEY_TIME + "," +
                DBHelper.SHARE_KEY_TITLE + "," +
                DBHelper.SHARE_KEY_DESC + "," +
                DBHelper.SHARE_KEY_VIEWERS + "," +
                DBHelper.SHARE_KEY_MAINTAINERS + "," +
                DBHelper.SHARE_KEY_IS_ALBUM + "," +
                DBHelper.SHARE_KEY_IS_ARCHIVED + "," +
                DBHelper.SHARE_KEY_IS_DATE + "," +
                DBHelper.SHARE_KEY_IS_COVER_IMAGE_DIGEST + "," +
                DBHelper.SHARE_KEY_IS_LOCAL + "," +
                DBHelper.SHARE_KEY_DIGEST + "," +
                DBHelper.SHARE_KEY_IS_STICKY + ")" +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    }

    private long insertMediaShare(String mediashareContentTableName, String mediashareTableName, Collection<MediaShare> mediashares) {

        long returnValue = 0;

        try {
            openWritableDB();

            String insertMediaShareContentSql = createInsertMediaShareContentSql(mediashareContentTableName);

            SQLiteStatement mediaShareContentSQLiteStatement = database.compileStatement(insertMediaShareContentSql);

            String insertMediaShareSql = createInsertMediaShareSql(mediashareTableName);

            SQLiteStatement mediaShareSQLiteStatement = database.compileStatement(insertMediaShareSql);

            database.beginTransaction();

            for (MediaShare mediashare : mediashares) {

                for (MediaShareContent mediashareContent : mediashare.getMediaShareContents()) {

                    bindMediaShareContent(mediaShareContentSQLiteStatement, mediashareContent, mediashare.getUuid());

                    mediaShareContentSQLiteStatement.executeInsert();
                }

                bindMediaShare(mediaShareSQLiteStatement, mediashare);

                returnValue = mediaShareSQLiteStatement.executeInsert();

            }

            database.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            database.endTransaction();

            close();
        }

        return returnValue;

    }

    public long insertLocalMediaShare(MediaShare mediaShare) {

        return insertMediaShare(DBHelper.LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME, DBHelper.LOCAL_SHARE_TABLE_NAME, Collections.singletonList(mediaShare));
    }

    public long insertRemoteMediaShare(MediaShare mediaShare) {

        return insertMediaShare(DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME, DBHelper.REMOTE_SHARE_TABLE_NAME, Collections.singletonList(mediaShare));

    }

    public long insertRemoteMediaShares(Collection<MediaShare> mediaShares) {

        return insertMediaShare(DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME, DBHelper.REMOTE_SHARE_TABLE_NAME, mediaShares);
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
        contentValues.put(DBHelper.USER_KEY_IS_ADMIN, user.isAdmin() ? 1 : 0);

        return contentValues;
    }

    public long insertRemoteUsers(Collection<User> users) {

        openWritableDB();

        long returnValue = 0;

        ContentValues contentValues;

        for (User user : users) {

            contentValues = createUserContentValues(user);

            returnValue = database.insert(DBHelper.REMOTE_USER_TABLE_NAME, null, contentValues);
        }

        close();

        return returnValue;
    }

    public long insertRemoteUser(User user) {

        return insertRemoteUsers(Collections.singletonList(user));

    }

    private ContentValues createLoggedInUserContentValues(LoggedInUser loggedInUser) {

        ContentValues contentValues = createUserContentValues(loggedInUser.getUser());
        contentValues.put(DBHelper.LOGGED_IN_USER_GATEWAY, loggedInUser.getGateway());
        contentValues.put(DBHelper.LOGGED_IN_USER_EQUIPMENT_NAME, loggedInUser.getEquipmentName());
        contentValues.put(DBHelper.LOGGED_IN_USER_TOKEN, loggedInUser.getToken());
        contentValues.put(DBHelper.LOGGED_IN_USER_DEVICE_ID, loggedInUser.getDeviceID());

        return contentValues;
    }

    public long insertLoggedInUserInDB(List<LoggedInUser> loggedInUsers) {

        openWritableDB();

        long returnValue = 0;

        ContentValues contentValues;

        for (LoggedInUser loggedInUser : loggedInUsers) {

            contentValues = createLoggedInUserContentValues(loggedInUser);

            returnValue = database.insert(DBHelper.LOGGED_IN_USER_TABLE_NAME, null, contentValues);
        }

        close();

        return returnValue;

    }


    private ContentValues createDownloadedFileContentValues(FileDownloadItem fileDownloadItem) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.FILE_KEY_NAME, fileDownloadItem.getFileName());
        contentValues.put(DBHelper.FILE_KEY_SIZE, fileDownloadItem.getFileSize());
        contentValues.put(DBHelper.FILE_KEY_UUID, fileDownloadItem.getFileUUID());
        contentValues.put(DBHelper.FILE_KEY_TIME, fileDownloadItem.getFileTime());
        contentValues.put(DBHelper.FILE_KEY_CREATOR_UUID, fileDownloadItem.getFileCreatorUUID());

        return contentValues;
    }

    public long insertDownloadedFile(FileDownloadItem fileDownloadItem) {

        openWritableDB();

        long returnValue = 0;

        returnValue = database.insert(DBHelper.DOWNLOADED_FILE_TABLE_NAME, null, createDownloadedFileContentValues(fileDownloadItem));

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
        contentValues.put(DBHelper.MEDIA_KEY_UPLOADED_DEVICE_ID, media.getUploadedDeviceIDs());
        contentValues.put(DBHelper.MEDIA_KEY_SHARING, media.isSharing() ? 1 : 0);
        contentValues.put(DBHelper.MEDIA_KEY_ORIENTATION_NUMBER, media.getOrientationNumber());
        contentValues.put(DBHelper.MEDIA_KEY_TYPE, media.getType());
        contentValues.put(DBHelper.MEDIA_KEY_MINI_THUMB, media.getMiniThumb());

        return contentValues;
    }

    private void bindMedia(SQLiteStatement sqLiteStatement, Media media) {
        sqLiteStatement.bindString(1, media.getUuid());
        sqLiteStatement.bindString(2, media.getTime());
        sqLiteStatement.bindString(3, media.getWidth());
        sqLiteStatement.bindString(4, media.getHeight());
        sqLiteStatement.bindString(5, media.getThumb());
        sqLiteStatement.bindLong(6, media.isLocal() ? 1 : 0);
        sqLiteStatement.bindString(7, media.getUploadedDeviceIDs());
        sqLiteStatement.bindLong(8, media.isSharing() ? 1 : 0);
        sqLiteStatement.bindLong(9, media.getOrientationNumber());
        sqLiteStatement.bindString(10, media.getType());
        sqLiteStatement.bindString(11, media.getMiniThumb());
    }

    @NonNull
    private String createInsertMediaSql(String dbName) {
        return "insert into " + dbName + "(" +
                DBHelper.MEDIA_KEY_UUID + "," +
                DBHelper.MEDIA_KEY_TIME + "," +
                DBHelper.MEDIA_KEY_WIDTH + "," +
                DBHelper.MEDIA_KEY_HEIGHT + "," +
                DBHelper.MEDIA_KEY_THUMB + "," +
                DBHelper.MEDIA_KEY_LOCAL + "," +
                DBHelper.MEDIA_KEY_UPLOADED_DEVICE_ID + "," +
                DBHelper.MEDIA_KEY_SHARING + "," +
                DBHelper.MEDIA_KEY_ORIENTATION_NUMBER + "," +
                DBHelper.MEDIA_KEY_TYPE + "," +
                DBHelper.MEDIA_KEY_MINI_THUMB + ")" +
                "values(?,?,?,?,?,?,?,?,?,?,?)";
    }

    private long insertMedias(String dbName, Collection<Media> medias) {

        long returnValue = 0;

        try {
            openWritableDB();

            String sql = createInsertMediaSql(dbName);

            SQLiteStatement sqLiteStatement = database.compileStatement(sql);
            database.beginTransaction();

            for (Media media : medias) {

                bindMedia(sqLiteStatement, media);

                returnValue = sqLiteStatement.executeInsert();

            }
            database.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            database.endTransaction();

            close();
        }

        return returnValue;
    }

    public long insertRemoteMedias(ConcurrentMap<String, Media> medias) {

        return insertMedias(DBHelper.REMOTE_MEDIA_TABLE_NAME, medias.values());

    }

    public long insertLocalMedias(List<Media> medias) {

        return insertMedias(DBHelper.LOCAL_MEDIA_TABLE_NAME, medias);

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

    private long deleteMediaShareContentByID(String mediashareContentTableName, String id) {
        openWritableDB();

        long returnValue = database.delete(mediashareContentTableName, DBHelper.SHARE_CONTENT_KEY_ID + " = ?", new String[]{id});

        close();

        return returnValue;
    }

    public long deleteLocalMediaShareContentByID(String id) {
        return deleteMediaShareContentByID(DBHelper.LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME, id);
    }

    public long deleteRemoteMediaShareContentByID(String id) {
        return deleteMediaShareContentByID(DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME, id);
    }

    private void bindMediaShareContent(SQLiteStatement sqLiteStatement, String mediaShareContentUUID) {
        sqLiteStatement.bindString(1, mediaShareContentUUID);
    }

    private String createDeleteShareContentSql(String dbName) {
        return "delete from " + dbName + " where " +
                DBHelper.SHARE_CONTENT_KEY_SHARE_UUID + " = ?";
    }

    private void bindMediaShare(SQLiteStatement sqLiteStatement, String mediaShareUUID) {
        sqLiteStatement.bindString(1, mediaShareUUID);
    }

    private String createDeleteShareSql(String dbName) {
        return "delete from " + dbName + " where " +
                DBHelper.SHARE_KEY_UUID + " = ?";
    }

    private long deleteShareByUUID(String shareDBName, String shareContentDBName, String[] shareUUIDs) {

        long returnValue = 0L;

        try {
            openWritableDB();

            String deleteShareContentSql = createDeleteShareContentSql(shareContentDBName);
            String deleteShareSql = createDeleteShareSql(shareDBName);

            SQLiteStatement deleteShareContentStatement = database.compileStatement(deleteShareContentSql);
            SQLiteStatement deleteShareStatement = database.compileStatement(deleteShareSql);

            database.beginTransaction();

            for (String uuid : shareUUIDs) {

                bindMediaShareContent(deleteShareContentStatement, uuid);
                deleteShareContentStatement.executeUpdateDelete();

                bindMediaShare(deleteShareStatement, uuid);
                returnValue = deleteShareStatement.executeUpdateDelete();

            }

            database.setTransactionSuccessful();

        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {

            database.endTransaction();

            close();
        }

        return returnValue;
    }

    public long deleteLocalShareByUUIDs(String[] shareUUIDs) {

        return deleteShareByUUID(DBHelper.LOCAL_SHARE_TABLE_NAME, DBHelper.LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME, shareUUIDs);
    }

    public long deleteRemoteShareByUUIDs(String[] shareUUIDs) {

        return deleteShareByUUID(DBHelper.REMOTE_SHARE_TABLE_NAME, DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME, shareUUIDs);

    }

    private long deleteAllShare(String shareDBName, String shareContentDBName) {


        long returnValue = 0L;

        try {
            openWritableDB();

            database.beginTransaction();

            database.delete(shareContentDBName, null, null);
            returnValue = database.delete(shareDBName, null, null);

            database.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {

            database.endTransaction();

            close();
        }

        return returnValue;
    }

    public long deleteAllLocalShare() {

        return deleteAllShare(DBHelper.LOCAL_SHARE_TABLE_NAME, DBHelper.LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME);
    }

    public long deleteAllRemoteShare() {

        return deleteAllShare(DBHelper.REMOTE_SHARE_TABLE_NAME, DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME);
    }

    public long deleteLoggerUserByUserUUID(String userUUID) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOGGED_IN_USER_TABLE_NAME, DBHelper.USER_KEY_UUID + " = ?", new String[]{userUUID});

        close();

        return returnValue;

    }

    public long deleteDownloadedFileByUUIDAndCreatorUUID(String fileUUID, String fileCreatorUUID) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.DOWNLOADED_FILE_TABLE_NAME, DBHelper.FILE_KEY_UUID + " = ? and " + DBHelper.FILE_KEY_CREATOR_UUID + " = ?", new String[]{fileUUID, fileCreatorUUID});

        close();

        return returnValue;
    }

    public long deleteDownloadedFileByUUID(String fileUUID) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.DOWNLOADED_FILE_TABLE_NAME, DBHelper.FILE_KEY_UUID + " = ?", new String[]{fileUUID});

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

    private List<MediaShareContent> getMediaShareContents(String mediashareContentTableName, String mediashareUUID) {

        List<MediaShareContent> mediashareContents = new ArrayList<>();
        MediaShareContentParser parser = new MediaShareContentParser();
        Cursor cursor = database.rawQuery("select * from " + mediashareContentTableName + " where " + DBHelper.SHARE_CONTENT_KEY_SHARE_UUID + " = ?", new String[]{mediashareUUID});
        while (cursor.moveToNext()) {
            mediashareContents.add(parser.parse(cursor));
        }
        cursor.close();

        return mediashareContents;
    }

    private Map<String, List<Comment>> getAllImageCommentKeyIsImageUUID(String dbName) {
        openReadableDB();

        Map<String, List<Comment>> map = new HashMap<>();

        LocalDataParser<Comment> parser = new LocalMediaCommentParser();

        Cursor cursor = database.rawQuery("select * from " + dbName, null);
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

    public Map<String, List<Comment>> getAllLocalImageCommentKeyIsImageUUID() {

        return getAllImageCommentKeyIsImageUUID(DBHelper.LOCAL_COMMENT_TABLE_NAME);
    }


    public Map<String, List<Comment>> getAllRemoteImageCommentKeyIsImageUUID() {

        return getAllImageCommentKeyIsImageUUID(DBHelper.REMOTE_COMMENT_TABLE_NAME);
    }

    private List<Comment> getImageCommentByUUid(String dbName, String uuid) {
        openReadableDB();

        List<Comment> commentList = new ArrayList<>();
        LocalDataParser<Comment> parser = new LocalMediaCommentParser();
        Cursor cursor = database.rawQuery("select * from " + dbName + " where " + DBHelper.COMMENT_IMAGE_UUID + " = ?", new String[]{uuid});

        while (cursor.moveToNext()) {

            commentList.add(parser.parse(cursor));

        }
        cursor.close();

        close();

        return commentList;
    }

    public List<Comment> getLocalImageCommentByUUid(String uuid) {

        return getImageCommentByUUid(DBHelper.LOCAL_COMMENT_TABLE_NAME, uuid);
    }

    public List<Comment> getRemoteImageCommentByUUid(String uuid) {

        return getImageCommentByUUid(DBHelper.REMOTE_COMMENT_TABLE_NAME, uuid);
    }

    private List<MediaShare> getAllShare(String shareDBName, String shareContentDBName) {
        openReadableDB();

        List<MediaShare> list = new ArrayList<>();
        LocalDataParser<MediaShare> parser = new LocalMediaShareParser();
        Cursor cursor = database.rawQuery("select * from " + shareDBName, null);
        while (cursor.moveToNext()) {

            list.add(parser.parse(cursor));
        }
        cursor.close();

        for (MediaShare mediaShare : list) {

            mediaShare.initMediaShareContents(getMediaShareContents(shareContentDBName, mediaShare.getUuid()));
        }

        close();

        return list;
    }

    public List<MediaShare> getAllLocalShare() {

        return getAllShare(DBHelper.LOCAL_SHARE_TABLE_NAME, DBHelper.LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME);
    }

    public List<MediaShare> getAllRemoteShare() {

        return getAllShare(DBHelper.REMOTE_SHARE_TABLE_NAME, DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME);

    }

    private MediaShare getShareByUuid(String shareDBName, String shareContentDBName, String uuid) {

        openReadableDB();

        Cursor cursor = database.rawQuery("select * from " + shareDBName + " where " + DBHelper.SHARE_KEY_UUID + " = ?", new String[]{uuid});
        MediaShare mediaShare = new MediaShare();
        LocalDataParser<MediaShare> parser = new LocalMediaShareParser();
        while (cursor.moveToNext()) {
            mediaShare = parser.parse(cursor);
        }
        cursor.close();

        mediaShare.initMediaShareContents(getMediaShareContents(shareContentDBName, mediaShare.getUuid()));

        close();

        return mediaShare;
    }

    public MediaShare getLocalShareByUuid(String uuid) {

        return getShareByUuid(DBHelper.LOCAL_SHARE_TABLE_NAME, DBHelper.LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME, uuid);

    }

    public MediaShare getRemoteShareByUuid(String uuid) {

        return getShareByUuid(DBHelper.REMOTE_SHARE_TABLE_NAME, DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME, uuid);

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

    public List<LoggedInUser> getAllLoggedInUser() {
        openReadableDB();

        List<LoggedInUser> loggedInUsers = new ArrayList<>();

        User user;
        String gateway;
        String equipmentName;
        String token;
        String deviceID;
        LocalDataParser<User> parser = new LocalUserParser();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.LOGGED_IN_USER_TABLE_NAME, null);

        while (cursor.moveToNext()) {
            user = parser.parse(cursor);

            gateway = cursor.getString(cursor.getColumnIndex(DBHelper.LOGGED_IN_USER_GATEWAY));
            equipmentName = cursor.getString(cursor.getColumnIndex(DBHelper.LOGGED_IN_USER_EQUIPMENT_NAME));
            token = cursor.getString(cursor.getColumnIndex(DBHelper.LOGGED_IN_USER_TOKEN));
            deviceID = cursor.getString(cursor.getColumnIndex(DBHelper.LOGGED_IN_USER_DEVICE_ID));

            loggedInUsers.add(new LoggedInUser(deviceID, token, gateway, equipmentName, user));
        }

        cursor.close();

        close();

        return loggedInUsers;
    }


    public List<FileDownloadItem> getAllCurrentLoginUserDownloadedFile() {

        openReadableDB();

        List<FileDownloadItem> fileDownloadItems = new ArrayList<>();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.DOWNLOADED_FILE_TABLE_NAME + " where " + DBHelper.FILE_KEY_CREATOR_UUID + " = ?", new String[]{FNAS.userUUID});
        while (cursor.moveToNext()) {

            String fileName = cursor.getString(cursor.getColumnIndex(DBHelper.FILE_KEY_NAME));
            String fileUUID = cursor.getString(cursor.getColumnIndex(DBHelper.FILE_KEY_UUID));
            long fileSize = cursor.getLong(cursor.getColumnIndex(DBHelper.FILE_KEY_SIZE));
            long fileTime = cursor.getLong(cursor.getColumnIndex(DBHelper.FILE_KEY_TIME));

            String fileCreatorUUID;
            if (cursor.isNull(cursor.getColumnIndex(DBHelper.FILE_KEY_CREATOR_UUID))) {
                fileCreatorUUID = FNAS.userUUID;
            } else {
                fileCreatorUUID = cursor.getString(cursor.getColumnIndex(DBHelper.FILE_KEY_CREATOR_UUID));
            }

            FileDownloadItem fileDownloadItem = new FileDownloadItem(fileName, fileSize, fileUUID);
            fileDownloadItem.setFileTime(fileTime);
            fileDownloadItem.setFileCreatorUUID(fileCreatorUUID);

            fileDownloadItems.add(fileDownloadItem);
        }

        cursor.close();

        close();

        return fileDownloadItems;
    }

    private List<Media> getAllMedia(String dbName) {
        openReadableDB();

        List<Media> medias = new ArrayList<>();

        LocalDataParser<Media> parser = new LocalMediaParser();

        Cursor cursor = database.rawQuery("select * from " + dbName, null);

        while (cursor.moveToNext()) {

            Media media = parser.parse(cursor);

            medias.add(media);
        }

        cursor.close();

        close();

        return medias;
    }

    public List<Media> getAllRemoteMedia() {
        return getAllMedia(DBHelper.REMOTE_MEDIA_TABLE_NAME);
    }

    public List<Media> getAllLocalMedia() {
        return getAllMedia(DBHelper.LOCAL_MEDIA_TABLE_NAME);
    }

    private long updateMediaShare(String dbName, MediaShare mediaShare) {
        openWritableDB();

        ContentValues contentValues = createMediaShareContentValues(mediaShare);

        long returnValue = database.update(dbName, contentValues, DBHelper.SHARE_KEY_UUID + " = ?", new String[]{mediaShare.getUuid()});

        close();

        return returnValue;
    }

    public long updateLocalMediaShare(MediaShare mediaShare) {
        return updateMediaShare(DBHelper.LOCAL_SHARE_TABLE_NAME, mediaShare);
    }

    public long updateRemoteMediaShare(MediaShare mediaShare) {

        return updateMediaShare(DBHelper.REMOTE_SHARE_TABLE_NAME, mediaShare);

    }

    private long updateMedia(String dbName, Media media) {
        openWritableDB();

        ContentValues contentValues = createMediaContentValues(media);

        long returnValue = database.update(dbName, contentValues, DBHelper.MEDIA_KEY_UUID + " = ?", new String[]{media.getUuid()});

        Log.d(TAG, "update media uuid:" + media.getUuid());

        close();

        return returnValue;
    }

    public long updateRemoteMedia(Media media) {

        return updateMedia(DBHelper.REMOTE_MEDIA_TABLE_NAME, media);

    }

    public long updateLocalMedia(Media media) {
        return updateMedia(DBHelper.LOCAL_MEDIA_TABLE_NAME, media);
    }

    public long deleteOldAndInsertNewRemoteMediaShare(Collection<MediaShare> oldMediaShares, Collection<MediaShare> newMediaShares) {

        return deleteOldAndInsertNewMediaShare(DBHelper.REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME, DBHelper.REMOTE_SHARE_TABLE_NAME, oldMediaShares, newMediaShares);

    }

    private long deleteOldAndInsertNewMediaShare(String shareContentDBName, String shareDBName, Collection<MediaShare> oldMediaShares, Collection<MediaShare> newMediaShares) {

        long returnValue = 0L;

        try {
            openWritableDB();

            String deleteShareContentSql = createDeleteShareContentSql(shareContentDBName);
            String deleteShareSql = createDeleteShareSql(shareDBName);
            SQLiteStatement deleteShareContentStatement = database.compileStatement(deleteShareContentSql);
            SQLiteStatement deleteShareStatement = database.compileStatement(deleteShareSql);

            String insertMediaShareContentSql = createInsertMediaShareContentSql(shareContentDBName);
            SQLiteStatement mediaShareContentSQLiteStatement = database.compileStatement(insertMediaShareContentSql);
            String insertMediaShareSql = createInsertMediaShareSql(shareDBName);
            SQLiteStatement mediaShareSQLiteStatement = database.compileStatement(insertMediaShareSql);

            database.beginTransaction();

            for (MediaShare oldMediaShare : oldMediaShares) {

                String uuid = oldMediaShare.getUuid();

                bindMediaShareContent(deleteShareContentStatement, uuid);
                deleteShareContentStatement.executeUpdateDelete();

                bindMediaShare(deleteShareStatement, uuid);
                returnValue = deleteShareStatement.executeUpdateDelete();

            }

            for (MediaShare mediashare : newMediaShares) {

                for (MediaShareContent mediashareContent : mediashare.getMediaShareContents()) {

                    bindMediaShareContent(mediaShareContentSQLiteStatement, mediashareContent, mediashare.getUuid());

                    mediaShareContentSQLiteStatement.executeInsert();
                }

                bindMediaShare(mediaShareSQLiteStatement, mediashare);

                returnValue = mediaShareSQLiteStatement.executeInsert();

            }

            database.setTransactionSuccessful();

        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {

            database.endTransaction();

            close();
        }

        return returnValue;

    }

}
