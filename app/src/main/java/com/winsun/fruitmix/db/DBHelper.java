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
    public static final String COMMENT_KEY_CREATOR = "creator";
    public static final String COMMENT_KEY_FORMAT_TIME = "format_time";
    public static final String COMMENT_KEY_TIME = "time";
    public static final String COMMENT_KEY_SHARE_ID = "share_id";
    public static final String COMMENT_KEY_TEXT = "text";
    public static final String COMMENT_IMAGE_UUID = "image_uuid";

    public static final String SHARE_KEY_ID = "id";
    public static final String SHARE_KEY_UUID = "share_uuid";
    public static final String SHARE_KEY_CREATOR = "share_creator";
    public static final String SHARE_KEY_TIME = "share_time";
    public static final String SHARE_KEY_TITLE = "share_title";
    public static final String SHARE_KEY_DESC = "share_desc";
    public static final String SHARE_KEY_DIGEST = "share_digest";
    public static final String SHARE_KEY_VIEWER = "share_viewer";
    public static final String SHARE_KEY_MAINTAINER = "share_maintainer";
    public static final String SHARE_KEY_IS_ALBUM = "share_is_album";

    public static final String USER_KEY_ID = "id";
    public static final String USER_KEY_USERNAME = "user_name";
    public static final String USER_KEY_UUID = "user_uuid";
    public static final String USER_KEY_AVATAR = "user_avatar";
    public static final String USER_KEY_EMAIL = "user_email";
    public static final String USER_KEY_DEFAULT_AVATAR = "user_default_avatar";
    public static final String USER_KEY_DEFAULT_AVATAR_BG_COLOR = "user_default_avatar_bg_color";

    public static final String MEDIA_KEY_ID = "id";
    public static final String MEDIA_KEY_UUID = "media_uuid";
    public static final String MEDIA_KEY_TIME = "media_time";
    public static final String MEDIA_KEY_WIDTH = "media_width";
    public static final String MEDIA_KEY_HEIGHT = "media_height";

    public static final String DB_NAME = "fruitmix";
    public static final String TASK_TABLE_NAME = "task";
    public static final String REMOTE_COMMENT_TABLE_NAME = "remote_comment";
    public static final String LOCAL_COMMENT_TABLE_NAME = "local_comment";
    public static final String LOCAL_SHARE_TABLE_NAME = "local_share";
    public static final String REMOTE_SHARE_TABLE_NAME = "remote_share";
    public static final String REMOTE_USER_TABLE_NAME = "remote_user";
    public static final String REMOTE_MEDIA_TABLE_NAME = "remote_media";

    public static final int DB_VERSION = 6;

    public static final String DATABASE_TASK_CREATE = "create table " + TASK_TABLE_NAME + " (" + TASK_KEY_ID + " integer primary key autoincrement," + TASK_KEY_HTTP_TYPE + " text not null," + TASK_KEY_OPERATION_TYPE + " text not null," + TASK_KEY_REQUEST + " text not null," + TASK_KEY_DATA + " text not null," + TASK_KEY_OPERATION_COUNT + " integer)";

    public static final String DATABASE_REMOTE_COMMENT_CREATE = "create table " + REMOTE_COMMENT_TABLE_NAME + " (" + COMMENT_KEY_ID + " integer primary key autoincrement," + COMMENT_KEY_CREATOR + " text not null," + COMMENT_KEY_TIME + " text not null," + COMMENT_KEY_FORMAT_TIME + " text not null," + COMMENT_KEY_SHARE_ID + " text not null," + COMMENT_KEY_TEXT + " text not null," + COMMENT_IMAGE_UUID + " text not null)";

    public static final String DATABASE_LOCAL_COMMENT_CREATE = "create table " + LOCAL_COMMENT_TABLE_NAME + " (" + COMMENT_KEY_ID + " integer primary key autoincrement," + COMMENT_KEY_CREATOR + " text not null," + COMMENT_KEY_TIME + " text not null," + COMMENT_KEY_FORMAT_TIME + " text not null," + COMMENT_KEY_SHARE_ID + " text not null," + COMMENT_KEY_TEXT + " text not null," + COMMENT_IMAGE_UUID + " text not null)";

    public static final String DATABASE_LOCAL_SHARE_CREATE = "create table " + LOCAL_SHARE_TABLE_NAME + " (" + SHARE_KEY_ID + " integer primary key autoincrement," + SHARE_KEY_UUID + " text not null," + SHARE_KEY_CREATOR + " text not null," + SHARE_KEY_TIME + " text not null," + SHARE_KEY_TITLE + " text not null," + SHARE_KEY_DESC + " text not null," + SHARE_KEY_DIGEST + " text not null," + SHARE_KEY_VIEWER + " text not null," + SHARE_KEY_MAINTAINER + " text not null," + SHARE_KEY_IS_ALBUM + " integer not null)";

    public static final String DATABASE_REMOTE_SHARE_CREATE = "create table " + REMOTE_SHARE_TABLE_NAME + " (" + SHARE_KEY_ID + " integer primary key autoincrement," + SHARE_KEY_UUID + " text not null," + SHARE_KEY_CREATOR + " text not null," + SHARE_KEY_TIME + " text not null," + SHARE_KEY_TITLE + " text not null," + SHARE_KEY_DESC + " text not null," + SHARE_KEY_DIGEST + " text not null," + SHARE_KEY_VIEWER + " text not null," + SHARE_KEY_MAINTAINER + " text not null," + SHARE_KEY_IS_ALBUM + " integer not null)";

    public static final String DATABASE_REMOTE_MEDIA_CREATE = "create table " + REMOTE_MEDIA_TABLE_NAME + " (" + MEDIA_KEY_ID + " integer primary key autoincrement," +  MEDIA_KEY_UUID + " text not null," + MEDIA_KEY_TIME + " text not null," + MEDIA_KEY_WIDTH + " text not null," + MEDIA_KEY_HEIGHT + " text not null)";

    public static final String DATABASE_REMOTE_USER_CREATE = "create table " + REMOTE_USER_TABLE_NAME + " (" + USER_KEY_ID + " integer primary key autoincrement," +  USER_KEY_UUID + " text not null," + USER_KEY_USERNAME + " text not null," + USER_KEY_AVATAR + " text not null," + USER_KEY_EMAIL + " text not null," +  USER_KEY_DEFAULT_AVATAR + " text not null," + USER_KEY_DEFAULT_AVATAR_BG_COLOR + " text not null)";

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
        db.execSQL(DATABASE_REMOTE_USER_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.i(TAG, "Upgrading database from version " + oldVersion + "to " +
                newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + REMOTE_COMMENT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LOCAL_COMMENT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LOCAL_SHARE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + REMOTE_SHARE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + REMOTE_MEDIA_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + REMOTE_USER_TABLE_NAME);
        onCreate(db);
    }
}
