package com.winsun.fruitmix.invivation;

import android.util.Log;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.invitation.Invitation;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2017/7/12.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class InvitationTest {

    private Invitation invitation;

    @Mock
    private IHttpUtil iHttpUtil;

    @Mock
    private IWXAPI iwxapi;

    @Before
    public void init() {

        MockitoAnnotations.initMocks(this);

        invitation = new Invitation(iHttpUtil,iwxapi);
    }

    @Test
    public void invitation_CreateTickets() {

        try {
            when(iHttpUtil.remoteCall(any(HttpRequest.class))).thenReturn(new HttpResponse(200, "[\n" +
                    "  {\n" +
                    "    \"uuid\": \"ticket id\",\n" +
                    "    \"url\": \"url\"\n" +
                    "  }\n" +
                    "]"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BaseOperateDataCallback<String> callback = new BaseOperateDataCallback<String>() {
            @Override
            public void onSucceed(String data, OperationResult result) {

                assertEquals("ticket id", data);
            }

            @Override
            public void onFail(OperationResult result) {

            }
        };

        invitation.createTicket(callback);

    }


}
