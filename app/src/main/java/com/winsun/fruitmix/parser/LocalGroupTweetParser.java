package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.user.User;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018/3/14.
 */

public class LocalGroupTweetParser implements LocalDataParser<UserComment> {

    @Override
    public UserComment parse(Cursor cursor) {

        LocalUserParser localUserParser = new LocalUserParser();
        User creator = localUserParser.parse(cursor);

        long storeTime = cursor.getLong(cursor.getColumnIndex(DBHelper.GROUP_COMMENT_KEY_STORE_TIME));
        String groupUUID = cursor.getString(cursor.getColumnIndex(DBHelper.GROUP_COMMENT_KEY_GROUP_UUID));
        String stationID = cursor.getString(cursor.getColumnIndex(DBHelper.GROUP_COMMENT_KEY_STATION_ID));

        String contentJsonStr = cursor.getString(cursor.getColumnIndex(DBHelper.GROUP_COMMENT_KEY_CONTENT));

        RemoteOneCommentParser remoteOneCommentParser = new RemoteOneCommentParser();
        try {

            UserComment userComment = remoteOneCommentParser.parse(new JSONObject(contentJsonStr));

            userComment.setStoreTime(storeTime);
            userComment.setGroupUUID(groupUUID);
            userComment.setStationID(stationID);

            creator.setAssociatedWeChatGUID(userComment.getCreator().getAssociatedWeChatGUID());

            userComment.setCreator(creator);

            return userComment;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

}
