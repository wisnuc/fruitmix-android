package com.winsun.fruitmix.group.data.model;

import android.content.Context;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.user.DefaultCommentUser;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/7/20.
 */

public class UserComment {

    private String uuid;

    private long index;

    private User creator;

    private long time;

    private String groupUUID;

    private String stationID;

    public UserComment(String uuid, User creator, long time, String groupUUID, String stationID) {

        this.uuid = uuid;
        this.creator = creator;
        this.time = time;
        this.groupUUID = groupUUID;
        this.stationID = stationID;
    }

    public String getUuid() {
        return uuid;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getIndex() {
        return index;
    }

    public long getTime() {
        return time;
    }

    public String getGroupUUID() {
        return groupUUID;
    }

    public void setGroupUUID(String groupUUID) {
        this.groupUUID = groupUUID;
    }

    public String getStationID() {
        return stationID;
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getCreateUserName(Context context) {

        if (creator instanceof DefaultCommentUser)
            return context.getString(R.string.quited_user);
        else
            return creator.getUserName();

    }


    public String getDate(Context context) {

        return formatTime(context, time);

    }

    private String formatTime(Context context, long time) {

        return Util.formatShareTime(context, time, System.currentTimeMillis());

    }

}
