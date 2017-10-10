package com.winsun.fruitmix.http.factory;

import android.util.Base64;

import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/9/18.
 */

public class NoWrapHttpRequestFactoryImpl extends BaseHttpRequestFactoryImpl implements NoWrapHttpRequestFactory{

    public NoWrapHttpRequestFactoryImpl(SystemSettingDataSource systemSettingDataSource) {
        super(systemSettingDataSource);
    }

    @Override
    public HttpRequest createHttpGetRequest(String httpPath, boolean isPipe) {

        HttpRequest httpRequest = new HttpRequest(createUrl(httpPath), Util.HTTP_GET_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, getToken());

        return httpRequest;

    }

    @Override
    public HttpRequest createHttpPostRequest(String httpPath, String body,boolean isPipe) {

        return createHasBodyRequest(createUrl(httpPath), Util.HTTP_POST_METHOD, body);
    }

    private HttpRequest createHasBodyRequest(String url, String method, String body) {

        HttpRequest httpRequest = new HttpRequest(url, method);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, getToken());
        httpRequest.setBody(body);

        return httpRequest;

    }


    @Override
    public HttpRequest createHttpGetTokenRequest(LoadTokenParam loadTokenParam) {

        String url = loadTokenParam.getGateway() + ":" + getPort() + Util.TOKEN_PARAMETER;

        HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_GET_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_BASE_HEAD + Base64.encodeToString((loadTokenParam.getUserUUID() + ":" + loadTokenParam.getUserPassword()).getBytes(), Base64.NO_WRAP));

        return httpRequest;

    }

    @Override
    public HttpRequest createGetRequestWithoutToken(String url) {
        return new HttpRequest(url, Util.HTTP_GET_METHOD);
    }

    @Override
    public HttpRequest createGetRequestByPathWithoutToken(String httpPath) {
        return createGetRequestWithoutToken(createUrl(httpPath));
    }

}
