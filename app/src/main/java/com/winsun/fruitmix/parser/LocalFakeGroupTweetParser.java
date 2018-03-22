package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.group.data.model.UserComment;

/**
 * Created by Administrator on 2018/3/21.
 */

public class LocalFakeGroupTweetParser extends LocalGroupTweetParser {

    @Override
    public UserComment parse(Cursor cursor) {
        UserComment userComment = super.parse(cursor);

        userComment.setFake(true);
        userComment.setRealUUIDWhenFake(cursor.getString(cursor.getColumnIndex(DBHelper.GROUP_COMMENT_DRAFT_KEY_REAL_COMMENT_UUID)));
        userComment.setFail(cursor.getInt(cursor.getColumnIndex(DBHelper.GROUP_COMMENT_DRAFT_KEY_IS_FAIL)) > 0);

        return userComment;
    }
}
