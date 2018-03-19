package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.group.data.model.GroupUserWrapper;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2018/3/14.
 */

public class LocalGroupUserParser implements LocalDataParser<GroupUserWrapper> {

    @Override
    public GroupUserWrapper parse(Cursor cursor) {

        LocalUserParser parser = new LocalUserParser();
        User user = parser.parse(cursor);

        String userGUID = cursor.getString(cursor.getColumnIndex(DBHelper.GROUP_USER_KEY_ASSOCIATED_WECHAT_GUID));
        String groupUUID = cursor.getString(cursor.getColumnIndex(DBHelper.GROUP_USER_KEY_GROUP_UUID));

        user.setAssociatedWeChatGUID(userGUID);

        return new GroupUserWrapper(user, groupUUID);

    }

}
