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
    public void testCreateBodyUsingQueryStr() {

        String httpPath = "/media/5669da130c25ccb372d11c916b928d561517c04bbac9e0d9c2c7c0713af06240?alt=thumbnail&width=64&height=64&autoOrient=true&moidifier=caret";

        String[] splitResult = httpPath.split("\\?");

        assertEquals("/media/5669da130c25ccb372d11c916b928d561517c04bbac9e0d9c2c7c0713af06240", splitResult[0]);

        String[] queryStrs = splitResult[1].split("&");

        String[] queryStrSplitResult = queryStrs[0].split("=");

        assertEquals("alt",queryStrSplitResult[0]);
        assertEquals("thumbnail",queryStrSplitResult[1]);

        queryStrSplitResult = queryStrs[1].split("=");

        assertEquals("width",queryStrSplitResult[0]);
        assertEquals("64",queryStrSplitResult[1]);

    }


}
