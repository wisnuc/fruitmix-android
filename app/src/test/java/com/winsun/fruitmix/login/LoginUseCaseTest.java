package com.winsun.fruitmix.login;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.http.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.NewPhotoListDataLoader;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.mock.MockThreadManager;
import com.winsun.fruitmix.model.operationResult.OperationFail;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.stations.Station;
import com.winsun.fruitmix.stations.StationsDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.token.TokenDataSource;
import com.winsun.fruitmix.token.WeChatTokenUserWrapper;
import com.winsun.fruitmix.upload.media.CheckMediaIsUploadStrategy;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.wechat.user.WeChatUser;
import com.winsun.fruitmix.wechat.user.WeChatUserDataSource;

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

    @Mock
    private NewPhotoListDataLoader newPhotoListDataLoader;

    @Captor
    private ArgumentCaptor<BaseLoadDataCallback<String>> loadTokenCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<BaseLoadDataCallback<User>> loadUserCallbackArgumentCaptor;

    @Mock
    private BaseLoadDataCallback<String> callback;

    @Mock
    private StationsDataSource stationsDataSource;

    @Mock
    private WeChatUserDataSource weChatUserDataSource;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        LoginUseCase.destroyInstance();

        loginUseCase = LoginUseCase.getInstance(loggedInUserDataSource, tokenDataSource,
                httpRequestFactory, checkMediaIsUploadStrategy, uploadMediaUseCase, userDataRepository, mediaDataSourceRepository,
                stationFileRepository, systemSettingDataSource, imageGifLoaderInstance, eventBus, new MockThreadManager(),
                newPhotoListDataLoader, stationsDataSource, weChatUserDataSource);

    }

    @Test
    public void loginWithNoParamFail() {

        when(systemSettingDataSource.getCurrentLoginToken()).thenReturn("");

        when(systemSettingDataSource.getCurrentLoginStationID()).thenReturn("");

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

        verify(systemSettingDataSource).getCurrentLoginToken();

        verify(systemSettingDataSource).getCurrentLoginStationID();

        verify(weChatUserDataSource).getWeChatUser(eq(""), eq(""));

        verify(loggedInUserDataSource).getLoggedInUserByToken(eq(""));

        verify(stationFileRepository, never()).clearDownloadFileRecordInCache();

    }

    @Test
    public void loginWithNoParamSucceedByWeChatUserToken() {

        when(systemSettingDataSource.getCurrentLoginToken()).thenReturn(testToken);

        when(systemSettingDataSource.getCurrentLoginStationID()).thenReturn(testStationID);

        when(loggedInUserDataSource.getLoggedInUserByToken(testToken)).thenReturn(null);

        WeChatUser weChatUser = new WeChatUser(testToken, testGUID, testStationID);

        when(weChatUserDataSource.getWeChatUser(testToken, testStationID)).thenReturn(weChatUser);

        loginUseCase.loginWithNoParam(new BaseOperateDataCallbackImpl<Boolean>());

        verify(systemSettingDataSource).getCurrentLoginToken();

        verify(loggedInUserDataSource).getLoggedInUserByToken(eq(testToken));

        verify(systemSettingDataSource).getCurrentLoginStationID();

        verify(weChatUserDataSource).getWeChatUser(eq(testToken), eq(testStationID));

        ArgumentCaptor<BaseLoadDataCallback<User>> getUserByGUIDCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(userDataRepository).getUserByGUIDWithCloudAPI(eq(testGUID), getUserByGUIDCaptor.capture());

        User user = new User();
        user.setUserName(testUserName);
        user.setAvatar(testUserAvatarUrl);

        getUserByGUIDCaptor.getValue().onSucceed(Collections.singletonList(user), new OperationSuccess());

        testAfterChooseStationID();

        initSystemStateAndVerify();

        verify(systemSettingDataSource).setCurrentLoginUserGUID(testGUID);
    }


    @Test
    public void loginWithNoParamSucceedByLocalUserToken() {

        when(systemSettingDataSource.getCurrentLoginToken()).thenReturn(testToken);

        User user = new User();
        user.setUuid(testUserUUID);

        when(loggedInUserDataSource.getLoggedInUserByToken(testToken)).thenReturn(new LoggedInUser(testDeviceID, testToken, testGateway, testEquipmentName, user));

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

        verify(systemSettingDataSource).getCurrentLoginToken();

        verify(loggedInUserDataSource).getLoggedInUserByToken(anyString());

        verify(httpRequestFactory).setCurrentData(anyString(), anyString());

        initSystemStateAndVerify();

        verify(systemSettingDataSource).setLoginWithWechatCodeOrNot(false);

        verify(userDataRepository, never()).clearAllUsersInDB();

        verify(mediaDataSourceRepository, never()).clearAllStationMediasInDB();

    }

    private void initSystemStateAndVerify() {

        verify(checkMediaIsUploadStrategy).setCurrentUserUUID(testUserUUID);

        verify(checkMediaIsUploadStrategy).setUploadedMediaHashs(Collections.<String>emptyList());

        verify(uploadMediaUseCase).resetState();

        verify(imageGifLoaderInstance).setToken(anyString());

        verify(systemSettingDataSource).setCurrentLoginUserUUID(anyString());

        verify(systemSettingDataSource).setCurrentLoginToken(anyString());

        verify(systemSettingDataSource).setCurrentEquipmentIp(anyString());

        verify(systemSettingDataSource).setCurrentLoginUserGUID(eq(""));

        verify(systemSettingDataSource).setCurrentLoginStationID(eq(""));

        verify(stationFileRepository).clearDownloadFileRecordInCache();

        verify(newPhotoListDataLoader).setNeedRefreshData(eq(true));

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
    private String testGUID = "testGUID";
    private String testUserHome = "testUserHome";

    private String testWeChatName = "testWeChatName";
    private String testWeChatAvatarUrl = "testWeChatAvatarUrl";

    private String testStationID = "testStationID";

    private String testUserName = "testUserName";
    private String testUserAvatarUrl = "testUserAvatarUrl";

    @Test
    public void testLoginWithLoadTokenParam_succeed() {

        LoadTokenParam loadTokenParam = new LoadTokenParam(testGateway, testUserUUID, testUserPwd, testEquipmentName);

        loginUseCase.loginWithLoadTokenParam(loadTokenParam, callback);

        verify(tokenDataSource).getToken(any(LoadTokenParam.class), loadTokenCallbackArgumentCaptor.capture());

        loadTokenCallbackArgumentCaptor.getValue().onSucceed(Collections.singletonList(testToken), new OperationSuccess());

        verify(httpRequestFactory).setCurrentData(anyString(), anyString());

        verify(userDataRepository).clearAllUsersInDB();

        verify(systemSettingDataSource).setLoginWithWechatCodeOrNot(false);

        verify(userDataRepository).getUsers(eq(testUserUUID), loadUserCallbackArgumentCaptor.capture());

    }

    @Test
    public void testLoadUserSuccessAfterLoginSuccess() {

        testLoginWithLoadTokenParam_succeed();

        verify(systemSettingDataSource).setLoginWithWechatCodeOrNot(false);

        User user = new User();
        user.setUuid(testUserUUID);

        String testUserHome = "testUserHome";

        ArgumentCaptor<BaseLoadDataCallback<String>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        loadUserCallbackArgumentCaptor.getValue().onSucceed(Collections.singletonList(user), new OperationSuccess());

        verify(userDataRepository).getCurrentUserHome(captor.capture());

        captor.getValue().onSucceed(Collections.singletonList(testUserHome), new OperationSuccess());

        assertEquals(testUserHome, user.getHome());

        verify(loggedInUserDataSource).insertLoggedInUsers(ArgumentMatchers.<LoggedInUser>anyCollection());

        initSystemStateAndVerify();

        verify(mediaDataSourceRepository).clearAllStationMediasInDB();

        verify(mediaDataSourceRepository).resetState();

        verify(callback).onSucceed(ArgumentMatchers.<String>anyList(), any(OperationResult.class));

    }

    @Test
    public void testLoggedInUserEmptyWhenLoadUserSuccessAfterLoginSuccess() {

        when(loggedInUserDataSource.getAllLoggedInUsers()).thenReturn(Collections.<LoggedInUser>emptyList());

        testLoadUserSuccessAfterLoginSuccess();

        verify(systemSettingDataSource).setAutoUploadOrNot(false);

        verify(systemSettingDataSource).setShowAutoUploadDialog(true);

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

        verify(mediaDataSourceRepository).resetState();

        verify(systemSettingDataSource).setLoginWithWechatCodeOrNot(false);

        verify(userDataRepository).getUsers(eq(testUserUUID), any(BaseLoadDataCallback.class));

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

        verify(systemSettingDataSource).setAutoUploadOrNot(eq(true));

    }

    @Test
    public void testLoginWithWeChatCode_succeed() {

        WeChatTokenUserWrapper weChatTokenUserWrapper = new WeChatTokenUserWrapper();
        weChatTokenUserWrapper.setToken(testToken);
        weChatTokenUserWrapper.setGuid(testGUID);
        weChatTokenUserWrapper.setNickName(testWeChatName);
        weChatTokenUserWrapper.setAvatarUrl(testWeChatAvatarUrl);

        BaseOperateDataCallback<Boolean> callback = Mockito.mock(BaseOperateDataCallback.class);

        loginUseCase.loginWithWeChatCode("", callback);

        ArgumentCaptor<BaseLoadDataCallback<WeChatTokenUserWrapper>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(tokenDataSource).getToken(anyString(), captor.capture());

        captor.getValue().onSucceed(Collections.singletonList(weChatTokenUserWrapper), new OperationSuccess());

        verify(systemSettingDataSource).setCurrentLoginToken(testToken);

        verify(httpRequestFactory).setCurrentData(anyString(), anyString());

        verify(systemSettingDataSource).setCurrentLoginToken(testToken);

    }

    public void testGetStationListAndGetLocalUserAndLoginWithWeChatCodeFail(){

        testLoginWithWeChatCode_succeed();

        ArgumentCaptor<BaseLoadDataCallback<Station>> stationLoadCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationsDataSource).getStationsByWechatGUID(eq(testGUID), stationLoadCaptor.capture());

        Station station = new Station();

        station.setId(testStationID);
        station.setLabel("testStationLabel");

        stationLoadCaptor.getValue().onFail(new OperationFail("fail"));

        verify(systemSettingDataSource).setCurrentLoginToken(eq(""));

    }


    @Test
    public void testGetStationListAndGetLocalUserAndLoginWithWeChatCodeSucceed() {

        testLoginWithWeChatCode_succeed();

        ArgumentCaptor<BaseLoadDataCallback<Station>> stationLoadCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationsDataSource).getStationsByWechatGUID(eq(testGUID), stationLoadCaptor.capture());

        Station station = new Station();

        station.setId(testStationID);
        station.setLabel("testStationLabel");

        stationLoadCaptor.getValue().onSucceed(Collections.singletonList(station), new OperationSuccess());

        verify(httpRequestFactory).setStationID(eq(testStationID));
        verify(httpRequestFactory).setDefaultFactory(eq(true));

        testAfterChooseStationID();

        initSystemStateAndVerifyAfterChooseStationID();

        verify(systemSettingDataSource).setCurrentLoginUserGUID(testGUID);

        verify(systemSettingDataSource).setAutoUploadOrNot(false);

        verify(systemSettingDataSource).setShowAutoUploadDialog(true);

        verify(mediaDataSourceRepository).clearAllStationMediasInDB();

        verify(mediaDataSourceRepository).resetState();

        verify(weChatUserDataSource).insertWeChatUser(any(WeChatUser.class));

    }

    private void testAfterChooseStationID() {
        ArgumentCaptor<BaseLoadDataCallback<User>> getUserByStationIDCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(userDataRepository).getUsersByStationIDWithCloudAPI(eq(testStationID), getUserByStationIDCaptor.capture());

        User user = new User();
        user.setUuid(testUserUUID);
        user.setAssociatedWeChatGUID(testGUID);

        getUserByStationIDCaptor.getValue().onSucceed(Collections.singletonList(user), new OperationSuccess());

        verify(systemSettingDataSource).setLoginWithWechatCodeOrNot(true);

        ArgumentCaptor<BaseLoadDataCallback<User>> getUsersCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(userDataRepository).getUserDetailedInfoByUUID(eq(testUserUUID), getUsersCaptor.capture());

        getUsersCaptor.getValue().onSucceed(Collections.singletonList(user), new OperationSuccess());

        ArgumentCaptor<BaseLoadDataCallback<String>> getUserHomeCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(userDataRepository).getCurrentUserHome(getUserHomeCaptor.capture());

        getUserHomeCaptor.getValue().onSucceed(Collections.singletonList(testUserHome), new OperationSuccess());

        verify(userDataRepository).insertUsers(ArgumentMatchers.<User>anyCollection());

        verify(systemSettingDataSource).setCurrentLoginStationID(eq(testStationID));

    }

    private void initSystemStateAndVerifyAfterChooseStationID() {

        verify(checkMediaIsUploadStrategy).setCurrentUserUUID(testUserUUID);

        verify(checkMediaIsUploadStrategy).setUploadedMediaHashs(Collections.<String>emptyList());

        verify(uploadMediaUseCase).resetState();

        verify(imageGifLoaderInstance).setToken(anyString());

        verify(systemSettingDataSource).setCurrentLoginUserUUID(anyString());

        verify(systemSettingDataSource,times(2)).setCurrentLoginToken(anyString());

        verify(systemSettingDataSource).setCurrentEquipmentIp(anyString());

        verify(systemSettingDataSource).setCurrentLoginUserGUID(eq(""));

        verify(systemSettingDataSource).setCurrentLoginStationID(eq(""));

        verify(stationFileRepository).clearDownloadFileRecordInCache();

        verify(newPhotoListDataLoader).setNeedRefreshData(eq(true));

    }


}
