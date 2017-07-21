package com.winsun.fruitmix.http;

/**
 * Created by Administrator on 2017/7/14.
 */

public class BaseRemoteDataSourceImpl {

    protected IHttpUtil iHttpUtil;
    protected HttpRequestFactory httpRequestFactory;

    protected  BaseHttpCallWrapper wrapper;

    public BaseRemoteDataSourceImpl(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        this.iHttpUtil = iHttpUtil;
        this.httpRequestFactory = httpRequestFactory;

        wrapper = new BaseHttpCallWrapper(iHttpUtil);
    }

}
