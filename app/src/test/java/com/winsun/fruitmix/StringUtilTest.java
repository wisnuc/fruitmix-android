package com.winsun.fruitmix;

import static org.junit.Assert.*;

import org.junit.Test;

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

}
