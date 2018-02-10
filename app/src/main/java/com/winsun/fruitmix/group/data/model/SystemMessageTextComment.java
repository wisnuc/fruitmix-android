package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SystemMessageTextComment extends TextComment{

    protected SystemMessageTextComment(String uuid, User creator, long time, String groupUUID, String stationID) {
        super(uuid, creator, time, groupUUID, stationID);
    }

    public SystemMessageTextComment(String uuid, User creator, long time, String groupUUID, String stationID, String text) {
        super(uuid, creator, time, groupUUID, stationID, text);
    }



}
