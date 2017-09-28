package com.winsun.fruitmix.token;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.parser.RemoteTokenParser;
import com.winsun.fruitmix.parser.RemoteWeChatTokenParser;

/**
 * Created by Administrator on 2017/7/13.
 */

public class TokenRemoteDataSource extends BaseRemoteDataSourceImpl implements TokenDataSource{

    public TokenRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    @Override
    public void getToken(LoadTokenParam loadTokenParam, BaseLoadDataCallback<String> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetTokenRequest(loadTokenParam);

        wrapper.loadCall(httpRequest, callback, new RemoteTokenParser());

    }

    @Override
    public void getToken(String wechatCode, BaseLoadDataCallback<WeChatTokenUserWrapper> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithWeChatCode(wechatCode);

        wrapper.loadCall(httpRequest, callback, new RemoteWeChatTokenParser());

    }

    @Override
    public void getTokenThroughWAToken(BaseLoadDataCallback<String> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest("/token");

        wrapper.loadCall(httpRequest,callback,new RemoteTokenParser());

    }

}
