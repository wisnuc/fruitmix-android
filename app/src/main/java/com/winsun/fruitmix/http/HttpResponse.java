package com.winsun.fruitmix.http;

/**
 * Created by Administrator on 2016/11/24.
 */

public class HttpResponse {

    private int responseCode;
    private String responseData;

    public HttpResponse(int responseCode, String responseData) {
        this.responseCode = responseCode;
        this.responseData = responseData;
    }

    public HttpResponse() {
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }
}
