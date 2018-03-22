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

    public static final int ADD_FILE_KEY_CREATOR_UUID_DB_VERSION = 21;

    public static final int ADD_MEDIA_LATITUDE_LONGITUDE_DB_VERSION = 28;

    public static final int ADD_LOGGED_IN_WECHAT_USER_DB_VERSION = 30;

    public static final int ADD_USER_ASSOCIATED_WECHAT_USER_NAME_DB_VERSION = 31;

    public static final int ADD_LOCAL_VIDEO_TABLE_VERSION = 32;

    public static final int ADD_REMOTE_VIDEO_TABLE_VERSION = 33;

    public static final int ADD_UPLOAD_FILE_TABLE_VERSION = 34;

    public static final int ADD_USER_IS_FIRST_USER_VERSION = 35;

    public static final int ADD_GROUP_STATION_AND_COMMENT_VERSION = 36;

    public static final int MODIFY_STATION_TABLE_COLUMN = 37;

    public static final int ADD_GROUP_LAST_RETRIEVE_COMMENT_INDEX_VERSION = 38;

    public static final int ADD_GROUP_COMMENT_DRAFT_TABLE_VERSION = 39;

    public static final int ADD_GROUP_COMMENT_DRAFT_KEY_IS_FAIL_VERSION = 40;

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
    public static final String USER_KEY_IS_FIRST_USER = "is_first_user";
    public static final String USER_ASSOCIATED_WECHAT_USER_NAME = "user_associated_wechat_user_name";

    public static final String MEDIA_KEY_ID = "id";
    public static final String MEDIA_KEY_UUID = "media_uuid";
    public static final String MEDIA_KEY_FORMATTED_TIME = "media_time";
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

    public static final String VIDEO_KEY_NAME = "video_key_name";
    public static final String VIDEO_KEY_SIZE = "video_key_size";
    public static final String VIDEO_KEY_DURATION = "video_key_duration";

    public static final String FILE_KEY_ID = "id";
    public static final String FILE_KEY_NAME = "file_name";
    public static final String FILE_KEY_UUID = "file_uuid";
    public static final String FILE_KEY_TIME = "file_time";
    public static final String FILE_KEY_SIZE = "file_size";
    public static final String FILE_KEY_CREATOR_UUID = "file_creator_uuid";

    public static final String FILE_KEY_PATH_SOURCE_FROM_OTHER_APP = "file_path_source_from_other_app";
    public static final String FILE_KEY_UPLOAD_TASK_STATE = "file_upload_task_state";

    public static final String LOGGED_IN_USER_GATEWAY = "logged_in_user_gateway";
    public static final String LOGGED_IN_USER_EQUIPMENT_NAME = "logged_in_user_equipment_name";
    public static final String LOGGED_IN_USER_TOKEN = "logged_in_user_token";
    public static final String LOGGED_IN_USER_DEVICE_ID = "logged_in_user_device_id";

    public static final String LOGGED_IN_WECHAT_USER_ID = "id";
    public static final String LOGGED_IN_WECHAT_USER_GUID = "logged_in_wechat_user_guid";
    public static final String LOGGED_IN_WECAHT_USER_TOKEN = "logged_in_wechat_user_token";
    public static final String LOGGED_IN_WECHAT_USER_STATION_ID = "logged_in_wechat_user_station_id";

    public static final String GROUP_KEY_ID = "id";
    public static final String GROUP_KEY_UUID = "group_key_uuid";
    public static final String GROUP_KEY_NAME = "group_key_name";
    public static final String GROUP_KEY_OWNER_GUID = "group_key_owner_guid";
    public static final String GROUP_KEY_CREATE_TIME = "group_key_create_time";
    public static final String GROUP_KEY_MODIFY_TIME = "group_key_modify_time";
    public static final String GROUP_KEY_LAST_READ_COMMENT_INDEX = "group_key_last_read_comment_index";
    public static final String GROUP_KEY_LOCATED_STATION_ID = "group_key_located_station_id";
    public static final String GROUP_KEY_STORE_USER_GUID = "group_key_store_user_guid";
    public static final String GROUP_KEY_LAST_RETRIEVE_COMMENT_INDEX = "group_key_last_retrieve_comment_index";

    public static final String ID = "id";

    public static final String STATION_KEY_ID = "station_key_id";
    public static final String STATION_KEY_NAME = "station_key_name";

    public static final String GROUP_USER_KEY_GROUP_UUID = "group_user_key_group_uuid";
    public static final String GROUP_USER_KEY_ASSOCIATED_WECHAT_GUID = "group_user_key_associated_wechat_user_guid";
    public static final String GROUP_USER_KEY_STORE_USER_GUID = "group_user_key_store_user_guid";

    /*    public static final String GROUP_COMMENT_KEY_UUID = "group_comment_key_uuid";
        public static final String GROUP_COMMENT_KEY_CREATE_TIME = "group_comment_key_create_time";
        public static final String GROUP_COMMENT_KEY_STORE_TIME = "group_comment_key_store_time";
        public static final String GROUP_COMMENT_KEY_CONTENT = "group_comment_key_content";
        public static final String GROUP_COMMENT_KEY_GROUP_UUID = "group_comment_key_group_uuid";
        public static final String GROUP_COMMENT_KEY_STATION_ID = "group_comment_key_station_id";
        public static final String GROUP_COMMENT_KEY_INDEX = "group_comment_key_index";*/

    public static final String GROUP_COMMENT_KEY_STORE_TIME = "group_comment_key_store_time";
    public static final String GROUP_COMMENT_KEY_GROUP_UUID = "group_comment_key_group_uuid";
    public static final String GROUP_COMMENT_KEY_STATION_ID = "group_comment_key_station_id";
    public static final String GROUP_COMMENT_KEY_CONTENT = "group_comment_key_content";
    public static final String GROUP_COMMENT_KEY_STORE_USER_GUID = "group_comment_key_store_user_guid";

    public static final String GROUP_COMMENT_DRAFT_KEY_FAKE_COMMENT_UUID = "group_comment_draft_key_fake_comment_uuid";
    public static final String GROUP_COMMENT_DRAFT_KEY_REAL_COMMENT_UUID = "group_comment_draft_key_real_comment_uuid";
    public static final String GROUP_COMMENT_DRAFT_KEY_IS_FAIL = "group_comment_draft_key_is_fail";

    private static final String DB_NAME = "fruitmix";
    static final String REMOTE_USER_TABLE_NAME = "remote_user";
    static final String REMOTE_MEDIA_TABLE_NAME = "remote_media";
    static final String LOCAL_MEDIA_TABLE_NAME = "local_media";
    static final String DOWNLOADED_FILE_TABLE_NAME = "downloaded_file";
    static final String LOGGED_IN_USER_TABLE_NAME = "logged_in_user";
    static final String LOGGED_IN_WECHAT_USER_TABLE_NAME = "logged_in_wechat_user";
    static final String LOCAL_VIDEO_TABLE_NAME = "local_video";
    static final String REMOTE_VIDEO_TABLE_NAME = "remote_video";
    static final String UPLOAD_FILE_TABLE_NAME = "upload_file";

    static final String REMOTE_GROUP_TABLE_NAME = "remote_group";
    static final String REMOTE_GROUP_USER_TABLE_NAME = "remote_group_user";

    static final String REMOTE_GROUP_LAST_TWEET_TABLE_NAME = "remote_group_last_tweet";

    static final String REMOTE_GROUP_TWEET_TABLE_NAME = "remote_group_tweet";
    static final String REMOTE_STATION_TABLE_NAME = "remote_station";

    static final String REMOTE_GROUP_TWEET_DRAFT_TABLE_NAME = "remote_group_tweet_draft";

    private static final String CREATE_TABLE = "create table if not exists ";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    private static final String BEGIN_SQL = " (";

    private static final String END_SQL = ")";

    public static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = " integer primary key autoincrement,";

    public static final String INTEGER_PRIMARY_KEY = " integer primary key,";

    public static final String TEXT_NOT_NULL = " text not null,";

    public static final String INTEGER_NOT_NULL = " integer not null,";

    public static final String INTEGER = " integer,";

    public static final String INTEGER_WITHOUT_COMMA = " integer";

    public static final String TEXT = " text,";

    public static final String TEXT_WITHOUT_COMMA = " text";

    public static final String TEXT_NOT_NULL_WITHOUT_COMMA = " text not null";

    public static final String INTEGER_NOT_NULL_WITHOUT_COMMA = " integer not null";

    public static final String COMMA = ",";

    private static final String DATABASE_MEDIA_CREATE = BEGIN_SQL + MEDIA_KEY_ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT
            + MEDIA_KEY_UUID + TEXT_NOT_NULL + MEDIA_KEY_FORMATTED_TIME + TEXT_NOT_NULL + MEDIA_KEY_WIDTH + TEXT_NOT_NULL
            + MEDIA_KEY_HEIGHT + TEXT_NOT_NULL + MEDIA_KEY_THUMB + TEXT + MEDIA_KEY_LOCAL + INTEGER_NOT_NULL
            + MEDIA_KEY_UPLOADED_USER_UUID + TEXT + MEDIA_KEY_SHARING + INTEGER_NOT_NULL
            + MEDIA_KEY_ORIENTATION_NUMBER + INTEGER + MEDIA_KEY_TYPE + TEXT
            + MEDIA_KEY_MINI_THUMB + TEXT + MEDIA_KEY_ORIGINAL_PHOTO_PATH + TEXT
            + MEDIA_KEY_LONGITUDE + TEXT + MEDIA_KEY_LATITUDE + TEXT_WITHOUT_COMMA;

    private static final String DATABASE_REMOTE_MEDIA_CREATE = CREATE_TABLE + REMOTE_MEDIA_TABLE_NAME + DATABASE_MEDIA_CREATE + END_SQL;

    private static final String DATABASE_LOCAL_MEDIA_CREATE = CREATE_TABLE + LOCAL_MEDIA_TABLE_NAME + DATABASE_MEDIA_CREATE + END_SQL;

    private static final String USER_FIELD_CREATE = BEGIN_SQL
            + USER_KEY_ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT + USER_KEY_UUID + TEXT_NOT_NULL
            + USER_KEY_USERNAME + TEXT_NOT_NULL + USER_KEY_AVATAR + TEXT_NOT_NULL
            + USER_KEY_EMAIL + TEXT + USER_KEY_DEFAULT_AVATAR + TEXT_NOT_NULL + USER_KEY_DEFAULT_AVATAR_BG_COLOR + INTEGER_NOT_NULL
            + USER_KEY_HOME + TEXT_NOT_NULL + USER_KEY_LIBRARY + TEXT_NOT_NULL + USER_KEY_IS_ADMIN + INTEGER_NOT_NULL_WITHOUT_COMMA;

    private static final String DATABASE_REMOTE_USER_CREATE = CREATE_TABLE + REMOTE_USER_TABLE_NAME + USER_FIELD_CREATE + COMMA
            + USER_KEY_IS_FIRST_USER + TEXT_NOT_NULL + USER_ASSOCIATED_WECHAT_USER_NAME + TEXT_WITHOUT_COMMA + END_SQL;

    public static final String FILE_FIELD_CREATE = BEGIN_SQL + FILE_KEY_ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT
            + FILE_KEY_NAME + TEXT + FILE_KEY_UUID + TEXT_NOT_NULL + FILE_KEY_TIME + TEXT + FILE_KEY_SIZE + TEXT + FILE_KEY_CREATOR_UUID + TEXT_NOT_NULL_WITHOUT_COMMA;

    private static final String DATABASE_DOWNLOADED_FILE_CREATE = CREATE_TABLE + DOWNLOADED_FILE_TABLE_NAME + FILE_FIELD_CREATE + END_SQL;

    public static final String DATABASE_UPLOAD_FILE_CREATE = CREATE_TABLE + UPLOAD_FILE_TABLE_NAME + FILE_FIELD_CREATE
            + COMMA + FILE_KEY_PATH_SOURCE_FROM_OTHER_APP + TEXT_NOT_NULL + FILE_KEY_UPLOAD_TASK_STATE + INTEGER_NOT_NULL_WITHOUT_COMMA + END_SQL;

    private static final String DATABASE_LOGGED_IN_USER_CREATE = CREATE_TABLE + LOGGED_IN_USER_TABLE_NAME + USER_FIELD_CREATE + COMMA
            + LOGGED_IN_USER_GATEWAY + TEXT_NOT_NULL + LOGGED_IN_USER_EQUIPMENT_NAME + TEXT_NOT_NULL + LOGGED_IN_USER_TOKEN + TEXT_NOT_NULL
            + LOGGED_IN_USER_DEVICE_ID + TEXT_NOT_NULL_WITHOUT_COMMA + END_SQL;

    private static final String DATABASE_LOGGED_IN_WECHAT_USER_CREATE = CREATE_TABLE + LOGGED_IN_WECHAT_USER_TABLE_NAME
            + BEGIN_SQL + LOGGED_IN_WECHAT_USER_ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT + LOGGED_IN_WECHAT_USER_GUID + TEXT_NOT_NULL
            + LOGGED_IN_WECAHT_USER_TOKEN + TEXT_NOT_NULL + LOGGED_IN_WECHAT_USER_STATION_ID + TEXT_NOT_NULL_WITHOUT_COMMA + END_SQL;

    private static final String DATABASE_LOCAL_VIDEO_CREATE = CREATE_TABLE + LOCAL_VIDEO_TABLE_NAME + DATABASE_MEDIA_CREATE
            + COMMA + VIDEO_KEY_NAME + TEXT_NOT_NULL + VIDEO_KEY_SIZE + INTEGER_NOT_NULL
            + VIDEO_KEY_DURATION + INTEGER_NOT_NULL_WITHOUT_COMMA + END_SQL;

    private static final String DATABASE_REMOTE_VIDEO_CREATE = CREATE_TABLE + REMOTE_VIDEO_TABLE_NAME + DATABASE_MEDIA_CREATE
            + COMMA + VIDEO_KEY_NAME + TEXT + VIDEO_KEY_SIZE + INTEGER_WITHOUT_COMMA + COMMA
            + VIDEO_KEY_DURATION + INTEGER_WITHOUT_COMMA + END_SQL;

    public static final String DATABASE_REMOTE_GROUP_CREATE = CREATE_TABLE + REMOTE_GROUP_TABLE_NAME + BEGIN_SQL
            + GROUP_KEY_ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT + GROUP_KEY_UUID + TEXT_NOT_NULL +
            GROUP_KEY_NAME + TEXT_NOT_NULL + GROUP_KEY_OWNER_GUID + TEXT_NOT_NULL + GROUP_KEY_CREATE_TIME + INTEGER_NOT_NULL
            + GROUP_KEY_MODIFY_TIME + INTEGER_NOT_NULL + GROUP_KEY_LOCATED_STATION_ID + TEXT_NOT_NULL
            + GROUP_KEY_STORE_USER_GUID + TEXT_NOT_NULL
            + GROUP_KEY_LAST_RETRIEVE_COMMENT_INDEX + INTEGER_NOT_NULL
            + GROUP_KEY_LAST_READ_COMMENT_INDEX + INTEGER_NOT_NULL_WITHOUT_COMMA + END_SQL;

    public static final String DATABASE_REMOTE_GROUP_USER_CREATE = CREATE_TABLE + REMOTE_GROUP_USER_TABLE_NAME
            + USER_FIELD_CREATE + COMMA + GROUP_USER_KEY_GROUP_UUID + TEXT_NOT_NULL
            + GROUP_USER_KEY_ASSOCIATED_WECHAT_GUID + TEXT_NOT_NULL
            + GROUP_USER_KEY_STORE_USER_GUID + TEXT_NOT_NULL_WITHOUT_COMMA + END_SQL;

