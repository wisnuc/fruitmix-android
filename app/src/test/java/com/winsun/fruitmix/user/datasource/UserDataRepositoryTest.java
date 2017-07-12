package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationJSONException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.user.datasource.UserDBDataSource;
import com.winsun.fruitmix.user.datasource.UserRemoteDataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/11.
 */

public class UserDataRepositoryTest {

    private static final String USER_PWD = "testPWD";
    private static final String USER_NAME = "testName";
    private static final String USER_UUID = "testUUID";

    @Mock
    private UserRemoteDataSource userRemoteDataSource;

    @Mock
    private UserDBDataSource userDBDataSource;

    private UserDataRepository userDataRepository;

    @Mock
    private BaseLoadDataCallback<User> baseLoadDataCallback;

    @Captor
    private ArgumentCaptor<BaseOperateDataCallback<User>> operateDataCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<BaseLoadDataCallback<User>> loadDBDataCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<BaseLoadDataCallback<User>> loadRemoteDataCallbackArgumentCaptor;


    private User createUser() {

        User user = new User();
        user.setUserName(USER_NAME);
        user.setUuid(USER_UUID);

        return user;
    }

    @Before
    public void init() {

        MockitoAnnotations.initMocks(this);

        userDataRepository = new UserDataRepository(userDBDataSource, userRemoteDataSource);

    }

    @Test
    public void insertUserToRemoteDataSource() {

        userDataRepository.insertUser(USER_NAME, USER_PWD, null);

        verify(userRemoteDataSource).insertUser(anyString(), anyString(), any(BaseOperateDataCallback.class));

    }

    @Test
    public void insertUserToDBDataSource_WhenRemoteInsertSucceed() {

        userDataRepository.insertUser(USER_NAME, USER_PWD, new BaseOperateDataCallback<User>() {
            @Override
            public void onSucceed(User data, OperationResult result) {

            }

            @Override
            public void onFail(OperationResult result) {

            }
        });

        verify(userRemoteDataSource).insertUser(anyString(), anyString(), operateDataCallbackArgumentCaptor.capture());

        User user = createUser();

        operateDataCallbackArgumentCaptor.getValue().onSucceed(user, new OperationSuccess(0));

        verify(userDBDataSource).insertUser(ArgumentMatchers.<User>anyCollection());

        assertEquals(1, userDataRepository.cacheUsers.size());

    }

    @Test
    public void insertUserToRemoteDataSourceFail() {

        userDataRepository.insertUser(USER_NAME, USER_PWD, new BaseOperateDataCallback<User>() {
            @Override
            public void onSucceed(User data, OperationResult result) {

            }

            @Override
            public void onFail(OperationResult result) {
                assertTrue(result instanceof OperationIOException);
            }
        });

        verify(userRemoteDataSource).insertUser(anyString(), anyString(), operateDataCallbackArgumentCaptor.capture());

        operateDataCallbackArgumentCaptor.getValue().onFail(new OperationIOException());

    }

    @Test
    public void getUser_RetrieveOrder() {

        userDataRepository.getUsers(null);

        InOrder inOrder = inOrder(userDBDataSource, userRemoteDataSource);

        inOrder.verify(userDBDataSource).getUsers(any(BaseLoadDataCallback.class));
        inOrder.verify(userRemoteDataSource).getUsers(any(BaseLoadDataCallback.class));

    }

    @Test
    public void getUser_RetrieveFromDBFail_FromRemoteSucceed() {

        userDataRepository.getUsers(baseLoadDataCallback);

        verify(userDBDataSource).getUsers(loadDBDataCallbackArgumentCaptor.capture());

        loadDBDataCallbackArgumentCaptor.getValue().onFail(new OperationJSONException());

        verify(userRemoteDataSource).getUsers(loadRemoteDataCallbackArgumentCaptor.capture());

        loadRemoteDataCallbackArgumentCaptor.getValue().onSucceed(Collections.singletonList(createUser()), new OperationSuccess(0));

        InOrder inOrder = inOrder(baseLoadDataCallback);

        inOrder.verify(baseLoadDataCallback).onFail(any(OperationResult.class));
        inOrder.verify(baseLoadDataCallback).onSucceed(ArgumentMatchers.<User>anyList(), any(OperationResult.class));

        assertEquals(USER_NAME, userDataRepository.cacheUsers.get(USER_UUID).getUserName());
    }

    @Test
    public void getUser_RetrieveFromDBSucceed_FromRemoteFail() {

        userDataRepository.getUsers(baseLoadDataCallback);

        verify(userDBDataSource).getUsers(loadDBDataCallbackArgumentCaptor.capture());

        loadDBDataCallbackArgumentCaptor.getValue().onSucceed(Collections.singletonList(createUser()), new OperationSuccess(0));

        verify(userRemoteDataSource).getUsers(loadRemoteDataCallbackArgumentCaptor.capture());

        loadRemoteDataCallbackArgumentCaptor.getValue().onFail(new OperationJSONException());

        InOrder inOrder = inOrder(baseLoadDataCallback);

        inOrder.verify(baseLoadDataCallback).onSucceed(ArgumentMatchers.<User>anyList(), any(OperationResult.class));
        inOrder.verify(baseLoadDataCallback).onFail(any(OperationResult.class));

        assertEquals(USER_NAME, userDataRepository.cacheUsers.get(USER_UUID).getUserName());
    }

    @Test
    public void getUser_RetrieveFromRemoteSucceed_deleteOldDataFromDB_InsertNewDataToDB() {

        userDataRepository.getUsers(null);

        verify(userRemoteDataSource).getUsers(loadRemoteDataCallbackArgumentCaptor.capture());

        User user = createUser();

        loadRemoteDataCallbackArgumentCaptor.getValue().onSucceed(Collections.singletonList(user), new OperationSuccess(0));

        InOrder inOrder = inOrder(userDBDataSource);

        inOrder.verify(userDBDataSource).clearUsers();
        inOrder.verify(userDBDataSource).insertUser(ArgumentMatchers.<User>anyCollection());

    }

}
