package com.winsun.fruitmix.http.request.factory;

import android.util.Log;

import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/10/9.
 */

public class BaseAbsHttpRequestFactory {

    public static final String TAG = BaseAbsHttpRequestFactory.class.getSimpleName();

    private String gateway;
    private int port;

    private HttpHeader httpHeader;

    BaseAbsHttpRequestFactory(HttpHeader httpHeader) {

        this.httpHeader = httpHeader;
    }

    void setGateway(String gateway) {
        this.gateway = gateway;
    }

     String getGateway() {
        return Util.HTTP + gateway;
    }

    private HttpHeader getHttpHeader() {
        return httpHeader;
    }

     int getPort() {
        return port;
    }

    void setPort(int port) {
        this.port = port;
    }


    public HttpRequest createHttpGetRequest(String httpPath, boolean isGetStream) {

        HttpRequest httpRequest = new HttpRequest(createUrl(httpPath), Util.HTTP_GET_METHOD);

        setHeader(httpRequest);

        return httpRequest;
    }


    public HttpRequest createHttpPostRequest(String httpPath, String body, boolean isPostStream) {

        HttpRequest httpRequest = new HttpRequest(createUrl(httpPath), Util.HTTP_POST_METHOD);

        setHeader(httpRequest);

        httpRequest.setBody(body);

        return httpRequest;

    }

    void setHeader(HttpRequest httpRequest) {
        if (getHttpHeader() != null)
            httpRequest.setHeader(getHttpHeader().getKey(), getHttpHeader().getValue());
    }

    String createUrl(String httpPath) {
        String url = getGateway() + ":" + getPort() + httpPath;

        Log.d(TAG, "createUrl: " + url);

        return url;
    }

}
