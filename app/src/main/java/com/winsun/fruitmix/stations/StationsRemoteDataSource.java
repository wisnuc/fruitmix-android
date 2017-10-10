package com.winsun.fruitmix.stations;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.request.factory.CloudHttpRequestFactory;
import com.winsun.fruitmix.parser.RemoteDatasParser;
import com.winsun.fruitmix.parser.RemoteStationParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

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

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequestByCloudAPIWithoutWrap(CloudHttpRequestFactory.CLOUD_API_LEVEL + "/users/" + guid + "/stations");

        wrapper.loadCall(httpRequest, callback, new RemoteStationParser());

    }

    @Override
    public void getStationInfoByStationAPI(String ip, BaseLoadDataCallback<Station> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(ip,"/station/info");

        wrapper.loadCall(httpRequest, callback, new RemoteDatasParser<Station>() {
            @Override
            public List<Station> parse(String json) throws JSONException {

                JSONObject jsonObject = new JSONObject(json);

                Station station = new Station();
                station.setId(jsonObject.optString("id"));

                return Collections.singletonList(station);
            }
        });

    }


}