/*    public static final String DATABASE_REMOTE_GROUP_COMMENT_CREATE = CREATE_TABLE + REMOTE_GROUP_TWEET_TABLE_NAME +
            USER_FIELD_CREATE + COMMA + GROUP_COMMENT_KEY_UUID + TEXT_NOT_NULL + GROUP_COMMENT_KEY_CREATE_TIME + INTEGER_NOT_NULL
            + GROUP_COMMENT_KEY_STORE_TIME + INTEGER_NOT_NULL
            + GROUP_COMMENT_KEY_CONTENT + TEXT_NOT_NULL + GROUP_COMMENT_KEY_GROUP_UUID + TEXT_NOT_NULL
            + GROUP_COMMENT_KEY_STATION_ID + TEXT_NOT_NULL + GROUP_COMMENT_KEY_INDEX + INTEGER_NOT_NULL_WITHOUT_COMMA + END_SQL;*/

    private static final String REMOTE_GROUP_COMMENT_FIELD_CREATE =
            USER_FIELD_CREATE + COMMA + GROUP_COMMENT_KEY_STORE_TIME + INTEGER
                    + GROUP_COMMENT_KEY_GROUP_UUID + TEXT_NOT_NULL
                    + GROUP_COMMENT_KEY_STATION_ID + TEXT_NOT_NULL
                    + GROUP_COMMENT_KEY_STORE_USER_GUID + TEXT_NOT_NULL +
                    GROUP_COMMENT_KEY_CONTENT + TEXT_NOT_NULL_WITHOUT_COMMA;

    public static final String DATABASE_REMOTE_GROUP_COMMENT_CREATE = CREATE_TABLE + REMOTE_GROUP_TWEET_TABLE_NAME +
            REMOTE_GROUP_COMMENT_FIELD_CREATE + END_SQL;

    public static final String DATABASE_REMOTE_GROUP_LAST_COMMENT_CREATE = CREATE_TABLE + REMOTE_GROUP_LAST_TWEET_TABLE_NAME
            + REMOTE_GROUP_COMMENT_FIELD_CREATE + END_SQL;

    public static final String DATABASE_REMOTE_STATION_CREATE = CREATE_TABLE + REMOTE_STATION_TABLE_NAME + BEGIN_SQL
            + ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT
            + STATION_KEY_ID + TEXT_NOT_NULL + STATION_KEY_NAME + TEXT_NOT_NULL_WITHOUT_COMMA + END_SQL;

    public static final String DATABASE_REMOTE_GROUP_TWEET_DRAFT_CREATE = CREATE_TABLE + REMOTE_GROUP_TWEET_DRAFT_TABLE_NAME
            + REMOTE_GROUP_COMMENT_FIELD_CREATE + COMMA
            + GROUP_COMMENT_DRAFT_KEY_FAKE_COMMENT_UUID + TEXT_NOT_NULL
            + GROUP_COMMENT_DRAFT_KEY_IS_FAIL + INTEGER_NOT_NULL
            + GROUP_COMMENT_DRAFT_KEY_REAL_COMMENT_UUID + TEXT_WITHOUT_COMMA + END_SQL;


    private static final int DB_VERSION = ADD_GROUP_COMMENT_DRAFT_KEY_IS_FAIL_VERSION;

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

        db.execSQL(DATABASE_LOCAL_VIDEO_CREATE);

        db.execSQL(DATABASE_REMOTE_VIDEO_CREATE);

        db.execSQL(DATABASE_UPLOAD_FILE_CREATE);

        db.execSQL(DATABASE_REMOTE_GROUP_CREATE);

        db.execSQL(DATABASE_REMOTE_GROUP_COMMENT_CREATE);

        db.execSQL(DATABASE_REMOTE_GROUP_USER_CREATE);

        db.execSQL(DATABASE_REMOTE_STATION_CREATE);

        db.execSQL(DATABASE_REMOTE_GROUP_LAST_COMMENT_CREATE);

        db.execSQL(DATABASE_REMOTE_GROUP_TWEET_DRAFT_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.i(TAG, "Upgrading database from version " + oldVersion + "to " +
                newVersion);

        if (oldVersion < ADD_FILE_KEY_CREATOR_UUID_DB_VERSION) {

            db.execSQL(DROP_TABLE + DOWNLOADED_FILE_TABLE_NAME);

        }

        if (oldVersion < ADD_MEDIA_LATITUDE_LONGITUDE_DB_VERSION) {

            db.execSQL(DROP_TABLE + REMOTE_MEDIA_TABLE_NAME);
            db.execSQL(DROP_TABLE + LOCAL_MEDIA_TABLE_NAME);
            db.execSQL(DROP_TABLE + REMOTE_USER_TABLE_NAME);

        }

        if (oldVersion < ADD_LOGGED_IN_WECHAT_USER_DB_VERSION) {

            db.execSQL(DROP_TABLE + LOGGED_IN_WECHAT_USER_TABLE_NAME);

        }

        if (oldVersion < ADD_USER_ASSOCIATED_WECHAT_USER_NAME_DB_VERSION) {

            db.execSQL(DROP_TABLE + REMOTE_USER_TABLE_NAME);

        }

        if (oldVersion < ADD_LOCAL_VIDEO_TABLE_VERSION) {

            db.execSQL(DROP_TABLE + LOCAL_VIDEO_TABLE_NAME);

        }

        if (oldVersion < ADD_REMOTE_VIDEO_TABLE_VERSION)
            db.execSQL(DROP_TABLE + REMOTE_VIDEO_TABLE_NAME);

        if (oldVersion < ADD_UPLOAD_FILE_TABLE_VERSION)
            db.execSQL(DROP_TABLE + UPLOAD_FILE_TABLE_NAME);

        if (oldVersion < ADD_USER_IS_FIRST_USER_VERSION)
            db.execSQL(DROP_TABLE + REMOTE_USER_TABLE_NAME);

        if (oldVersion < ADD_GROUP_STATION_AND_COMMENT_VERSION) {

            db.execSQL(DROP_TABLE + REMOTE_GROUP_TABLE_NAME);
            db.execSQL(DROP_TABLE + REMOTE_GROUP_TWEET_TABLE_NAME);
            db.execSQL(DROP_TABLE + REMOTE_GROUP_USER_TABLE_NAME);
            db.execSQL(DROP_TABLE + REMOTE_STATION_TABLE_NAME);
            db.execSQL(DROP_TABLE + REMOTE_GROUP_LAST_TWEET_TABLE_NAME);

        }

        if (oldVersion < MODIFY_STATION_TABLE_COLUMN) {

            db.execSQL(DROP_TABLE + REMOTE_STATION_TABLE_NAME);

        }

        if (oldVersion < ADD_GROUP_LAST_RETRIEVE_COMMENT_INDEX_VERSION) {

            db.execSQL(DROP_TABLE + REMOTE_GROUP_TABLE_NAME);

        }

        if (oldVersion < ADD_GROUP_COMMENT_DRAFT_TABLE_VERSION) {
            db.execSQL(DROP_TABLE + REMOTE_GROUP_TWEET_DRAFT_TABLE_NAME);
        }

        if (oldVersion < ADD_GROUP_COMMENT_DRAFT_KEY_IS_FAIL_VERSION) {
            db.execSQL(DROP_TABLE + REMOTE_GROUP_TWEET_DRAFT_TABLE_NAME);
        }

        onCreate(db);

    }
}
