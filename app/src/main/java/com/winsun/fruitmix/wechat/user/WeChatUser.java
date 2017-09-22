package com.winsun.fruitmix.wechat.user;

/**
 * Created by Administrator on 2017/9/20.
 */

public class WeChatUser {

    private String token;
    private String guid;
    private String stationID;

    public WeChatUser() {
    }

    public WeChatUser(String token, String guid, String stationID) {
        this.token = token;
        this.guid = guid;
        this.stationID = stationID;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    public String getStationID() {
        return stationID;
    }

    public String getToken() {
        return token;
    }

    public String getGuid() {
        return guid;
    }

}
