package com.winsun.fruitmix.user;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.user.datasource.UserDBDataSource;
import com.winsun.fruitmix.user.datasource.UserDBDataSourceImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Created by Administrator on 2017/7/10.
 */

@RunWith(AndroidJUnit4.class)
public class UserDBDataSourceTest {

    private static final String USER_UUID = "testUUID";
    private static final String USER_NAME = "testName";

    private UserDBDataSource userDBDataSource;

    private User createUser() {

        User user = new User();
        user.setUuid(USER_UUID);
        user.setUserName(USER_NAME);

        return user;
    }

    @Before
    public void init() {

        DBUtils dbUtils = DBUtils.getInstance(InstrumentationRegistry.getTargetContext());

        userDBDataSource = new UserDBDataSourceImpl(dbUtils);

        userDBDataSource.clearUsers();
    }


    @Test
    public void insertUserToDB_retrieveUser() {

        User user = createUser();

        userDBDataSource.insertUser(Collections.singletonList(user));

        List<User> data = userDBDataSource.getUsers();

        assertEquals(1, data.size());
        assertEquals(USER_UUID, data.get(0).getUuid());
        assertEquals(USER_NAME, data.get(0).getUserName());

    }


}
