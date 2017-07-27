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

/*        String json = "[\n" +
                "   {\n" +
                "    \"digest\": \"ceeb92546f72b949f629995edeadf64ef5a4cf28aa3db451f3d82ed233e3ea16\",\n" +
                "    \"type\": \"JPEG\",\n" +
                "    \"format\": \"JPEG\",\n" +
                "    \"width\": 1601,\n" +
                "    \"height\": 1601,\n" +
                "    \"size\": 321176,\n" +
                "    \"sharing\": 1\n" +
                "  },\n" +
                "  {\n" +
                "    \"digest\": \"65fac2a5c61906c851727076cc25d2da54d0a908ec492b8307be595f83bb7705\",\n" +
                "    \"type\": \"JPEG\",\n" +
                "    \"format\": \"JPEG\",\n" +
                "    \"width\": 3024,\n" +
                "    \"height\": 4032,\n" +
                "    \"exifDateTime\": \"2016:10:13 16:42:16\",\n" +
                "    \"exifMake\": \"LGE\",\n" +
                "    \"exifModel\": \"Nexus 5X\",\n" +
                "    \"size\": 2204833,\n" +
                "    \"sharing\": 1\n" +
                "  },\n" +
                "  {\n" +
                "    \"digest\": \"08b8be04f9802c6b6547dfd72bf9add7ed58ab70fb1aad499212c6bb80c12455\",\n" +
                "    \"type\": \"JPEG\",\n" +
                "    \"format\": \"JPEG\",\n" +
                "    \"width\": 3264,\n" +
                "    \"height\": 2448,\n" +
                "    \"exifOrientation\": 6,\n" +
                "    \"exifDateTime\": \"2016:08:08 11:43:49\",\n" +
                "    \"exifMake\": \"SAMSUNG\",\n" +
                "    \"exifModel\": \"GT-N7100\",\n" +
                "    \"size\": 1208081,\n" +
                "    \"sharing\": 1\n" +
                "  }" +
                "]";*/

        String json = "[\n" +
                "  [\n" +
                "    \"01534abee224484fc82892f0d620a35c3a915c6222c878f83a86aca376af7505\",\n" +
                "    {\n" +
                "      \"metadata\": {\n" +
                "        \"format\": \"JPEG\",\n" +
                "        \"width\": 3264,\n" +
                "        \"height\": 2448,\n" +
                "        \"exifOrientation\": 6,\n" +
                "        \"exifDateTime\": \"2016:12:13 13:34:52\",\n" +
                "        \"exifMake\": \"Apple\",\n" +
                "        \"exifModel\": \"iPhone 6 Plus\",\n" +
                "        \"size\": 1786157\n" +
                "      },\n" +
                "      \"permittedToShare\": true\n" +
                "    }\n" +
                "  ],\n" +
                "  [\n" +
                "    \"0232827939051677a2c78e605a4bd3ae5d581e10d1b51105938c907a8ede8a63\",\n" +
                "    {\n" +
                "      \"metadata\": {\n" +
                "        \"format\": \"JPEG\",\n" +
                "        \"width\": 1000,\n" +
                "        \"height\": 669,\n" +
                "        \"exifOrientation\": 1,\n" +
                "        \"exifDateTime\": \"2010:10:10 20:45:31\",\n" +
                "        \"exifMake\": \"NIKON CORPORATION\",\n" +
                "        \"exifModel\": \"NIKON D40X\",\n" +
                "        \"size\": 244642\n" +
                "      },\n" +
                "      \"permittedToShare\": true\n" +
                "    }\n" +
                "  ],\n" +
                "  [\n" +
                "    \"4a669343bad6371b6d6b18405e965e6829c02f282f24c92b24158debe88bb552\",\n" +
                "    {\n" +
                "      \"metadata\": {\n" +
                "        \"format\": \"JPEG\",\n" +
                "        \"width\": 1024,\n" +
                "        \"height\": 680,\n" +
                "        \"exifOrientation\": 1,\n" +
                "        \"exifDateTime\": \"2014:08:31 18:55:03\",\n" +
                "        \"exifMake\": \"SONY\",\n" +
                "        \"exifModel\": \"NEX-3N\",\n" +
                "        \"size\": 241967\n" +
                "      },\n" +
                "      \"permittedToShare\": true\n" +
                "    }\n" +
                "  ]\n" +
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
        assertEquals(media.getUuid(), "01534abee224484fc82892f0d620a35c3a915c6222c878f83a86aca376af7505");
        assertEquals(media.getWidth(), "3264");
        assertEquals(media.getHeight(), "2448");
        assertEquals(media.getTime(), "2016-12-13");
        assertEquals(media.isSharing(), true);

        media = medias.get(1);
        assertEquals(media.getTime(), "2010-10-10");

    }

    @Test
    public void parseRemoteFileFolderTest() {
/*        String json = "[\n" +
                "  {\n" +
                "    \"uuid\": \"5d463eac-f73f-4987-a3e9-bb68fee726e0\",\n" +
                "    \"type\": \"file\",\n" +
                "    \"owner\": [\n" +
                "      \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\"\n" +
                "    ],\n" +
                "    \"name\": \"7685_spec.png\",\n" +
                "    \"mtime\": 1477386652380,\n" +
                "    \"size\": 95516\n" +
                "  },\n" +
                "  {\n" +
                "    \"uuid\": \"62584c06-0799-4d42-97e4-e40fbd563c19\",\n" +
                "    \"type\": \"file\",\n" +
                "    \"owner\": [\n" +
                "      \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\"\n" +
                "    ],\n" +
                "    \"name\": \"ic_appifi_1 (1).png\",\n" +
                "    \"mtime\": 1477386652508,\n" +
                "    \"size\": 1318\n" +
                "  },\n" +
                "  {\n" +
                "    \"uuid\": \"2dad4fe1-41f4-4d4d-b4c7-8d13624a7df5\",\n" +
                "    \"type\": \"file\",\n" +
                "    \"owner\": [\n" +
                "      \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\"\n" +
                "    ],\n" +
                "    \"name\": \"naiveTest.js\",\n" +
                "    \"mtime\": 1477386652624,\n" +
                "    \"size\": 714\n" +
                "  },\n" +
                "  {\n" +
                "    \"uuid\": \"d37368f1-624c-42d7-8c4a-330c94eefcc7\",\n" +
                "    \"type\": \"folder\",\n" +
                "    \"owner\": [\n" +
                "      \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\"\n" +
                "    ],\n" +
                "    \"name\": \"src\"\n" +
                "  }\n" +
                "]";*/

        String json  = "[\n" +
                "  {\n" +
                "    \"uuid\": \"a27ce6f9-580f-4065-9917-29c0765e890a\",\n" +
                "    \"type\": \"file\",\n" +
                "    \"name\": \"1-2 [DOM] 事件捕获.mp4\",\n" +
                "    \"size\": 4789790,\n" +
                "    \"mtime\": 1494318844512\n" +
                "  },\n" +
                "  {\n" +
                "    \"uuid\": \"caabcf69-360c-456a-9ca7-10dab1ba1216\",\n" +
                "    \"type\": \"file\",\n" +
                "    \"name\": \"1-1 [DOM] 事件冒泡.mp4\",\n" +
                "    \"size\": 15550310,\n" +
                "    \"mtime\": 1494318845724\n" +
                "  },\n" +
                "  {\n" +
                "    \"uuid\": \"72315ee9-0154-4536-a839-cfe52f72ca2f\",\n" +
                "    \"type\": \"file\",\n" +
                "    \"name\": \"4-2 [DOM事件] QQ面板拖拽效果（下）.mp4\",\n" +
                "    \"size\": 125267353,\n" +
                "    \"mtime\": 1494318899467\n" +
                "  },\n" +
                "  {\n" +
                "    \"uuid\": \"e8b9ee4b-55be-4f0c-ba9e-570b0766c374\",\n" +
                "    \"type\": \"file\",\n" +
                "    \"name\": \"4-3 [DOM事件] QQ面板状态切换效果.mp4\",\n" +
                "    \"size\": 134264485,\n" +
                "    \"mtime\": 1494318900643\n" +
                "  }\n" +
                "]";

        RemoteDatasParser<AbstractRemoteFile> remoteDatasParser = new RemoteFileFolderParser();
        List<AbstractRemoteFile> abstractRemoteFiles = new ArrayList<>();
        try {
            abstractRemoteFiles = remoteDatasParser.parse(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AbstractRemoteFile abstractRemoteFile = abstractRemoteFiles.get(0);
        assertEquals(abstractRemoteFile.getTime(), "1494318844512");
        assertEquals(abstractRemoteFile.getUuid(), "a27ce6f9-580f-4065-9917-29c0765e890a");
        assertEquals(abstractRemoteFile.getName(), "1-2 [DOM] 事件捕获.mp4");
        assertEquals(abstractRemoteFile.getSize(), "4789790");

    }

    @Test
    public void parseRemoteFileShareTest() {
        String json = "[\n" +
                "  {\n" +
                "    \"uuid\": \"ed1d9638-8130-4077-9ed8-05be641a9ab4\",\n" +
                "    \"type\": \"folder\",\n" +
                "    \"owner\": [\n" +
                "      \"e5f23cb9-1852-475d-937d-162d2554e22c\"\n" +
                "    ],\n" +
                "    \"writelist\": [],\n" +
                "    \"readlist\": [\n" +
                "      \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\"\n" +
                "    ],\n" +
                "    \"mtime\": 1476343620053,\n" +
                "    \"root\": \"ed1d9638-8130-4077-9ed8-05be641a9ab4\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"uuid\": \"4d807647-0feb-4692-aea4-4eaf26232916\",\n" +
                "    \"type\": \"folder\",\n" +
                "    \"owner\": [\n" +
                "      \"831b5cc9-6a14-4a4f-b1b6-666c5b282783\"\n" +
                "    ],\n" +
                "    \"writelist\": [\n" +
                "      \"e5f23cb9-1852-475d-937d-162d2554e22c\"\n" +
                "    ],\n" +
                "    \"readlist\": [\n" +
                "      \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\"\n" +
                "    ],\n" +
                "    \"mtime\": 1476343620053,\n" +
                "    \"root\": \"4d807647-0feb-4692-aea4-4eaf26232916\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"uuid\": \"bc097836-b056-46ef-862c-e0423e440b4c\",\n" +
                "    \"type\": \"folder\",\n" +
                "    \"owner\": [\n" +
                "      \"278a60cf-2ba3-4eab-8641-e9a837c12950\"\n" +
                "    ],\n" +
                "    \"writelist\": [\n" +
                "      \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\",\n" +
                "      \"e5f23cb9-1852-475d-937d-162d2554e22c\"\n" +
                "    ],\n" +
                "    \"readlist\": [],\n" +
                "    \"mtime\": 1476343620053,\n" +
                "    \"root\": \"bc097836-b056-46ef-862c-e0423e440b4c\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"uuid\": \"ec374b5a-490c-47ea-9a33-cb9ae1103b3b\",\n" +
                "    \"type\": \"folder\",\n" +
                "    \"owner\": [\n" +
                "      \"bc53b2f7-045b-4e86-91b9-9b5731489a13\"\n" +
                "    ],\n" +
                "    \"writelist\": [\n" +
                "      \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\",\n" +
                "      \"e5f23cb9-1852-475d-937d-162d2554e22c\"\n" +
                "    ],\n" +
                "    \"readlist\": [\n" +
                "      \"1f4faecf-1bb5-4ff1-ab41-bd44a0cd0809\",\n" +
                "      \"3908afee-0818-4a3e-b327-76c2578ecb80\"\n" +
                "    ],\n" +
                "    \"mtime\": 1476343620053,\n" +
                "    \"root\": \"ec374b5a-490c-47ea-9a33-cb9ae1103b3b\"\n" +
                "  }\n" +
                "]";

        RemoteDatasParser<AbstractRemoteFile> parser = new RemoteFileShareParser();
        List<AbstractRemoteFile> abstractRemoteFiles = new ArrayList<>();
        try {
            abstractRemoteFiles = parser.parse(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AbstractRemoteFile abstractRemoteFile = abstractRemoteFiles.get(0);
        assertEquals(abstractRemoteFile.getUuid(), "ed1d9638-8130-4077-9ed8-05be641a9ab4");
        assertEquals(abstractRemoteFile.getName(), "");
        assertEquals(abstractRemoteFile.getTime(), "1476343620053");
        assertEquals(abstractRemoteFile.getWriteList().size(), 0);
        assertEquals(abstractRemoteFile.getReadList().get(0), "5da92303-33a1-4f79-8d8f-a7b6becde6c3");
    }

}
