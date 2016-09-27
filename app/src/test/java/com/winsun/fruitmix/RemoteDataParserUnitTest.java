package com.winsun.fruitmix;

import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.parser.RemoteDataParser;

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
                "  {\n" +
                "    \"hash\": \"820cf6f1e3efa5007f36909df9822ee4966d8bedeae12ab73f647d0db45df06d\",\n" +
                "    \"kind\": \"image\",\n" +
                "    \"type\": \"jpg\",\n" +
                "    \"height\": 200,\n" +
                "    \"width\": 200,\n" +
                "    \"detail\": {}\n" +
                "  },\n" +
                "  {\n" +
                "    \"hash\": \"1141f04e565f4e41c4e2eba140715b935d943b4f3c392dd1fa008edb427e8026\",\n" +
                "    \"kind\": \"image\",\n" +
                "    \"type\": \"jpg\",\n" +
                "    \"height\": 200,\n" +
                "    \"width\": 200,\n" +
                "    \"detail\": {}\n" +
                "  },\n" +
                "  {\n" +
                "    \"hash\": \"bd3f40934856999cb872ff8db54b7f953bdcaed094eb4930464a1e7a21ed3c77\",\n" +
                "    \"kind\": \"image\",\n" +
                "    \"type\": \"jpg\",\n" +
                "    \"height\": 200,\n" +
                "    \"width\": 200,\n" +
                "    \"detail\": {}\n" +
                "  },\n" +
                "  {\n" +
                "    \"hash\": \"321920dbf7091dbcc614e4ede30f0f4cdc9b40fe11d7e6d8ed0d1f495c1b0252\",\n" +
                "    \"kind\": \"image\",\n" +
                "    \"type\": \"jpg\",\n" +
                "    \"height\": 200,\n" +
                "    \"width\": 200,\n" +
                "    \"detail\": {}\n" +
                "  },\n" +
                "  {\n" +
                "    \"hash\": \"2cb17a7e37e48e5a320fe7eff2a8a568d48bf027a1117b553848641f743c1834\",\n" +
                "    \"kind\": \"image\",\n" +
                "    \"type\": \"jpg\",\n" +
                "    \"height\": 200,\n" +
                "    \"width\": 200,\n" +
                "    \"detail\": {}\n" +
                "  }]";

        ParserFactory<Media> parserFactory = new MediaDataParserFactory();
        RemoteDataParser<Media> remoteDataParser = parserFactory.createRemoteDataParser();

        List<Media> medias = remoteDataParser.parse(json);

        assertFalse(medias.size() == 0);
    }

    @Test
    public void parseRemoteShareTest() {
        String json = "[\n" +
                "  {\n" +
                "    \"uuid\": \"6417e99e-1aa3-4e2a-88ff-58f000aafb55\",\n" +
                "    \"latest\": {\n" +
                "      \"_id\": \"57a821a51c6d7a2500b469e7\",\n" +
                "      \"docversion\": \"1.0\",\n" +
                "      \"creator\": \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\",\n" +
                "      \"album\": true,\n" +
                "      \"sticky\": false,\n" +
                "      \"archived\": false,\n" +
                "      \"mtime\": 1470636453734,\n" +
                "      \"contents\": [\n" +
                "        {\n" +
                "          \"type\": \"media\",\n" +
                "          \"digest\": \"a1109f6870507048e8b4b87c870b958d80d3c5c80ffef5aceeb93647387bd793\",\n" +
                "          \"ctime\": 1470636453734,\n" +
                "          \"creator\": \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"tags\": [\n" +
                "        {\n" +
                "          \"albumname\": \"未命名 2016-08-08\",\n" +
                "          \"desc\": \"\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"viewers\": [\n" +
                "        \"\"\n" +
                "      ],\n" +
                "      \"maintainers\": [\n" +
                "        \"062446c8-e57b-4834-8c71-390552667766\"\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"uuid\": \"b939f3b6-537b-40c4-ae31-1b3144d3978b\",\n" +
                "    \"latest\": {\n" +
                "      \"_id\": \"57ad80871c6d7a2500b46a31\",\n" +
                "      \"docversion\": \"1.0\",\n" +
                "      \"creator\": \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\",\n" +
                "      \"album\": false,\n" +
                "      \"sticky\": false,\n" +
                "      \"archived\": false,\n" +
                "      \"mtime\": 1470988423506,\n" +
                "      \"contents\": [\n" +
                "        {\n" +
                "          \"type\": \"media\",\n" +
                "          \"digest\": \"7ea0425b90f8fe7b47a1e20168848be02dd7e9bf463c4ad2d8acf1c49a00ff4a\",\n" +
                "          \"ctime\": 1470988423506,\n" +
                "          \"creator\": \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"media\",\n" +
                "          \"digest\": \"848f4ffa5e4d444fd2defe700aaed90952dacfc7bc047963879bcc7cd8028b05\",\n" +
                "          \"ctime\": 1470988423506,\n" +
                "          \"creator\": \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"media\",\n" +
                "          \"digest\": \"7a344a3bc77816143ca955365e508a4a5fa4e6a1d046f4e0ff1f5e344703d050\",\n" +
                "          \"ctime\": 1470988423506,\n" +
                "          \"creator\": \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"tags\": [\n" +
                "        null\n" +
                "      ],\n" +
                "      \"viewers\": [\n" +
                "        \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\",\n" +
                "        \"cfa09650-e370-4cfa-8691-7160dae07098\"\n" +
                "      ],\n" +
                "      \"maintainers\": [\n" +
                "        \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\"\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"uuid\": \"0761de51-1f37-44da-a101-d7ed34aa15da\",\n" +
                "    \"latest\": {\n" +
                "      \"_id\": \"57ba69001c6d7a2500b46c53\",\n" +
                "      \"docversion\": \"1.0\",\n" +
                "      \"creator\": \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\",\n" +
                "      \"album\": true,\n" +
                "      \"sticky\": false,\n" +
                "      \"archived\": true,\n" +
                "      \"mtime\": 1471834368241,\n" +
                "      \"contents\": [\n" +
                "        {\n" +
                "          \"creator\": \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\",\n" +
                "          \"ctime\": 1470989158383,\n" +
                "          \"digest\": \"ad0be5e28b0158a29475c4eb5830f6346f9e458440d508262e52e31e6817a1f8\",\n" +
                "          \"type\": \"media\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"tags\": [\n" +
                "        {\n" +
                "          \"albumname\": \"未命名 2016-08-12\",\n" +
                "          \"desc\": \"\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"viewers\": [],\n" +
                "      \"maintainers\": [\n" +
                "        \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\"\n" +
                "      ]\n" +
                "    }\n" +
                "  }]";

        ParserFactory<MediaShare> shareParserFactory = new MediaShareDataParserFactory();
        RemoteDataParser<MediaShare> shareParser = shareParserFactory.createRemoteDataParser();

        List<MediaShare> shares = shareParser.parse(json);

        assertFalse(shares.size() == 0);

    }

    @Test
    public void parseRemoteUserTest() {

        String json = "[\n" +
                "  {\n" +
                "    \"username\": \"admin\",\n" +
                "    \"uuid\": \"77c6abe4-f7cd-46b3-80c7-ff08aa37742e\",\n" +
                "    \"avatar\": \"defaultAvatar.jpg\",\n" +
                "    \"isAdmin\": true,\n" +
                "    \"isFirstUser\": true,\n" +
                "    \"type\": \"user\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"username\": \"1\",\n" +
                "    \"uuid\": \"cfa09650-e370-4cfa-8691-7160dae07098\",\n" +
                "    \"avatar\": \"defaultAvatar.jpg\",\n" +
                "    \"email\": \"1\",\n" +
                "    \"isAdmin\": false,\n" +
                "    \"isFirstUser\": false,\n" +
                "    \"type\": \"user\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"username\": \"2\",\n" +
                "    \"uuid\": \"e1993040-d40f-4b4a-abad-0d1adb71bd9b\",\n" +
                "    \"avatar\": \"defaultAvatar.jpg\",\n" +
                "    \"email\": \"\",\n" +
                "    \"isAdmin\": false,\n" +
                "    \"isFirstUser\": false,\n" +
                "    \"type\": \"user\"\n" +
                "  }\n" +
                "]";

        ParserFactory<User> parserFactory = new UserDataParserFactory();
        RemoteDataParser<User> parser = parserFactory.createRemoteDataParser();

        List<User> users = parser.parse(json);

        assertFalse(users.size() == 0);

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

        ParserFactory<Comment> parserFactory = new MediaCommentDataParserFactory();
        RemoteDataParser<Comment> remoteDataParser = parserFactory.createRemoteDataParser();

        List<Comment> comments = remoteDataParser.parse(json);


        assertFalse(comments.size() == 0);
    }


}
