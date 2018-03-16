package com.winsun.fruitmix;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.Formatter;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.util.FilterRule;
import com.winsun.fruitmix.util.ItemFilterKt;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.util.Util;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    public void testAndroidFormatSize() {

        long size = 32 * 1024 * 1024;

        assertEquals("32.00MB", Formatter.formatFileSize(RuntimeEnvironment.application, size));

        String testSizeStr = "1796336 kB";

        long testSize2 = Util.getFirstNumInStr(testSizeStr) * 1024;

        assertEquals("1.71GB", Formatter.formatFileSize(RuntimeEnvironment.application, testSize2));

    }

    @Test
    public void testGetLastParam() {

        String fileName = "20171026_155540.mp4";

        String filePath = "/DCIM/Camera/" + fileName;

        int start = filePath.lastIndexOf("/");

        int end = filePath.length();

        assertEquals(fileName, filePath.substring(start + 1, end));

    }

    @Test
    public void testFormatDuration() {

        long duration = 22037;

        String date = Util.formatDuration(duration);

        assertEquals("0:22", date);

        long largeDuration = 90725000L;

        String largeDate = Util.formatDuration(largeDuration);

        assertEquals("25:12:05", largeDate);

    }

    @Test
    public void testDoubleToLong() {

        double durationSec = 2.84;

        long duration = (long) (durationSec * 1000);

        assertEquals(2840, duration);

    }

    @Test
    public void testMediaIsGif() {

        Media media = new Media();
        media.setOriginalPhotoPath("fadf.GIF");

        assertTrue(MediaUtil.checkMediaIsGif(media));

        media.setOriginalPhotoPath("fasd.gif");

        assertTrue(MediaUtil.checkMediaIsGif(media));
    }

    @Test
    public void testFormatAvatarName() {

        String userName = "Leo";
        assertEquals("LE", Util.getUserNameForAvatar(userName));

        userName = "L我";
        assertEquals("L", Util.getUserNameForAvatar(userName));

        userName = "Leo Wu";
        assertEquals("LW", Util.getUserNameForAvatar(userName));

        userName = "Leo 无";
        assertEquals("L", Util.getUserNameForAvatar(userName));

        userName = "Leo K Wu";
        assertEquals("LW", Util.getUserNameForAvatar(userName));

        userName = "元 发";
        assertEquals("元", Util.getUserNameForAvatar(userName));

    }

    @Test
    public void testMagnet() {

        String url = "magnet:?xt=urn:btih:56d89ae3ee58595803d51b6be911ffa6de9adf1a&tr=udp://9.rarbg.to:2710/announce&tr=udp://9.rarbg.me:2710/announce&tr=http://tr.cili001.com:8070/announce&tr=http://tracker.trackerfix.com:80/announce&tr=udp://open.demonii.com:1337&tr=udp://tracker.opentrackr.org:1337/announce&tr=udp://p4p.arenabg.com:1337&tr=wss://tracker.openwebtorrent.com&tr=wss://tracker.btorrent.xyz&tr=wss://tracker.fastcast.nz";

        assertFalse((url.length() < 60 || url.startsWith("magnet:\\?xt=urn:btih:")));

    }

    @Test
    public void testFormatTime() {

        CharSequence time = DateFormat.format("yyyy.MM.dd", new Date(1513332835525L));

        assertEquals("2017.12.15", time.toString());

    }

    @Test
    public void testCalcProgress() {

        long downloaded = 7771362L;
        long length = 9550832L;


        double result = ((double) downloaded / length) * 100;

        assertEquals("81%", (int) result + "%");

    }

    @Test
    public void testCompareStr() {

        String a = "1.0.6";
        String b = "1.0.14";

        assertTrue(Util.compareVersion(a, b) < 0);

        String a2 = "1.0.6";
        String b2 = "0.9.4";

        assertTrue(Util.compareVersion(a2, b2) > 0);

        String a3 = "1.0.14";
        String b3 = "1.0.14";

        assertTrue(Util.compareVersion(a3, b3) == 0);

    }

    @Test
    public void testFilter() {

        final String filterStr = "filterStr";

        List<String> list = new ArrayList<>();
        list.add(filterStr);
        list.add("f");

        List<String> result = ItemFilterKt.filterItem(list, new FilterRule<String>() {
            @Override
            public boolean isFiltered(String item) {
                return item.equals(filterStr);
            }
        });

        assertEquals(filterStr, result.get(0));
        assertEquals(1,result.size());

    }

}