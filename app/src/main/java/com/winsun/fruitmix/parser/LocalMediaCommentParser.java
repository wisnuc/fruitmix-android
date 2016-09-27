package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.model.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/2.
 */
public class LocalMediaCommentParser implements LocalDataParser<Comment> {

    @Override
    public Comment parse(Cursor cursor) {

        Comment comment = new Comment();
        comment.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COMMENT_KEY_ID)));
        comment.setCreator(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_CREATOR)));
        comment.setTime(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_TIME)));
        comment.setFormatTime(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_FORMAT_TIME)));
        comment.setShareId(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_SHARE_ID)));
        comment.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_TEXT)));

        return comment;
    }
}
