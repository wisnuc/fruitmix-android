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

    public static final String CLOUD_IP = Util.HTTP + CloudHttpRequestFactory.CLOUD_DOMAIN_NAME;

    public static HttpRequestFactory instance;

    private SystemSettingDataSource systemSettingDataSource;

    private BaseAbsHttpRequestFactory currentDefaultHttpRequestFactory;

    private String mToken;
    private String mGateway;

    private String mStationID;

    private HttpRequestFactory(SystemSettingDataSource systemSettingDataSource) {

        this.systemSettingDataSource = systemSettingDataSource;

        mToken = "";
        mGateway = "";

        mStationID = "";

    }

    public static void destroyInstance() {

        Log.d(TAG, "destroyInstance: " + instance);

        instance = null;
    }

    public static HttpRequestFactory getInstance(SystemSettingDataSource systemSettingDataSource) {

        if (instance == null) {

            instance = new HttpRequestFactory(systemSettingDataSource);

            Log.d(TAG, "getInstance: create new instance: " + instance);

        }

        return instance;
    }

    public synchronized void reset() {

        Log.d(TAG, "reset: set mToken,mGateway station to empty");

        setCurrentData("", "");
        setStationID("");

    }

    public void setGateway(String gateway) {

        Log.d(TAG, "setGateway: " + gateway);

        this.mGateway = gateway;

        Log.d(TAG, "finish setGateway: getGateway: " + getGateway() + " this: " + this);
    }

    private synchronized String getGateway() {

        Log.d(TAG, "getGateway: this: " + this);

        if (mGateway == null || mGateway.isEmpty()) {

            Log.d(TAG, "getGateway: mGateway is null or empty");

            mGateway = systemSettingDataSource.getCurrentEquipmentIp();

        }

        Log.d(TAG, "getGateway: " + mGateway + " threadID: " + Thread.currentThread().getId());

        return mGateway;

    }

    private void setToken(String token) {

        Log.d(TAG, "setToken: " + token);

        this.mToken = token;

        Log.d(TAG, "finish setToken: getToken: " + getToken() + " this: " + this);
    }

    private synchronized String getToken() {

        if (mToken == null || mToken.isEmpty()) {

            Log.d(TAG, "getToken: mToken is null or empty");

            mToken = systemSettingDataSource.getCurrentLoginToken();

        }

        Log.d(TAG, "getToken: " + mToken + " this: " + this);

        return mToken;


    }

    public void setStationID(String stationID) {

        Log.d(TAG, "setStationID: " + stationID + " this: " + this);

        this.mStationID = stationID;

    }

    private synchronized String getStationID() {

        if (mStationID == null || mStationID.isEmpty()) {

            Log.d(TAG, "getStationID: station is null or empty");

            mStationID = systemSettingDataSource.getCurrentLoginStationID();

        }

        Log.d(TAG, "getStationID: " + mStationID + " this:" + this);

        return mStationID;


    }

    public synchronized void checkToLocalUser(String token, String ip) {

        Log.d(TAG, "checkToLocalUser mToken: " + token + " ip: " + ip + " this: " + this);

        systemSettingDataSource.setCurrentLoginToken(token);

        systemSettingDataSource.setCurrentEquipmentIp(Util.HTTP + ip);

        systemSettingDataSource.setLoginWithWechatCodeOrNot(false);

        setCurrentData(token, Util.HTTP + ip);

        setStationID("");

    }

    public synchronized void checkToWeChatUser(String token) {

        Log.d(TAG, "checkToWeChatUser: mToken: " + token + " this:" + this);

        systemSettingDataSource.setCurrentLoginToken(token);

        systemSettingDataSource.setCurrentEquipmentIp("");

        systemSettingDataSource.setLoginWithWechatCodeOrNot(true);

        setCurrentData(token, CLOUD_IP);

        setStationID(systemSettingDataSource.getCurrentLoginStationID());


    }

    public void setCurrentData(String token, String gateway) {

        Log.d(TAG, "setCurrentData: mToken: " + token + " mGateway: " + gateway);

        setToken(token);
        setGateway(gateway);

    }

    public HttpRequest createHttpGetTokenRequestByCloudAPI(String httpPath) {

        BaseAbsHttpRequestFactory factory = new CloudHttpRequestFactory(null);

        return factory.createHttpGetRequest(httpPath, false);

    }

    private HttpHeader createTokenHeaderUsingCloudToken() {

        String token = systemSettingDataSource.getCurrentWAToken();

        Log.d(TAG, "createTokenHeaderUsingCloudToken: " + token);

        return new HttpHeader(Util.KEY_AUTHORIZATION, token);

    }

    private HttpHeader createTokenHeaderForCloudAPI() {
        return new HttpHeader(Util.KEY_AUTHORIZATION, getToken());
    }

    public synchronized HttpRequest createHttpPostRequestByCloudAPIWithoutWrap(String httpPath, String body, String token) {

        Log.d(TAG, "createHttpPostRequestByCloudAPIWithoutWrap: token: " + token);

        BaseAbsHttpRequestFactory factory = new CloudHttpRequestFactory(new HttpHeader(Util.KEY_AUTHORIZATION, token));

        return factory.createHttpPostRequest(httpPath, body, false);

    }

    public synchronized HttpRequest createHttpGetRequestByCloudAPIWithoutWrap(String httpPath) {

        BaseAbsHttpRequestFactory factory = new CloudHttpRequestFactory(createTokenHeaderUsingCloudToken());

        return factory.createHttpGetRequest(httpPath, false);

    }

    public HttpRequest createGetRequestWithoutToken(String ip, String httpPath) {

        BaseAbsHttpRequestFactory factory = new StationHttpRequestFactory(ip, null);

        return factory.createHttpGetRequest(httpPath, false);

    }

    public HttpRequest createPostRequestWithoutToken(String ip, String httpPath,String body) {

        BaseAbsHttpRequestFactory factory = new StationHttpRequestFactory(ip, null);

        return factory.createHttpPostRequest(httpPath, body,false);

    }

    public HttpRequest createPatchRequestWithoutToken(String ip, String httpPath,String body) {

        BaseAbsHttpRequestFactory factory = new StationHttpRequestFactory(ip, null);

        return factory.createHttpPatchRequest(httpPath, body);

    }


    public HttpRequest createGetRequestWithToken(String ip,String token, String httpPath) {

        BaseAbsHttpRequestFactory factory = new StationHttpRequestFactory(ip, new HttpHeader(Util.KEY_AUTHORIZATION,Util.KEY_JWT_HEAD + token));

        return factory.createHttpGetRequest(httpPath, false);

    }

    public HttpRequest createHttpGetTokenRequest(LoadTokenParam loadTokenParam) {

        HttpHeader httpHeader = new HttpHeader(Util.KEY_AUTHORIZATION, Util.KEY_BASE_HEAD + Base64.encodeToString((loadTokenParam.getUserUUID() + ":" + loadTokenParam.getUserPassword()).getBytes(), Base64.NO_WRAP));

        BaseAbsHttpRequestFactory factory = new StationHttpRequestFactory(loadTokenParam.getGateway(), httpHeader);

        return factory.createHttpGetRequest(Util.TOKEN_PARAMETER, false);

    }

    public synchronized HttpRequest createGetRequestByPathWithoutToken(String httpPath) {

        BaseAbsHttpRequestFactory factory = new StationHttpRequestFactory(getGateway(), null);

        return factory.createHttpGetRequest(httpPath, false);

    }

    private boolean checkIsLoginWithWeChatCode() {

        boolean loginWithWeChatCodeOrNot = systemSettingDataSource.getLoginWithWechatCodeOrNot();

        Log.d(TAG, "checkIsLoginWithWeChatCode: loginWithWeChatCodeOrNot: " + loginWithWeChatCodeOrNot);

        return loginWithWeChatCodeOrNot;

    }

    public synchronized HttpRequest createHttpGetRequestByCloudAPIWithWrap(String httpPath, String stationID) {

        BaseAbsHttpRequestFactory factory = new CloudHttpRequestForStationAPIFactory(createTokenHeaderUsingCloudToken(), stationID);

        return factory.createHttpGetRequest(httpPath, false);


    }


    public HttpRequest createHttpGetFileRequest(String httpPath) {

        return createHttpGetRequest(httpPath, true);

    }


    public HttpRequest createHttpGetRequest(String httpPath) {

        return createHttpGetRequest(httpPath, false);

    }

    public synchronized HttpRequest createHttpGetRequest(String httpPath,int port){

        setDefaultFactoryState();

        currentDefaultHttpRequestFactory.setPort(port);

        return currentDefaultHttpRequestFactory.createHttpGetRequest(httpPath,false);

    }

    private synchronized HttpRequest createHttpGetRequest(String httpPath, boolean isGetStream) {

        setDefaultFactoryState();

        return currentDefaultHttpRequestFactory.createHttpGetRequest(httpPath, isGetStream);

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

    private synchronized HttpRequest createHttpPostRequest(String httpPath, String body, boolean isPostStream) {

        setDefaultFactoryState();

        return currentDefaultHttpRequestFactory.createHttpPostRequest(httpPath, body, isPostStream);

    }

    public synchronized HttpRequest createPatchRequestByPathWithoutToken(String httpPath, String body) {

        BaseAbsHttpRequestFactory factory = new StationHttpRequestFactory(getGateway(), null);

        return factory.createHttpPatchRequest(httpPath, body);

    }

    public synchronized HttpRequest createHttpPatchRequest(String httpPath, String body) {

        setDefaultFactoryState();

        return currentDefaultHttpRequestFactory.createHttpPatchRequest(httpPath, body);

    }

    public synchronized HttpRequest createHttpPatchRequest(String httpPath, String body,int port) {

        setDefaultFactoryState();

        currentDefaultHttpRequestFactory.setPort(port);

        return currentDefaultHttpRequestFactory.createHttpPatchRequest(httpPath, body);

    }

    public synchronized HttpRequest createHttpPutRequest(String httpPath, String body,int port) {

        setDefaultFactoryState();

        currentDefaultHttpRequestFactory.setPort(port);

        return currentDefaultHttpRequestFactory.createHttpPutRequest(httpPath, body);

    }

    public synchronized HttpRequest createModifyPasswordRequest(String httpPath, String body, String userUUID, String originalPassword) {

        if (checkIsLoginWithWeChatCode()) {
            currentDefaultHttpRequestFactory = new CloudHttpRequestForStationAPIFactory(createTokenHeaderForCloudAPI(), getStationID());
        } else {

            HttpHeader httpHeader = new HttpHeader(Util.KEY_AUTHORIZATION, Util.KEY_BASE_HEAD + Base64.encodeToString((userUUID + ":" + originalPassword).getBytes(), Base64.NO_WRAP));

            currentDefaultHttpRequestFactory = new StationHttpRequestFactory(getGateway(), httpHeader);
        }

        return currentDefaultHttpRequestFactory.createHttpPutRequest(httpPath, body);

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

    public synchronized HttpRequest createHttpGetRequestForLocalMedia(String url) {

        Log.d(TAG, "createHttpGetRequestForLocalMedia: url: " + url);

        HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_GET_METHOD);

        httpRequest.setHeader(Util.KEY_AUTHORIZATION, getTokenForHeaderValue());

        return httpRequest;


    }


}
