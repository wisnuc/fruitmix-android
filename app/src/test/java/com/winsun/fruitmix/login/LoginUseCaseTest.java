package com.winsun.fruitmix.login;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.mock.MockThreadManager;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.token.TokenDataSource;
import com.winsun.fruitmix.upload.media.CheckMediaIsUploadStrategy;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/14.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class LoginUseCaseTest {

    @Mock
    private LoggedInUserDataSource loggedInUserDataSource;

    @Mock
    private TokenDataSource tokenDataSource;

    @Mock
    private HttpRequestFactory httpRequestFactory;

    @Mock
    private CheckMediaIsUploadStrategy checkMediaIsUploadStrategy;

    @Mock
    private UploadMediaUseCase uploadMediaUseCase;

    @Mock
    private UserDataRepository userDataRepository;

    @Mock
    private MediaDataSourceRepository mediaDataSourceRepository;

    @Mock
    private StationFileRepository stationFileRepository;

    @Mock
    private SystemSettingDataSource systemSettingDataSource;

    private LoginUseCase loginUseCase;

    @Mock
    private ImageGifLoaderInstance imageGifLoaderInstance;

    @Mock
    private EventBus eventBus;

    @Captor
    private ArgumentCaptor<BaseLoadDataCallback<String>> loadTokenCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<BaseLoadDataCallback<User>> loadUserCallbackArgumentCaptor;

    @Mock
    private BaseLoadDataCallback<String> callback;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        LoginUseCase.destroyInstance();

        loginUseCase = LoginUseCase.getInstance(loggedInUserDataSource, tokenDataSource,
                httpRequestFactory, checkMediaIsUploadStrategy, uploadMediaUseCase, userDataRepository, mediaDataSourceRepository,
                stationFileRepository, systemSettingDataSource, imageGifLoaderInstance, eventBus, new MockThreadManager());

    }

    @Test
    public void loginWithNoParamFail() {

        when(systemSettingDataSource.getCurrentLoginUserUUID()).thenReturn("");

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

        verify(systemSettingDataSource).getCurrentLoginUserUUID();

        verify(loggedInUserDataSource).getLoggedInUserByUserUUID(anyString());

        verify(stationFileRepository, never()).clearDownloadFileRecordInCache();

    }

    @Test
    public void loginWithNoParamSucceed() {

        when(systemSettingDataSource.getCurrentLoginUserUUID()).thenReturn(testUserUUID);

        User user = new User();
        user.setUuid(testUserUUID);

        when(loggedInUserDataSource.getLoggedInUserByUserUUID(testUserUUID)).thenReturn(new LoggedInUser(testDeviceID, testToken, testGateway, testEquipmentName, user));

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

        verify(systemSettingDataSource).getCurrentLoginUserUUID();

        verify(loggedInUserDataSource).getLoggedInUserByUserUUID(anyString());

        initSystemStateAndVerify();

        verify(userDataRepository, never()).clearAllUsersInDB();

        verify(mediaDataSourceRepository, never()).clearAllStationMediasInDB();

    }

    private void initSystemStateAndVerify() {
        verify(httpRequestFactory).setCurrentData(anyString(), anyString());

        verify(checkMediaIsUploadStrategy).setCurrentUserUUID(testUserUUID);

        verify(checkMediaIsUploadStrategy).setUploadedMediaHashs(Collections.<String>emptyList());

        verify(uploadMediaUseCase).resetState();

        verify(imageGifLoaderInstance).setToken(anyString());

        verify(systemSettingDataSource).setCurrentLoginUserUUID(anyString());

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

        loginUseCase.loginWithLoadTokenParam(loadTokenParam, callback);

        verify(tokenDataSource).getToken(any(LoadTokenParam.class), loadTokenCallbackArgumentCaptor.capture());

        loadTokenCallbackArgumentCaptor.getValue().onSucceed(Collections.singletonList(testToken), new OperationSuccess());

        initSystemStateAndVerify();

        verify(userDataRepository).clearAllUsersInDB();

        verify(mediaDataSourceRepository).clearAllStationMediasInDB();

        verify(userDataRepository).getUsers(loadUserCallbackArgumentCaptor.capture());

    }

    @Test
    public void testLoadUserSuccessAfterLoginSuccess() {

        testLoginWithLoadTokenParam_succeed();

        User user = new User();
        user.setUuid(testUserUUID);

        String testUserHome = "testUserHome";

        ArgumentCaptor<BaseLoadDataCallback<String>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        loadUserCallbackArgumentCaptor.getValue().onSucceed(Collections.singletonList(user), new OperationSuccess());

        verify(userDataRepository).getCurrentUserHome(captor.capture());

        captor.getValue().onSucceed(Collections.singletonList(testUserHome), new OperationSuccess());

        assertEquals(testUserHome, user.getHome());

        verify(loggedInUserDataSource).getAllLoggedInUsers();

        verify(loggedInUserDataSource).insertLoggedInUsers(ArgumentMatchers.<LoggedInUser>anyCollection());

        verify(callback).onSucceed(ArgumentMatchers.<String>anyList(), any(OperationResult.class));

        verify(eventBus).postSticky(ArgumentMatchers.any(OperationEvent.class));

    }

    @Test
    public void testLoggedInUserEmptyWhenLoadUserSuccessAfterLoginSuccess() {

        when(loggedInUserDataSource.getAllLoggedInUsers()).thenReturn(Collections.<LoggedInUser>emptyList());

        testLoadUserSuccessAfterLoginSuccess();

        verify(systemSettingDataSource).setAutoUploadOrNot(true);
        verify(systemSettingDataSource).setCurrentUploadUserUUID(testUserUUID);

        verify(systemSettingDataSource).setShowAutoUploadDialog(false);

    }

    @Test
    public void testLoggedInUserNotEmptyWhenLoadUserSuccessAfterLoginSuccess() {

        when(loggedInUserDataSource.getAllLoggedInUsers()).thenReturn(Collections.singletonList(new LoggedInUser()));

        testLoadUserSuccessAfterLoginSuccess();

        verify(systemSettingDataSource).setAutoUploadOrNot(false);
        verify(systemSettingDataSource).setShowAutoUploadDialog(true);

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

        verify(tokenDataSource).getToken(any(LoadTokenParam.class), loadTokenCallbackArgumentCaptor.capture());

        loadTokenCallbackArgumentCaptor.getValue().onFail(new OperationIOException());

        verify(callback).onFail(any(OperationResult.class));

    }


    private void testLoginWithUser() {

        User user = new User();
        user.setUuid(testUserUUID);

        LoggedInUser loggedInUser = new LoggedInUser(testDeviceID, testToken, testGateway, "", user);

        when(loggedInUserDataSource.getAllLoggedInUsers()).thenReturn(Collections.singletonList(loggedInUser));

        loginUseCase.loginWithUser(user, new BaseOperateDataCallbackImpl<Boolean>());

    }

    private void testLoginWithLoggedInUser() {

        testLoginWithUser();

        initSystemStateAndVerify();

        verify(userDataRepository).clearAllUsersInDB();

        verify(mediaDataSourceRepository).clearAllStationMediasInDB();

        verify(userDataRepository).getUsers(any(BaseLoadDataCallback.class));

        verify(systemSettingDataSource).getCurrentUploadUserUUID();

    }

    @Test
    public void testCurrentUploadDeviceIDNotEqualsNewUserDeviceID() {

        when(systemSettingDataSource.getCurrentUploadUserUUID()).thenReturn("");

        testLoginWithLoggedInUser();

        verify(systemSettingDataSource).setAutoUploadOrNot(false);

    }

    @Test
    public void testCurrentUploadDeviceIDEqualsNewUserDeviceID() {

        when(systemSettingDataSource.getCurrentUploadUserUUID()).thenReturn(testUserUUID);

        testLoginWithLoggedInUser();

        verify(systemSettingDataSource, never()).setAutoUploadOrNot(anyBoolean());

    }

    @Test
    public void testLoginWithWeChatCode_succeed() {

        BaseOperateDataCallback<Boolean> callback = Mockito.mock(BaseOperateDataCallback.class);

        loginUseCase.loginWithWeChatCode("", callback);

        verify(tokenDataSource).getToken(anyString(), loadTokenCallbackArgumentCaptor.capture());

        loadTokenCallbackArgumentCaptor.getValue().onSucceed(Collections.singletonList(testToken), new OperationSuccess());

        verify(httpRequestFactory).setCurrentData(anyString(), anyString());

        verify(imageGifLoaderInstance).setToken(anyString());

        verify(stationFileRepository).clearDownloadFileRecordInCache();

        verify(callback).onSucceed(anyBoolean(), any(OperationResult.class));

    }


}
