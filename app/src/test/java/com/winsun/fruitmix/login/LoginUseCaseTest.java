package com.winsun.fruitmix.login;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.token.TokenRemoteDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/14.
 */

public class LoginUseCaseTest {

    @Mock
    private LoggedInUserDataSource loggedInUserDataSource;

    @Mock
    private TokenRemoteDataSource tokenRemoteDataSource;

    @Mock
    private HttpRequestFactory httpRequestFactory;

    @Mock
    private UserDataRepository userDataRepository;

    @Mock
    private StationFileRepository stationFileRepository;

    @Mock
    private SystemSettingDataSource systemSettingDataSource;

    private LoginUseCase loginUseCase;

    @Mock
    private ImageGifLoaderInstance imageGifLoaderInstance;

    @Mock
    private EventBus eventBus;

    @Mock
    private ThreadManager threadManager;

    @Captor
    private ArgumentCaptor<BaseLoadDataCallback<String>> loadTokenCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<BaseLoadDataCallback<User>> loadUserCallbackArgumentCaptor;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        LoginUseCase.destroyInstance();

        loginUseCase = LoginUseCase.getInstance(loggedInUserDataSource, tokenRemoteDataSource,
                httpRequestFactory, userDataRepository, stationFileRepository, systemSettingDataSource, imageGifLoaderInstance, eventBus, threadManager);

    }

    @Test
    public void loginWithNoParamFail() {

        loginUseCase.loginWithNoParam(new BaseOperateDataCallback<Boolean>() {

            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                toastFail();
            }

            @Override
            public void onFail(OperationResult result) {

                assertFalse(result instanceof OperationSuccess);
            }
        });

        verify(loggedInUserDataSource).getCurrentLoggedInUser();

        verify(stationFileRepository, never()).clearDownloadFileRecordInCache();

    }

    @Test
    public void loginWithNoParamSucceed() {

        when(loggedInUserDataSource.getCurrentLoggedInUser()).thenReturn(new LoggedInUser());

        loginUseCase.loginWithNoParam(new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                assertTrue(data);
            }

            @Override
            public void onFail(OperationResult result) {
                toastFail();
            }
        });

        verify(loggedInUserDataSource).getCurrentLoggedInUser();

        verify(httpRequestFactory).setCurrentData(anyString(), anyString());

        verify(imageGifLoaderInstance).setToken(anyString());

        verify(stationFileRepository).clearDownloadFileRecordInCache();

    }

    private void toastFail() {
        fail("should not enter here");
    }

    private String testGateway = "testGateway";
    private String testUserUUID = "testUserUUID";
    private String testUserPwd = "testUserPwd";
    private String testEquipmentName = "testEquipmentName";
    private String testToken = "testToken";
    private String testDeviceID = "testDeviceID";

    @Test
    public void testLoginWithLoadTokenParam_succeed() {

        LoadTokenParam loadTokenParam = new LoadTokenParam(testGateway, testUserUUID, testUserPwd, testEquipmentName);

        BaseLoadDataCallback<String> callback = Mockito.mock(BaseLoadDataCallback.class);

        loginUseCase.loginWithLoadTokenParam(loadTokenParam, callback);

        verify(tokenRemoteDataSource).getToken(any(LoadTokenParam.class), loadTokenCallbackArgumentCaptor.capture());

        loadTokenCallbackArgumentCaptor.getValue().onSucceed(Collections.singletonList(testToken), new OperationSuccess());

        verify(httpRequestFactory).setCurrentData(anyString(), anyString());

        verify(imageGifLoaderInstance).setToken(anyString());

        verify(stationFileRepository).clearDownloadFileRecordInCache();

        verify(callback).onSucceed(ArgumentMatchers.<String>anyList(), any(OperationResult.class));

        verify(userDataRepository).getUsers(loadUserCallbackArgumentCaptor.capture());

    }

    @Test
    public void testLoadUserSuccessAfterLoginSuccess() {

        testLoginWithLoadTokenParam_succeed();

        User user = new User();
        user.setUuid(testUserUUID);

        loadUserCallbackArgumentCaptor.getValue().onSucceed(Collections.singletonList(user), new OperationSuccess());

        verify(loggedInUserDataSource).getAllLoggedInUsers();

        verify(loggedInUserDataSource).setCurrentLoggedInUser(any(LoggedInUser.class));

        verify(loggedInUserDataSource).insertLoggedInUsers(ArgumentMatchers.<LoggedInUser>anyCollection());

        verify(eventBus).postSticky(ArgumentMatchers.any(OperationEvent.class));


    }

    @Test
    public void testLoggedInUserEmptyWhenLoadUserSuccessAfterLoginSuccess() {

        when(loggedInUserDataSource.getAllLoggedInUsers()).thenReturn(Collections.<LoggedInUser>emptyList());

        testLoadUserSuccessAfterLoginSuccess();

        verify(systemSettingDataSource).setAutoUploadOrNot(true);
        verify(systemSettingDataSource).setCurrentUploadDeviceID(anyString());

        verify(systemSettingDataSource).setShowAutoUploadDialog(true);

    }

    @Test
    public void testLoggedInUserNotEmptyWhenLoadUserSuccessAfterLoginSuccess() {

        when(loggedInUserDataSource.getAllLoggedInUsers()).thenReturn(Collections.singletonList(new LoggedInUser()));

        testLoadUserSuccessAfterLoginSuccess();

        verify(systemSettingDataSource).setShowAutoUploadDialog(false);

    }

    @Ignore
    public void testLoadUserFailAfterLoginSuccess() {

        testLoginWithLoadTokenParam_succeed();

        loadUserCallbackArgumentCaptor.getValue().onFail(new OperationIOException());

    }


    @Test
    public void testLoginWithLoadTokenParam_fail() {

        LoadTokenParam loadTokenParam = new LoadTokenParam(testGateway, testUserUUID, testUserPwd, testEquipmentName);

        BaseLoadDataCallback<String> callback = Mockito.mock(BaseLoadDataCallback.class);

        loginUseCase.loginWithLoadTokenParam(loadTokenParam, callback);

        verify(tokenRemoteDataSource).getToken(any(LoadTokenParam.class), loadTokenCallbackArgumentCaptor.capture());

        loadTokenCallbackArgumentCaptor.getValue().onFail(new OperationIOException());

        verify(callback).onFail(any(OperationResult.class));

    }


    private void testLoginWithUser() {

        User user = new User();
        user.setUuid(testUserUUID);

        LoggedInUser loggedInUser = new LoggedInUser(testDeviceID, testToken, testGateway, "", user);

        when(loggedInUserDataSource.getAllLoggedInUsers()).thenReturn(Collections.singletonList(loggedInUser));

        loginUseCase.loginWithUser(user, new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {
                assertTrue(data);
            }

            @Override
            public void onFail(OperationResult result) {
                toastFail();
            }
        });

    }

    private void testLoginWithLoggedInUser() {

        testLoginWithUser();

        verify(httpRequestFactory).setCurrentData(testToken, testGateway);

        verify(imageGifLoaderInstance).setToken(anyString());

        verify(loggedInUserDataSource).setCurrentLoggedInUser(any(LoggedInUser.class));

        verify(stationFileRepository).clearDownloadFileRecordInCache();

        verify(userDataRepository).getUsers(any(BaseLoadDataCallback.class));

        verify(systemSettingDataSource).getCurrentUploadDeviceID();

    }

    @Test
    public void testCurrentUploadDeviceIDNotEqualsNewUserDeviceID() {

        when(systemSettingDataSource.getCurrentUploadDeviceID()).thenReturn("");

        testLoginWithLoggedInUser();

        verify(systemSettingDataSource).setAutoUploadOrNot(false);

    }

    @Test
    public void testCurrentUploadDeviceIDEqualsNewUserDeviceID() {

        when(systemSettingDataSource.getCurrentUploadDeviceID()).thenReturn(testDeviceID);

        testLoginWithLoggedInUser();

        verify(systemSettingDataSource).setAutoUploadOrNot(true);

    }

    @Test
    public void testLoginWithWeChatCode_succeed() {

        BaseOperateDataCallback<Boolean> callback = Mockito.mock(BaseOperateDataCallback.class);

        loginUseCase.loginWithWeChatCode("", callback);

        verify(tokenRemoteDataSource).getToken(anyString(), loadTokenCallbackArgumentCaptor.capture());

        loadTokenCallbackArgumentCaptor.getValue().onSucceed(Collections.singletonList(testToken), new OperationSuccess());

        verify(httpRequestFactory).setCurrentData(anyString(), anyString());

        verify(imageGifLoaderInstance).setToken(anyString());

        verify(stationFileRepository).clearDownloadFileRecordInCache();

        verify(callback).onSucceed(anyBoolean(), any(OperationResult.class));


    }


}
