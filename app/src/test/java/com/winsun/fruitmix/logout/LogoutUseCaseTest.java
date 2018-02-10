package com.winsun.fruitmix.logout;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.file.data.model.FileTaskManager;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.mqtt.MqttUseCase;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.FileTool;
import com.winsun.fruitmix.wechat.user.WeChatUser;
import com.winsun.fruitmix.wechat.user.WeChatUserDataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;

/**
 * Created by Administrator on 2017/8/30.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class LogoutUseCaseTest {

    @Mock
    private SystemSettingDataSource systemSettingDataSource;

    @Mock
    private LoggedInUserDataSource loggedInUserDataSource;

    private LogoutUseCase logoutUseCase;

    @Mock
    private UploadMediaUseCase uploadMediaUseCase;

    @Mock
    private WeChatUserDataSource weChatUserDataSource;

    @Mock
    private HttpRequestFactory httpRequestFactory;

    @Mock
    private FileTool mFileTool;

    @Mock
    private StationFileRepository mStationFileRepository;

    @Mock
    private UserDataRepository mUserDataRepository;

    @Mock
    private FileTaskManager mFileTaskManager;

    @Mock
    private MqttUseCase mMqttUseCase;

    private String testToken = "testToken";

    private String testStationID = "testStationID";

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        logoutUseCase = LogoutUseCase.getInstance(systemSettingDataSource, loggedInUserDataSource,
                uploadMediaUseCase, weChatUserDataSource, httpRequestFactory, "", mFileTool,
                mStationFileRepository, mUserDataRepository, mFileTaskManager,mMqttUseCase);

        when(systemSettingDataSource.getCurrentLoginUserUUID()).thenReturn("");

        when(mFileTool.getTemporaryUploadFolderPath(anyString(), anyString())).thenReturn("");

    }

    @After
    public void teardown() {

        LogoutUseCase.destroyInstance();

    }

    @Test
    public void testChangeLoginUser() {

        logoutUseCase.stopUploadTask();

        testChangeLoginUserResult();

    }

    private void testChangeLoginUserResult() {
        verify(uploadMediaUseCase).stopUploadMedia();

        verify(uploadMediaUseCase).stopRetryUploadTemporary();
    }

    @Test
    public void testLogoutWithLocalUser() {

        when(systemSettingDataSource.getCurrentLoginToken()).thenReturn(testToken);

        when(loggedInUserDataSource.getLoggedInUserByToken(testToken)).thenReturn(new LoggedInUser("", "", "", "", new User()));

        logoutUseCase.logout();

        verify(loggedInUserDataSource).getLoggedInUserByToken(eq(testToken));

        verify(loggedInUserDataSource).deleteLoggedInUsers(ArgumentMatchers.<LoggedInUser>anyCollection());

        verify(weChatUserDataSource, never()).getWeChatUser(anyString(), anyString());

        verify(weChatUserDataSource, never()).deleteWeChatUser(anyString());

        handleLogout();

        testChangeLoginUserResult();

    }

    @Test
    public void testLogoutWithWeChatUser() {

        when(systemSettingDataSource.getCurrentLoginToken()).thenReturn(testToken);

        when(systemSettingDataSource.getCurrentLoginStationID()).thenReturn(testStationID);

        when(loggedInUserDataSource.getLoggedInUserByToken(testToken)).thenReturn(null);

        when(weChatUserDataSource.getWeChatUser(testToken, testStationID)).thenReturn(new WeChatUser(testToken, "", testStationID));

        logoutUseCase.logout();

        verify(loggedInUserDataSource).getLoggedInUserByToken(eq(testToken));

        verify(loggedInUserDataSource, never()).deleteLoggedInUsers(ArgumentMatchers.<LoggedInUser>anyCollection());

        verify(weChatUserDataSource).getWeChatUser(eq(testToken), eq(testStationID));

        verify(weChatUserDataSource).deleteWeChatUser(eq(testToken));

        handleLogout();

        testChangeLoginUserResult();

    }

    private void handleLogout() {
        verify(systemSettingDataSource).setCurrentLoginToken(eq(""));

        verify(systemSettingDataSource).setCurrentWAToken(eq(""));

        verify(systemSettingDataSource).setCurrentLoginUserGUID(eq(""));

        verify(systemSettingDataSource).setCurrentLoginStationID(eq(""));

        verify(systemSettingDataSource).setCurrentEquipmentIp(eq(""));

        verify(systemSettingDataSource).setCurrentLoginUserUUID(eq(""));

        verify(systemSettingDataSource).setCurrentUploadUserUUID(eq(""));

        verify(httpRequestFactory).reset();

        verify(mFileTaskManager).cancelAllStartItem();

        verify(mStationFileRepository).clearAllFileTaskItemInCache();

        verify(mFileTool).getTemporaryUploadFolderPath(anyString(), anyString());

        verify(mFileTool).deleteDir(anyString());

        verify(mUserDataRepository).clearAllUsersInDB();

        verify(mUserDataRepository).clearAllUsersInCache();

        verify(mMqttUseCase).stopMqtt();

    }

}
