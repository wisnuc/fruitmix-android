package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class LocalUserParser implements LocalDataParser<User> {

    @Override
    public User parse(Cursor cursor) {
        User user = new User();
        user.setUuid(cursor.getString(cursor.getColumnIndex(DBHelper.USER_KEY_UUID)));
        user.setUserName(cursor.getString(cursor.getColumnIndex(DBHelper.USER_KEY_USERNAME)));
        user.setAvatar(cursor.getString(cursor.getColumnIndex(DBHelper.USER_KEY_AVATAR)));
        user.setEmail(cursor.getString(cursor.getColumnIndex(DBHelper.USER_KEY_EMAIL)));
        user.setDefaultAvatar(cursor.getString(cursor.getColumnIndex(DBHelper.USER_KEY_DEFAULT_AVATAR)));
        user.setDefaultAvatarBgColor(cursor.getString(cursor.getColumnIndex(DBHelper.USER_KEY_DEFAULT_AVATAR_BG_COLOR)));
        user.setHome(cursor.getString(cursor.getColumnIndex(DBHelper.USER_KEY_HOME)));
        user.setLibrary(cursor.getString(cursor.getColumnIndex(DBHelper.USER_KEY_LIBRARY)));

        return user;
    }
}
