package com.winsun.fruitmix.http.request.factory;

/**
 * Created by Administrator on 2018/3/6.
 */

public abstract class BaseBoxWithSCloudTokenFactory extends BaseBoxHttpRequestFactory {

    private String sCloudToken;

    public BaseBoxWithSCloudTokenFactory(HttpRequestFactory httpRequestFactory, String sCloudToken) {
        super(httpRequestFactory);
        this.sCloudToken = sCloudToken;
    }

    @Override
    protected BaseAbsHttpRequestFactory createFactoryWhenLocalBoxAPIAvailable(String operateStationID) {
        if (sCloudToken != null && sCloudToken.length() > 0) {

            return  mHttpRequestFactory.createStationHttpRequestFactory(sCloudToken);

        } else {

            return createCloudFactoryWhenLocalBoxAPIAvailable(operateStationID);

        }
    }

    abstract BaseAbsHttpRequestFactory createCloudFactoryWhenLocalBoxAPIAvailable(String operateStationID);


    @Override
    protected BaseAbsHttpRequestFactory createFactoryWhenLocalBoxAPIUnAvailable(String operateStationID) {
        return createCloudFactoryWhenLocalBoxAPIAvailable(operateStationID);
    }
}
