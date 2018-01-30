package com.winsun.fruitmix.contact.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.parser.RemoteLoginUsersParser;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2018/1/27.
 */

public class ContactRemoteDataSource extends BaseRemoteDataSourceImpl implements ContactDataSource {

    public ContactRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    @Override
    public void getContacts(BaseLoadDataCallback<User> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest("/users");

        wrapper.loadCall(httpRequest,callback,new RemoteLoginUsersParser());

    }

}
