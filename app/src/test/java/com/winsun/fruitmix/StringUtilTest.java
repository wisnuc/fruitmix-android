package com.winsun.fruitmix;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2017/7/27.
 */

public class StringUtilTest {

    @Test
    public void testSplitString() {

        String url = "/v1/tickets/586f0eb6-2db8-47b1-8dc3-0bb13b0c7088";

        String[] result = url.split("/");

        assertEquals("586f0eb6-2db8-47b1-8dc3-0bb13b0c7088", result[result.length - 1]);

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


}
