package com.winsun.fruitmix.http.request.factory;

/**
 * Created by Administrator on 2018/3/6.
 */

public abstract class BaseBoxHttpRequestFactory {

    HttpRequestFactory mHttpRequestFactory;

    BaseBoxHttpRequestFactory(HttpRequestFactory httpRequestFactory) {
        mHttpRequestFactory = httpRequestFactory;
    }

    BaseAbsHttpRequestFactory createFactoryForBox(String operateStationID) {

        BaseAbsHttpRequestFactory factory;

        if (mHttpRequestFactory.checkLocalBoxAPIAvailable(operateStationID)) {

            factory = createFactoryWhenLocalBoxAPIAvailable(operateStationID);

        } else {

            factory = createFactoryWhenLocalBoxAPIUnAvailable(operateStationID);

        }

        return factory;

    }

    protected abstract BaseAbsHttpRequestFactory createFactoryWhenLocalBoxAPIAvailable(String operateStationID);

    protected abstract BaseAbsHttpRequestFactory createFactoryWhenLocalBoxAPIUnAvailable(String operateStationID);


}
