package com.winsun.fruitmix.equipment;

import android.support.annotation.NonNull;
import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationJSONException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteEquipmentHostAliasParser;
import com.winsun.fruitmix.parser.RemoteUserParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/11.
 */

public class EquipmentDataSource {

    public static final String TAG = EquipmentDataSource.class.getSimpleName();

    private static final String SYSTEM_PORT = "3000";
    private static final String IPALIASING = "/system/ipaliasing";

    private IHttpUtil iHttpUtil;

    public EquipmentDataSource(IHttpUtil iHttpUtil) {
        this.iHttpUtil = iHttpUtil;
    }

    public void getUsersInEquipment(Equipment equipment, BaseLoadDataCallback<User> callback) {

        String url = Util.HTTP + equipment.getHosts().get(0) + ":" + Util.PORT + Util.LOGIN_PARAMETER;

        HttpRequest httpRequest = getHttpRequest(url);

        try {

            HttpResponse httpResponse = iHttpUtil.remoteCall(httpRequest);

            if (httpResponse.getResponseCode() == 200) {

                String str = iHttpUtil.remoteCall(httpRequest).getResponseData();

                RemoteUserParser parser = new RemoteUserParser();

                List<User> users = parser.parse(str);

                callback.onSucceed(users, new OperationSuccess(0));
            } else {
                callback.onFail(new OperationNetworkException(httpResponse.getResponseCode()));
            }


        } catch (IOException e) {
            e.printStackTrace();

            callback.onFail(new OperationIOException());

        } catch (JSONException e) {
            e.printStackTrace();

            callback.onFail(new OperationJSONException());
        }

    }

    public void getEquipmentHostAlias(Equipment equipment, BaseLoadDataCallback<String> callback) {

        String ipaliasingUrl = Util.HTTP + equipment.getHosts().get(0) + ":" + SYSTEM_PORT + IPALIASING;

        Log.d(TAG, "login retrieve equipment alias:" + ipaliasingUrl);

        String str;

        try {

            HttpRequest httpRequest = getHttpRequest(ipaliasingUrl);

            HttpResponse httpResponse = iHttpUtil.remoteCall(httpRequest);

            if (httpResponse.getResponseCode() == 200) {
                str = httpResponse.getResponseData();

                RemoteDataParser<String> parser = new RemoteEquipmentHostAliasParser();

                List<String> aliasList = parser.parse(str);

                callback.onSucceed(aliasList, new OperationSuccess(0));


            } else {
                callback.onFail(new OperationNetworkException(httpResponse.getResponseCode()));
            }

        } catch (IOException e) {
            e.printStackTrace();

            callback.onFail(new OperationIOException());

        } catch (JSONException e) {
            e.printStackTrace();

            callback.onFail(new OperationJSONException());
        }


    }

    @NonNull
    private HttpRequest getHttpRequest(String url) {
        HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_GET_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        return httpRequest;
    }

}
