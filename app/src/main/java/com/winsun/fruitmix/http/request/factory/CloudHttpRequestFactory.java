package com.winsun.fruitmix.http.request.factory;


import com.winsun.fruitmix.BuildConfig;

/**
 * Created by Administrator on 2017/10/9.
 */

public class CloudHttpRequestFactory extends BaseAbsHttpRequestFactory {

    private static final String CLOUD_DOMAIN_NAME = "www.siyouqun.com";

    private static final String TEST_CLOUD_DOMAIN_NAME = "test.siyouqun.com";

    private static final String DEV_CLOUD_DOMAIN_NAME = "10.10.9.87";

    private static final int DEBUG_CLOUD_PORT = 4000;

    private static final int RELEASE_CLOUD_PORT = 80;

    public static final String CLOUD_API_LEVEL = "/c/v1";

    static String CURRENT_DOMAIN_NAME = BuildConfig.server_url;

    CloudHttpRequestFactory(HttpHeader httpHeader) {
        super(httpHeader);

        setGateway(CURRENT_DOMAIN_NAME);

        setPort(BuildConfig.server_port);

    }

}
