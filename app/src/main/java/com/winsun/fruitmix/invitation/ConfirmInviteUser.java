package com.winsun.fruitmix.invitation;

/**
 * Created by Andy on 2017/7/12.
 */

public class ConfirmInviteUser {

    public static final String OPERATE_TYPE_ACCEPT = "accept";
    public static final String OPERATE_TYPE_REFUSE = "refuse";

    public static final String OPERATE_TYPE_PENDING = "pending";

    private String userName;
    private String userAvatar;
    private String station;

    private String userUUID;

    private String operateType = OPERATE_TYPE_PENDING;

    private String ticketUUID;

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

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public String getTicketUUID() {
        return ticketUUID;
    }

    public void setTicketUUID(String ticketUUID) {
        this.ticketUUID = ticketUUID;
    }

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }
}
