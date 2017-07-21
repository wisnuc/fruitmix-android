package com.winsun.fruitmix.invitation;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.http.BaseHttpCallWrapper;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.parser.RemoteTicketParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/7/12.
 */

public class InvitationRemoteDataSource extends BaseRemoteDataSourceImpl {

    public static final String TAG = InvitationRemoteDataSource.class.getSimpleName();

    public static final String TICKETS_PARAMETER = "/station/tickets";

    public InvitationRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    public void createInvitation(BaseOperateDataCallback<String> callback) {

//        callback.onFail(new OperationIOException());

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(TICKETS_PARAMETER, "{\"type\":1}");

        wrapper.operateCall(httpRequest, callback, new RemoteTicketParser());

    }

    public void getInvitation(final BaseLoadDataCallback<ConfirmInviteUser> callback) {

        callback.onFail(new OperationIOException());

/*        BaseHttpCallWrapper wrapper = new BaseHttpCallWrapper(iHttpUtil);

        HttpRequest httpRequest = new HttpRequest(FNAS.generateUrl(TICKETS_PARAMETER), Util.HTTP_POST_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        httpRequest.setBody("{\"type\":1}");

        wrapper.loadCall(httpRequest, new BaseLoadDataCallback<ConfirmInviteUser>() {
            @Override
            public void onSucceed(List<ConfirmInviteUser> data, OperationResult operationResult) {

                callback.onSucceed(data, operationResult);
            }

            @Override
            public void onFail(OperationResult operationResult) {
                callback.onFail(operationResult);
            }
        }, new RemoteConfirmInviteUsersParser());*/

    }

    public void acceptInvitation(ConfirmInviteUser confirmInviteUser, BaseOperateDataCallback<ConfirmInviteUser> callback) {

        callback.onFail(new OperationIOException());

//        BaseHttpCallWrapper wrapper = new BaseHttpCallWrapper(iHttpUtil);
//
//        HttpRequest httpRequest = new HttpRequest(FNAS.generateUrl(TICKETS_PARAMETER), Util.HTTP_POST_METHOD);
//        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
//        httpRequest.setBody("{\"type\":1}");
//
//        wrapper.operateCall(httpRequest, callback, new RemoteConfirmInviteUserParser());

    }

    public void refuseInvitation(ConfirmInviteUser confirmInviteUser, BaseOperateDataCallback<ConfirmInviteUser> callback) {

        callback.onFail(new OperationIOException());

//        BaseHttpCallWrapper wrapper = new BaseHttpCallWrapper(iHttpUtil);
//
//        HttpRequest httpRequest = new HttpRequest(FNAS.generateUrl(TICKETS_PARAMETER), Util.HTTP_POST_METHOD);
//        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
//        httpRequest.setBody("{\"type\":1}");
//
//        wrapper.operateCall(httpRequest, callback, new RemoteConfirmInviteUserParser());

    }


}
