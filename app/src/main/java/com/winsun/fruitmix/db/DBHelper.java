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

    public static final int ADD_MEDIA_LATITUDE_L0NGITUDE_DB_VERSION = 28;

    public static final int ADD_LOGGED_IN_WECHAT_USER_DB_VERSION = 30;

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
    public static final String MEDIA_KEY_UPLOADED_USER_UUID = "media_key_uploaded_device_id";
    public static final String MEDIA_KEY_SHARING = "media_key_sharing";
    public static final String MEDIA_KEY_ORIENTATION_NUMBER = "media_key_orientation_number";
    public static final String MEDIA_KEY_TYPE = "media_key_type";
    public static final String MEDIA_KEY_MINI_THUMB = "media_key_mini_thumb";
    public static final String MEDIA_KEY_ORIGINAL_PHOTO_PATH = "media_key_original_photo_path";
    public static final String MEDIA_KEY_LATITUDE = "media_key_latitude";
    public static final String MEDIA_KEY_LONGITUDE = "media_key_longitude";

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

    public static final String LOGGED_IN_WECHAT_USER_ID = "id";
    public static final String LOGGED_IN_WECHAT_USER_GUID = "logged_in_wechat_user_guid";
    public static final String LOGGED_IN_WECAHT_USER_TOKEN = "logged_in_wechat_user_token";
    public static final String LOGGED_IN_WECHAT_USER_STATION_ID = "logged_in_wechat_user_station_id";

    private static final String DB_NAME = "fruitmix";
    private static final String REMOTE_COMMENT_TABLE_NAME = "remote_comment";
    private static final String LOCAL_COMMENT_TABLE_NAME = "local_comment";
    private static final String LOCAL_SHARE_TABLE_NAME = "local_share";
    private static final String REMOTE_SHARE_TABLE_NAME = "remote_share";
    static final String REMOTE_USER_TABLE_NAME = "remote_user";
    static final String REMOTE_MEDIA_TABLE_NAME = "remote_media";
    static final String LOCAL_MEDIA_TABLE_NAME = "local_media";
    private static final String REMOTE_MEDIA_SHARE_CONTENT_TABLE_NAME = "remote_media_share_content";
    private static final String LOCAL_MEDIA_SHARE_CONTENT_TABLE_NAME = "local_media_share_content";
    static final String DOWNLOADED_FILE_TABLE_NAME = "downloaded_file";
    static final String LOGGED_IN_USER_TABLE_NAME = "logged_in_user";
    public static final String LOGGED_IN_WECHAT_USER_TABLE_NAME = "logged_in_wechat_user";

    private static final int DB_VERSION = 30;

    private static final String CREATE_TABLE = "create table if not exists ";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    private static final String BEGIN_SQL = " (";

    private static final String END_SQL = ")";

    public static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = " integer primary key autoincrement,";

    public static final String TEXT_NOT_NULL = " text not null,";

    public static final String INTEGER_NOT_NULL = " integer not null,";

    public static final String TEXT = " text,";

    public static final String TEXT_NOT_NULL_WITHOUT_COMMA = " text not null";

    private static final String DATABASE_MEDIA_CREATE = BEGIN_SQL + MEDIA_KEY_ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT
            + MEDIA_KEY_UUID + TEXT_NOT_NULL + MEDIA_KEY_TIME + TEXT_NOT_NULL + MEDIA_KEY_WIDTH + TEXT_NOT_NULL
            + MEDIA_KEY_HEIGHT + TEXT_NOT_NULL + MEDIA_KEY_THUMB + TEXT + MEDIA_KEY_LOCAL + INTEGER_NOT_NULL
            + MEDIA_KEY_UPLOADED_USER_UUID + TEXT + MEDIA_KEY_SHARING + INTEGER_NOT_NULL
            + MEDIA_KEY_ORIENTATION_NUMBER + " integer," + MEDIA_KEY_TYPE + TEXT
            + MEDIA_KEY_MINI_THUMB + TEXT + MEDIA_KEY_ORIGINAL_PHOTO_PATH + TEXT
            + MEDIA_KEY_LONGITUDE + TEXT + MEDIA_KEY_LATITUDE + " text" + END_SQL;

    private static final String DATABASE_REMOTE_MEDIA_CREATE = CREATE_TABLE + REMOTE_MEDIA_TABLE_NAME + DATABASE_MEDIA_CREATE;

    private static final String DATABASE_LOCAL_MEDIA_CREATE = CREATE_TABLE + LOCAL_MEDIA_TABLE_NAME + DATABASE_MEDIA_CREATE;

    private static final String USER_FIELD_CREATE = BEGIN_SQL
            + USER_KEY_ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT + USER_KEY_UUID + TEXT_NOT_NULL
            + USER_KEY_USERNAME + TEXT_NOT_NULL + USER_KEY_AVATAR + TEXT_NOT_NULL
            + USER_KEY_EMAIL + TEXT + USER_KEY_DEFAULT_AVATAR + TEXT_NOT_NULL + USER_KEY_DEFAULT_AVATAR_BG_COLOR + INTEGER_NOT_NULL
            + USER_KEY_HOME + TEXT_NOT_NULL + USER_KEY_LIBRARY + TEXT_NOT_NULL + USER_KEY_IS_ADMIN + " integer not null";

    private static final String DATABASE_REMOTE_USER_CREATE = CREATE_TABLE + REMOTE_USER_TABLE_NAME + USER_FIELD_CREATE + END_SQL;


    private static final String DATABASE_DOWNLOADED_FILE_CREATE = CREATE_TABLE + DOWNLOADED_FILE_TABLE_NAME + BEGIN_SQL + FILE_KEY_ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT
            + FILE_KEY_NAME + TEXT + FILE_KEY_UUID + TEXT_NOT_NULL + FILE_KEY_TIME + TEXT + FILE_KEY_SIZE + TEXT + FILE_KEY_CREATOR_UUID + TEXT_NOT_NULL_WITHOUT_COMMA + END_SQL;

    private static final String DATABASE_LOGGED_IN_USER_CREATE = CREATE_TABLE + LOGGED_IN_USER_TABLE_NAME + USER_FIELD_CREATE + ","
            + LOGGED_IN_USER_GATEWAY + TEXT_NOT_NULL + LOGGED_IN_USER_EQUIPMENT_NAME + TEXT_NOT_NULL + LOGGED_IN_USER_TOKEN + TEXT_NOT_NULL
            + LOGGED_IN_USER_DEVICE_ID + TEXT_NOT_NULL_WITHOUT_COMMA + END_SQL;

    private static final String DATABASE_LOGGED_IN_WECHAT_USER_CREATE = CREATE_TABLE + LOGGED_IN_WECHAT_USER_TABLE_NAME
            + BEGIN_SQL + LOGGED_IN_WECHAT_USER_ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT + LOGGED_IN_WECHAT_USER_GUID + TEXT_NOT_NULL
            + LOGGED_IN_WECAHT_USER_TOKEN + TEXT_NOT_NULL + LOGGED_IN_WECHAT_USER_STATION_ID + TEXT_NOT_NULL_WITHOUT_COMMA + END_SQL;


    DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(DATABASE_REMOTE_MEDIA_CREATE);
        db.execSQL(DATABASE_LOCAL_MEDIA_CREATE);
        db.execSQL(DATABASE_REMOTE_USER_CREATE);

        db.execSQL(DATABASE_DOWNLOADED_FILE_CREATE);
        db.execSQL(DATABASE_LOGGED_IN_USER_CREATE);

        db.execSQL(DATABASE_LOGGED_IN_WECHAT_USER_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.i(TAG, "Upgrading database from version " + oldVersion + "to " +
                newVersion);

        if (oldVersion < ADD_MEDIA_LATITUDE_L0NGITUDE_DB_VERSION) {

            db.execSQL(DROP_TABLE + REMOTE_MEDIA_TABLE_NAME);
            db.execSQL(DROP_TABLE + LOCAL_MEDIA_TABLE_NAME);
            db.execSQL(DROP_TABLE + REMOTE_USER_TABLE_NAME);

            onCreate(db);
        }else if(oldVersion < ADD_LOGGED_IN_WECHAT_USER_DB_VERSION){

            db.execSQL(DROP_TABLE + LOGGED_IN_WECHAT_USER_TABLE_NAME);

            onCreate(db);
        }


    }
}
