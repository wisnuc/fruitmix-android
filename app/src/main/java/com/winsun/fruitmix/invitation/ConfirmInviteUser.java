package com.winsun.fruitmix.invitation;

/**
 * Created by Andy on 2017/7/12.
 */

public class ConfirmInviteUser {

    private String userName;
    private String userAvatar;
    private String station;

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
}
