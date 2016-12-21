package com.winsun.fruitmix.model;

/**
 * Created by Administrator on 2016/12/20.
 */

public enum ErrorCode {

    ERR_NOT_DEFINED("0"), ERR_URL_PARAMETER_ILLEGAL("1"), ERR_HTTP_RESPONSE_CODE("2"), ERR_DATABASE("3"), ERR_NETWORK_DATA_PARSE_FAILED("4"), ERR_RESOURCE("5");

    private String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
