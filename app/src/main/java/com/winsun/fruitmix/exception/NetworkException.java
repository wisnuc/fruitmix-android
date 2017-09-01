package com.winsun.fruitmix.exception;

import com.winsun.fruitmix.http.HttpResponse;

/**
 * Created by Administrator on 2017/8/30.
 */

public class NetworkException extends Exception {

    private HttpResponse httpResponse;

    /**
     * Constructs a new {@code Exception} with the current stack trace and the
     * specified detail message.
     *
     * @param detailMessage the detail message for this exception.
     */
    public NetworkException(String detailMessage, HttpResponse httpResponse) {
        super(detailMessage);
        this.httpResponse = httpResponse;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public String getHttpResponseBody(){
        return httpResponse.getResponseData();
    }

    public int getHttpResponseCode(){
        return httpResponse.getResponseCode();
    }



}
