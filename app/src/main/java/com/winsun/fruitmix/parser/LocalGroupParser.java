package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.group.data.model.PrivateGroup;

/**
 * Created by Administrator on 2018/3/14.
 */

public class LocalGroupParser implements LocalDataParser<PrivateGroup> {

    @Override
    public PrivateGroup parse(Cursor cursor) {

        String groupName = cursor.getString(cursor.getColumnIndex(DBHelper.GROUP_KEY_NAME));
        String groupUUID = cursor.getString(cursor.getColumnIndex(DBHelper.GROUP_KEY_UUID));
        String groupOwnerGUID = cursor.getString(cursor.getColumnIndex(DBHelper.GROUP_KEY_OWNER_GUID));
        long groupCreateTime = cursor.getLong(cursor.getColumnIndex(DBHelper.GROUP_KEY_CREATE_TIME));
        long groupModifyTime = cursor.getLong(cursor.getColumnIndex(DBHelper.GROUP_KEY_MODIFY_TIME));

        long groupUnreadCommentCount = cursor.getLong(cursor.getColumnIndex(DBHelper.GROUP_KEY_UNREAD_COMMENT_COUNT));
        String groupLocatedStationId = cursor.getString(cursor.getColumnIndex(DBHelper.GROUP_KEY_LOCATED_STATION_ID));

        long groupLastRetrieveCommentIndex = cursor.getLong(cursor.getColumnIndex(DBHelper.GROUP_KEY_LAST_RETRIEVE_COMMENT_INDEX));

        PrivateGroup group = new PrivateGroup(groupUUID, groupName, groupOwnerGUID, groupLocatedStationId);
        group.setUnreadCommentCount(groupUnreadCommentCount);
        group.setCreateTime(groupCreateTime);
        group.setModifyTime(groupModifyTime);
        group.setLastRetrievedCommentIndex(groupLastRetrieveCommentIndex);

        return group;

    }

}
