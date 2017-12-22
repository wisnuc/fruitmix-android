package com.winsun.fruitmix.plugin.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemotePluginStatusParser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/12/22.
 */

public class PluginManageRemoteDataSource extends BaseRemoteDataSourceImpl implements PluginManageDataSource {

    private static final String FEATURE = "/features/";

    private static final String STATUS = "/status";

    public static final String TORRENT_STATUS = "/download/switch";

    public PluginManageRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    @Override
    public void getPluginStatus(String type, BaseLoadDataCallback<PluginStatus> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(FEATURE + type + STATUS);

        wrapper.loadCall(httpRequest, callback, new RemotePluginStatusParser());

    }

    @Override
    public void updatePluginStatus(String type, String action, BaseOperateDataCallback<Void> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(FEATURE + type + "/" + action, "");

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<Void>() {
            @Override
            public Void parse(String json) throws JSONException {
                return null;
            }
        });

    }

    @Override
    public void getBTStatus(BaseLoadDataCallback<PluginStatus> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(TORRENT_STATUS);

        wrapper.loadCall(httpRequest, callback, new RemotePluginStatusParser());

    }

    @Override
    public void updateBTStatus(String op, BaseOperateDataCallback<Void> callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("op", op);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequest(TORRENT_STATUS, jsonObject.toString());

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<Void>() {
            @Override
            public Void parse(String json) throws JSONException {
                return null;
            }
        });

    }
}
