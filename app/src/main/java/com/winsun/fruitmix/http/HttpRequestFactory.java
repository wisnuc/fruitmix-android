package com.winsun.fruitmix.http;

import android.util.Base64;
import android.util.Log;

import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/7/14.
 */

public class HttpRequestFactory {

    public static final String TAG = HttpRequestFactory.class.getSimpleName();

    private static final String CALL_STATION_THROUGH_CLOUD_PRE = "/c/v1/stations/";

    private static final String CALL_STATION_THROUGH_CLOUD_END = "/json";

    public static HttpRequestFactory instance;

    private SystemSettingDataSource systemSettingDataSource;

    private String token;
    private String gateway;
    private int port;

    private String stationID;

    private HttpRequestFactory(SystemSettingDataSource systemSettingDataSource) {

        this.systemSettingDataSource = systemSettingDataSource;

        token = "";
        gateway = "";

        port = 3000;

    }

    public static void destroyInstance() {

        Log.d(TAG, "destroyInstance: ");

        instance = null;
    }

    public static HttpRequestFactory getInstance(SystemSettingDataSource systemSettingDataSource) {

        if (instance == null)
            instance = new HttpRequestFactory(systemSettingDataSource);

        return instance;
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
        if (gateway == null)
            gateway = systemSettingDataSource.getCurrentEquipmentIp();

        return gateway;
    }

    private String getToken() {

        if (token == null)
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

        if (stationID == null)
            stationID = systemSettingDataSource.getCurrentLoginStationID();

        return stationID;
    }

    public HttpRequest createGetRequestWithoutToken(String url) {

        return new HttpRequest(url, Util.HTTP_GET_METHOD);

    }

    public HttpRequest createHttpGetTokenRequest(LoadTokenParam loadTokenParam) {

        String url = loadTokenParam.getGateway() + ":" + getPort() + Util.TOKEN_PARAMETER;

        HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_GET_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_BASE_HEAD + Base64.encodeToString((loadTokenParam.getUserUUID() + ":" + loadTokenParam.getUserPassword()).getBytes(), Base64.NO_WRAP));

        return httpRequest;
    }

    //TODO:refactor create http request:use base class and subclass

    public HttpRequest createGetRequestByPathWithoutToken(String httpPath) {

        if (getStationID() != null) {
            return createHttpGetRequestThroughPipe(getStationID(), httpPath);
        } else {
            return new HttpRequest(createUrl(httpPath), Util.HTTP_GET_METHOD);
        }
    }

    public HttpRequest createHttpGetRequest(String httpPath) {

        if (getStationID() != null) {
            return createHttpGetRequestThroughPipe(getStationID(), httpPath);
        } else {
            return createHttpGetRequestWithFullUrl(createUrl(httpPath));
        }

    }

    public HttpRequest createHttpGetRequestThroughPipe(String stationID, String httpPath) {

        Log.d(TAG, "createHttpGetRequestThroughPipe: " + getGateway() + ":" + getPort() + httpPath);

        String httpPathEncodeByBase64 = Base64.encodeToString(httpPath.getBytes(), Base64.NO_WRAP);

        String newHttpPath = CALL_STATION_THROUGH_CLOUD_PRE + stationID + CALL_STATION_THROUGH_CLOUD_END + "?resource=" + httpPathEncodeByBase64
                + "&method=GET";

        return createHttpGetRequestWithOutJWTHeader(newHttpPath);

    }

    private HttpRequest createHttpGetRequestWithFullUrl(String url) {
        HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_GET_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, getTokenWithHead());

        return httpRequest;
    }


    public HttpRequest createHttpGetRequestWithOutJWTHeader(String httpPath) {

        HttpRequest httpRequest = new HttpRequest(createUrl(httpPath), Util.HTTP_GET_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, getToken());

        return httpRequest;

    }


    public HttpRequest createHttpGetRequestThroughPipeWithoutToken(String stationID, String httpPath) {

        String httpPathEncodeByBase64 = Base64.encodeToString(httpPath.getBytes(), Base64.NO_WRAP);

        String url = getGateway() + ":" + getPort() + "/c/v1/stations/" + stationID + "/json?resource=" + httpPathEncodeByBase64
                + "&method=GET";

        return createGetRequestWithoutToken(url);

    }

    public HttpRequest createHttpPostRequest(String httpPath, String body) {

        return createHasBodyRequest(createUrl(httpPath), Util.HTTP_POST_METHOD, body);
    }

    public HttpRequest createHttpPatchRequest(String httpPath, String body) {

        return createHasBodyRequest(createUrl(httpPath), Util.HTTP_PATCH_METHOD, body);
    }

    public HttpRequest createHttpDeleteRequest(String httpPath, String body) {

        return createHasBodyRequest(createUrl(httpPath), Util.HTTP_DELETE_METHOD, body);
    }

    private String createUrl(String httpPath) {
        return getGateway() + ":" + getPort() + httpPath;
    }

    private HttpRequest createHasBodyRequest(String url, String method, String body) {

        HttpRequest httpRequest = new HttpRequest(url, method);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, getTokenWithHead());
        httpRequest.setBody(body);

        return httpRequest;
    }

    private String getTokenWithHead() {
        return Util.KEY_JWT_HEAD + getToken();
    }


    public String getJWT() {

        if (getStationID() != null)
            return getToken();
        else
            return getTokenWithHead();
    }

}
