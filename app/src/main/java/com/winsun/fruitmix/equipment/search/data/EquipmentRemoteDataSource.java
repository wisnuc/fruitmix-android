package com.winsun.fruitmix.equipment.search.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.parser.RemoteEquipmentTypeInfoParser;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.parser.RemoteEquipmentHostAliasParser;
import com.winsun.fruitmix.parser.RemoteLoginUsersParser;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/7/11.
 */

public class EquipmentRemoteDataSource extends BaseRemoteDataSourceImpl implements EquipmentDataSource {

    public static final String TAG = EquipmentRemoteDataSource.class.getSimpleName();

    private static final String IPALIASING = "/system/ipaliasing";

    private static final String EQUIPMENT_CONTROL_SYSTEM = "/control/system";

    public EquipmentRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    @Override
    public void getUsersInEquipment(Equipment equipment, BaseLoadDataCallback<User> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(equipment.getHosts().get(0),Util.LOGIN_PARAMETER);

        wrapper.loadCall(httpRequest, callback, new RemoteLoginUsersParser());

    }

    public void getEquipmentHostAlias(Equipment equipment, BaseLoadDataCallback<String> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(equipment.getHosts().get(0),IPALIASING);

        wrapper.loadCall(httpRequest, callback, new RemoteEquipmentHostAliasParser());

    }

    @Override
    public void getEquipmentTypeInfo(String equipmentIP, BaseLoadDataCallback<EquipmentTypeInfo> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(equipmentIP, EQUIPMENT_CONTROL_SYSTEM);

        wrapper.loadCall(httpRequest, callback, new RemoteEquipmentTypeInfoParser());

    }


}
