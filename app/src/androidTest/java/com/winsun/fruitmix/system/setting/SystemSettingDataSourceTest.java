package com.winsun.fruitmix.system.setting;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/18.
 */

@RunWith(AndroidJUnit4.class)
public class SystemSettingDataSourceTest {

    private SystemSettingDataSource systemSettingDataSource;

    @Before
    public void setup() {

        systemSettingDataSource = SystemSettingDataSource.getInstance(InstrumentationRegistry.getTargetContext());

    }

    @After
    public void clean() {
        systemSettingDataSource.destroyInstance();
    }

    @Test
    public void testSetCurrentUploadDeviceID() {

        String currentUploadDeviceID = "testDeviceID";

        systemSettingDataSource.setCurrentUploadUserUUID(currentUploadDeviceID);

        String result = systemSettingDataSource.getCurrentUploadUserUUID();

        assertEquals(result, currentUploadDeviceID);

    }

    @Test
    public void testSetAutoUploadOrNot() {

        systemSettingDataSource.setAutoUploadOrNot(true);

        boolean result = systemSettingDataSource.getAutoUploadOrNot();

        assertEquals(result, true);
    }

    @Test
    public void testSetShowAutoUploadDialog() {

        systemSettingDataSource.setShowAutoUploadDialog(true);

        boolean result = systemSettingDataSource.getShowAutoUploadDialog();

        assertEquals(result, true);

    }


}
