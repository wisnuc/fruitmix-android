package com.winsun.fruitmix.http.request.factory;

/**
 * Created by Administrator on 2017/10/9.
 */

class HttpHeader {

    private String key;
    private String value;

    HttpHeader(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
