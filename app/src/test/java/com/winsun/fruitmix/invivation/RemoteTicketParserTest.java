package com.winsun.fruitmix.invivation;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.parser.RemoteTicketParser;

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
public class RemoteTicketParserTest {

    private RemoteTicketParser parser;

    @Before
    public void setup(){

        parser = new RemoteTicketParser();

    }

    @Test
    public void test_parse_data(){

        String data = "{\n" +
                "    \"url\": \"/v1/tickets/373e3985-1ebe-46a1-a67b-9b2c7924d35c\",\n" +
                "    \"ticket\": {\n" +
                "        \"id\": \"373e3985-1ebe-46a1-a67b-9b2c7924d35c\",\n" +
                "        \"status\": 0,\n" +
                "        \"stationId\": \"1b58af44-38ff-47f0-aeda-0a2d39184481\",\n" +
                "        \"data\": \"123456\",\n" +
                "        \"creator\": \"8fb13ae4-c98f-4104-80eb-a14970866abe\",\n" +
                "        \"type\": 1,\n" +
                "        \"updatedAt\": \"2017-07-14T08:00:38.000Z\",\n" +
                "        \"createdAt\": \"2017-07-14T08:00:38.000Z\"\n" +
                "    }\n" +
                "}";

        try {
            String ticket = parser.parse(data);

            assertEquals("/v1/tickets/373e3985-1ebe-46a1-a67b-9b2c7924d35c", ticket);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



}
