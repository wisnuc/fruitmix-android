package com.winsun.fruitmix.person.info;

import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.request.factory.CloudHttpRequestFactory;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.parser.RemoteConfirmTicketResultParser;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteFillBindWeChatUserTicketParser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/10/19.
 */

public class PersonInfoRemoteDataSource extends BaseRemoteDataSourceImpl implements PersonInfoDataSource {

    public static final String TAG = PersonInfoRemoteDataSource.class.getSimpleName();

    public static final String TICKETS_PARAMETER = "/station/tickets";

    public static final String CONFIRM_TICKET_PARAMETER = "/station/tickets/wechat/";

    public PersonInfoRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    @Override
    public void createBindWeChatUserTicket(BaseOperateDataCallback<String> callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "bind");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(TICKETS_PARAMETER, jsonObject.toString());

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<String>() {
            @Override
            public String parse(String json) throws JSONException {

                JSONObject root = new JSONObject(json);

                return root.optString("id");
            }
        });

    }

    @Override
    public void fillBindWeChatUserTicket(String ticketID, String wechatUserToken, BaseOperateDataCallback<String> callback) {

        String httpPath = CloudHttpRequestFactory.CLOUD_API_LEVEL + "/tickets/" + ticketID + "/users";

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequestByCloudAPIWithoutWrap(httpPath, "", wechatUserToken);

        wrapper.operateCall(httpRequest, callback, new RemoteFillBindWeChatUserTicketParser());

    }

    @Override
    public void confirmBindWeChatUserTicket(String ticketID, String guid, boolean isAccept, BaseOperateDataCallback<String> callback) {

        String httpPath = CONFIRM_TICKET_PARAMETER + ticketID;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("guid", guid);
            jsonObject.put("state", isAccept);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(httpPath, jsonObject.toString());

        wrapper.operateCall(httpRequest, callback, new RemoteConfirmTicketResultParser());

    }
}
