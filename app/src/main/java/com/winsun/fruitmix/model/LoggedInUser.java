package com.winsun.fruitmix.model;

/**
 * Created by Administrator on 2017/2/14.
 */

public class LoggedInUser {
    private User user;
    private String gateway;
    private String equipmentName;
    private String token;
    private String deviceID;

    public LoggedInUser(String deviceID, String token, String gateway, String equipmentName,User user) {
        this.deviceID = deviceID;
        this.token = token;
        this.gateway = gateway;
        this.equipmentName = equipmentName;
        this.user = user;
    }

    public LoggedInUser() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
