package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.user.User;

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
        user.setDefaultAvatarBgColor(cursor.getInt(cursor.getColumnIndex(DBHelper.USER_KEY_DEFAULT_AVATAR_BG_COLOR)));
        user.setHome(cursor.getString(cursor.getColumnIndex(DBHelper.USER_KEY_HOME)));
        user.setLibrary(cursor.getString(cursor.getColumnIndex(DBHelper.USER_KEY_LIBRARY)));
        user.setAdmin(cursor.getInt(cursor.getColumnIndex(DBHelper.USER_KEY_IS_ADMIN)) == 1);

        if(cursor.getColumnIndex(DBHelper.USER_KEY_IS_FIRST_USER) != -1){
            user.setFirstUser(cursor.getInt(cursor.getColumnIndex(DBHelper.USER_KEY_IS_FIRST_USER)) == 1);
        }

        if (cursor.getColumnIndex(DBHelper.USER_ASSOCIATED_WECHAT_USER_NAME) != -1) {
            user.setAssociatedWeChatUserName(cursor.getString(cursor.getColumnIndex(DBHelper.USER_ASSOCIATED_WECHAT_USER_NAME)));
        }

        return user;
    }
}
