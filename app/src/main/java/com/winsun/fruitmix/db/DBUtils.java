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
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.parser.LocalDataParser;
import com.winsun.fruitmix.parser.LocalMediaParser;
import com.winsun.fruitmix.parser.LocalUserParser;
import com.winsun.fruitmix.util.FNAS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

    private synchronized void openWritableDB() {

        referenceCount.incrementAndGet();

        database = dbHelper.getWritableDatabase();
    }

    private synchronized void openReadableDB() {

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

    public long insertLoggedInUserInDB(Collection<LoggedInUser> loggedInUsers) {

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
        contentValues.put(DBHelper.MEDIA_KEY_MINI_THUMB, media.getMiniThumbPath());
        contentValues.put(DBHelper.MEDIA_KEY_ORIGINAL_PHOTO_PATH, media.getOriginalPhotoPath());
        contentValues.put(DBHelper.MEDIA_KEY_LONGITUDE, media.getLongitude());
        contentValues.put(DBHelper.MEDIA_KEY_LATITUDE, media.getLatitude());

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
        sqLiteStatement.bindString(11, media.getMiniThumbPath());
        sqLiteStatement.bindString(12, media.getOriginalPhotoPath());
        sqLiteStatement.bindString(13, media.getLongitude());
        sqLiteStatement.bindString(14, media.getLatitude());
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
                DBHelper.MEDIA_KEY_MINI_THUMB + "," +
                DBHelper.MEDIA_KEY_ORIGINAL_PHOTO_PATH + "," +
                DBHelper.MEDIA_KEY_LONGITUDE + "," +
                DBHelper.MEDIA_KEY_LATITUDE + ")" +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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

    public long insertRemoteMedias(Collection<Media> medias) {

        return insertMedias(DBHelper.REMOTE_MEDIA_TABLE_NAME, medias);

    }

    public long insertLocalMedias(Collection<Media> medias) {

        return insertMedias(DBHelper.LOCAL_MEDIA_TABLE_NAME, medias);

    }

    private long deleteAllDataInTable(String tableName) {
        openWritableDB();

        long returnValue = database.delete(tableName, null, null);

        close();

        return returnValue;

    }

    public long deleteAllLoggedInUser(){
        return deleteAllDataInTable(DBHelper.LOGGED_IN_USER_TABLE_NAME);
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

    public long updateRemoteMedia(Media media) {

        openWritableDB();

        ContentValues contentValues = createMediaContentValues(media);

        long returnValue = database.update(DBHelper.REMOTE_MEDIA_TABLE_NAME, contentValues, DBHelper.MEDIA_KEY_UUID + " = ?", new String[]{media.getUuid()});

        Log.d(TAG, "update media uuid:" + media.getUuid());

        close();

        return returnValue;

    }

    public long updateLocalMedia(Media media) {
        openWritableDB();

        ContentValues contentValues = createMediaContentValues(media);

        long returnValue = database.update(DBHelper.LOCAL_MEDIA_TABLE_NAME, contentValues, DBHelper.MEDIA_KEY_ORIGINAL_PHOTO_PATH + " = ?", new String[]{media.getOriginalPhotoPath()});

        Log.d(TAG, "update media original photo path:" + media.getOriginalPhotoPath());

        close();

        return returnValue;
    }

}
