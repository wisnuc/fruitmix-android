package com.winsun.fruitmix.equipment;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.parser.RemoteEquipmentHostAliasParser;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/17.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class RemoteEquipmentHostAliasParserTest {

    private RemoteEquipmentHostAliasParser parser;

    @Before
    public void setUp(){

        parser = new RemoteEquipmentHostAliasParser();

    }

    @Test
    public void test_parseData(){

        String data = "[{\n" +
                "\t\"ipv4\":\"10.10.9.84\"\n" +
                "}]";

        try {
            List<String> hosts = parser.parse(data);

            assertEquals("10.10.9.84", hosts.get(0));

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


}
