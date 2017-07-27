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

}
