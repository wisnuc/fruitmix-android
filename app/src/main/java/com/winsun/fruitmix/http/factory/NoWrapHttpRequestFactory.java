package com.winsun.fruitmix.http.factory;

import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.token.param.StationTokenParam;

/**
 * Created by Administrator on 2017/9/19.
 */

public interface NoWrapHttpRequestFactory extends BaseHttpRequestFactory {

    HttpRequest createHttpGetTokenRequest(StationTokenParam stationTokenParam);

    HttpRequest createGetRequestWithoutToken(String url);

}
