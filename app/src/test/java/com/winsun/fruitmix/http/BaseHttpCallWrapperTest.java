package com.winsun.fruitmix.http;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteDatasParser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Created by Administrator on 2017/7/13.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class BaseHttpCallWrapperTest {

    private BaseHttpCallWrapper baseHttpCallWrapper;

    @Mock
    private IHttpUtil iHttpUtil;

    @Mock
    BaseOperateDataCallback operateDataCallback;

    @Mock
    BaseLoadDataCallback loadDataCallback;

    @Mock
    RemoteDataParser remoteDataParser;

    @Mock
    RemoteDatasParser remoteDatasParser;

    @Before
    public void init() {

        MockitoAnnotations.initMocks(this);

        baseHttpCallWrapper = new BaseHttpCallWrapper(iHttpUtil);
    }

    @Test
    public void operateCall_testCallRemoteCall() {

        try {
            when(iHttpUtil.remoteCall(any(HttpRequest.class))).thenReturn(new HttpResponse(404, ""));

            HttpRequest httpRequest = new HttpRequest("", "");

            baseHttpCallWrapper.operateCall(httpRequest, operateDataCallback, remoteDataParser);

            verify(iHttpUtil, never()).remoteCall(any(HttpRequest.class));
            verify(operateDataCallback).onFail(any(OperationResult.class));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void loadCall_testCallRemoteCall() {

        try {
            when(iHttpUtil.remoteCall(any(HttpRequest.class))).thenReturn(new HttpResponse(404, ""));

            HttpRequest httpRequest = new HttpRequest("", "");

            baseHttpCallWrapper.loadCall(httpRequest, loadDataCallback, remoteDatasParser);

            verify(loadDataCallback).onFail(any(OperationResult.class));

            verify(iHttpUtil, never()).remoteCall(any(HttpRequest.class));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
