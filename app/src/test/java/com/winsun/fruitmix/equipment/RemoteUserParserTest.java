package com.winsun.fruitmix.equipment;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.parser.RemoteUserParser;
import com.winsun.fruitmix.user.User;

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
public class RemoteUserParserTest {

    private RemoteUserParser parser;

    @Before
    public void setUp() {

        parser = new RemoteUserParser();

    }

    @Test
    public void test_parseData() {

        String data = "[\n" +
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
                "]";

        try {
            List<User> users = parser.parse(data);

            User user = users.get(0);
            assertEquals("511eecb5-0362-41a2-ac79-624ac5e9c03f", user.getUuid());
            assertEquals("w", user.getUserName());

            user = users.get(1);
            assertEquals("3f2d3d52-d096-4e51-b414-c74f7acb474f", user.getUuid());
            assertEquals("2", user.getUserName());

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}
