package com.winsun.fruitmix.refactor.business;

/**
 * Created by Administrator on 2017/2/7.
 */

public class LoadTokenParam {

    private String gateway;
    private String userUUID;
    private String userPassword;

    public LoadTokenParam(String gateway, String userUUID, String userPassword) {
        this.gateway = gateway;
        this.userUUID = userUUID;
        this.userPassword = userPassword;
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
}
