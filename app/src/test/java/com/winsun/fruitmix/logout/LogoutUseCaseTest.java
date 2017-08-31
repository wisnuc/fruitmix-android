package com.winsun.fruitmix.logout;

import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
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

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        logoutUseCase = LogoutUseCase.getInstance(systemSettingDataSource, loggedInUserDataSource);
    }

    @After
    public void teardown() {

    }

    @Test
    public void testLogout() {

        String testUserUUID = "testUserUUID";

        when(systemSettingDataSource.getCurrentLoginUserUUID()).thenReturn(testUserUUID);

        logoutUseCase.logout();

        verify(loggedInUserDataSource).getLoggedInUserByUserUUID(eq(testUserUUID));

        verify(loggedInUserDataSource).deleteLoggedInUsers(ArgumentMatchers.<LoggedInUser>anyCollection());

        verify(systemSettingDataSource).setCurrentLoginUserUUID(eq(""));

    }

}
