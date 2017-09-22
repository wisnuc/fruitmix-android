package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.wechat.user.WeChatUser;

/**
 * Created by Administrator on 2017/9/20.
 */

public class LocalWeChatUserParser implements LocalDataParser<WeChatUser> {

    @Override
    public WeChatUser parse(Cursor cursor) {

        WeChatUser weChatUser = new WeChatUser();

        weChatUser.setStationID(cursor.getString(cursor.getColumnIndex(DBHelper.LOGGED_IN_WECHAT_USER_STATION_ID)));
        weChatUser.setGuid(cursor.getString(cursor.getColumnIndex(DBHelper.LOGGED_IN_WECHAT_USER_GUID)));
        weChatUser.setToken(cursor.getString(cursor.getColumnIndex(DBHelper.LOGGED_IN_WECAHT_USER_TOKEN)));

        return weChatUser;
    }
}
