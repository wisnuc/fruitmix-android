package com.winsun.fruitmix.http;

/**
 * Created by Administrator on 2017/1/19.
 */

public class HttpRequest {

    public static final String TAG = HttpRequest.class.getSimpleName();

    private String url;
    private String httpMethod;
    private String headerKey;
    private String headerValue;
    private String body;

    public HttpRequest(String url, String httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setHeader(String key, String value) {
        headerKey = key;
        headerValue = value;
    }

    public String getHeaderKey() {
        return headerKey;
    }

    public String getHeaderValue() {
        return headerValue;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
