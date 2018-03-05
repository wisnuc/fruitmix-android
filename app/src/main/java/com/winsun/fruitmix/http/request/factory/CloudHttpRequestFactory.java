package com.winsun.fruitmix.http.request.factory;


/**
 * Created by Administrator on 2017/10/9.
 */

public class CloudHttpRequestFactory extends BaseAbsHttpRequestFactory {

    static final String CLOUD_DOMAIN_NAME = "www.siyouqun.com";

    private static final String TEST_CLOUD_DOMAIN_NAME = "test.siyouqun.com";

    private static final String DEV_CLOUD_DOMAIN_NAME = "10.10.9.87";

//    static final String CLOUD_DOMAIN_NAME = "10.10.9.59";

    private static final int DEBUG_CLOUD_PORT = 4000;

    private static final int RELEASE_CLOUD_PORT = 80;

    public static final String CLOUD_API_LEVEL = "/c/v1";

    CloudHttpRequestFactory(HttpHeader httpHeader) {
        super(httpHeader);

        setGateway(TEST_CLOUD_DOMAIN_NAME);

        setPort(DEBUG_CLOUD_PORT);

    }

}
