package com.winsun.fruitmix.token.param;

/**
 * Created by Administrator on 2017/2/7.
 */

public class StationTokenParam implements TokenParam{

    private String gateway;
    private String userUUID;
    private String userPassword;
    private String equipmentName;

    public StationTokenParam(String gateway, String userUUID, String userPassword, String equipmentName) {
        this.gateway = gateway;
        this.userUUID = userUUID;
        this.userPassword = userPassword;
        this.equipmentName = equipmentName;
    }

    public String getGateway() {
        return gateway;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public String getEquipmentName() {
        return equipmentName;
    }
}
