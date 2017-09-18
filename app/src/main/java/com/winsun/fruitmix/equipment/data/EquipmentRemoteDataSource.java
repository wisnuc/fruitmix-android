package com.winsun.fruitmix.equipment.data;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.model.EquipmentInfo;
import com.winsun.fruitmix.parser.RemoteEquipmentInfoParser;
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

    private static final String EQUIPMENT_INFO = "/control/system";

    public EquipmentRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    @Override
    public void getUsersInEquipment(Equipment equipment, BaseLoadDataCallback<User> callback) {

        String url = Util.HTTP + equipment.getHosts().get(0) + ":" + httpRequestFactory.getPort() + Util.LOGIN_PARAMETER;

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(url);

        wrapper.loadCall(httpRequest, callback, new RemoteLoginUsersParser());

    }

    public void getEquipmentHostAlias(Equipment equipment, BaseLoadDataCallback<String> callback) {

        String ipaliasingUrl = Util.HTTP + equipment.getHosts().get(0) + ":" + httpRequestFactory.getPort() + IPALIASING;

        Log.d(TAG, "login retrieve equipment alias:" + ipaliasingUrl);

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(ipaliasingUrl);

        wrapper.loadCall(httpRequest, callback, new RemoteEquipmentHostAliasParser());

    }

    //TODO: get equipment info when login by wechat code,how to handle it

    @Override
    public void getEquipmentInfo(String equipmentIP, BaseLoadDataCallback<EquipmentInfo> callback) {

        String url = Util.HTTP + equipmentIP + ":" + httpRequestFactory.getPort() + EQUIPMENT_INFO;

        Log.d(TAG, "getEquipmentInfo: url: " + url);

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(url);

        wrapper.loadCall(httpRequest, callback, new RemoteEquipmentInfoParser());

    }


}
