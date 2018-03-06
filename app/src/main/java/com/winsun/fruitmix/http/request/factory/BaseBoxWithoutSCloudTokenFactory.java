package com.winsun.fruitmix.http.request.factory;

/**
 * Created by Administrator on 2018/3/6.
 */

public class BaseBoxWithoutSCloudTokenFactory extends BaseBoxHttpRequestFactory {

    private String operateGroupUUID;

    public BaseBoxWithoutSCloudTokenFactory(HttpRequestFactory httpRequestFactory, String operateGroupUUID) {
        super(httpRequestFactory);
        this.operateGroupUUID = operateGroupUUID;
    }

    @Override
    protected BaseAbsHttpRequestFactory createFactoryWhenLocalBoxAPIAvailable(String operateStationID) {
        return mHttpRequestFactory.createStationHttpRequestFactory();
    }

    @Override
    protected BaseAbsHttpRequestFactory createFactoryWhenLocalBoxAPIUnAvailable(String operateStationID) {
        return mHttpRequestFactory.createCloudHttpRequestForStationBoxAPIFactory(operateGroupUUID, operateStationID);
    }
}
