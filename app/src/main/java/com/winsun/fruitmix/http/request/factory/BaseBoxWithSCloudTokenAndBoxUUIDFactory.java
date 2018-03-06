package com.winsun.fruitmix.http.request.factory;

/**
 * Created by Administrator on 2018/3/6.
 */

public class BaseBoxWithSCloudTokenAndBoxUUIDFactory extends BaseBoxWithSCloudTokenFactory {

    private String operateGroupUUID;

    public BaseBoxWithSCloudTokenAndBoxUUIDFactory(HttpRequestFactory httpRequestFactory, String sCloudToken, String operateGroupUUID) {
        super(httpRequestFactory, sCloudToken);
        this.operateGroupUUID = operateGroupUUID;
    }


    @Override
    BaseAbsHttpRequestFactory createCloudFactoryWhenLocalBoxAPIAvailable(String operateStationID) {
        return mHttpRequestFactory.createCloudHttpRequestForStationBoxAPIFactory(operateGroupUUID, operateStationID);
    }

}
