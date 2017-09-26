package com.winsun.fruitmix.http.factory;

import com.winsun.fruitmix.http.HttpRequest;

/**
 * Created by Administrator on 2017/9/19.
 */

public interface BaseHttpRequestFactory {

    void setStationID(String stationID);

    void setGateway(String gateway);

    void setPort(int port);

    void setToken(String token);

    HttpRequest createHttpGetRequest(String httpPath, boolean isPipe);

    HttpRequest createHttpPostRequest(String httpPath, String body);

    HttpRequest createGetRequestByPathWithoutToken(String httpPath);

}
