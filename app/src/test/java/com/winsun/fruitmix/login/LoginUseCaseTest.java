package com.winsun.fruitmix.login;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserRepository;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.token.TokenRemoteDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;

import org.junit.After;
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
    private LoggedInUserRepository loggedInUserRepository;

    @Mock
    private TokenRemoteDataSource tokenRemoteDataSource;

    @Mock
    private HttpRequestFactory httpRequestFactory;

    @Mock
    private UserDataRepository userDataRepository;

    @Mock
    private FileDownloadManager fileDownloadManager;

    @Mock
    private SystemSettingDataSource systemSettingDataSource;

    private LoginUseCase loginUseCase;

    @Captor
    private ArgumentCaptor<BaseLoadDataCallback<String>> loadTokenCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<BaseLoadDataCallback<User>> loadUserCallbackArgumentCaptor;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        LoginUseCase.destroyInstance();

        loginUseCase = LoginUseCase.getInstance(loggedInUserRepository, tokenRemoteDataSource,
                httpRequestFactory, userDataRepository, fileDownloadManager, systemSettingDataSource);

    }

    @Test
    public void loginWithNoParamFail() {

        boolean result = loginUseCase.loginWithNoParam();

        verify(loggedInUserRepository).getCurrentLoggedInUser();

        assertEquals(false, result);

        verify(fileDownloadManager, never()).clearFileDownloadItems();

    }

    @Test
    public void loginWithNoParamSucceed() {

        when(loggedInUserRepository.getCurrentLoggedInUser()).thenReturn(new LoggedInUser());

        loginUseCase.loginWithNoParam();

        verify(loggedInUserRepository).getCurrentLoggedInUser();

        verify(httpRequestFactory).setCurrentData(anyString(), anyString());

        verify(fileDownloadManager).clearFileDownloadItems();

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

        verify(fileDownloadManager).clearFileDownloadItems();

        verify(callback).onSucceed(ArgumentMatchers.<String>anyList(), any(OperationResult.class));

        verify(userDataRepository).getUsers(loadUserCallbackArgumentCaptor.capture());

    }

    @Test
    public void testLoadUserSuccessAfterLoginSuccess() {

        testLoginWithLoadTokenParam_succeed();

        User user = new User();
        user.setUuid(testUserUUID);

        loadUserCallbackArgumentCaptor.getValue().onSucceed(Collections.singletonList(user), new OperationSuccess());

        verify(loggedInUserRepository).getAllLoggedInUsers();


    }

    @Test
    public void testLoggedInUserEmptyWhenLoadUserSuccessAfterLoginSuccess() {

        when(loggedInUserRepository.getAllLoggedInUsers()).thenReturn(Collections.<LoggedInUser>emptyList());

        testLoadUserSuccessAfterLoginSuccess();

        verify(systemSettingDataSource).setAutoUploadOrNot(true);
        verify(systemSettingDataSource).setCurrentUploadDeviceID(anyString());

        verify(systemSettingDataSource).setShowAutoUploadDialog(true);

        verify(loggedInUserRepository).setCurrentLoggedInUser(any(LoggedInUser.class));

        verify(loggedInUserRepository).insertLoggedInUsers(ArgumentMatchers.<LoggedInUser>anyCollection());
    }

    @Test
    public void testLoggedInUserNotEmptyWhenLoadUserSuccessAfterLoginSuccess() {

        when(loggedInUserRepository.getAllLoggedInUsers()).thenReturn(Collections.singletonList(new LoggedInUser()));

        testLoadUserSuccessAfterLoginSuccess();

        verify(systemSettingDataSource).setShowAutoUploadDialog(false);

        verify(loggedInUserRepository).setCurrentLoggedInUser(any(LoggedInUser.class));

        verify(loggedInUserRepository).insertLoggedInUsers(ArgumentMatchers.<LoggedInUser>anyCollection());

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

        when(loggedInUserRepository.getAllLoggedInUsers()).thenReturn(Collections.singletonList(loggedInUser));

        boolean result = loginUseCase.loginWithUser(user);

        assertEquals(true, result);

    }

    private void testLoginWithLoggedInUser() {

        testLoginWithUser();

        verify(httpRequestFactory).setCurrentData(testToken, testGateway);

        verify(loggedInUserRepository).setCurrentLoggedInUser(any(LoggedInUser.class));

        verify(fileDownloadManager).clearFileDownloadItems();

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


}
