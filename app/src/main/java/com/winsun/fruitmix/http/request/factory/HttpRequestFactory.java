package com.winsun.fruitmix.http.request.factory;

import android.util.Base64;
import android.util.Log;

import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/7/14.
 */

public class HttpRequestFactory {

    public static final String TAG = HttpRequestFactory.class.getSimpleName();

//    public static final String CLOUD_IP = Util.HTTP + "10.10.9.59";

    public static final String CLOUD_IP = Util.HTTP + "www.siyouqun.org";

//    public static final int CLOUD_PORT = 4000;

    public static HttpRequestFactory instance;

    private SystemSettingDataSource systemSettingDataSource;

    private BaseAbsHttpRequestFactory currentDefaultHttpRequestFactory;

    private String token;
    private String gateway;

    private String stationID;

    public static final Object httpCreateRequestLock = new Object();

    private HttpRequestFactory(SystemSettingDataSource systemSettingDataSource) {

        this.systemSettingDataSource = systemSettingDataSource;

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

            instance = new HttpRequestFactory(systemSettingDataSource);

        }

        return instance;
    }

    public void reset() {

        Log.d(TAG, "reset: set token,gateway station to empty");

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
        if (gateway == null || gateway.isEmpty()) {

            Log.d(TAG, "getGateway: gateway is null or empty");

            gateway = systemSettingDataSource.getCurrentEquipmentIp();

        }

        Log.d(TAG, "getGateway: " + gateway);

        return gateway;
    }

    private String getToken() {

        if (token == null || token.isEmpty()) {

            Log.d(TAG, "getToken: token is null or empty");

            token = systemSettingDataSource.getCurrentLoginToken();

        }

        Log.d(TAG, "getToken: " + token);

        return token;

    }

    public void setCurrentData(String token, String gateway) {

        Log.d(TAG, "setCurrentData: token: " + token + " gateway: " + gateway);

        setToken(token);
        setGateway(gateway);

    }


    public void setStationID(String stationID) {

        Log.d(TAG, "setStationID: " + stationID);

        this.stationID = stationID;

    }

    private String getStationID() {

        if (stationID == null || stationID.isEmpty()) {

            Log.d(TAG, "getStationID: station is null or empty");

            stationID = systemSettingDataSource.getCurrentLoginStationID();

        }

        Log.d(TAG, "getStationID: " + stationID);

        return stationID;
    }

    public HttpRequest createHttpGetTokenRequestByCloudAPI(String httpPath) {

        BaseAbsHttpRequestFactory factory = new CloudHttpRequestFactory(null);

        return factory.createHttpGetRequest(httpPath, false);

    }

    private HttpHeader createTokenHeaderForCloudAPI() {
        return new HttpHeader(Util.KEY_AUTHORIZATION, getToken());
    }

    public HttpRequest createHttpGetRequestByCloudAPIWithoutWrap(String httpPath) {

        BaseAbsHttpRequestFactory factory = new CloudHttpRequestFactory(createTokenHeaderForCloudAPI());

        return factory.createHttpGetRequest(httpPath, false);

    }

    public HttpRequest createGetRequestWithoutToken(String ip, String httpPath) {

        BaseAbsHttpRequestFactory factory = new StationHttpRequestFactory(ip, null);

        return factory.createHttpGetRequest(httpPath, false);

    }

    public HttpRequest createHttpGetTokenRequest(LoadTokenParam loadTokenParam) {

        HttpHeader httpHeader = new HttpHeader(Util.KEY_AUTHORIZATION, Util.KEY_BASE_HEAD + Base64.encodeToString((loadTokenParam.getUserUUID() + ":" + loadTokenParam.getUserPassword()).getBytes(), Base64.NO_WRAP));

        BaseAbsHttpRequestFactory factory = new StationHttpRequestFactory(loadTokenParam.getGateway(), httpHeader);

        return factory.createHttpGetRequest(Util.TOKEN_PARAMETER, false);

    }

    //TODO:refactor create http request:use base class and subclass

    public HttpRequest createGetRequestByPathWithoutToken(String httpPath) {

        BaseAbsHttpRequestFactory factory = new StationHttpRequestFactory(getGateway(), null);

        return factory.createHttpGetRequest(httpPath, false);

    }

    private boolean checkIsLoginWithWeChatCode() {

        boolean loginWithWeChatCodeOrNot = systemSettingDataSource.getLoginWithWechatCodeOrNot();

        Log.d(TAG, "checkIsLoginWithWeChatCode: loginWithWeChatCodeOrNot: " + loginWithWeChatCodeOrNot);

        return loginWithWeChatCodeOrNot;

    }

    public HttpRequest createHttpGetRequestByCloudAPIWithWrap(String httpPath, String stationID) {

        synchronized (httpCreateRequestLock){

            BaseAbsHttpRequestFactory factory = new CloudHttpRequestForStationAPIFactory(createTokenHeaderForCloudAPI(), stationID);

            return factory.createHttpGetRequest(httpPath, false);

        }

    }


    public HttpRequest createHttpGetFileRequest(String httpPath) {

        return createHttpGetRequest(httpPath, true);

    }


    public HttpRequest createHttpGetRequest(String httpPath) {

        return createHttpGetRequest(httpPath, false);

    }

    private HttpRequest createHttpGetRequest(String httpPath, boolean isGetStream) {

        synchronized (httpCreateRequestLock){

            setDefaultFactoryState();

            return currentDefaultHttpRequestFactory.createHttpGetRequest(httpPath, isGetStream);

        }

    }

    private void setDefaultFactoryState() {

        if (checkIsLoginWithWeChatCode()) {

            currentDefaultHttpRequestFactory = new CloudHttpRequestForStationAPIFactory(createTokenHeaderForCloudAPI(), getStationID());


        } else {

            currentDefaultHttpRequestFactory = new StationHttpRequestFactory(getGateway(), createTokenHeaderForStationAPI());

        }

    }

    private HttpHeader createTokenHeaderForStationAPI() {
        return new HttpHeader(Util.KEY_AUTHORIZATION, getTokenWithPrefix());
    }


    public HttpRequest createHttpPostFileRequest(String httpPath, String body) {

        return createHttpPostRequest(httpPath, body, true);
    }

    public HttpRequest createHttpPostRequest(String httpPath, String body) {

        return createHttpPostRequest(httpPath, body, false);

    }

    private HttpRequest createHttpPostRequest(String httpPath, String body, boolean isPostStream) {

        synchronized (httpCreateRequestLock){

            setDefaultFactoryState();

            return currentDefaultHttpRequestFactory.createHttpPostRequest(httpPath, body, isPostStream);

        }

    }

    private String getTokenWithPrefix() {
        return Util.KEY_JWT_HEAD + getToken();
    }

    public String getTokenForHeaderValue() {

        if (checkIsLoginWithWeChatCode())
            return getToken();
        else
            return getTokenWithPrefix();
    }

    public HttpRequest createHttpGetRequestForLocalMedia(String url) {

        synchronized (httpCreateRequestLock) {

            HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_GET_METHOD);

            httpRequest.setHeader(Util.KEY_AUTHORIZATION, getTokenForHeaderValue());

            return httpRequest;

        }

    }


}
