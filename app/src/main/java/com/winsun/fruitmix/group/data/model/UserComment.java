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

    private long createTime;

    private long storeTime;

    private String groupUUID;

    private String stationID;

    private String contentJsonStr;

    private boolean isFake;

    private String realUUIDWhenFake;

    private boolean isFail;

    public UserComment(String uuid, User creator, long createTime, String groupUUID, String stationID) {

        this.uuid = uuid;
        this.creator = creator;
        this.createTime = createTime;
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

    public long getCreateTime() {
        return createTime;
    }

    public void setStoreTime(long storeTime) {
        this.storeTime = storeTime;
    }

    public long getStoreTime() {
        return storeTime;
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

        return formatTime(context, createTime);

    }

    private String formatTime(Context context, long time) {

        return Util.formatShareTime(context, time, System.currentTimeMillis());

    }

    public void setContentJsonStr(String contentJsonStr) {
        this.contentJsonStr = contentJsonStr;
    }

    public String getContentJsonStr() {
        return contentJsonStr;
    }

    public void setFake(boolean fake) {
        isFake = fake;
    }

    public boolean isFake() {
        return isFake;
    }

    public boolean isFail() {
        return isFail;
    }

    public void setFail(boolean fail) {
        isFail = fail;
    }

    public void setRealUUIDWhenFake(String realUUIDWhenFake) {
        this.realUUIDWhenFake = realUUIDWhenFake;
    }

    public String getRealUUIDWhenFake() {
        return realUUIDWhenFake;
    }

}
