package com.winsun.fruitmix.http;

import android.util.Base64;

import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/7/14.
 */

public class HttpRequestFactory {

    public static HttpRequestFactory instance;

    private String token;
    private String gateway;
    private int port;

    private HttpRequestFactory() {

        token = "";
        gateway = "";

        port = 3000;

    }

    public static void destroyInstance() {
        instance = null;
    }

    public static HttpRequestFactory getInstance() {

        if (instance == null)
            instance = new HttpRequestFactory();

        return instance;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getGateway() {
        return FNAS.Gateway;
    }

    public String getToken() {
        return FNAS.JWT;
    }

    public void setCurrentData(String token, String gateway) {
        setToken(token);
        setGateway(gateway);
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public HttpRequest createGetRequestWithoutToken(String url) {

        return new HttpRequest(url, Util.HTTP_GET_METHOD);

    }

    public HttpRequest createHttpGetRequestWithFullUrl(String url) {
        HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_GET_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, getTokenWithHead());

        return httpRequest;
    }

    public HttpRequest createHttpGetTokenRequest(LoadTokenParam loadTokenParam) {

        String url = loadTokenParam.getGateway() + ":" + port + Util.TOKEN_PARAMETER;

        HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_GET_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_BASE_HEAD + Base64.encodeToString((loadTokenParam.getUserUUID() + ":" + loadTokenParam.getUserPassword()).getBytes(), Base64.NO_WRAP));

        return httpRequest;
    }

    public HttpRequest createHttpGetRequest(String httpPath) {

        return createHttpGetRequestWithFullUrl(createUrl(httpPath));
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
        return getGateway() + ":" + port + httpPath;
    }

    //TODO: consider set token logic

    private HttpRequest createHasBodyRequest(String url, String method, String body) {

        HttpRequest httpRequest = new HttpRequest(url, method);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, getTokenWithHead());
        httpRequest.setBody(body);

        return httpRequest;
    }

    private String getTokenWithHead() {
        return Util.KEY_JWT_HEAD + getToken();
    }


}
