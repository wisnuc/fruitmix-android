package com.winsun.fruitmix.http.request.factory;


/**
 * Created by Administrator on 2017/10/9.
 */

public class CloudHttpRequestFactory extends BaseAbsHttpRequestFactory {

    private static final String CLOUD_DOMAIN_NAME = "www.siyouqun.org";

    public static final String CLOUD_API_LEVEL = "/c/v1";

    CloudHttpRequestFactory( HttpHeader httpHeader) {
        super(httpHeader);

        setGateway(CLOUD_DOMAIN_NAME);

        setPort(80);

    }

}
