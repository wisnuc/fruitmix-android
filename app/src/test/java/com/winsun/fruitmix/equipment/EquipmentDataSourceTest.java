package com.winsun.fruitmix.equipment;

import android.support.annotation.NonNull;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/11.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class EquipmentDataSourceTest {

    @Mock
    private IHttpUtil iHttpUtil;

    private EquipmentDataSource equipmentDataSource;

    @Before
    public void init() {

        MockitoAnnotations.initMocks(this);

        equipmentDataSource = new EquipmentDataSource(iHttpUtil);

    }

    @Test
    public void getUsersInEquipment_remoteCallSucceed() {

        try {
            when(iHttpUtil.remoteCall(any(HttpRequest.class))).thenReturn(new HttpResponse(200, "[\n" +
                    "    {\n" +
                    "        \"uuid\": \"511eecb5-0362-41a2-ac79-624ac5e9c03f\",\n" +
                    "        \"username\": \"w\",\n" +
                    "        \"avatar\": null,\n" +
                    "        \"unixUID\": 2000\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"uuid\": \"3f2d3d52-d096-4e51-b414-c74f7acb474f\",\n" +
                    "        \"username\": \"2\",\n" +
                    "        \"avatar\": null,\n" +
                    "        \"unixUID\": 2001\n" +
                    "    }\n" +
                    "]"));


        } catch (IOException e) {
            e.printStackTrace();
        }

        Equipment equipment = getEquipment();

        equipmentDataSource.getUsersInEquipment(equipment, new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                User user = data.get(0);
                assertEquals("511eecb5-0362-41a2-ac79-624ac5e9c03f", user.getUuid());
                assertEquals("w", user.getUserName());

                user = data.get(1);
                assertEquals("3f2d3d52-d096-4e51-b414-c74f7acb474f", user.getUuid());
                assertEquals("2", user.getUserName());
            }

            @Override
            public void onFail(OperationResult operationResult) {
                assertShouldNotEnter();
            }
        });

    }

    @NonNull
    private Equipment getEquipment() {
        return new Equipment("", Collections.singletonList("test"), 0);
    }

    @Test
    public void getUsersInEquipment_remoteCallFail() {

        try {
            when(iHttpUtil.remoteCall(any(HttpRequest.class))).thenReturn(new HttpResponse(404, ""));

        } catch (IOException e) {
            e.printStackTrace();
        }

        Equipment equipment = getEquipment();

        equipmentDataSource.getUsersInEquipment(equipment, new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {
                assertShouldNotEnter();
            }

            @Override
            public void onFail(OperationResult operationResult) {
                assertTrue(operationResult instanceof OperationNetworkException);
            }
        });

    }

    private void assertShouldNotEnter() {
        assertTrue("should not enter here", false);
    }

    @Test
    public void getEquipmentHostAlias() {

        try {
            when(iHttpUtil.remoteCall(any(HttpRequest.class))).thenReturn(new HttpResponse(200, "[{\n" +
                    "\t\"ipv4\":\"10.10.9.84\"\n" +
                    "}]"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        final Equipment equipment = getEquipment();

        equipmentDataSource.getEquipmentHostAlias(equipment, new BaseLoadDataCallback<String>() {

            @Override
            public void onSucceed(List<String> data, OperationResult operationResult) {

                assertEquals("10.10.9.84", data.get(0));
            }

            @Override
            public void onFail(OperationResult operationResult) {
                assertShouldNotEnter();
            }
        });

    }


}
