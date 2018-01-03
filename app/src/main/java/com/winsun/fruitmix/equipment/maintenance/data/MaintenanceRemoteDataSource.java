package com.winsun.fruitmix.equipment.maintenance.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.equipment.maintenance.VolumeStateViewModel;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.BaseRemoteDataParser;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteDatasParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2018/1/2.
 */

public class MaintenanceRemoteDataSource extends BaseRemoteDataSourceImpl implements MaintenanceDataSource {

    public static final String STORAGE_VOLUMES = "/storage/volumes";

    public static final String BOOT = "/boot";

    public MaintenanceRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    @Override
    public void getDiskState(final String ip, final BaseLoadDataCallback<VolumeState> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(ip, STORAGE_VOLUMES);

        wrapper.loadCall(httpRequest, new BaseLoadDataCallback<VolumeState>() {
            @Override
            public void onSucceed(List<VolumeState> data, OperationResult operationResult) {
                handleVolumeStates(ip, data, callback);
            }

            @Override
            public void onFail(OperationResult operationResult) {
                callback.onFail(operationResult);
            }
        }, new RemoteVolumeState());

    }

    private class RemoteVolumeState extends BaseRemoteDataParser implements RemoteDatasParser<VolumeState> {

        @Override
        public List<VolumeState> parse(String json) throws JSONException {

            String root = checkHasWrapper(json);

            JSONArray jsonArray = new JSONArray(root);

            List<VolumeState> volumeStates = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.optJSONObject(i);

                boolean isMounted = jsonObject.optBoolean("isMounted");
                boolean noMissing = !jsonObject.optBoolean("missing");
                String uuid = jsonObject.optString("uuid");

                String type = jsonObject.optString("fileSystemType");

                JSONArray usersJsonArray = jsonObject.optJSONArray("users");

                boolean fruitmixOK;
                boolean usersOK;

                if (usersJsonArray == null) {

                    String users = jsonObject.optString("users");

                    fruitmixOK = users.equals("EDATA");

                    usersOK = false;

                } else {

                    fruitmixOK = true;

                    usersOK = true;

                }

                String mode = jsonObject.optJSONObject("usage").optJSONObject("data").optString("mode");

                VolumeState volumeState = new VolumeState(i, type, mode, uuid, isMounted, noMissing, false, fruitmixOK, usersOK);

                volumeStates.add(volumeState);

            }

            return volumeStates;
        }
    }

    private void handleVolumeStates(String ip, final List<VolumeState> volumeStates, final BaseLoadDataCallback<VolumeState> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(ip, BOOT);

        wrapper.loadCall(httpRequest, new BaseLoadDataCallback<String>() {
            @Override
            public void onSucceed(List<String> data, OperationResult operationResult) {

                String last = data.get(0);

                for (VolumeState volumeState : volumeStates) {
                    if (volumeState.getUuid().equals(last)) {
                        volumeState.setLastSystem(true);
                    }
                }

                callback.onSucceed(volumeStates, new OperationSuccess());

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);

            }
        }, new RemoteBootParser());

    }

    private class RemoteBootParser extends BaseRemoteDataParser implements RemoteDatasParser<String> {

        @Override
        public List<String> parse(String json) throws JSONException {
            return Collections.singletonList(new JSONObject(checkHasWrapper(json)).optString("last"));
        }
    }


    @Override
    public void startSystem(String ip, String volumeUUID, BaseOperateDataCallback<Void> callback) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("current", volumeUUID);

        HttpRequest httpRequest = httpRequestFactory.createPatchRequestWithoutToken(ip, BOOT, jsonObject.toString());

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<Void>() {
            @Override
            public Void parse(String json) throws JSONException {
                return null;
            }
        });

    }

}
