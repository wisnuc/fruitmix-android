package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/7.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class UserRemoteDataSourceTest {

    private UserRemoteDataSource userRemoteDataSource;

    @Mock
    private IHttpUtil iHttpUtil;

    @Mock
    private HttpRequestFactory httpRequestFactory;

    @Before
    public void init() {

        MockitoAnnotations.initMocks(this);

        userRemoteDataSource = new UserRemoteDataSourceImpl(iHttpUtil, httpRequestFactory);

    }

    @Test
    public void createUser_testResult() {

        String testUserName = "testUserName";
        String testUserPwd = "testUserPwd";

        try {

            HttpResponse httpResponse = new HttpResponse(404, "");

            when(httpRequestFactory.createHttpPostRequest(anyString(), anyString())).thenReturn(new HttpRequest("http://10.10.9.1/user", ""));

            when(iHttpUtil.remoteCall(any(HttpRequest.class))).thenReturn(httpResponse);

            BaseOperateDataCallback<User> callback = new BaseOperateDataCallback<User>() {
                @Override
                public void onSucceed(User data, OperationResult result) {
                    assertTrue(result instanceof OperationSuccess);

                    assertEquals("Alice", data.getUserName());
                    assertEquals("5da92303-33a1-4f79-8d8f-a7b6becde6c3", data.getUuid());
                }

                @Override
                public void onFail(OperationResult result) {
                    assertTrue(result instanceof OperationNetworkException);
                }
            };

            userRemoteDataSource.insertUser(testUserName, testUserPwd, callback);

            httpResponse.setResponseCode(200);
            httpResponse.setResponseData("  {\n" +
                    "    \"uuid\": \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\",\n" +
                    "    \"username\": \"Alice\",\n" +
                    "    \"avatar\": null,\n" +
                    "    \"email\": null,\n" +
                    "    \"isFirstUser\": true,\n" +
                    "    \"isAdmin\": true,\n" +
                    "    \"home\": \"b9aa7c34-8b86-4306-9042-396cf8fa1a9c\",\n" +
                    "    \"library\": \"f97f9e1f-848b-4ed4-bd47-1ddfa82b2777\"\n" +
                    "  },\n");

            userRemoteDataSource.insertUser(testUserName, testUserPwd, callback);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void getUserTest_firstCallFail() {

        BaseLoadDataCallback<User> callback = new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                assertTrue("get user always call onSucceed", data.isEmpty());

            }

            @Override
            public void onFail(OperationResult operationResult) {

            }
        };

        HttpResponse httpResponse = new HttpResponse(404, "");

        try {

            prepareHttpRequest();

            when(iHttpUtil.remoteCall(any(HttpRequest.class))).thenReturn(httpResponse);

            userRemoteDataSource.getUsers("", callback);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void prepareHttpRequest() {
        HttpRequest httpRequest = new HttpRequest("http://10.10.9.1/user", "");

        when(httpRequestFactory.createHttpGetRequest(anyString())).thenReturn(httpRequest);
        when(httpRequestFactory.createGetRequestByPathWithoutToken(anyString())).thenReturn(httpRequest);
    }


    @Test
    public void getUserTest_firstCallSucceed_SecondCallFail() {

        HttpResponse httpResponse = new HttpResponse(200, "{\n" +
                "    \"type\": \"local\",\n" +
                "    \"uuid\": \"ed51ee08-9bca-4e28-88b4-d39f47bbb933\",\n" +
                "    \"username\": \"admin\",\n" +
                "    \"nologin\": false,\n" +
                "    \"isFirstUser\": true,\n" +
                "    \"isAdmin\": true,\n" +
                "    \"email\": null,\n" +
                "    \"avatar\": null,\n" +
                "    \"home\": \"a9d6462c-6f19-4fc2-8c07-36c0e94d150c\",\n" +
                "    \"library\": \"87f7a449-cad8-4603-a975-e666ad72db5d\",\n" +
                "    \"service\": \"73d32b1a-40f5-421d-9776-45a2f4177a40\",\n" +
                "    \"unixuid\": 2000,\n" +
                "    \"lastChangeTime\": 1494235322044,\n" +
                "    \"friends\": [],\n" +
                "    \"unixPassword\": \"$6$0EtR39dZ$VcG9j5HWGy8YNzjg8Ge9GSXqMBMRxtDkiBv70vIJp2I2VcqoNZ7LJpURPG1vxcVXjF0a2AjzwgWDrpofNzXHE.\",\n" +
                "    \"smbPassword\": \"69943C5E63B4D2C104DBBCC15138B72B\",\n" +
                "    \"password\": \"$2a$10$1Luj4PA.Ta4nhVjPU..TOOii1m4PGyOvIzivoiKtygaTZg5AB46oW\"\n" +
                "}");


        try {

            prepareHttpRequest();

            when(iHttpUtil.remoteCall(any(HttpRequest.class))).thenReturn(httpResponse).thenReturn(new HttpResponse(404, ""));

            userRemoteDataSource.getUsers("", new BaseLoadDataCallbackImpl<User>());

            verify(iHttpUtil, times(2)).remoteCall(any(HttpRequest.class));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void getUser_FirstCallSucceed_SecondCallSucceed() {

        BaseLoadDataCallback<User> callback = new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                assertEquals(3, data.size());
                assertEquals("ed51ee08-9bca-4e28-88b4-d39f47bbb933", data.get(0).getUuid());
                assertEquals("511eecb5-0362-41a2-ac79-624ac5e9c03f", data.get(1).getUuid());
                assertEquals("3f2d3d52-d096-4e51-b414-c74f7acb474f", data.get(2).getUuid());
            }

            @Override
            public void onFail(OperationResult operationResult) {

            }
        };

        HttpResponse firstResponse = new HttpResponse(200, "{\n" +
                "    \"type\": \"local\",\n" +
                "    \"uuid\": \"ed51ee08-9bca-4e28-88b4-d39f47bbb933\",\n" +
                "    \"username\": \"admin\",\n" +
                "    \"nologin\": false,\n" +
                "    \"isFirstUser\": true,\n" +
                "    \"isAdmin\": true,\n" +
                "    \"email\": null,\n" +
                "    \"avatar\": null,\n" +
                "    \"home\": \"a9d6462c-6f19-4fc2-8c07-36c0e94d150c\",\n" +
                "    \"library\": \"87f7a449-cad8-4603-a975-e666ad72db5d\",\n" +
                "    \"service\": \"73d32b1a-40f5-421d-9776-45a2f4177a40\",\n" +
                "    \"unixuid\": 2000,\n" +
                "    \"lastChangeTime\": 1494235322044,\n" +
                "    \"friends\": [],\n" +
                "    \"unixPassword\": \"$6$0EtR39dZ$VcG9j5HWGy8YNzjg8Ge9GSXqMBMRxtDkiBv70vIJp2I2VcqoNZ7LJpURPG1vxcVXjF0a2AjzwgWDrpofNzXHE.\",\n" +
                "    \"smbPassword\": \"69943C5E63B4D2C104DBBCC15138B72B\",\n" +
                "    \"password\": \"$2a$10$1Luj4PA.Ta4nhVjPU..TOOii1m4PGyOvIzivoiKtygaTZg5AB46oW\"\n" +
                "}");

        HttpResponse secondResponse = new HttpResponse(200, "[\n" +
                "    {\n" +
                "        \"uuid\": \"511eecb5-0362-41a2-ac79-624ac5e9c03f\",\n" +
                "        \"username\": \"w\",\n" +
                "        \"avatar\": null,\n" +
                "        \"unixUID\": 2000\n" +
                "    },\n" +
                "    {\n" +
                "        \"uuid\": \"3f2d3d52-d096-4e51-b414-c74f7acb474f\",\n" +
                "        \"username\": \"2\",\n" +
                "        \"avatar\": null,\n" +
                "        \"unixUID\": 2001\n" +
                "    }\n" +
                "]");

        try {

            prepareHttpRequest();

            when(iHttpUtil.remoteCall(any(HttpRequest.class))).thenReturn(firstResponse).thenReturn(secondResponse);

            userRemoteDataSource.getUsers("", callback);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
