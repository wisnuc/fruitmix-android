package com.winsun.fruitmix.equipment.manage.data;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.equipment.manage.model.BaseEquipmentInfo;
import com.winsun.fruitmix.equipment.manage.model.EquipmentInfoInControlSystem;
import com.winsun.fruitmix.equipment.manage.model.EquipmentInfoInStorage;
import com.winsun.fruitmix.equipment.manage.model.EquipmentNetworkInfo;
import com.winsun.fruitmix.equipment.manage.model.EquipmentTimeInfo;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteBootParser;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteEquipmentInfoInControlSystemParser;
import com.winsun.fruitmix.parser.RemoteEquipmentInfoInStorageParser;
import com.winsun.fruitmix.parser.RemoteEquipmentNetworkInfoParser;
import com.winsun.fruitmix.parser.RemoteEquipmentTimeInfoParser;
import com.winsun.fruitmix.parser.RemoteStationsCallByStationAPIParser;
import com.winsun.fruitmix.stations.StationInfoCallByStationAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/10/17.
 */

public class EquipmentInfoRemoteDataSource extends BaseRemoteDataSourceImpl implements EquipmentInfoDataSource {

    public static final String TAG = EquipmentInfoRemoteDataSource.class.getSimpleName();

    private static final String EQUIPMENT_CONTROL_SYSTEM = "/control/system";

    private static final String EQUIPMENT_INFO = "/station/info";

    private static final String EQUIPMENT_STORAGE = "/storage";

    private static final String EQUIPMENT_NETWORK = "/control/net/interfaces";

    private static final String EQUIPMENT_TIME = "/control/timedate";

    public static final String MANAGE_EQUIPMENT = "/boot";

    public EquipmentInfoRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    /*
     * WISNUC API:GET SYSTEM
     */
    @Override
    public void getBaseEquipmentInfo(final BaseLoadDataCallback<BaseEquipmentInfo> callback) {

        HttpRequest controlSystemHttpRequest = httpRequestFactory.createGetRequestByPathWithoutToken(EQUIPMENT_CONTROL_SYSTEM);

        wrapper.loadCall(controlSystemHttpRequest, new BaseLoadDataCallback<EquipmentInfoInControlSystem>() {
            @Override
            public void onSucceed(List<EquipmentInfoInControlSystem> data, OperationResult operationResult) {

                getEquipmentInfo(data.get(0), callback);

            }

            @Override
            public void onFail(OperationResult operationResult) {
                callback.onFail(operationResult);
            }
        }, new RemoteEquipmentInfoInControlSystemParser());

    }

    /*
     * WISNUC API:GET STATION INFO
     */
    private void getEquipmentInfo(final EquipmentInfoInControlSystem equipmentInfoInControlSystem, final BaseLoadDataCallback<BaseEquipmentInfo> callback) {

        HttpRequest equipmentInfoHttpRequest = httpRequestFactory.createGetRequestByPathWithoutToken(EQUIPMENT_INFO);

        wrapper.loadCall(equipmentInfoHttpRequest, new BaseLoadDataCallback<StationInfoCallByStationAPI>() {
            @Override
            public void onSucceed(List<StationInfoCallByStationAPI> data, OperationResult operationResult) {

                equipmentInfoInControlSystem.setEquipmentName(data.get(0).getName());

                getBoot(equipmentInfoInControlSystem, callback);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                getBoot(equipmentInfoInControlSystem, callback);

            }
        }, new RemoteStationsCallByStationAPIParser());

    }


