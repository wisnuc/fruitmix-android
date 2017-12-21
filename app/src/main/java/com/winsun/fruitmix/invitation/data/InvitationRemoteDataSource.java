package com.winsun.fruitmix.invitation.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.invitation.ConfirmInviteUser;
import com.winsun.fruitmix.parser.RemoteConfirmInviteUsersParser;
import com.winsun.fruitmix.parser.RemoteConfirmTicketResultParser;
import com.winsun.fruitmix.parser.RemoteTicketParser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/7/12.
 */

public class InvitationRemoteDataSource extends BaseRemoteDataSourceImpl implements InvitationDataSource {

    public static final String TAG = InvitationRemoteDataSource.class.getSimpleName();

    public static final String TICKETS_PARAMETER = "/station/tickets";

    public static final String CONFIRM_TICKET_PARAMETER = "/station/tickets/wechat/";

    public InvitationRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    /*
     * WISNUC API:POST TICKET LIST
     */
    @Override
    public void createInvitation(final BaseOperateDataCallback<String> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(TICKETS_PARAMETER, "{\"type\":\"invite\"}");

        wrapper.operateCall(httpRequest, callback, new RemoteTicketParser());

    }

    /*
     * WISNUC API:GET TICKET LIST
     */
    @Override
    public void getInvitation(final BaseLoadDataCallback<ConfirmInviteUser> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(TICKETS_PARAMETER);

        wrapper.loadCall(httpRequest, callback, new RemoteConfirmInviteUsersParser());

    }

    /*
     * WISNUC API:POST TICKET
     */
    @Override
    public void confirmInvitation(final ConfirmInviteUser confirmInviteUser, final BaseOperateDataCallback<String> callback) {

        String path = CONFIRM_TICKET_PARAMETER + confirmInviteUser.getTicketUUID();

        boolean state;

        state = confirmInviteUser.getOperateType() == ConfirmInviteUser.OPERATE_TYPE_ACCEPT;

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("guid",confirmInviteUser.getUserGUID());
            jsonObject.put("state",state);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(path, jsonObject.toString());

        wrapper.operateCall(httpRequest, callback, new RemoteConfirmTicketResultParser());

    }

}
