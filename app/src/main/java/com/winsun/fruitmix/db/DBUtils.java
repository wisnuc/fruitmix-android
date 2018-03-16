package com.winsun.fruitmix.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.util.Log;

import com.winsun.fruitmix.file.data.download.FinishedTaskItem;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.upload.FileUploadErrorState;
import com.winsun.fruitmix.file.data.upload.FileUploadFinishedState;
import com.winsun.fruitmix.file.data.upload.FileUploadItem;
import com.winsun.fruitmix.file.data.upload.FileUploadPendingState;
import com.winsun.fruitmix.file.data.upload.FileUploadState;
import com.winsun.fruitmix.group.data.model.GroupUserWrapper;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.parser.FileFinishedTaskItemParser;
import com.winsun.fruitmix.parser.LocalGroupParser;
import com.winsun.fruitmix.parser.LocalGroupTweetParser;
import com.winsun.fruitmix.parser.LocalGroupUserParser;
import com.winsun.fruitmix.parser.LocalStationParser;
import com.winsun.fruitmix.parser.LocalVideoParser;
import com.winsun.fruitmix.parser.LocalWeChatUserParser;
import com.winsun.fruitmix.stations.Station;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.parser.LocalDataParser;
import com.winsun.fruitmix.parser.LocalMediaParser;
import com.winsun.fruitmix.parser.LocalUserParser;
import com.winsun.fruitmix.wechat.user.WeChatUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2016/7/8.
 */
public class DBUtils {

    private static final String TAG = DBUtils.class.getSimpleName();

    public static final int FILE_UPLOAD_TASK_STATE_UNKNOWN = -1;
    public static final int FILE_UPLOAD_TASK_STATE_PENDING = 0;
    public static final int FILE_UPLOAD_TASK_STATE_FINISHED = 1;
    public static final int FILE_UPLOAD_TASK_STATE_ERROR = 2;

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

    public long insertRemoteUser(User user) {

        return insertRemoteUsers(Collections.singletonList(user));

    }

    public long insertRemoteUsers(Collection<User> users) {

        openWritableDB();

        long returnValue = 0;

        ContentValues contentValues;

        for (User user : users) {

            contentValues = createRemoteUserContentValues(user);

            returnValue = database.insert(DBHelper.REMOTE_USER_TABLE_NAME, null, contentValues);
        }

        close();

        return returnValue;
    }

    @NonNull
    private ContentValues createRemoteUserContentValues(User user) {
        ContentValues contentValues;
        contentValues = createUserContentValues(user);

        contentValues.put(DBHelper.USER_ASSOCIATED_WECHAT_USER_NAME, user.getAssociatedWeChatUserName());
        contentValues.put(DBHelper.USER_KEY_IS_FIRST_USER, user.isFirstUser() ? 1 : 0);
        return contentValues;
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

    private ContentValues createLoggedInUserContentValues(LoggedInUser loggedInUser) {

        ContentValues contentValues = createUserContentValues(loggedInUser.getUser());
        contentValues.put(DBHelper.LOGGED_IN_USER_GATEWAY, loggedInUser.getGateway());
        contentValues.put(DBHelper.LOGGED_IN_USER_EQUIPMENT_NAME, loggedInUser.getEquipmentName());
        contentValues.put(DBHelper.LOGGED_IN_USER_TOKEN, loggedInUser.getToken());
        contentValues.put(DBHelper.LOGGED_IN_USER_DEVICE_ID, loggedInUser.getDeviceID());

        return contentValues;
    }

    public long insertFileUploadTaskItem(FinishedTaskItem finishedTaskItem) {

        openWritableDB();

        long returnValue = 0;

        ContentValues contentValues = createFileFinishedTaskItemContentValues(finishedTaskItem);

        FileUploadItem fileUploadItem = (FileUploadItem) finishedTaskItem.getFileTaskItem();

        contentValues.put(DBHelper.FILE_KEY_PATH_SOURCE_FROM_OTHER_APP, fileUploadItem.getFilePath());

        FileUploadState fileUploadState = fileUploadItem.getFileUploadState();

        if (fileUploadState instanceof FileUploadPendingState)
            contentValues.put(DBHelper.FILE_KEY_UPLOAD_TASK_STATE, FILE_UPLOAD_TASK_STATE_PENDING);
        else if (fileUploadState instanceof FileUploadFinishedState)
            contentValues.put(DBHelper.FILE_KEY_UPLOAD_TASK_STATE, FILE_UPLOAD_TASK_STATE_FINISHED);
        else if (fileUploadState instanceof FileUploadErrorState)
            contentValues.put(DBHelper.FILE_KEY_UPLOAD_TASK_STATE, FILE_UPLOAD_TASK_STATE_ERROR);
        else
            contentValues.put(DBHelper.FILE_KEY_UPLOAD_TASK_STATE, FILE_UPLOAD_TASK_STATE_UNKNOWN);

        returnValue = database.insert(DBHelper.UPLOAD_FILE_TABLE_NAME, null, contentValues);

        return returnValue;

    }


    public long insertDownloadedFile(FinishedTaskItem finishedTaskItem) {

        openWritableDB();

        long returnValue = 0;

        returnValue = database.insert(DBHelper.DOWNLOADED_FILE_TABLE_NAME, null, createFileFinishedTaskItemContentValues(finishedTaskItem));

        return returnValue;
    }

    private ContentValues createFileFinishedTaskItemContentValues(FinishedTaskItem finishedTaskItem) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.FILE_KEY_NAME, finishedTaskItem.getFileName());
        contentValues.put(DBHelper.FILE_KEY_SIZE, finishedTaskItem.getFileSize());
        contentValues.put(DBHelper.FILE_KEY_UUID, finishedTaskItem.getFileUUID());
        contentValues.put(DBHelper.FILE_KEY_TIME, finishedTaskItem.getFileTaskItem().getFileTime());
        contentValues.put(DBHelper.FILE_KEY_CREATOR_UUID, finishedTaskItem.getFileCreatorUUID());

