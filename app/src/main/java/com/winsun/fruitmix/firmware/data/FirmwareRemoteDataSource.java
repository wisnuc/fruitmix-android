package com.winsun.fruitmix.firmware.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.firmware.model.Firmware;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteFirmwareParser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/12/28.
 */

public class FirmwareRemoteDataSource extends BaseRemoteDataSourceImpl implements FirmwareDataSource {

    public static final String V1 = "/v1/";

    public static final String APP = "app";

    public static final String FATCH = "fetch";

    public static final String RELEASES = "/releases/";

    public static final int PORT = 3001;

    public FirmwareRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    @Override
    public void installFirmware(String versionName, BaseOperateDataCallback<Void> callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("tagName", versionName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPutRequest(V1 + APP, jsonObject.toString(), PORT);

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<Void>() {
            @Override
            public Void parse(String json) throws JSONException {
                return null;
            }
        });

    }

    @Override
    public void updateFirmwareState(String state, BaseOperateDataCallback<Void> callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("state", state);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequest(V1 + APP, jsonObject.toString(), PORT);

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<Void>() {
            @Override
            public Void parse(String json) throws JSONException {
                return null;
            }
        });

    }

    @Override
    public void updateDownloadFirmwareState(String versionName, String state, BaseOperateDataCallback<Void> callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("state", state);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequest(V1 + RELEASES + versionName, jsonObject.toString(), PORT);

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<Void>() {
            @Override
            public Void parse(String json) throws JSONException {
                return null;
            }
        });

    }

    @Override
    public void checkFirmwareUpdate(BaseOperateDataCallback<Void> callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("state", "Working");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequest(V1 + FATCH, jsonObject.toString(), PORT);

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<Void>() {
            @Override
            public Void parse(String json) throws JSONException {
                return null;
            }
        });

    }

    @Override
    public void getFirmware(BaseLoadDataCallback<Firmware> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(V1, PORT);

        wrapper.loadCall(httpRequest, callback, new RemoteFirmwareParser());

    }
}
