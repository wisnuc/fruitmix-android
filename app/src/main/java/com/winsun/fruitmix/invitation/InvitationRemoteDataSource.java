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

    public InvitationRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    public void createInvitation(BaseOperateDataCallback<String> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(TICKETS_PARAMETER, "{\"type\":\"invite\"}");

        wrapper.operateCall(httpRequest, callback, new RemoteTicketParser());

    }

    public void getInvitation(final BaseLoadDataCallback<ConfirmInviteUser> callback) {

        String url = "http://10.10.13.16:3000/station/tickets";

        String token = "JWT eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1dWlkIjoiOWY5M2RiNDMtMDJlNi00YjI2LThmYWUtN2Q2ZjUxZGExMmFmIn0.83z5kzghi7R8FGumxsKoXAtM6RlrthDFceI3_ryRPSs";

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequestWithFullUrlAndToken(url, token);

        wrapper.loadCall(httpRequest, callback, new RemoteConfirmInviteUsersParser());

    }

    public void confirmInvitation(List<ConfirmInviteUser> confirmInviteUsers, BaseOperateDataCallback<String> callback) {

        ConfirmInviteUser confirmInviteUser = confirmInviteUsers.get(0);

        String url = "http://10.10.13.16:3000/station/tickets/wechat/" + confirmInviteUser.getTicketUUID();

        String token = "JWT eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1dWlkIjoiOWY5M2RiNDMtMDJlNi00YjI2LThmYWUtN2Q2ZjUxZGExMmFmIn0.83z5kzghi7R8FGumxsKoXAtM6RlrthDFceI3_ryRPSs";

        boolean state;

        state = confirmInviteUser.getOperateType().equals("accept");

        String body = "{\n" +
                "\t\"guid\":\"" + confirmInviteUser.getUserUUID() + "\",\n" +
                "\t\"state\":" + state + "\n" +
                "}";

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequestWithFullUrlAndToken(url, token, body);

        wrapper.operateCall(httpRequest,callback,new RemoteConfirmUserResultParser());

    }

}
