package com.winsun.fruitmix.usecase;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInWeChatUser;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.stations.Station;
import com.winsun.fruitmix.stations.StationsDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Administrator on 2017/9/21.
 */

public class GetAllBindingLocalUserUseCaseTest {

    @Mock
    private UserDataRepository userDataRepository;

    @Mock
    private StationsDataSource stationsDataSource;

    private GetAllBindingLocalUserUseCase getAllBindingLocalUserUseCase;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        getAllBindingLocalUserUseCase = new GetAllBindingLocalUserUseCase(userDataRepository, stationsDataSource);

    }

    @After
    public void tearDown() throws Exception {

        getAllBindingLocalUserUseCase = null;

    }


    @Test
    public void testGetAllBindingLocalUserByEmptyParam() {

        getAllBindingLocalUserUseCase.getAllBindingLocalUser("", "",new BaseLoadDataCallback<LoggedInWeChatUser>() {
            @Override
            public void onSucceed(List<LoggedInWeChatUser> data, OperationResult operationResult) {

                assertEquals(0, data.size());

            }

            @Override
            public void onFail(OperationResult operationResult) {

            }
        });

    }

    @Test
    public void testGetAllBindingLocalUserWhenLoginByWeChatUser() {

        String testGUID = "testGUID";
        String testToken = "testToken";

        String stationID1 = "stationID1";
        String stationID2 = "stationID2";

        final String testUserName1 = "userName1";
        final String testUserName2 = "userName2";

        getAllBindingLocalUserUseCase.getAllBindingLocalUser(testGUID,testToken, new BaseLoadDataCallback<LoggedInWeChatUser>() {
            @Override
            public void onSucceed(List<LoggedInWeChatUser> data, OperationResult operationResult) {

                assertEquals(2, data.size());

                LoggedInUser loggedInUser1 = data.get(0);

                User user1 = loggedInUser1.getUser();

                assertEquals(testUserName1, user1.getUserName());

                LoggedInUser loggedInUser2 = data.get(1);

                User user2 = loggedInUser2.getUser();

                assertEquals(testUserName2, user2.getUserName());

            }

            @Override
            public void onFail(OperationResult operationResult) {

                fail("should not enter here");
            }
        });

        ArgumentCaptor<BaseLoadDataCallback<Station>> getStationsCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationsDataSource).getStationsByWechatGUID(eq(testGUID), getStationsCaptor.capture());

        List<Station> stations = new ArrayList<>();

        Station station1 = new Station();
        station1.setId(stationID1);
        station1.addIp("testIP1");
        stations.add(station1);

        Station station2 = new Station();
        station2.setId(stationID2);
        station2.addIp("testIP2");
        stations.add(station2);

        getStationsCaptor.getValue().onSucceed(stations, new OperationSuccess());

        ArgumentCaptor<BaseLoadDataCallback<User>> getUser1Captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(userDataRepository).getUsersByStationIDWithCloudAPI(eq(stationID1), getUser1Captor.capture());

        User user1 = new User();
        user1.setUserName(testUserName1);
        user1.setAssociatedWeChatGUID(testGUID);

        getUser1Captor.getValue().onSucceed(Collections.singletonList(user1), new OperationSuccess());

        ArgumentCaptor<BaseLoadDataCallback<User>> getUser2Captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(userDataRepository).getUsersByStationIDWithCloudAPI(eq(stationID2), getUser2Captor.capture());

        User user2 = new User();
        user2.setUserName(testUserName2);
        user2.setAssociatedWeChatGUID(testGUID);

        getUser2Captor.getValue().onSucceed(Collections.singletonList(user2), new OperationSuccess());


    }


}
