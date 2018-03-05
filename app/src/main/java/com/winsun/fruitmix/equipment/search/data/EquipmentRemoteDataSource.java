package com.winsun.fruitmix.equipment.search.data;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationFail;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.model.operationResult.OperationSuccessWithEquipmentBootInfo;
import com.winsun.fruitmix.parser.RemoteDatasParser;
import com.winsun.fruitmix.parser.RemoteEquipmentBootInfoParser;
import com.winsun.fruitmix.parser.RemoteEquipmentTypeInfoParser;
import com.winsun.fruitmix.parser.RemoteStationsCallByStationAPIParser;
import com.winsun.fruitmix.stations.StationInfoCallByStationAPI;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.parser.RemoteEquipmentHostAliasParser;
import com.winsun.fruitmix.parser.RemoteLoginUsersParser;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/11.
 */

public class EquipmentRemoteDataSource extends BaseRemoteDataSourceImpl implements EquipmentDataSource {

    public static final String TAG = EquipmentRemoteDataSource.class.getSimpleName();

    private static final String IPALIASING = "/system/ipaliasing";

    private static final String EQUIPMENT_CONTROL_SYSTEM = "/control/system";

    private static final String EQUIPMENT_NAME = "/station/info";

    public static final String BOOT = "/boot";
    public static final String STORAGE = "/storage";

    public static final String BOOT_ENOALT = "ENOALT";
    public static final String BOOT_ELASTNOTMOUNT = "ELASTNOTMOUNT";
    public static final String BOOT_ELASTMISSING = "ELASTMISSING";
    public static final String BOOT_ELASTDAMAGED = "ELASTDAMAGED";
    public static final String MODE_MAINTENANCE = "maintenance";

    public EquipmentRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    @Override
    public void getUsersInEquipment(Equipment equipment, BaseLoadDataCallback<User> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(equipment.getHosts().get(0), Util.USERS_PARAMETER);

        wrapper.loadCall(httpRequest, callback, new RemoteLoginUsersParser());

    }

    @Override
    public void checkEquipmentState(final String equipmentIP, final BaseOperateDataCallback<Integer> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(equipmentIP, BOOT);

        int totalCount = 3;
        int currentCount = 0;

        while (true) {

            if (currentCount < totalCount) {
                currentCount++;
            } else
                break;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            OperationResult operationResult = wrapper.loadEquipmentBootInfo(httpRequest, new RemoteEquipmentBootInfoParser());

            Log.d(TAG, "checkEquipmentState: ");

            if (operationResult.getOperationResultType() != OperationResultType.SUCCEED) {

                callback.onFail(operationResult);
                break;
            }

            EquipmentBootInfo equipmentBootInfo = ((OperationSuccessWithEquipmentBootInfo) operationResult).getEquipmentBootInfos().get(0);

            if (equipmentBootInfo.getState().equals("started")) {

                if (equipmentBootInfo.getError().equals("null") && (!equipmentBootInfo.getCurrent().equals("null") && equipmentBootInfo.getCurrent().length() > 0)) {

                    Log.d(TAG, "onSucceed: ip:" + equipmentIP + " ready");

                    callback.onSucceed(EQUIPMENT_READY, new OperationSuccess());

                } else {

                    if (equipmentBootInfo.getError().equals(BOOT_ELASTNOTMOUNT) || equipmentBootInfo.getError().equals(BOOT_ELASTMISSING)
                            || equipmentBootInfo.getError().equals(BOOT_ELASTDAMAGED)) {

                        Log.d(TAG, "checkEquipmentState: ip:" + equipmentIP + " " + BOOT_ELASTNOTMOUNT + " or " + BOOT_ELASTDAMAGED + " or " + BOOT_ELASTMISSING);

                        callback.onSucceed(EQUIPMENT_MAINTENANCE, new OperationSuccess());

                    } else if (equipmentBootInfo.getError().equals(BOOT_ENOALT)) {

                        handleGetEquipmentBootInfo(equipmentIP, callback);

                    } else if (equipmentBootInfo.getMode().equals(MODE_MAINTENANCE)) {

                        Log.d(TAG, "checkEquipmentState: ip:" + equipmentIP + " " + MODE_MAINTENANCE);

                        callback.onSucceed(EQUIPMENT_MAINTENANCE, new OperationSuccess());

                    } else {

                        Log.d(TAG, "checkEquipmentState: ip:" + equipmentIP + "unknown maintenance");

                        callback.onSucceed(EQUIPMENT_MAINTENANCE, new OperationSuccess());
                    }

                }

                break;


            } else if (equipmentBootInfo.getState().equals("stopping")) {

                callback.onFail(new OperationFail("device initWizard: fruitmix stopping (unexpected), stop"));

                break;
            }

        }


    }