        return contentValues;
    }

    public long insertWeChatUser(WeChatUser weChatUser) {

        openWritableDB();

        long returnValue = 0;

        returnValue = database.insert(DBHelper.LOGGED_IN_WECHAT_USER_TABLE_NAME, null, createLoggedWeChatUser(weChatUser));

        return returnValue;

    }

    private ContentValues createLoggedWeChatUser(WeChatUser weChatUser) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.LOGGED_IN_WECAHT_USER_TOKEN, weChatUser.getToken());
        contentValues.put(DBHelper.LOGGED_IN_WECHAT_USER_GUID, weChatUser.getGuid());
        contentValues.put(DBHelper.LOGGED_IN_WECHAT_USER_STATION_ID, weChatUser.getStationID());

        return contentValues;
    }

    private void bindVideoWhenCreate(SQLiteStatement sqLiteStatement, Video video) {

        bindMediaWhenCreate(sqLiteStatement, video);

        sqLiteStatement.bindString(15, video.getName());
        sqLiteStatement.bindLong(16, video.getSize());
        sqLiteStatement.bindLong(17, video.getDuration());

    }

    private String createInsertVideoSql(String DBName) {

        return "insert into " + DBName + "(" +
                DBHelper.MEDIA_KEY_UUID + "," +
                DBHelper.MEDIA_KEY_FORMATTED_TIME + "," +
                DBHelper.MEDIA_KEY_WIDTH + "," +
                DBHelper.MEDIA_KEY_HEIGHT + "," +
                DBHelper.MEDIA_KEY_THUMB + "," +
                DBHelper.MEDIA_KEY_LOCAL + "," +
                DBHelper.MEDIA_KEY_UPLOADED_USER_UUID + "," +
                DBHelper.MEDIA_KEY_SHARING + "," +
                DBHelper.MEDIA_KEY_ORIENTATION_NUMBER + "," +
                DBHelper.MEDIA_KEY_TYPE + "," +
                DBHelper.MEDIA_KEY_MINI_THUMB + "," +
                DBHelper.MEDIA_KEY_ORIGINAL_PHOTO_PATH + "," +
                DBHelper.MEDIA_KEY_LONGITUDE + "," +
                DBHelper.MEDIA_KEY_LATITUDE + "," +
                DBHelper.VIDEO_KEY_NAME + "," +
                DBHelper.VIDEO_KEY_SIZE + "," +
                DBHelper.VIDEO_KEY_DURATION + ")" +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    }

    private long insertVideos(String dbName, Collection<Video> videos) {

        long returnValue = 0;

        try {
            openWritableDB();

            String sql = createInsertVideoSql(dbName);

            SQLiteStatement sqLiteStatement = database.compileStatement(sql);
            database.beginTransaction();

            for (Video video : videos) {

                bindVideoWhenCreate(sqLiteStatement, video);

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

    public long insertLocalVideos(Collection<Video> videos) {

        return insertVideos(DBHelper.LOCAL_VIDEO_TABLE_NAME, videos);

    }

    public long insertRemoteVideos(Collection<Video> videos) {
        return insertVideos(DBHelper.REMOTE_VIDEO_TABLE_NAME, videos);
    }

    private void bindMediaWhenCreate(SQLiteStatement sqLiteStatement, Media media) {
        sqLiteStatement.bindString(1, media.getUuid());
        sqLiteStatement.bindString(2, media.getFormattedTime());
        sqLiteStatement.bindString(3, media.getWidth());
        sqLiteStatement.bindString(4, media.getHeight());
        sqLiteStatement.bindString(5, media.getThumb());
        sqLiteStatement.bindLong(6, media.isLocal() ? 1 : 0);
        sqLiteStatement.bindString(7, media.getUploadedUserUUIDs());
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
                DBHelper.MEDIA_KEY_FORMATTED_TIME + "," +
                DBHelper.MEDIA_KEY_WIDTH + "," +
                DBHelper.MEDIA_KEY_HEIGHT + "," +
                DBHelper.MEDIA_KEY_THUMB + "," +
                DBHelper.MEDIA_KEY_LOCAL + "," +
                DBHelper.MEDIA_KEY_UPLOADED_USER_UUID + "," +
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

                bindMediaWhenCreate(sqLiteStatement, media);

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

    public long insertRemoteGroups(Collection<PrivateGroup> groups) {

        openWritableDB();

        long returnValue = 0;

        ContentValues contentValues;

        Map<String, Station> stationMap = new HashMap<>();

        for (PrivateGroup group : groups) {

            contentValues = createGroupContentValues(group);

            if (!stationMap.containsKey(group.getStationID()))
                stationMap.put(group.getStationID(), group.getStation());

            long insertRemoteGroupUsersResult = insertRemoteGroupUsers(group.getUsers(), group.getUUID());

            Log.d(TAG, "insertRemoteGroupUsers result:" + insertRemoteGroupUsersResult);

            returnValue = database.insert(DBHelper.REMOTE_GROUP_TABLE_NAME, null, contentValues);

        }

        long insertRemoteStationsResult = insertRemoteStations(stationMap.values());

        Log.d(TAG, "insertRemoteStations result: " + insertRemoteStationsResult);

        close();

        Log.d(TAG, "insertRemoteGroups result: " + returnValue);

        return returnValue;

    }

    @NonNull
    private ContentValues createGroupContentValues(PrivateGroup group) {
        ContentValues contentValues;
        contentValues = new ContentValues();
        contentValues.put(DBHelper.GROUP_KEY_UUID, group.getUUID());
        contentValues.put(DBHelper.GROUP_KEY_NAME, group.getName());
        contentValues.put(DBHelper.GROUP_KEY_OWNER_GUID, group.getOwnerGUID());
        contentValues.put(DBHelper.GROUP_KEY_CREATE_TIME, group.getCreateTime());
        contentValues.put(DBHelper.GROUP_KEY_MODIFY_TIME, group.getModifyTime());

        contentValues.put(DBHelper.GROUP_KEY_LAST_READ_COMMENT_INDEX, group.getLastReadCommentIndex());
        contentValues.put(DBHelper.GROUP_KEY_LOCATED_STATION_ID, group.getStationID());

        return contentValues;
    }

    public long insertRemoteGroupTweets(Collection<UserComment> userComments) {

        long returnValue = 0;

        try {
            openWritableDB();

            String sql = createInsertGroupTweetSql(DBHelper.REMOTE_GROUP_TWEET_TABLE_NAME);

            SQLiteStatement sqLiteStatement = database.compileStatement(sql);
            database.beginTransaction();

            for (UserComment userComment : userComments) {

                bindGroupTweetWhenCreate(sqLiteStatement, userComment);

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

    private void bindGroupTweetWhenCreate(SQLiteStatement sqLiteStatement, UserComment userComment) {

        sqLiteStatement.bindLong(1, System.currentTimeMillis());
        sqLiteStatement.bindString(2, userComment.getGroupUUID());
        sqLiteStatement.bindString(3, userComment.getStationID());
        sqLiteStatement.bindString(4, userComment.getContentJsonStr());

        User user = userComment.getCreator();

        sqLiteStatement.bindString(5, user.getUserName());
        sqLiteStatement.bindString(6, user.getUuid());
        sqLiteStatement.bindString(7, user.getAvatar());
        sqLiteStatement.bindString(8, user.getEmail());
        sqLiteStatement.bindString(9, user.getDefaultAvatar());
        sqLiteStatement.bindLong(10, user.getDefaultAvatarBgColor());
        sqLiteStatement.bindString(11, user.getHome());
        sqLiteStatement.bindString(12, user.getLibrary());
        sqLiteStatement.bindLong(13, user.isAdmin() ? 1 : 0);
    }

    @NonNull
    private String createInsertGroupTweetSql(String dbName) {
        return "insert into " + dbName + "(" +
                DBHelper.GROUP_COMMENT_KEY_STORE_TIME + "," +
                DBHelper.GROUP_COMMENT_KEY_GROUP_UUID + "," +
                DBHelper.GROUP_COMMENT_KEY_STATION_ID + "," +
                DBHelper.GROUP_COMMENT_KEY_CONTENT + "," +
                DBHelper.USER_KEY_USERNAME + "," +
                DBHelper.USER_KEY_UUID + "," +
                DBHelper.USER_KEY_AVATAR + "," +
                DBHelper.USER_KEY_EMAIL + "," +
                DBHelper.USER_KEY_DEFAULT_AVATAR + "," +
                DBHelper.USER_KEY_DEFAULT_AVATAR_BG_COLOR + "," +
                DBHelper.USER_KEY_HOME + "," +
                DBHelper.USER_KEY_LIBRARY + "," +
                DBHelper.USER_KEY_IS_ADMIN + ")" +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

    }


    public long insertRemoteGroupUsers(Collection<User> users, String groupUUID) {

        openWritableDB();

        long returnValue = 0;

        ContentValues contentValues;

        for (User user : users) {

            contentValues = createUserContentValues(user);
            contentValues.put(DBHelper.GROUP_USER_KEY_GROUP_UUID, groupUUID);
            contentValues.put(DBHelper.GROUP_USER_ASSOCIATED_WECHAT_GUID, user.getAssociatedWeChatGUID());

            returnValue = database.insert(DBHelper.REMOTE_GROUP_USER_TABLE_NAME, null, contentValues);

        }

        close();

        return returnValue;

    }

    private long insertRemoteStations(Collection<Station> stations) {

        openWritableDB();

        long returnValue = 0;

        ContentValues contentValues;

        for (Station station : stations) {

            contentValues = createStationContentValues(station);

            returnValue = database.insert(DBHelper.REMOTE_STATION_TABLE_NAME, null, contentValues);

        }

        close();

        return returnValue;

    }

    @NonNull
    private ContentValues createStationContentValues(Station station) {
        ContentValues contentValues;
        contentValues = new ContentValues();
        contentValues.put(DBHelper.STATION_KEY_ID, station.getId());
        contentValues.put(DBHelper.STATION_KEY_NAME, station.getLabel());
        return contentValues;
    }


    private long deleteAllDataInTable(String tableName) {
        openWritableDB();

        long returnValue = database.delete(tableName, null, null);

        close();

        return returnValue;

    }

    public long deleteAllLoggedInUser() {
        return deleteAllDataInTable(DBHelper.LOGGED_IN_USER_TABLE_NAME);
    }

    public long deleteLoggerUserByUserUUID(String userUUID) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOGGED_IN_USER_TABLE_NAME, DBHelper.USER_KEY_UUID + " = ?", new String[]{userUUID});

        close();

        return returnValue;

    }

    public long deleteFileUploadTaskByUUIDAndCreatorUUID(String fileUUID, String fileCreatorUUID) {

        return deleteFileFinishedTaskItem(DBHelper.UPLOAD_FILE_TABLE_NAME, fileUUID, fileCreatorUUID);

    }

    public long deleteFileDownloadedTaskByUUIDAndCreatorUUID(String fileUUID, String fileCreatorUUID) {

        return deleteFileFinishedTaskItem(DBHelper.DOWNLOADED_FILE_TABLE_NAME, fileUUID, fileCreatorUUID);

    }

    private long deleteFileFinishedTaskItem(String tableName, String fileUUID, String fileCreatorUUID) {

        openWritableDB();

        long returnValue = database.delete(tableName, DBHelper.FILE_KEY_UUID + " = ? and " + DBHelper.FILE_KEY_CREATOR_UUID + " = ?", new String[]{fileUUID, fileCreatorUUID});

        close();

        return returnValue;

    }


    public long deleteDownloadedFileByUUID(String fileUUID) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.DOWNLOADED_FILE_TABLE_NAME, DBHelper.FILE_KEY_UUID + " = ?", new String[]{fileUUID});

        close();

        return returnValue;
    }

    public long deleteWeChatUser(String token) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOGGED_IN_WECHAT_USER_TABLE_NAME,
                DBHelper.LOGGED_IN_WECAHT_USER_TOKEN + " = ?",
                new String[]{token});

        close();

        return returnValue;

    }


    public long deleteAllRemoteUser() {

        return deleteAllDataInTable(DBHelper.REMOTE_USER_TABLE_NAME);
    }

    public long deleteAllRemoteMedia() {

        return deleteAllDataInTable(DBHelper.REMOTE_MEDIA_TABLE_NAME);
    }

    public long deleteAllRemoteVideo() {
        return deleteAllDataInTable(DBHelper.REMOTE_VIDEO_TABLE_NAME);
    }


    public long deleteAllLocalMedia() {

        return deleteAllDataInTable(DBHelper.LOCAL_MEDIA_TABLE_NAME);
    }

    public long deleteLocalMedias(Collection<String> mediaPaths) {
        return deleteMediasByPath(DBHelper.LOCAL_MEDIA_TABLE_NAME, mediaPaths);
    }

    public long deleteLocalVideos(Collection<String> mediaPaths) {
        return deleteMediasByPath(DBHelper.LOCAL_VIDEO_TABLE_NAME, mediaPaths);
    }

    private long deleteMediasByPath(String dbName, Collection<String> mediaPaths) {

        long returnValue = 0;

        try {
            openWritableDB();

            String sql = createDeleteMediaSql(dbName);

            SQLiteStatement sqLiteStatement = database.compileStatement(sql);
            database.beginTransaction();

            for (String path : mediaPaths) {

                bindMediaWhenDelete(sqLiteStatement, path);

                returnValue = sqLiteStatement.executeUpdateDelete();

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

    private void bindMediaWhenDelete(SQLiteStatement sqLiteStatement, String path) {
        sqLiteStatement.bindString(1, path);
    }

    @NonNull
    private String createDeleteMediaSql(String dbName) {
        return "delete from " + dbName + " where " +
                DBHelper.MEDIA_KEY_ORIGINAL_PHOTO_PATH + " = ?";
    }

    public long deleteRemoteGroupUsers(Collection<String> userGUIDs, String groupUUID) {

        openWritableDB();

        long returnValue = 0;

        for (String userGUID : userGUIDs) {

            returnValue = database.delete(DBHelper.REMOTE_GROUP_USER_TABLE_NAME,
                    DBHelper.GROUP_USER_KEY_GROUP_UUID + " = ? and " + DBHelper.GROUP_USER_ASSOCIATED_WECHAT_GUID
                            + " = ?", new String[]{groupUUID, userGUID});

        }

        close();

        return returnValue;

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

    public LoggedInUser getCurrentLoggedInUserByUUID(String userUUID) {
        return getCurrentLoggedInUser(DBHelper.USER_KEY_UUID, userUUID);
    }

    public LoggedInUser getCurrentLoggedInUserByToken(String token) {
        return getCurrentLoggedInUser(DBHelper.LOGGED_IN_USER_TOKEN, token);
    }

    private LoggedInUser getCurrentLoggedInUser(String where, String param) {
        openReadableDB();

        LoggedInUser loggedInUser = null;

        User user;
        String gateway;
        String equipmentName;
        String token;
        String deviceID;
        LocalDataParser<User> parser = new LocalUserParser();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.LOGGED_IN_USER_TABLE_NAME + " where " + where + " = ?", new String[]{param});

        if (!cursor.moveToFirst())
            return null;

        user = parser.parse(cursor);

        gateway = cursor.getString(cursor.getColumnIndex(DBHelper.LOGGED_IN_USER_GATEWAY));
        equipmentName = cursor.getString(cursor.getColumnIndex(DBHelper.LOGGED_IN_USER_EQUIPMENT_NAME));
        token = cursor.getString(cursor.getColumnIndex(DBHelper.LOGGED_IN_USER_TOKEN));
        deviceID = cursor.getString(cursor.getColumnIndex(DBHelper.LOGGED_IN_USER_DEVICE_ID));

        loggedInUser = new LoggedInUser(deviceID, token, gateway, equipmentName, user);

        cursor.close();

        close();

        return loggedInUser;
    }


    public List<FinishedTaskItem> getAllCurrentLoginUserDownloadedFile(String currentUserUUID) {

        openReadableDB();

        List<FinishedTaskItem> fileDownloadItems = new ArrayList<>();

        Cursor cursor = database.rawQuery(String.format("select * from %s where %s = ?", DBHelper.DOWNLOADED_FILE_TABLE_NAME, DBHelper.FILE_KEY_CREATOR_UUID), new String[]{currentUserUUID});

        while (cursor.moveToNext()) {

            FileDownloadItem fileDownloadItem = new FileDownloadItem();

            FileFinishedTaskItemParser parser = new FileFinishedTaskItemParser();

            FinishedTaskItem finishedTaskItem = parser.fillFileTaskItem(fileDownloadItem, cursor, currentUserUUID);

            fileDownloadItems.add(finishedTaskItem);
        }

        cursor.close();

        close();

        return fileDownloadItems;
    }

    public List<FinishedTaskItem> getAllCurrentLoginUserUploadedFile(String currentUserUUID) {

        openReadableDB();

        List<FinishedTaskItem> finishedTaskItems = new ArrayList<>();

        Cursor cursor = database.rawQuery(String.format("select * from %s where %s = ? and %s = ?", DBHelper.UPLOAD_FILE_TABLE_NAME, DBHelper.FILE_KEY_CREATOR_UUID, DBHelper.FILE_KEY_UPLOAD_TASK_STATE), new String[]{currentUserUUID, FILE_UPLOAD_TASK_STATE_FINISHED + ""});

        while (cursor.moveToNext()) {

            String filePathSourceFromOtherApp = cursor.getString(cursor.getColumnIndex(DBHelper.FILE_KEY_PATH_SOURCE_FROM_OTHER_APP));

            FileFinishedTaskItemParser parser = new FileFinishedTaskItemParser();

            FileUploadItem fileUploadItem = new FileUploadItem();
            fileUploadItem.setFilePath(filePathSourceFromOtherApp);

            FinishedTaskItem finishedTaskItem = parser.fillFileTaskItem(fileUploadItem, cursor, currentUserUUID);

            finishedTaskItems.add(finishedTaskItem);
        }

        cursor.close();

        close();

        return finishedTaskItems;

    }


    public WeChatUser getWeChatUserByToken(String token, String stationID) {

        openReadableDB();

        LocalDataParser<WeChatUser> parser = new LocalWeChatUserParser();

        Cursor cursor = database.rawQuery(String.format("select * from %s where %s = ? and %s = ?", DBHelper.LOGGED_IN_WECHAT_USER_TABLE_NAME, DBHelper.LOGGED_IN_WECAHT_USER_TOKEN, DBHelper.LOGGED_IN_WECHAT_USER_STATION_ID), new String[]{token, stationID});

        if (!cursor.moveToFirst())
            return null;

        WeChatUser weChatUser = parser.parse(cursor);

        cursor.close();

        close();

        return weChatUser;

    }

    private List<Video> getAllVideos(String DBName) {

        openReadableDB();

        List<Video> videos = new ArrayList<>();

        LocalDataParser<Media> parser = new LocalVideoParser();

        Cursor cursor = database.rawQuery("select * from " + DBName, null);

        while (cursor.moveToNext()) {

            Video video = (Video) parser.parse(cursor);

            videos.add(video);

        }

        cursor.close();

        close();

        return videos;

    }

    public List<Video> getAllRemoteVideos() {
        return getAllVideos(DBHelper.REMOTE_VIDEO_TABLE_NAME);
    }

    public List<Video> getAllLocalVideos() {
        return getAllVideos(DBHelper.LOCAL_VIDEO_TABLE_NAME);
    }

    private List<Media> getAllMedia(String dbName) {
        openReadableDB();

        List<Media> medias = new ArrayList<>();

        LocalDataParser<Media> parser = new LocalMediaParser();

        Cursor cursor = database.rawQuery("select * from " + dbName, null);

        while (cursor.moveToNext()) {

            Media media = parser.parse(cursor);

            if (media != null)
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

    public List<PrivateGroup> getAllPrivateGroup() {

        openReadableDB();

        List<PrivateGroup> groups = new ArrayList<>();

        Cursor groupCursor = database.query(DBHelper.REMOTE_GROUP_TABLE_NAME, null, null, null, null, null, null);

        LocalGroupParser localGroupParser = new LocalGroupParser();

        while (groupCursor.moveToNext()) {

            PrivateGroup group = localGroupParser.parse(groupCursor);

            groups.add(group);

        }

        groupCursor.close();

        Cursor groupUserCursor = database.query(DBHelper.REMOTE_GROUP_USER_TABLE_NAME, null, null, null, null, null, null);

        LocalGroupUserParser localGroupUserParser = new LocalGroupUserParser();

        while (groupUserCursor.moveToNext()) {

            GroupUserWrapper groupUserWrapper = localGroupUserParser.parse(groupUserCursor);

            for (PrivateGroup group : groups) {

                if (group.getUUID().equals(groupUserWrapper.getGroupUUID())) {

                    group.addUser(groupUserWrapper.getUser());

                }

            }

        }

        groupUserCursor.close();

        Cursor stationCursor = database.query(DBHelper.REMOTE_STATION_TABLE_NAME, null, null, null, null, null, null);

        LocalStationParser localStationParser = new LocalStationParser();

        while (stationCursor.moveToNext()) {

            Station station = localStationParser.parse(stationCursor);

            for (PrivateGroup group : groups) {

                if (group.getStationID().equals(station.getId())) {

                    group.setStationOnline(true);
                    group.setStationName(station.getLabel());

                }

            }

        }

        stationCursor.close();

        for (PrivateGroup group : groups) {

            List<UserComment> userComments = getUserComments(DBHelper.REMOTE_GROUP_LAST_TWEET_TABLE_NAME, group.getUUID());

            if (userComments.size() > 0)
                group.setLastComment(userComments.get(0));

        }

        return groups;

    }

    public List<UserComment> getUserComments(String groupUUID) {

        return getUserComments(DBHelper.REMOTE_GROUP_TWEET_TABLE_NAME, groupUUID);

    }


    private List<UserComment> getUserComments(String dbName, String groupUUID) {

        Cursor groupTweetCursor = database.query(dbName, null, DBHelper.GROUP_COMMENT_KEY_GROUP_UUID + " = ?"
                , new String[]{groupUUID}, null, null, null);

        LocalGroupTweetParser localGroupTweetParser = new LocalGroupTweetParser();

        List<UserComment> userComments = new ArrayList<>();

        while (groupTweetCursor.moveToNext()) {

            UserComment userComment = localGroupTweetParser.parse(groupTweetCursor);

            userComments.add(userComment);

        }

        groupTweetCursor.close();

        return userComments;

    }

    public long updateRemoteMedia(Media media) {

        return updateRemoteMedia(media, DBHelper.REMOTE_MEDIA_TABLE_NAME);

    }

    public long updateRemoteVideo(Video video) {

        return updateRemoteMedia(video, DBHelper.REMOTE_VIDEO_TABLE_NAME);

    }

    private long updateRemoteMedia(Media media, String DBName) {

        openWritableDB();

        ContentValues contentValues = createMediaContentValues(media);

        long returnValue = database.update(DBName, contentValues, DBHelper.MEDIA_KEY_UUID + " = ?", new String[]{media.getUuid()});

        Log.d(TAG, "update media uuid:" + media.getUuid());

        close();

        return returnValue;

    }

    public long updateLocalMedia(Media media) {
        return updateLocalMedia(media, DBHelper.LOCAL_MEDIA_TABLE_NAME);
    }

    public long updateLocalVideo(Video video) {
        return updateLocalMedia(video, DBHelper.LOCAL_VIDEO_TABLE_NAME);
    }

    private long updateLocalMedia(Media media, String DBName) {
        openWritableDB();

        ContentValues contentValues = createMediaContentValues(media);

        long returnValue = database.update(DBName, contentValues, DBHelper.MEDIA_KEY_ORIGINAL_PHOTO_PATH + " = ?", new String[]{media.getOriginalPhotoPath()});

        Log.d(TAG, "update media original photo path:" + media.getOriginalPhotoPath());

        close();

        return returnValue;
    }

    private ContentValues createMediaContentValues(Media media) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.MEDIA_KEY_UUID, media.getUuid());
        contentValues.put(DBHelper.MEDIA_KEY_FORMATTED_TIME, media.getFormattedTime());
        contentValues.put(DBHelper.MEDIA_KEY_WIDTH, media.getWidth());
        contentValues.put(DBHelper.MEDIA_KEY_HEIGHT, media.getHeight());
        contentValues.put(DBHelper.MEDIA_KEY_THUMB, media.getThumb());
        contentValues.put(DBHelper.MEDIA_KEY_LOCAL, media.isLocal() ? 1 : 0);
        contentValues.put(DBHelper.MEDIA_KEY_UPLOADED_USER_UUID, media.getUploadedUserUUIDs());
        contentValues.put(DBHelper.MEDIA_KEY_SHARING, media.isSharing() ? 1 : 0);
        contentValues.put(DBHelper.MEDIA_KEY_ORIENTATION_NUMBER, media.getOrientationNumber());
        contentValues.put(DBHelper.MEDIA_KEY_TYPE, media.getType());
        contentValues.put(DBHelper.MEDIA_KEY_MINI_THUMB, media.getMiniThumbPath());
        contentValues.put(DBHelper.MEDIA_KEY_ORIGINAL_PHOTO_PATH, media.getOriginalPhotoPath());
        contentValues.put(DBHelper.MEDIA_KEY_LONGITUDE, media.getLongitude());
        contentValues.put(DBHelper.MEDIA_KEY_LATITUDE, media.getLatitude());

        return contentValues;
    }

    public long updateRemoteUserName(String userUUID, String newUserName) {

        openWritableDB();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.USER_KEY_USERNAME, newUserName);

        long returnValue = database.update(DBHelper.REMOTE_USER_TABLE_NAME, contentValues, DBHelper.USER_KEY_UUID + " = ?", new String[]{userUUID});

        Log.d(TAG, "updateRemoteUserName: " + newUserName + " userUUID: " + userUUID + " result: " + returnValue);

        close();

        return returnValue;
    }

    public long updateRemoteUserIsAdminState(String userUUID, boolean isAdmin) {

        openWritableDB();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.USER_KEY_IS_ADMIN, isAdmin ? 1 : 0);

        long returnValue = database.update(DBHelper.REMOTE_USER_TABLE_NAME, contentValues, DBHelper.USER_KEY_UUID + " = ?", new String[]{userUUID});

        Log.d(TAG, "updateRemoteUserIsAdminState: " + isAdmin + " userUUID: " + userUUID + " result: " + returnValue);

        close();

        return returnValue;
    }

    public boolean updateUser(User user) {

        openWritableDB();

        long returnValue;

        ContentValues contentValues = createRemoteUserContentValues(user);

        returnValue = database.update(DBHelper.REMOTE_USER_TABLE_NAME, contentValues, DBHelper.USER_KEY_UUID + " = ?", new String[]{user.getUuid()});

        close();

        return returnValue > 0;
    }

    public long updateGroupName(String groupUUID, String newGroupName) {

        openWritableDB();

        long returnValue;

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.GROUP_KEY_NAME, newGroupName);

        returnValue = database.update(DBHelper.REMOTE_GROUP_TABLE_NAME, contentValues, DBHelper.GROUP_KEY_UUID + " = ?",
                new String[]{groupUUID});

        close();

        return returnValue;

    }

    public long updateStation(Station station) {

        openWritableDB();

        long returnValue;

        ContentValues contentValues = createStationContentValues(station);

        returnValue = database.update(DBHelper.REMOTE_STATION_TABLE_NAME, contentValues, DBHelper.STATION_KEY_ID + " = ?",
                new String[]{station.getId()});

        close();

        return returnValue;

    }


}
