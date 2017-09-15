package com.winsun.fruitmix.stations;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.parser.RemoteStationParser;

/**
 * Created by Administrator on 2017/9/14.
 */

public class StationsRemoteDataSource extends BaseRemoteDataSourceImpl implements StationsDataSource {

    private static StationsRemoteDataSource instance;

    public StationsRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    public static StationsRemoteDataSource getInstance(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {

        if (instance == null)
            instance = new StationsRemoteDataSource(iHttpUtil, httpRequestFactory);

        return instance;
    }

    @Override
    public void getStationsByWechatGUID(String guid, BaseLoadDataCallback<Station> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequestWithOutJWTHeader("/c/v1/users/" + guid + "/stations");

        wrapper.loadCall(httpRequest, callback, new RemoteStationParser());

    }


}
