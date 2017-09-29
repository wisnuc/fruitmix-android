package com.winsun.fruitmix.http.factory;

import android.util.Base64;
import android.util.Log;

import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/7/14.
 */

public class HttpRequestFactory {

    public static final String TAG = HttpRequestFactory.class.getSimpleName();

    public static final String CLOUD_API_LEVEL = "/c/v1";

//    public static final String CLOUD_IP = Util.HTTP + "10.10.9.59";

    public static final String CLOUD_IP = Util.HTTP + "www.siyouqun.org";

//    public static final int CLOUD_PORT = 4000;

    public static final int CLOUD_PORT = 80;

    public static final int STATION_PORT = 3000;

    public static HttpRequestFactory instance;

    private SystemSettingDataSource systemSettingDataSource;

    private BaseHttpRequestFactory wrapHttpRequestFactory;
    private NoWrapHttpRequestFactory noWrapHttpRequestFactory;

    private BaseHttpRequestFactory currentDefaultHttpRequestFactory;

    private String token;
    private String gateway;
    private int port;

    private String stationID;

    public static final Object httpCreateRequestLock = new Object();

    private HttpRequestFactory(SystemSettingDataSource systemSettingDataSource, BaseHttpRequestFactory wrapHttpRequestFactory,
                               NoWrapHttpRequestFactory noWrapHttpRequestFactory) {

        this.systemSettingDataSource = systemSettingDataSource;
        this.wrapHttpRequestFactory = wrapHttpRequestFactory;
        this.noWrapHttpRequestFactory = noWrapHttpRequestFactory;

        token = "";
        gateway = "";

        stationID = "";

    }

    public static void destroyInstance() {

        Log.d(TAG, "destroyInstance: ");

        instance = null;
    }

    public static HttpRequestFactory getInstance(SystemSettingDataSource systemSettingDataSource) {

        if (instance == null) {

            Log.d(TAG, "getInstance: create new instance");

            BaseHttpRequestFactory wrapHttpRequestFactory = new WrapHttpRequestFactoryImpl();
            NoWrapHttpRequestFactory noWrapHttpRequestFactory = new NoWrapHttpRequestFactoryImpl();

            instance = new HttpRequestFactory(systemSettingDataSource, wrapHttpRequestFactory, noWrapHttpRequestFactory);

        }

        return instance;
    }

    public void reset() {

        setCurrentData("", "");
        setStationID("");

    }

    private void setToken(String token) {

        Log.d(TAG, "setToken: " + token);

        this.token = token;
    }

    public void setGateway(String gateway) {

        Log.d(TAG, "setGateway: " + gateway);

        this.gateway = gateway;
    }

    public String getGateway() {
        if (gateway == null || gateway.isEmpty())
            gateway = systemSettingDataSource.getCurrentEquipmentIp();

        return gateway;
    }

    private String getToken() {

        if (token == null || token.isEmpty())
            token = systemSettingDataSource.getCurrentLoginToken();

        return token;
    }

    public void setCurrentData(String token, String gateway) {

        Log.d(TAG, "setCurrentData: token: " + token + " gateway: " + gateway);

        setToken(token);
        setGateway(gateway);

    }

