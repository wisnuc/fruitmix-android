package com.winsun.fruitmix.logout;

import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.wechat.user.WeChatUser;
import com.winsun.fruitmix.wechat.user.WeChatUserDataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * Created by Administrator on 2017/8/30.
 */

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

    private String testToken = "testToken";

    private String testGUID = "testGUID";

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        logoutUseCase = LogoutUseCase.getInstance(systemSettingDataSource, loggedInUserDataSource, uploadMediaUseCase, weChatUserDataSource);
    }

    @After
    public void teardown() {

        LogoutUseCase.destroyInstance();

    }

    @Test
    public void testChangeLoginUser() {

        logoutUseCase.changeLoginUser();

        testChangeLoginUserResult();

    }

    private void testChangeLoginUserResult() {
        verify(uploadMediaUseCase).stopUploadMedia();

        verify(uploadMediaUseCase).stopRetryUpload();
    }

    @Test
    public void testLogoutWithLocalUser() {

        when(systemSettingDataSource.getCurrentLoginToken()).thenReturn(testToken);

        when(systemSettingDataSource.getCurrentLoginUserGUID()).thenReturn(testGUID);

        when(loggedInUserDataSource.getLoggedInUserByToken(testToken)).thenReturn(new LoggedInUser("", "", "", "", new User()));

        logoutUseCase.logout();

        verify(loggedInUserDataSource).getLoggedInUserByToken(eq(testToken));

        verify(loggedInUserDataSource).deleteLoggedInUsers(ArgumentMatchers.<LoggedInUser>anyCollection());

        verify(weChatUserDataSource, never()).getWeChatUser(anyString(), anyString());

        verify(weChatUserDataSource, never()).deleteWeChatUser(anyString());

        verify(systemSettingDataSource).setCurrentLoginToken(eq(""));

        testChangeLoginUserResult();

    }

    @Test
    public void testLogoutWithWeChatUser() {

        when(systemSettingDataSource.getCurrentLoginToken()).thenReturn(testToken);

        when(systemSettingDataSource.getCurrentLoginUserGUID()).thenReturn(testGUID);

        when(loggedInUserDataSource.getLoggedInUserByToken(testToken)).thenReturn(null);

        when(weChatUserDataSource.getWeChatUser(testToken, testGUID)).thenReturn(new WeChatUser(testToken, testGUID, ""));

        logoutUseCase.logout();

        verify(loggedInUserDataSource).getLoggedInUserByToken(eq(testToken));

        verify(loggedInUserDataSource, never()).deleteLoggedInUsers(ArgumentMatchers.<LoggedInUser>anyCollection());

        verify(weChatUserDataSource).getWeChatUser(eq(testToken), eq(testGUID));

        verify(weChatUserDataSource).deleteWeChatUser(eq(testToken));

        verify(systemSettingDataSource).setCurrentLoginToken(eq(""));

        verify(systemSettingDataSource).setCurrentLoginUserGUID(eq(""));

        testChangeLoginUserResult();

    }


}