    private void handleGetEquipmentBootInfo(final String ip, final BaseOperateDataCallback<Integer> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(ip, STORAGE);

        wrapper.loadCall(httpRequest, new BaseLoadDataCallback<EquipmentStorageInfo>() {
            @Override
            public void onSucceed(List<EquipmentStorageInfo> data, OperationResult operationResult) {

                EquipmentStorageInfo equipmentStorageInfo = data.get(0);

                if (equipmentStorageInfo.getEmptyVolumes()) {

                    Log.d(TAG, "onSucceed: ip:" + ip + " uninitialized");

                    callback.onSucceed(EQUIPMENT_UNINITIALIZED, new OperationSuccess());
                } else {

                    Log.d(TAG, "onSucceed: ip:" + ip + " no volume");

                    callback.onSucceed(EQUIPMENT_MAINTENANCE, new OperationSuccess());

                }

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);

            }
        }, new RemoteDatasParser<EquipmentStorageInfo>() {
            @Override
            public List<EquipmentStorageInfo> parse(String json) throws JSONException {

                JSONObject jsonObject = new JSONObject(json);

                boolean emptyVolumes = jsonObject.has("volumes");

                if (emptyVolumes) {

                    JSONArray jsonArray = jsonObject.optJSONArray("volumes");

                    emptyVolumes = jsonArray.length() == 0;

                }

                return Collections.singletonList(new EquipmentStorageInfo(emptyVolumes));
            }
        });

    }


    public void getEquipmentHostAlias(Equipment equipment, BaseLoadDataCallback<String> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(equipment.getHosts().get(0), IPALIASING);

        wrapper.loadCall(httpRequest, callback, new RemoteEquipmentHostAliasParser());

    }

    @Override
    public void getEquipmentTypeInfo(final String equipmentIP, final BaseLoadDataCallback<EquipmentTypeInfo> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(equipmentIP, EQUIPMENT_CONTROL_SYSTEM);

        wrapper.loadCall(httpRequest, new BaseLoadDataCallback<EquipmentTypeInfo>() {
            @Override
            public void onSucceed(List<EquipmentTypeInfo> data, OperationResult operationResult) {

                getEquipmentName(callback, equipmentIP, data.get(0));

            }

            @Override
            public void onFail(OperationResult operationResult) {
                callback.onFail(operationResult);
            }
        }, new RemoteEquipmentTypeInfoParser());

    }

    private void getEquipmentName(final BaseLoadDataCallback<EquipmentTypeInfo> callback, String equipmentIP, final EquipmentTypeInfo equipmentTypeInfo) {
        HttpRequest getEquipmentNameHttpRequest = httpRequestFactory.createGetRequestWithoutToken(equipmentIP, EQUIPMENT_NAME);
        wrapper.loadCall(getEquipmentNameHttpRequest, new BaseLoadDataCallback<StationInfoCallByStationAPI>() {
            @Override
            public void onSucceed(List<StationInfoCallByStationAPI> data, OperationResult operationResult) {

                equipmentTypeInfo.setLabel(data.get(0).getName());
                callback.onSucceed(Collections.singletonList(equipmentTypeInfo), new OperationSuccess());

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onSucceed(Collections.singletonList(equipmentTypeInfo), new OperationSuccess());
            }

        }, new RemoteStationsCallByStationAPIParser());
    }


}
