package com.winsun.fruitmix;

import android.text.format.Formatter;

import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2017/7/27.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class StringUtilTest {

    @Test
    public void testSplitString() {

        String url = "/v1/tickets/586f0eb6-2db8-47b1-8dc3-0bb13b0c7088";

        String[] result = url.split("/");

        assertEquals("", result[0]);
        assertEquals("v1", result[1]);
        assertEquals("tickets", result[2]);
        assertEquals("586f0eb6-2db8-47b1-8dc3-0bb13b0c7088", result[3]);

    }

    @Test
    public void testRemoveHttpHead() {

        String ip = "10.10.9.100";

        String httpHead = "http://";

        String url = httpHead + ip;

        String[] result = url.split(httpHead);

        assertEquals(ip, result[1]);

    }

    @Test
    public void testSortTime() {

        String time1 = "2017-04-06 17:50:30";
        String time2 = "2017-04-06 17:50:31";
        String time3 = "2017-04-06 17:50:29";

        List<String> strings = new ArrayList<>();

        strings.add(time1);
        strings.add(time2);
        strings.add(time3);

        Collections.sort(strings, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        assertEquals(time3, strings.get(0));
        assertEquals(time1, strings.get(1));
        assertEquals(time2, strings.get(2));
    }

    @Test
    public void testCreateBodyUsingQueryStr() {

        String httpPath = "/media/5669da130c25ccb372d11c916b928d561517c04bbac9e0d9c2c7c0713af06240?alt=thumbnail&width=64&height=64&autoOrient=true&moidifier=caret";

        String[] splitResult = httpPath.split("\\?");

        assertEquals("/media/5669da130c25ccb372d11c916b928d561517c04bbac9e0d9c2c7c0713af06240", splitResult[0]);

        String[] queryStrs = splitResult[1].split("&");

        String[] queryStrSplitResult = queryStrs[0].split("=");

        assertEquals("alt", queryStrSplitResult[0]);
        assertEquals("thumbnail", queryStrSplitResult[1]);

        queryStrSplitResult = queryStrs[1].split("=");

        assertEquals("width", queryStrSplitResult[0]);
        assertEquals("64", queryStrSplitResult[1]);

    }

    @Test
    public void testEnterIP() {

        String ip = "10.2.10.55";

        assertEquals(true, Util.checkIpLegal(ip));

        ip = "10.2.10.115:3000";

        assertEquals(false, Util.checkIpLegal(ip));

        ip = "151.254.12.1.1";

        assertEquals(false, Util.checkIpLegal(ip));

        ip = "14.15.258.40";

        assertEquals(false, Util.checkIpLegal(ip));

    }

    @Test
    public void testFindNumInStr() {

        String testStr1 = "512 kB";

        assertEquals(512, Util.getFirstNumInStr(testStr1));

        String testStr2 = "1796336 kB";

        assertEquals(1796336, Util.getFirstNumInStr(testStr2));

        String testStr3 = "fadsf";

        assertEquals(0, Util.getFirstNumInStr(testStr3));

    }

    @Test
    public void testAndroidFormatSize(){

        long size = 32 * 1024 * 1024;

        assertEquals("32.00MB", Formatter.formatFileSize(RuntimeEnvironment.application,size));

        String testSizeStr = "1796336 kB";

        long testSize2 = Util.getFirstNumInStr(testSizeStr) * 1024;

        assertEquals("1.71GB", Formatter.formatFileSize(RuntimeEnvironment.application,testSize2));

    }

}
