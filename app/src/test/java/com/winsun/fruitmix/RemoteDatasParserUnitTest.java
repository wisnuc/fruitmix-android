package com.winsun.fruitmix;

import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.parser.RemoteDatasParser;
import com.winsun.fruitmix.parser.RemoteFileFolderParser;
import com.winsun.fruitmix.parser.RemoteFileShareParser;
import com.winsun.fruitmix.parser.RemoteMediaParser;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.junit.Ignore;
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
    public void parseRemoteMediaTest() {

        String json = "[\n" +
                "{\n" +
                "        \"hash\": \"91e4ff515f445c16f2b5697040c79b980396248c057ea7e04ad761e635cae062\",\n" +
                "        \"m\": \"JPEG\",\n" +
                "        \"w\": 5203,\n" +
                "        \"h\": 3451,\n" +
                "        \"datetime\": \"2016:09:25 17:54:53\",\n" +
                "        \"make\": \"Canon\",\n" +
                "        \"model\": \"Canon EOS 5D Mark III\",\n" +
                "        \"size\": 4676424\n" +
                "    },\n" +
                "    {\n" +
                "        \"hash\": \"d2f9622b7e064cfcf51caa70fc08cc63a9e06591f98b5b2e85bd0880aaf8594c\",\n" +
                "        \"m\": \"JPEG\",\n" +
                "        \"w\": 707,\n" +
                "        \"h\": 1200,\n" +
                "        \"size\": 62162\n" +
                "    },\n" +
                "    {\n" +
                "        \"hash\": \"621926b371ea3cfa246b64394c511459131f1ae39de243300ae3dc98682a4bbc\",\n" +
                "        \"m\": \"JPEG\",\n" +
                "        \"w\": 5184,\n" +
                "        \"h\": 3456,\n" +
                "        \"orient\": 1,\n" +
                "        \"datetime\": \"2016:07:21 10:07:27\",\n" +
                "        \"make\": \"Canon\",\n" +
                "        \"model\": \"Canon EOS 600D\",\n" +
                "        \"size\": 4890773\n" +
                "    },\n" +
                "    {\n" +
                "        \"hash\": \"866688c5a93a2a9f0dfc16c6bbee0c6ecf3fa442a8d173be261ae721f3f7856f\",\n" +
                "        \"m\": \"JPEG\",\n" +
                "        \"w\": 5184,\n" +
                "        \"h\": 3456,\n" +
                "        \"orient\": 1,\n" +
                "        \"datetime\": \"2016:07:15 14:43:36\",\n" +
                "        \"make\": \"Canon\",\n" +
                "        \"model\": \"Canon EOS 600D\",\n" +
                "        \"size\": 5021723\n" +
                "    }" +
                "]";

        RemoteDatasParser<Media> remoteDatasParser = new RemoteMediaParser();

        List<Media> medias = new ArrayList<>();
        try {
            medias = remoteDatasParser.parse(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assertFalse(medias.size() == 0);

        Media media = medias.get(0);
        assertEquals("91e4ff515f445c16f2b5697040c79b980396248c057ea7e04ad761e635cae062", media.getUuid());
        assertEquals("5203", media.getWidth());
        assertEquals("3451", media.getHeight());
        assertEquals("2016-09-25", media.getTime());

    }

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
        assertEquals("1502960620288", abstractRemoteFile.getTime());
        assertEquals("1ed05292-5e98-4fb4-9307-e4e561411af1", abstractRemoteFile.getUuid());
        assertEquals("e9ccf83b68ff0a2e4664.hot-update.json", abstractRemoteFile.getName());
        assertEquals("43", abstractRemoteFile.getSize());

    }


}
