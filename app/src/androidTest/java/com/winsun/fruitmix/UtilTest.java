package com.winsun.fruitmix;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.winsun.fruitmix.util.Util;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Created by Administrator on 2018/1/23.
 */

@RunWith(AndroidJUnit4.class)
public class UtilTest {

    @Test
    public void testFormatShareTime() {

        long currentTime = 1516699621000L;

        Context context = InstrumentationRegistry.getTargetContext();

        long yearShareTime = 1484154061000L;

        assertEquals("2017年1月12日", Util.formatShareTime(context, yearShareTime,currentTime));

        long monthShareTime = 1515690061000L;

        assertEquals("1月12日", Util.formatShareTime(context, monthShareTime,currentTime));

        long yesterdayShareTime = 1516554061000L;

        assertEquals("昨天 1:01", Util.formatShareTime(context, yesterdayShareTime,currentTime));

        long hourShareTime = 1516676461000L;

        assertEquals("11:01", Util.formatShareTime(context, hourShareTime,currentTime));

        long inHourShareTime = 1516687261000L;

        assertEquals("3小时前", Util.formatShareTime(context, inHourShareTime,currentTime));

        long inMinShareTime = 1516698061000L;

        assertEquals("26分钟前", Util.formatShareTime(context, inMinShareTime,currentTime));

        long twoMinShareTime = 1516699561000L;

        assertEquals("1分钟前", Util.formatShareTime(context, twoMinShareTime,currentTime));

    }

}
