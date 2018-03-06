package com.winsun.fruitmix.http.request.factory;

/**
 * Created by Administrator on 2018/3/6.
 */

public class BaseBoxWithSCloudTokenWithoutBoxUUIDFactory extends BaseBoxWithSCloudTokenFactory {

    public BaseBoxWithSCloudTokenWithoutBoxUUIDFactory(HttpRequestFactory httpRequestFactory, String sCloudToken) {
        super(httpRequestFactory, sCloudToken);
    }

    @Override
    BaseAbsHttpRequestFactory createCloudFactoryWhenLocalBoxAPIAvailable(String operateStationID) {
        return mHttpRequestFactory.createCloudHttpRequestForStationAPIFactory(operateStationID);
    }
}
