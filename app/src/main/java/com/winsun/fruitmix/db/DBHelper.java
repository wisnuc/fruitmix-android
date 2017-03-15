package com.winsun.fruitmix.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Administrator on 2016/7/8.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String TAG = DBHelper.class.getSimpleName();

    public static final String TASK_KEY_ID = "id";
    public static final String TASK_KEY_HTTP_TYPE = "http_type";
    public static final String TASK_KEY_OPERATION_TYPE = "operation_type";
    public static final String TASK_KEY_REQUEST = "request";
    public static final String TASK_KEY_DATA = "data";
    public static final String TASK_KEY_OPERATION_COUNT = "operation_count";

    public static final String COMMENT_KEY_ID = "id";
    public static final String COMMENT_KEY_CREATOR_UUID = "creator_uuid";
    public static final String COMMENT_KEY_FORMAT_TIME = "format_time";
    public static final String COMMENT_KEY_TIME = "time";
    public static final String COMMENT_KEY_SHARE_UUID = "share_id";
    public static final String COMMENT_KEY_TEXT = "text";
    public static final String COMMENT_IMAGE_UUID = "image_uuid";

    public static final String SHARE_KEY_ID = "id";
    public static final String SHARE_KEY_UUID = "share_uuid";
    public static final String SHARE_KEY_CREATOR_UUID = "share_creator_uuid";
    public static final String SHARE_KEY_TIME = "share_time";
    public static final String SHARE_KEY_TITLE = "share_title";
    public static final String SHARE_KEY_DESC = "share_desc";
    public static final String SHARE_KEY_VIEWERS = "share_viewer";
    public static final String SHARE_KEY_MAINTAINERS = "share_maintainer";
    public static final String SHARE_KEY_IS_ALBUM = "share_is_album";
    public static final String SHARE_KEY_IS_ARCHIVED = "share_key_is_archived";
    public static final String SHARE_KEY_IS_DATE = "share_key_is_date";
    public static final String SHARE_KEY_IS_COVER_IMAGE_DIGEST = "share_key_is_cover_image_digest";
    public static final String SHARE_KEY_IS_LOCAL = "share_key_is_local";
    public static final String SHARE_KEY_DIGEST = "share_digest";
    public static final String SHARE_KEY_IS_STICKY = "share_is_sticky";

    public static final String USER_KEY_ID = "id";
    public static final String USER_KEY_USERNAME = "user_name";
    public static final String USER_KEY_UUID = "user_uuid";
    public static final String USER_KEY_AVATAR = "user_avatar";
    public static final String USER_KEY_EMAIL = "user_email";
    public static final String USER_KEY_DEFAULT_AVATAR = "user_default_avatar";
    public static final String USER_KEY_DEFAULT_AVATAR_BG_COLOR = "user_default_avatar_bg_color";
    public static final String USER_KEY_HOME = "home";
    public static final String USER_KEY_LIBRARY = "library";
    public static final String USER_KEY_IS_ADMIN = "is_admin";

    public static final String MEDIA_KEY_ID = "id";
    public static final String MEDIA_KEY_UUID = "media_uuid";
    public static final String MEDIA_KEY_TIME = "media_time";
    public static final String MEDIA_KEY_WIDTH = "media_width";
    public static final String MEDIA_KEY_HEIGHT = "media_height";
    public static final String MEDIA_KEY_THUMB = "media_thumb";
    public static final String MEDIA_KEY_LOCAL = "media_local";
    public static final String MEDIA_KEY_UPLOADED_DEVICE_ID = "media_key_uploaded_device_id";
    public static final String MEDIA_KEY_SHARING = "media_key_sharing";
    public static final String MEDIA_KEY_ORIENTATION_NUMBER = "media_key_orientation_number";
    public static final String MEDIA_KEY_TYPE = "media_key_type";
    public static final String MEDIA_KEY_MINI_THUMB = "media_key_mini_thumb";

    public static final String SHARE_CONTENT_KEY_ID = "id";
    public static final String SHARE_CONTENT_KEY_SHARE_UUID = "share_uuid";
    public static final String SHARE_CONTENT_KEY_CREATOR_UUID = "creator_uuid";
    public static final String SHARE_CONTENT_KEY_DIGEST = "digest";
    public static final String SHARE_CONTENT_KEY_TIME = "time";

    public static final String FILE_KEY_ID = "id";
    public static final String FILE_KEY_NAME = "file_name";
    public static final String FILE_KEY_UUID = "file_uuid";
    public static final String FILE_KEY_TIME = "file_time";
    public static final String FILE_KEY_SIZE = "file_size";
    public static final String FILE_KEY_CREATOR_UUID = "file_creator_uuid";

    public static final String LOGGED_IN_USER_GATEWAY = "logged_in_user_gateway";
    public static final String LOGGED_IN_USER_EQUIPMENT_NAME = "logged_in_user_equipment_name";
    public static final String LOGGED_IN_USER_TOKEN = "logged_in_user_token";
    public static final String LOGGED_IN_USER_DEVICE_ID = "logged_in_user_device_id";

    private static final String DB_NAME = "fruitmix";
    static final String TASK_TABLE_NAME = "task";
    static final String REMOTE_COMMENT_TABLE_NAME = "remote_comment";
    static final String LOCAL_COMMENT_TABLE_NAME = "local_comment";
    static final String LOCAL_SHARE_TABLE_NAME = "local_share";
    static final String REMOTE_SHARE_TABLE_NAME = "remote_share";
    static final String REMOTE_USER_TABLE_NAME = "remote_user";
    static final String REMOTE_MEDIA_TABLE_NAME = "remote_media";
    static final String LOCAL_MEDIA_TABLE_NAME = "local_media";
    static final String REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME = "remote_media_share_content";
    static final String LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME = "local_media_share_content";
    static final String DOWNLOADED_FILE_TABLE_NAME = "downloaded_file";
    static final String LOGGED_IN_USER_TABLE_NAME = "logged_in_user";

    private static final int DB_VERSION = 22;

    private static final String CREATE_TABLE = "create table ";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    public static final String DATABASE_TASK_CREATE = "create table " + TASK_TABLE_NAME + " (" + TASK_KEY_ID + " integer primary key autoincrement," + TASK_KEY_HTTP_TYPE + " text not null," + TASK_KEY_OPERATION_TYPE + " text not null," + TASK_KEY_REQUEST + " text not null," + TASK_KEY_DATA + " text not null," + TASK_KEY_OPERATION_COUNT + " integer)";

    private static final String DATABASE_COMMENT_CREATE = " (" + COMMENT_KEY_ID + " integer primary key autoincrement,"
            + COMMENT_KEY_CREATOR_UUID + " text not null," + COMMENT_KEY_TIME + " text not null,"
            + COMMENT_KEY_FORMAT_TIME + " text not null," + COMMENT_KEY_SHARE_UUID + " text not null,"
            + COMMENT_KEY_TEXT + " text not null," + COMMENT_IMAGE_UUID + " text not null)";

    private static final String DATABASE_REMOTE_COMMENT_CREATE = CREATE_TABLE + REMOTE_COMMENT_TABLE_NAME + DATABASE_COMMENT_CREATE;

    private static final String DATABASE_LOCAL_COMMENT_CREATE = CREATE_TABLE + LOCAL_COMMENT_TABLE_NAME + DATABASE_COMMENT_CREATE;

    private static final String DATABASE_SHARE_CREATE = " (" + SHARE_KEY_ID + " integer primary key autoincrement,"
            + SHARE_KEY_UUID + " text not null," + SHARE_KEY_CREATOR_UUID + " text not null,"
            + SHARE_KEY_TIME + " text not null," + SHARE_KEY_TITLE + " text," + SHARE_KEY_DESC + " text,"
            + SHARE_KEY_VIEWERS + " text not null,"
            + SHARE_KEY_MAINTAINERS + " text not null," + SHARE_KEY_IS_ALBUM + " integer not null,"
            + SHARE_KEY_IS_ARCHIVED + " integer not null," + SHARE_KEY_IS_COVER_IMAGE_DIGEST + " text not null,"
            + SHARE_KEY_IS_DATE + " text not null," + SHARE_KEY_IS_LOCAL + " integer not null,"
            + SHARE_KEY_DIGEST + " text," + SHARE_KEY_IS_STICKY + " integer not null)";

    private static final String DATABASE_LOCAL_SHARE_CREATE = CREATE_TABLE + LOCAL_SHARE_TABLE_NAME + DATABASE_SHARE_CREATE;

    private static final String DATABASE_REMOTE_SHARE_CREATE = CREATE_TABLE + REMOTE_SHARE_TABLE_NAME + DATABASE_SHARE_CREATE;

    private static final String DATABASE_MEDIA_CREATE = " (" + MEDIA_KEY_ID + " integer primary key autoincrement,"
            + MEDIA_KEY_UUID + " text not null," + MEDIA_KEY_TIME + " text not null," + MEDIA_KEY_WIDTH + " text not null,"
            + MEDIA_KEY_HEIGHT + " text not null," + MEDIA_KEY_THUMB + " text," + MEDIA_KEY_LOCAL + " integer not null,"
            + MEDIA_KEY_UPLOADED_DEVICE_ID + " text," + MEDIA_KEY_SHARING + " integer not null,"
            + MEDIA_KEY_ORIENTATION_NUMBER + " integer," + MEDIA_KEY_TYPE + " text," + MEDIA_KEY_MINI_THUMB + " text)";

    private static final String DATABASE_REMOTE_MEDIA_CREATE = CREATE_TABLE + REMOTE_MEDIA_TABLE_NAME + DATABASE_MEDIA_CREATE;

    private static final String DATABASE_LOCAL_MEDIA_CREATE = CREATE_TABLE + LOCAL_MEDIA_TABLE_NAME + DATABASE_MEDIA_CREATE;

    private static final String USER_FIELD_CREATE = " ("
            + USER_KEY_ID + " integer primary key autoincrement," + USER_KEY_UUID + " text not null,"
            + USER_KEY_USERNAME + " text not null," + USER_KEY_AVATAR + " text not null,"
            + USER_KEY_EMAIL + " text," + USER_KEY_DEFAULT_AVATAR + " text not null," + USER_KEY_DEFAULT_AVATAR_BG_COLOR + " integer not null,"
            + USER_KEY_HOME + " text not null," + USER_KEY_LIBRARY + " text not null," + USER_KEY_IS_ADMIN + " integer not null";

    private static final String DATABASE_REMOTE_USER_CREATE = CREATE_TABLE + REMOTE_USER_TABLE_NAME + USER_FIELD_CREATE + ")";

    private static final String DATABASE_MEDIA_SHARE_CONTENT_CREATE = " (" + SHARE_CONTENT_KEY_ID + " integer primary key autoincrement,"
            + SHARE_CONTENT_KEY_SHARE_UUID + " text not null," + SHARE_CONTENT_KEY_CREATOR_UUID + " text not null,"
            + SHARE_CONTENT_KEY_DIGEST + " text not null," + SHARE_CONTENT_KEY_TIME + " text not null)";

    private static final String DATABASE_REMOTE_MEDIA_SHARE_CONTENT_CREATE = CREATE_TABLE + REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME + DATABASE_MEDIA_SHARE_CONTENT_CREATE;
    private static final String DATABASE_LOCAL_MEDIA_SHARE_CONTENT_CREATE = CREATE_TABLE + LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME + DATABASE_MEDIA_SHARE_CONTENT_CREATE;

    private static final String DATABASE_DOWNLOADED_FILE_CREATE = CREATE_TABLE + DOWNLOADED_FILE_TABLE_NAME + " (" + FILE_KEY_ID + " integer primary key autoincrement,"
            + FILE_KEY_NAME + " text," + FILE_KEY_UUID + " text not null," + FILE_KEY_TIME + " text," + FILE_KEY_SIZE + " text," + FILE_KEY_CREATOR_UUID + " text not null)";

    private static final String DATABASE_LOGGED_IN_USER_CREATE = CREATE_TABLE + LOGGED_IN_USER_TABLE_NAME + USER_FIELD_CREATE + ","
            + LOGGED_IN_USER_GATEWAY + " text not null," + LOGGED_IN_USER_EQUIPMENT_NAME + " text not null," + LOGGED_IN_USER_TOKEN + " text not null,"
            + LOGGED_IN_USER_DEVICE_ID + " text not null)";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_REMOTE_COMMENT_CREATE);
        db.execSQL(DATABASE_LOCAL_COMMENT_CREATE);
        db.execSQL(DATABASE_LOCAL_SHARE_CREATE);
        db.execSQL(DATABASE_REMOTE_SHARE_CREATE);
        db.execSQL(DATABASE_REMOTE_MEDIA_CREATE);
        db.execSQL(DATABASE_LOCAL_MEDIA_CREATE);
        db.execSQL(DATABASE_REMOTE_USER_CREATE);
        db.execSQL(DATABASE_REMOTE_MEDIA_SHARE_CONTENT_CREATE);
        db.execSQL(DATABASE_LOCAL_MEDIA_SHARE_CONTENT_CREATE);
        db.execSQL(DATABASE_DOWNLOADED_FILE_CREATE);
        db.execSQL(DATABASE_LOGGED_IN_USER_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.i(TAG, "Upgrading database from version " + oldVersion + "to " +
                newVersion + ", which will destroy all old data");
        db.execSQL(DROP_TABLE + REMOTE_COMMENT_TABLE_NAME);
        db.execSQL(DROP_TABLE + LOCAL_COMMENT_TABLE_NAME);
        db.execSQL(DROP_TABLE + LOCAL_SHARE_TABLE_NAME);
        db.execSQL(DROP_TABLE + REMOTE_SHARE_TABLE_NAME);
        db.execSQL(DROP_TABLE + REMOTE_MEDIA_TABLE_NAME);
        db.execSQL(DROP_TABLE + LOCAL_MEDIA_TABLE_NAME);
        db.execSQL(DROP_TABLE + REMOTE_USER_TABLE_NAME);
        db.execSQL(DROP_TABLE + REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME);
        db.execSQL(DROP_TABLE + LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME);
        db.execSQL(DROP_TABLE + DOWNLOADED_FILE_TABLE_NAME);
        db.execSQL(DROP_TABLE + LOGGED_IN_USER_TABLE_NAME);

        onCreate(db);
    }
}
