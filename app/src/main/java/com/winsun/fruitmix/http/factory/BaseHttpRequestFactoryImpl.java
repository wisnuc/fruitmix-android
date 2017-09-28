package com.winsun.fruitmix.http.factory;

import android.util.Log;

import com.winsun.fruitmix.system.setting.SystemSettingDataSource;

/**
 * Created by Administrator on 2017/9/18.
 */

public class BaseHttpRequestFactoryImpl {

    public static final String TAG = BaseHttpRequestFactoryImpl.class.getSimpleName();

    protected String gateway;
    protected String token;

    private int port;

    private String stationID;

    protected SystemSettingDataSource systemSettingDataSource;

    BaseHttpRequestFactoryImpl(SystemSettingDataSource systemSettingDataSource) {
        gateway = "";
        token = "";

        stationID = "";

        this.systemSettingDataSource = systemSettingDataSource;
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setToken(String token) {
        this.token = token;
    }

    int getPort() {
        return port;
    }

    protected String getGateway() {
        return gateway;
    }

    protected String getToken() {
        return token;
    }

    String getStationID() {
        return stationID;
    }

    String createUrl(String httpPath) {
        String url = getGateway() + ":" + getPort() + httpPath;

        Log.d(TAG, "createUrl: " + url);

        return url;
    }

}
