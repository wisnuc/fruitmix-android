package com.winsun.fruitmix;

import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteDatasParser;
import com.winsun.fruitmix.parser.RemoteFileFolderParser;
import com.winsun.fruitmix.parser.RemoteMediaParser;
import com.winsun.fruitmix.parser.RemoteMkDirParser;
import com.winsun.fruitmix.parser.RemoteStationsCallByCloudAPIParser;
import com.winsun.fruitmix.stations.Station;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/2.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class RemoteDatasParserUnitTest {


    @Test
    public void parseRemoteFileFolderTest() {

        String json = "{\n" +
                "    \"path\": [\n" +
                "        {\n" +
                "            \"uuid\": \"a02f2c82-89d5-40f4-84ee-1188d3ac8812\",\n" +
                "            \"name\": \"a02f2c82-89d5-40f4-84ee-1188d3ac8812\",\n" +
                "            \"mtime\": 1502960704221\n" +
                "        }\n" +
                "    ],\n" +
                "    \"entries\": [\n" +
                "        {\n" +
                "            \"uuid\": \"1ed05292-5e98-4fb4-9307-e4e561411af1\",\n" +
                "            \"type\": \"file\",\n" +
                "            \"name\": \"e9ccf83b68ff0a2e4664.hot-update.json\",\n" +
                "            \"mtime\": 1502960620288,\n" +
                "            \"size\": 43,\n" +
                "            \"magic\": 0,\n" +
                "            \"hash\": \"7ea403dbe101816b7a3ecca68dba6da3ac2939df2958f88d82fad29daedcd5bd\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"uuid\": \"cb4ee48f-89cb-48dc-a7fb-33b9b8bba9af\",\n" +
                "            \"type\": \"file\",\n" +
                "            \"name\": \"芭蕾-唐吉軻德-女主角.jpg\",\n" +
                "            \"mtime\": 1502960666844,\n" +
                "            \"size\": 62162,\n" +
                "            \"magic\": \"JPEG\",\n" +
                "            \"hash\": \"d2f9622b7e064cfcf51caa70fc08cc63a9e06591f98b5b2e85bd0880aaf8594c\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"uuid\": \"d0d771ce-7784-443e-8746-04f1be89fb49\",\n" +
                "            \"type\": \"file\",\n" +
                "            \"name\": \"陈天天.JPG\",\n" +
                "            \"mtime\": 1502960704213,\n" +
                "            \"size\": 4890773,\n" +
                "            \"magic\": \"JPEG\",\n" +
                "            \"hash\": \"621926b371ea3cfa246b64394c511459131f1ae39de243300ae3dc98682a4bbc\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        RemoteDatasParser<AbstractRemoteFile> remoteDatasParser = new RemoteFileFolderParser();
        List<AbstractRemoteFile> abstractRemoteFiles = new ArrayList<>();
        try {
            abstractRemoteFiles = remoteDatasParser.parse(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AbstractRemoteFile abstractRemoteFile = abstractRemoteFiles.get(0);
        assertEquals(1502960620288L, abstractRemoteFile.getTime());
        assertEquals("1ed05292-5e98-4fb4-9307-e4e561411af1", abstractRemoteFile.getUuid());
        assertEquals("e9ccf83b68ff0a2e4664.hot-update.json", abstractRemoteFile.getName());
        assertEquals(43, abstractRemoteFile.getSize());

    }

    @Test
    public void parseRemoteStations() {

        String json = "{\n" +
                "    \"code\": 200,\n" +
                "    \"message\": \"ok\",\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"id\": \"9bfad174-151e-4e59-bb26-6e80e7790b24\",\n" +
                "            \"name\": \"station_1506478105047\",\n" +
                "            \"LANIP\": [\n" +
                "                \"10.10.9.170\"\n" +
                "            ],\n" +
                "            \"isOnline\": true\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"278ac47c-7cce-4fd4-a6b6-bf0f996157fa\",\n" +
                "            \"name\": \"HomeStation\",\n" +
                "            \"LANIP\": [\n" +
                "                \"10.10.9.189\"\n" +
                "            ],\n" +
                "            \"isOnline\": false\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        RemoteDatasParser<Station> parser = new RemoteStationsCallByCloudAPIParser();

        List<Station> stations = new ArrayList<>();

        try {
            stations = parser.parse(json);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Station station = stations.get(0);

        assertEquals("10.10.9.170", station.getFirstIp());

    }

    @Test
    public void parseRemoteMkDirResult() {

        String json = "{\"code\":200,\"message\":\"ok\",\"data\":{\"uuid\":\"3269d04b-6646-438d-80e4-4026c752e28e\",\"type\":\"directory\",\"name\":\"来自Huawei-Nexus 6P\",\"mtime\":1506505327067}}";

        RemoteDataParser<AbstractRemoteFile> parser = new RemoteMkDirParser();

        try {
            AbstractRemoteFile file = parser.parse(json);

            assertEquals("3269d04b-6646-438d-80e4-4026c752e28e", file.getUuid());

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static final String localMkdirResult = "[{\"number\":0,\"op\":\"mkdir\",\"name\":\"来自Huawei-Nexus 6P\",\"data\":{\"uuid\":\"9b174a5f-9db5-4599-9694-3e0d5115ae44\",\"type\":\"directory\",\"name\":\"来自Huawei-Nexus 6P\",\"mtime\":1506509766789}}]";

    @Test
    public void parseLocalMkDirResult(){

        String json = localMkdirResult;

        RemoteDataParser<AbstractRemoteFile> parser = new RemoteMkDirParser();

        try {
            AbstractRemoteFile file = parser.parse(json);

            assertEquals("9b174a5f-9db5-4599-9694-3e0d5115ae44", file.getUuid());

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



}
