package com.winsun.fruitmix.token.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.request.factory.CloudHttpRequestFactory;
import com.winsun.fruitmix.parser.RemoteTokenParser;
import com.winsun.fruitmix.parser.RemoteWeChatTokenParser;
import com.winsun.fruitmix.token.param.StationTokenParam;
import com.winsun.fruitmix.token.WeChatTokenUserWrapper;

/**
 * Created by Administrator on 2017/7/13.
 */

public class TokenRemoteDataSource extends BaseRemoteDataSourceImpl implements TokenDataSource {

    public TokenRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    /*
     * WISNUC API:GET TOKEN
     * get token by user uuid and password
     */
    @Override
    public void getStationToken(StationTokenParam stationTokenParam, BaseLoadDataCallback<String> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetTokenRequest(stationTokenParam);

        wrapper.loadCall(httpRequest, callback, new RemoteTokenParser());

    }

    @Override
    public void getCloudToken(String wechatCode, BaseLoadDataCallback<WeChatTokenUserWrapper> callback) {

        String path = CloudHttpRequestFactory.CLOUD_API_LEVEL + "/token?code=" + wechatCode + "&platform=mobile";

        HttpRequest httpRequest = httpRequestFactory.createHttpGetTokenRequestByCloudAPI(path);

        wrapper.loadCall(httpRequest, callback, new RemoteWeChatTokenParser());

    }

    @Override
    public void getStationTokenThroughCloudToken(BaseLoadDataCallback<String> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest("/token");

        wrapper.loadCall(httpRequest, callback, new RemoteTokenParser());

    }

    @Override
    public void getSCloudTokenThroughStationTokenWithThreadChange(String userGUID, BaseLoadDataCallback<String> callback) {

        getSCloudToken(userGUID,callback);
    }

    @Override
    public void getSCloudTokenThroughStationTokenWithoutThreadChange(String userGUID, BaseLoadDataCallback<String> callback) {

        getSCloudToken(userGUID, callback);

    }

    private void getSCloudToken(String userGUID, BaseLoadDataCallback<String> callback) {
        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest("/cloudToken?guid=" + userGUID);

        wrapper.loadCall(httpRequest, callback, new RemoteTokenParser());
    }
}
