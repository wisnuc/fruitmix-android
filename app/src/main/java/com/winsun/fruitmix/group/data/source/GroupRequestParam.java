package com.winsun.fruitmix.group.data.source;

/**
 * Created by Administrator on 2018/2/7.
 */

public class GroupRequestParam {

    private String groupUUID;
    private String stationID;

    public GroupRequestParam(String groupUUID, String stationID) {
        this.groupUUID = groupUUID;
        this.stationID = stationID;
    }

    public String getStationID() {
        return stationID;
    }

    public String getGroupUUID() {
        return groupUUID;
    }
}
