package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.mock.MockThreadManager;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationJSONException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import org.junit.After;
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

public class UserDataRepositoryImplTest {

    private static final String USER_PWD = "testPWD";
    private static final String USER_NAME = "testName";
    private static final String USER_UUID = "testUUID";

    @Mock
    private UserRemoteDataSource userRemoteDataSource;

    @Mock
    private UserDBDataSource userDBDataSource;

    private UserDataRepositoryImpl userDataRepositoryImpl;

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

        userDataRepositoryImpl = UserDataRepositoryImpl.getInstance(userDBDataSource, userRemoteDataSource, new MockThreadManager());

    }

    @After
    public void clean() {
        UserDataRepositoryImpl.destroyInstance();
    }

    @Test
    public void insertUserToRemoteDataSource() {

        userDataRepositoryImpl.insertUser(USER_NAME, USER_PWD, null);

        verify(userRemoteDataSource).insertUser(anyString(), anyString(), any(BaseOperateDataCallback.class));

    }

    @Test
    public void insertUserToDBDataSource_WhenRemoteInsertSucceed() {

        userDataRepositoryImpl.insertUser(USER_NAME, USER_PWD, new BaseOperateDataCallbackImpl<User>());

        verify(userRemoteDataSource).insertUser(anyString(), anyString(), operateDataCallbackArgumentCaptor.capture());

        User user = createUser();

        operateDataCallbackArgumentCaptor.getValue().onSucceed(user, new OperationSuccess());

        verify(userDBDataSource).insertUser(ArgumentMatchers.<User>anyCollection());

        assertEquals(1, userDataRepositoryImpl.cacheUsers.size());

    }

    @Test
    public void insertUserToRemoteDataSourceFail() {

        userDataRepositoryImpl.insertUser(USER_NAME, USER_PWD, new BaseOperateDataCallback<User>() {
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
    public void getUserRetrieveWhenCacheDirty() {

        userDataRepositoryImpl.getUsers("",new BaseLoadDataCallbackImpl<User>());

        ArgumentCaptor<BaseLoadDataCallback<User>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(userRemoteDataSource).getUsers(anyString(),captor.capture());

        captor.getValue().onSucceed(Collections.<User>emptyList(), new OperationSuccess());

        userDataRepositoryImpl.getUsers("",new BaseLoadDataCallbackImpl<User>());

        verify(userRemoteDataSource, times(1)).getUsers(anyString(),any(BaseLoadDataCallback.class));

        userDataRepositoryImpl.setCacheDirty();

        userDataRepositoryImpl.getUsers("",new BaseLoadDataCallbackImpl<User>());

        verify(userRemoteDataSource, times(2)).getUsers(anyString(),any(BaseLoadDataCallback.class));
    }

    @Test
    public void getUser_RetrieveFromRemoteSucceed() {

        userDataRepositoryImpl.getUsers("",baseLoadDataCallback);

        verify(userRemoteDataSource).getUsers(anyString(),loadRemoteDataCallbackArgumentCaptor.capture());

        loadRemoteDataCallbackArgumentCaptor.getValue().onSucceed(Collections.singletonList(createUser()), new OperationSuccess());

        verify(userDBDataSource, never()).getUsers();

        assertEquals(USER_NAME, userDataRepositoryImpl.cacheUsers.get(USER_UUID).getUserName());

        assertEquals(false, userDataRepositoryImpl.cacheDirty);
    }

    @Test
    public void getUser_RetrieveFromRemoteFail_ThenRemoteFromDB() {

        when(userDBDataSource.getUsers()).thenReturn(Collections.singletonList(createUser()));

        userDataRepositoryImpl.getUsers("",baseLoadDataCallback);

        verify(userRemoteDataSource).getUsers(anyString(),loadRemoteDataCallbackArgumentCaptor.capture());

        loadRemoteDataCallbackArgumentCaptor.getValue().onFail(new OperationJSONException());

        assertEquals(USER_NAME, userDataRepositoryImpl.cacheUsers.get(USER_UUID).getUserName());

        assertEquals(false, userDataRepositoryImpl.cacheDirty);
    }

    @Test
    public void getUser_RetrieveFromRemoteSucceed_deleteOldDataFromDB_InsertNewDataToDB() {

        userDataRepositoryImpl.getUsers("",new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

            }

            @Override
            public void onFail(OperationResult operationResult) {

            }
        });

        verify(userRemoteDataSource).getUsers(anyString(),loadRemoteDataCallbackArgumentCaptor.capture());

        User user = createUser();

        loadRemoteDataCallbackArgumentCaptor.getValue().onSucceed(Collections.singletonList(user), new OperationSuccess());

        InOrder inOrder = inOrder(userDBDataSource);

        inOrder.verify(userDBDataSource).clearUsers();
        inOrder.verify(userDBDataSource).insertUser(ArgumentMatchers.<User>anyCollection());

    }






}
