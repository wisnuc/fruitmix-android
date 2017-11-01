package com.winsun.fruitmix.equipment.search.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteDatasParser;
import com.winsun.fruitmix.parser.RemoteEquipmentTypeInfoParser;
import com.winsun.fruitmix.parser.RemoteStationsCallByStationAPIParser;
import com.winsun.fruitmix.stations.StationInfoCallByStationAPI;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.parser.RemoteEquipmentHostAliasParser;
import com.winsun.fruitmix.parser.RemoteLoginUsersParser;
import com.winsun.fruitmix.util.Util;

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

    public EquipmentRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    @Override
    public void getUsersInEquipment(Equipment equipment, BaseLoadDataCallback<User> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(equipment.getHosts().get(0), Util.LOGIN_PARAMETER);

        wrapper.loadCall(httpRequest, callback, new RemoteLoginUsersParser());

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
                callback.onFail(operationResult);
            }

        }, new RemoteStationsCallByStationAPIParser());
    }


}
