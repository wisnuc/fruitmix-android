package com.winsun.fruitmix.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/19.
 */

public class HttpRequest {

    public static final String TAG = HttpRequest.class.getSimpleName();

    private String url;
    private String httpMethod;

    private Map<String,String> headers;

    private String body;

    public HttpRequest(String url, String httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod;

        headers = new HashMap<>();
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

    public void addHeader(String headerKey,String headerValue){
        headers.put(headerKey,headerValue);
    }

    public Map<String,String> getHeaders(){
        return Collections.unmodifiableMap(headers);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