    private void getBoot(final EquipmentInfoInControlSystem equipmentInfoInControlSystem, final BaseLoadDataCallback<BaseEquipmentInfo> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestByPathWithoutToken(MANAGE_EQUIPMENT);

        Log.d(TAG, "getBoot: ");

        wrapper.loadCall(httpRequest, new BaseLoadDataCallback<Boot>() {

            @Override
            public void onSucceed(List<Boot> data, OperationResult operationResult) {

                getEquipmentInfoInStorage(data.get(0), equipmentInfoInControlSystem, callback);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);

            }
        }, new RemoteBootParser());

    }


    private void getEquipmentInfoInStorage(final Boot boot, final EquipmentInfoInControlSystem equipmentInfoInControlSystem, final BaseLoadDataCallback<BaseEquipmentInfo> callback) {

        HttpRequest storageHttpRequest = httpRequestFactory.createGetRequestByPathWithoutToken(EQUIPMENT_STORAGE);

        wrapper.loadCall(storageHttpRequest, new BaseLoadDataCallback<EquipmentInfoInStorage>() {
            @Override
            public void onSucceed(List<EquipmentInfoInStorage> data, OperationResult operationResult) {

                EquipmentInfoInStorage equipmentInfoInStorage = data.get(0);

                for (EquipmentInfoInStorage storage : data) {

                    if (storage.getEquipmentFileSystem().getUuid().equals(boot.getCurrentFileSystemUUID())) {
                        equipmentInfoInStorage = storage;
                        break;
                    }

                }

                BaseEquipmentInfo baseEquipmentInfo = new BaseEquipmentInfo();

                baseEquipmentInfo.setEquipmentName(equipmentInfoInControlSystem.getEquipmentName());
                baseEquipmentInfo.setEquipmentStorage(equipmentInfoInStorage.getEquipmentStorage());
                baseEquipmentInfo.setEquipmentFileSystem(equipmentInfoInStorage.getEquipmentFileSystem());
                baseEquipmentInfo.setEquipmentCPU(equipmentInfoInControlSystem.getEquipmentCPU());
                baseEquipmentInfo.setEquipmentHardware(equipmentInfoInControlSystem.getEquipmentHardware());
                baseEquipmentInfo.setEquipmentMemory(equipmentInfoInControlSystem.getEquipmentMemory());

                callback.onSucceed(Collections.singletonList(baseEquipmentInfo), new OperationSuccess());


            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);

            }
        }, new RemoteEquipmentInfoInStorageParser());

    }

    /*
     * WISNUC API:GET NETWORK INTERFACES
     */
    @Override
    public void getEquipmentNetworkInfo(BaseLoadDataCallback<EquipmentNetworkInfo> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestByPathWithoutToken(EQUIPMENT_NETWORK);

        wrapper.loadCall(httpRequest, callback, new RemoteEquipmentNetworkInfoParser());

    }

    /*
     * WISNUC API:GET TIMEDATE
     */
    @Override
    public void getEquipmentTimeInfo(BaseLoadDataCallback<EquipmentTimeInfo> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestByPathWithoutToken(EQUIPMENT_TIME);

        wrapper.loadCall(httpRequest, callback, new RemoteEquipmentTimeInfoParser());

    }

    @Override
    public void shutdownEquipment(BaseOperateDataCallback<Boolean> callback) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("state", "poweroff");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        manageEquipment(jsonObject.toString(), callback);

    }

    private void manageEquipment(String body, BaseOperateDataCallback<Boolean> callback) {

        HttpRequest httpRequest = httpRequestFactory.createPatchRequestByPathWithoutToken(MANAGE_EQUIPMENT, body);

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<Boolean>() {
            @Override
            public Boolean parse(String json) throws JSONException {
                return true;
            }
        });

    }


    @Override
    public void rebootEquipment(BaseOperateDataCallback<Boolean> callback) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("state", "reboot");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        manageEquipment(jsonObject.toString(), callback);

    }

    @Override
    public void rebootAndEnterMaintenanceMode(BaseOperateDataCallback<Boolean> callback) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("state", "reboot");
            jsonObject.put("mode", "maintenance");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        manageEquipment(jsonObject.toString(), callback);

    }

    /*
     * WISNUC API:PATCH STATION INFO
     */
    @Override
    public void modifyEquipmentLabel(String newEquipmentLabel, BaseOperateDataCallback<Boolean> callback) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("name", newEquipmentLabel);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequest(EQUIPMENT_INFO, jsonObject.toString());

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<Boolean>() {
            @Override
            public Boolean parse(String json) throws JSONException {
                return true;
            }
        });

    }


}
