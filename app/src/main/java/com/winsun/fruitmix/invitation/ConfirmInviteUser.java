package com.winsun.fruitmix.invitation;

import android.util.Log;

import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

import java.util.Random;

/**
 * Created by Andy on 2017/7/12.
 */

public class ConfirmInviteUser {

    public static final String TAG = ConfirmInviteUser.class.getSimpleName();

    public static final int OPERATE_TYPE_ACCEPT = 1;
    public static final int OPERATE_TYPE_REFUSE = 2;

    public static final int OPERATE_TYPE_PENDING = 0;

    private String createFormatTime;
    private String userName;
    private String userAvatar;
    private String station;

    private String userGUID;

    private int operateType = OPERATE_TYPE_PENDING;

    private String ticketUUID;

    public String getCreateFormatTime() {

        if (createFormatTime.length() > 10)
            return createFormatTime.substring(0, 10);
        else
            return createFormatTime;
    }

    public void setCreateFormatTime(String createFormatTime) {
        this.createFormatTime = createFormatTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getUserGUID() {
        return userGUID == null ? "" : userGUID;
    }

    public void setUserGUID(String userUUID) {
        this.userGUID = userUUID;
    }

    public String getTicketUUID() {
        return ticketUUID;
    }

    public void setTicketUUID(String ticketUUID) {
        this.ticketUUID = ticketUUID;
    }

    public int getOperateType() {
        return operateType;
    }

    public void setOperateType(int operateType) {
        this.operateType = operateType;
    }

    public void setOperateType(String type) {

        switch (type) {
            case "pending":
                setOperateType(OPERATE_TYPE_PENDING);
                break;
            case "resolved":
                setOperateType(OPERATE_TYPE_ACCEPT);
                break;
            case "rejected":
                setOperateType(OPERATE_TYPE_REFUSE);
                break;
            default:
                setOperateType(OPERATE_TYPE_REFUSE);
                break;
        }

    }

    public User generateUser(Random random){

        User user = new User();
        user.setUserName(getUserName());
        user.setAvatar(getUserAvatar());

        user.setDefaultAvatar(Util.getUserNameForAvatar(user.getUserName()));
        user.setDefaultAvatarBgColor(random.nextInt(3) + 1);

        Log.d(TAG, "refreshView: user avatar:" + user.getAvatar());

        return user;

    }



}
