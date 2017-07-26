package com.winsun.fruitmix.invitation;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.parser.RemoteConfirmInviteUsersParser;
import com.winsun.fruitmix.parser.RemoteConfirmUserResultParser;
import com.winsun.fruitmix.parser.RemoteTicketParser;

import java.util.List;

/**
 * Created by Administrator on 2017/7/12.
 */

public class InvitationRemoteDataSource extends BaseRemoteDataSourceImpl {

    public static final String TAG = InvitationRemoteDataSource.class.getSimpleName();

    public static final String TICKETS_PARAMETER = "/station/tickets";

    public static final String CONFIRM_TICKET_PARAMETER = "/station/tickets/wechat/";

    public InvitationRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    public void createInvitation(BaseOperateDataCallback<String> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(TICKETS_PARAMETER, "{\"type\":\"invite\"}");

        wrapper.operateCall(httpRequest, callback, new RemoteTicketParser());

    }

    public void getInvitation(final BaseLoadDataCallback<ConfirmInviteUser> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(TICKETS_PARAMETER);

        wrapper.loadCall(httpRequest, callback, new RemoteConfirmInviteUsersParser());

    }

    public void confirmInvitation(ConfirmInviteUser confirmInviteUser, BaseOperateDataCallback<String> callback) {

        String path = CONFIRM_TICKET_PARAMETER + confirmInviteUser.getTicketUUID();

        boolean state;

        state = confirmInviteUser.getOperateType().equals(ConfirmInviteUser.OPERATE_TYPE_ACCEPT);

        String body = "{\n" +
                "\t\"guid\":\"" + confirmInviteUser.getUserUUID() + "\",\n" +
                "\t\"state\":" + state + "\n" +
                "}";

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(path, body);

        wrapper.operateCall(httpRequest, callback, new RemoteConfirmUserResultParser());

    }

}
