package com.winsun.fruitmix;

import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteMediaCommentParser;
import com.winsun.fruitmix.parser.RemoteMediaParser;
import com.winsun.fruitmix.parser.RemoteMediaShareParser;
import com.winsun.fruitmix.parser.RemoteUserParser;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

/**
 * Created by Administrator on 2016/9/2.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class RemoteDataParserUnitTest {

    @Test
    public void parseRemoteMediaTest() {

        String json = "[\n" +
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
                "]";


        RemoteDataParser<Media> remoteDataParser = new RemoteMediaParser();

        List<Media> medias = remoteDataParser.parse(json);

        assertFalse(medias.size() == 0);

        Media media = medias.get(0);
        assertEquals(media.getUuid(), "ceeb92546f72b949f629995edeadf64ef5a4cf28aa3db451f3d82ed233e3ea16");
        assertEquals(media.getWidth(), "1601");
        assertEquals(media.getHeight(), "1601");
        assertEquals(media.getTime(), "1916-01-01");
        assertEquals(media.isSharing(), true);

        media = medias.get(1);
        assertEquals(media.getTime(), "2016-10-13");

    }

    @Test
    public void parseRemoteShareTest() {
        String json = "[\n" +
                "  {\n" +
                "    \"digest\": \"afd6d9f46d5284d5b9153e5807e6d2d7e07757a676515a78ce219aed0f09bdd7\",\n" +
                "    \"doc\": {\n" +
                "      \"doctype\": \"mediashare\",\n" +
                "      \"docversion\": \"1.0\",\n" +
                "      \"uuid\": \"0336459b-d541-45d3-8f54-26b64ba9e600\",\n" +
                "      \"author\": \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\",\n" +
                "      \"maintainers\": [],\n" +
                "      \"viewers\": [\n" +
                "        \"e5f23cb9-1852-475d-937d-162d2554e22c\"\n" +
                "      ],\n" +
                "      \"album\": {\n" +
                "        \"title\": \"test\",\n" +
                "        \"text\": \"text\"\n" +
                "      },\n" +
                "      \"sticky\": true,\n" +
                "      \"ctime\": 1476242504219,\n" +
                "      \"mtime\": 1476242504219,\n" +
                "      \"contents\": [\n" +
                "        {\n" +
                "          \"author\": \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\",\n" +
                "          \"digest\": \"ceeb92546f72b949f629995edeadf64ef5a4cf28aa3db451f3d82ed233e3ea16\",\n" +
                "          \"time\": 1476242504219\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"digest\": \"81551c323de559e02219ea60246015efcb3b7f738378c7befc8a0d81180f2805\",\n" +
                "    \"doc\": {\n" +
                "      \"doctype\": \"mediashare\",\n" +
                "      \"docversion\": \"1.0\",\n" +
                "      \"uuid\": \"1ea4e86d-a136-46f5-bf7c-ae51a0e6493c\",\n" +
                "      \"author\": \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\",\n" +
                "      \"maintainers\": [],\n" +
                "      \"viewers\": [],\n" +
                "      \"album\": null,\n" +
                "      \"sticky\": false,\n" +
                "      \"ctime\": 1476181623560,\n" +
                "      \"mtime\": 1476181623560,\n" +
                "      \"contents\": [\n" +
                "        {\n" +
                "          \"author\": \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\",\n" +
                "          \"digest\": \"7803e8fa1b804d40d412bcd28737e3ae027768ecc559b51a284fbcadcd0e21be\",\n" +
                "          \"time\": 1476181623559\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"digest\": \"06eb37e9a96d401c32491b2e4af026ecd56e63554c3c86c7e7e207b48bab2c36\",\n" +
                "    \"doc\": {\n" +
                "      \"doctype\": \"mediashare\",\n" +
                "      \"docversion\": \"1.0\",\n" +
                "      \"uuid\": \"eba00fe9-2d26-4c35-9f40-3f3295f5690f\",\n" +
                "      \"author\": \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\",\n" +
                "      \"maintainers\": [],\n" +
                "      \"viewers\": [],\n" +
                "      \"album\": null,\n" +
                "      \"sticky\": false,\n" +
                "      \"ctime\": 1476236841293,\n" +
                "      \"mtime\": 1476236841293,\n" +
                "      \"contents\": [\n" +
                "        {\n" +
                "          \"author\": \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\",\n" +
                "          \"digest\": \"7803e8fa1b804d40d412bcd28737e3ae027768ecc559b51a284fbcadcd0e21be\",\n" +
                "          \"time\": 1476236841293\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "]";

        RemoteDataParser<MediaShare> shareParser = new RemoteMediaShareParser();

        List<MediaShare> shares = shareParser.parse(json);

        assertFalse(shares.size() == 0);

        MediaShare mediaShare = shares.get(0);
        assertEquals(mediaShare.isSticky(), true);
        assertEquals(mediaShare.getShareDigest(), "afd6d9f46d5284d5b9153e5807e6d2d7e07757a676515a78ce219aed0f09bdd7");

    }

    @Test
    public void parseRemoteUserTest() {

        String json = "[\n" +
                "  {\n" +
                "    \"uuid\": \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\",\n" +
                "    \"username\": \"Alice\",\n" +
                "    \"avatar\": null,\n" +
                "    \"email\": null,\n" +
                "    \"isFirstUser\": true,\n" +
                "    \"isAdmin\": true,\n" +
                "    \"home\": \"b9aa7c34-8b86-4306-9042-396cf8fa1a9c\",\n" +
                "    \"library\": \"f97f9e1f-848b-4ed4-bd47-1ddfa82b2777\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"uuid\": \"e5f23cb9-1852-475d-937d-162d2554e22c\",\n" +
                "    \"username\": \"Bob\",\n" +
                "    \"avatar\": null,\n" +
                "    \"email\": null,\n" +
                "    \"isAdmin\": true,\n" +
                "    \"home\": \"ed1d9638-8130-4077-9ed8-05be641a9ab4\",\n" +
                "    \"library\": \"c18aa308-ab32-4e2d-bc34-0c6385711b55\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"uuid\": \"1f4faecf-1bb5-4ff1-ab41-bd44a0cd0809\",\n" +
                "    \"username\": \"Charlie\",\n" +
                "    \"avatar\": null,\n" +
                "    \"email\": null,\n" +
                "    \"home\": \"6bd8cbad-3c7d-4a32-831b-0fadf3c8ef53\",\n" +
                "    \"library\": \"1ec6533f-fab8-4fad-8e76-adc76f80aa2f\"\n" +
                "  }" +
                "]";


        RemoteDataParser<User> parser = new RemoteUserParser();

        List<User> users = parser.parse(json);

        assertFalse(users.size() == 0);

        User user = users.get(0);
        assertEquals(user.getUuid(), "5da92303-33a1-4f79-8d8f-a7b6becde6c3");
        assertEquals(user.getUserName(), "Alice");
        assertEquals(user.getHome(), "b9aa7c34-8b86-4306-9042-396cf8fa1a9c");
        assertEquals(user.getLibrary(), "f97f9e1f-848b-4ed4-bd47-1ddfa82b2777");

    }


    @Test
    public void parseRemoteMediaCommentTest() {
        String json = "[\n" +
                "  {\n" +
                "    \"creator\": \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\",\n" +
                "    \"datatime\": 1472438565983,\n" +
                "    \"text\": \"梵蒂冈\",\n" +
                "    \"shareid\": \"8fdbee30-8ade-4188-890b-04420ff8b00c\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"creator\": \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\",\n" +
                "    \"datatime\": 1472438642033,\n" +
                "    \"text\": \"梵蒂冈\",\n" +
                "    \"shareid\": \"8fdbee30-8ade-4188-890b-04420ff8b00c\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"creator\": \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\",\n" +
                "    \"datatime\": 1472438651423,\n" +
                "    \"text\": \"梵蒂冈\",\n" +
                "    \"shareid\": \"8fdbee30-8ade-4188-890b-04420ff8b00c\"\n" +
                "  }\n" +
                "]";

        RemoteDataParser<Comment> remoteDataParser = new RemoteMediaCommentParser();

        List<Comment> comments = remoteDataParser.parse(json);


        assertFalse(comments.size() == 0);
    }


}