    public int getPort() {

        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    private String getStationID() {

        if (stationID == null || stationID.isEmpty())
            stationID = systemSettingDataSource.getCurrentLoginStationID();

        return stationID;
    }

    public void setDefaultFactory(boolean needWrap) {

        if (needWrap) {
            currentDefaultHttpRequestFactory = wrapHttpRequestFactory;
        } else
            currentDefaultHttpRequestFactory = noWrapHttpRequestFactory;

    }

    public HttpRequest createGetRequestWithWeChatCode(String weChatCode) {

        String path = CLOUD_API_LEVEL + "/token?code=" + weChatCode + "&platform=mobile";

        noWrapHttpRequestFactory.setGateway(CLOUD_IP);
        noWrapHttpRequestFactory.setPort(CLOUD_PORT);

        return noWrapHttpRequestFactory.createGetRequestByPathWithoutToken(path);

    }

    public HttpRequest createHttpGetRequestByCloudAPIWithoutWrap(String httpPath) {

        noWrapHttpRequestFactory.setGateway(CLOUD_IP);
        noWrapHttpRequestFactory.setPort(CLOUD_PORT);
        noWrapHttpRequestFactory.setToken(getToken());

        return noWrapHttpRequestFactory.createHttpGetRequest(httpPath, false);

    }

    public HttpRequest createGetRequestWithoutToken(String ip, String httpPath) {

        if (ip.startsWith(Util.HTTP)) {
            noWrapHttpRequestFactory.setGateway(ip);
        } else {
            noWrapHttpRequestFactory.setGateway(Util.HTTP + ip);
        }

        noWrapHttpRequestFactory.setPort(STATION_PORT);

        return noWrapHttpRequestFactory.createGetRequestByPathWithoutToken(httpPath);

    }

    public HttpRequest createHttpGetTokenRequest(LoadTokenParam loadTokenParam) {

        noWrapHttpRequestFactory.setPort(STATION_PORT);
        return noWrapHttpRequestFactory.createHttpGetTokenRequest(loadTokenParam);

    }

    //TODO:refactor create http request:use base class and subclass

    public HttpRequest createGetRequestByPathWithoutToken(String httpPath) {

        return currentDefaultHttpRequestFactory.createGetRequestByPathWithoutToken(httpPath);

    }

    private boolean checkIsLoginWithWeChatCode() {

        return systemSettingDataSource.getLoginWithWechatCodeOrNot();

    }

    public HttpRequest createHttpGetRequestByCloudAPIWithWrap(String httpPath, String stationID) {

        synchronized (httpCreateRequestLock) {

            wrapHttpRequestFactory.setGateway(CLOUD_IP);
            wrapHttpRequestFactory.setPort(CLOUD_PORT);
            wrapHttpRequestFactory.setToken(getToken());
            wrapHttpRequestFactory.setStationID(stationID);

            return wrapHttpRequestFactory.createHttpGetRequest(httpPath, false);

        }

    }


    public HttpRequest createHttpGetFileRequest(String httpPath) {

        return createHttpGetRequest(httpPath, true);

    }


    public HttpRequest createHttpGetRequest(String httpPath) {

        return createHttpGetRequest(httpPath, false);

    }

    private HttpRequest createHttpGetRequest(String httpPath, boolean isPipe) {

        synchronized (httpCreateRequestLock) {

            setDefaultFactoryState();

            return currentDefaultHttpRequestFactory.createHttpGetRequest(httpPath, isPipe);

        }

    }

    private void setDefaultFactoryState() {

        if (checkIsLoginWithWeChatCode()) {

            currentDefaultHttpRequestFactory = wrapHttpRequestFactory;

            currentDefaultHttpRequestFactory.setGateway(CLOUD_IP);
            currentDefaultHttpRequestFactory.setPort(CLOUD_PORT);

            currentDefaultHttpRequestFactory.setStationID(getStationID());
            currentDefaultHttpRequestFactory.setToken(getToken());

        } else {

            currentDefaultHttpRequestFactory = noWrapHttpRequestFactory;

            currentDefaultHttpRequestFactory.setGateway(getGateway());
            currentDefaultHttpRequestFactory.setPort(STATION_PORT);

            currentDefaultHttpRequestFactory.setToken(getTokenWithHead());

        }
    }


    public HttpRequest createHttpPostFileRequest(String httpPath, String body) {

        return createHttpPostRequest(httpPath, body, true);
    }

    public HttpRequest createHttpPostRequest(String httpPath, String body) {

        return createHttpPostRequest(httpPath, body, false);

    }


    private HttpRequest createHttpPostRequest(String httpPath, String body, boolean isPipe) {

        synchronized (httpCreateRequestLock) {

            setDefaultFactoryState();

            return currentDefaultHttpRequestFactory.createHttpPostRequest(httpPath, body, isPipe);

        }

    }

    private String getTokenWithHead() {
        return Util.KEY_JWT_HEAD + getToken();
    }

    public String getJWT() {

        if (checkIsLoginWithWeChatCode())
            return getToken();
        else
            return getTokenWithHead();
    }

    public HttpRequest createHttpGetRequestForLocalMedia(String url) {

        synchronized (httpCreateRequestLock) {

            HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_GET_METHOD);

            httpRequest.setHeader(Util.KEY_AUTHORIZATION, getJWT());

            return httpRequest;

        }

    }


}
