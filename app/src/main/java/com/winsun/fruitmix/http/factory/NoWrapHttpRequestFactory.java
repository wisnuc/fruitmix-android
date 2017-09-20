package com.winsun.fruitmix.http.factory;

import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.token.LoadTokenParam;

/**
 * Created by Administrator on 2017/9/19.
 */

public interface NoWrapHttpRequestFactory extends BaseHttpRequestFactory {

    HttpRequest createHttpGetTokenRequest(LoadTokenParam loadTokenParam);

    HttpRequest createGetRequestWithoutToken(String url);

}
