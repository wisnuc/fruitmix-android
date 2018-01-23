package com.winsun.fruitmix.logged.in.user;

import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.equipment.search.data.Equipment;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;

/**
 * Created by Administrator on 2017/11/29.
 */

public class LoggedInUserRemoteDataSourceImpl extends BaseRemoteDataSourceImpl implements LoggedInUserRemoteDataSource {

    public LoggedInUserRemoteDataSourceImpl(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    @Override
    public void checkFoundedEquipment(Equipment foundedEquipment, LoggedInUser loggedInUser, BaseOperateDataCallback<Boolean> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithToken(foundedEquipment.getHosts().get(0), loggedInUser.getToken(), Util.USERS_PARAMETER + "/" + loggedInUser.getUser().getUuid());

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<Boolean>() {
            @Override
            public Boolean parse(String json) throws JSONException {
                return true;
            }
        });

    }
}
