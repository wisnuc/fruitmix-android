package com.winsun.fruitmix;

import com.winsun.fruitmix.business.LoadTokenParam;
import com.winsun.fruitmix.data.dataOperationResult.DeviceIDLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.FileSharesLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.FilesLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.MediaSharesLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.MediasLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.OperateMediaShareResult;
import com.winsun.fruitmix.data.dataOperationResult.OperateUserResult;
import com.winsun.fruitmix.data.dataOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.UsersLoadOperationResult;
import com.winsun.fruitmix.data.server.ServerDataSource;
import com.winsun.fruitmix.fileModule.interfaces.FileDownloadUploadInterface;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.http.OkHttpUtil;
import com.winsun.fruitmix.http.retrofit.RetrofitInstance;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.model.EquipmentAlias;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.List;

import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import static org.hamcrest.core.Is.is;

/**
 * Created by Administrator on 2017/4/6.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class ServerDataSourceTest {

    @Mock
    private OkHttpUtil okHttpUtil;

    @Mock
    private RetrofitInstance retrofitInstance;

    private ServerDataSource serverDataSource;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        serverDataSource = ServerDataSource.getInstance(okHttpUtil, retrofitInstance);

    }

    @After
    public void destroy() {
        serverDataSource.destroyInstance();
    }

    @Test
    public void loadEquipmentAlias_retrieveEquipmentAlias() {

        HttpResponse httpResponse = new HttpResponse(200, "[\n" +
                "  {\n" +
                "    \"dev\": \"enp1s12f0\",\n" +
                "    \"mac\": \"00:1e:33:ed:1a:35\",\n" +
                "    \"ipv4\": \"192.168.5.234\"\n" +
                "  }\n" +
                "]");

        try {
            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            List<EquipmentAlias> equipmentAliases = serverDataSource.loadEquipmentAlias("");

            assertEquals(1, equipmentAliases.size());

            assertEquals("192.168.5.234", equipmentAliases.get(0).getIpv4());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void loadRemoteUser_retrieveRemoteUser() {

        HttpResponse httpResponse = new HttpResponse(200, "[]");

        try {
            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            List<User> users = serverDataSource.loadRemoteUserByLoginApi("");

            assertEquals(0, users.size());

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

            httpResponse = new HttpResponse(200, json);

            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            users = serverDataSource.loadRemoteUserByLoginApi("");

            assertFalse(users.size() == 0);

            User user = users.get(0);
            assertEquals("5da92303-33a1-4f79-8d8f-a7b6becde6c3", user.getUuid());
            assertEquals("Alice", user.getUserName());
            assertEquals("b9aa7c34-8b86-4306-9042-396cf8fa1a9c", user.getHome());
            assertEquals("f97f9e1f-848b-4ed4-bd47-1ddfa82b2777", user.getLibrary());
            assertThat(user.isAdmin(), is(true));


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void insertRemoteUser_verifyResult() {

        HttpResponse httpResponse = new HttpResponse(404, "");

        try {
            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            OperateUserResult result = serverDataSource.insertRemoteUser("", "");

            assertEquals(OperationResultType.NETWORK_EXCEPTION, result.getOperationResult().getOperationResultType());

            httpResponse = new HttpResponse(200, "  {\n" +
                    "    \"uuid\": \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\",\n" +
                    "    \"username\": \"Alice\",\n" +
                    "    \"avatar\": null,\n" +
                    "    \"email\": null,\n" +
                    "    \"isFirstUser\": true,\n" +
                    "    \"isAdmin\": true,\n" +
                    "    \"home\": \"b9aa7c34-8b86-4306-9042-396cf8fa1a9c\",\n" +
                    "    \"library\": \"f97f9e1f-848b-4ed4-bd47-1ddfa82b2777\"\n" +
                    "  },\n");

            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            result = serverDataSource.insertRemoteUser("", "");

            User user = result.getUser();

            assertEquals("5da92303-33a1-4f79-8d8f-a7b6becde6c3", user.getUuid());
            assertEquals("Alice", user.getUserName());
            assertEquals("b9aa7c34-8b86-4306-9042-396cf8fa1a9c", user.getHome());
            assertEquals("f97f9e1f-848b-4ed4-bd47-1ddfa82b2777", user.getLibrary());
            assertThat(user.isAdmin(), is(true));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void insertRemoteMediaShare_verifyResult() {

        HttpResponse httpResponse = new HttpResponse(404, "");

        MediaShare mediashare = new MediaShare();

        try {
            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            OperateMediaShareResult result = serverDataSource.insertRemoteMediaShare(mediashare);

            assertEquals(OperationResultType.NETWORK_EXCEPTION, result.getOperationResult().getOperationResultType());

            httpResponse = new HttpResponse(200, "  {\n" +
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
                    "  },\n");

            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            result = serverDataSource.insertRemoteMediaShare(mediashare);

            mediashare = result.getMediaShare();

            assertThat(mediashare.isSticky(), is(true));
            assertEquals("afd6d9f46d5284d5b9153e5807e6d2d7e07757a676515a78ce219aed0f09bdd7", mediashare.getShareDigest());


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void insertLocalMedia_verifyResult() {

        when(okHttpUtil.uploadFile(any(HttpRequest.class), any(Media.class))).thenReturn(false);

        Media media = new Media();
        media.setUuid("");

        OperationResult result = serverDataSource.insertLocalMedia(media);

        assertEquals(OperationResultType.IO_EXCEPTION, result.getOperationResultType());

        when(okHttpUtil.uploadFile(any(HttpRequest.class), any(Media.class))).thenReturn(true);

        result = serverDataSource.insertLocalMedia(media);

        assertEquals(OperationResultType.SUCCEED, result.getOperationResultType());

    }

    @Test
    public void modifyMediaShare_verifyResult() {

        HttpResponse httpResponse = new HttpResponse(404, "");

        MediaShare mediashare = new MediaShare();
        mediashare.setUuid("");

        try {
            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            OperationResult result = serverDataSource.modifyRemoteMediaShare("", mediashare);

            assertEquals(OperationResultType.NETWORK_EXCEPTION, result.getOperationResultType());

            httpResponse.setResponseCode(200);

            result = serverDataSource.modifyRemoteMediaShare("", mediashare);

            assertEquals(OperationResultType.SUCCEED, result.getOperationResultType());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void deleteRemoteMediaShare_verifyResult() {

        HttpResponse httpResponse = new HttpResponse(404, "");

        MediaShare mediashare = new MediaShare();
        mediashare.setUuid("");

        try {
            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            OperationResult result = serverDataSource.deleteRemoteMediaShare(mediashare);

            assertEquals(OperationResultType.NETWORK_EXCEPTION, result.getOperationResultType());

            httpResponse.setResponseCode(200);

            result = serverDataSource.deleteRemoteMediaShare(mediashare);

            assertEquals(OperationResultType.SUCCEED, result.getOperationResultType());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void loadDeviceID_verifyResult() {

        HttpResponse httpResponse = new HttpResponse(404, "");

        try {
            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            DeviceIDLoadOperationResult result = serverDataSource.loadDeviceID();

            assertEquals(OperationResultType.NETWORK_EXCEPTION, result.getOperationResult().getOperationResultType());

            httpResponse = new HttpResponse(200, "{\n" +
                    "  \"uuid\": \"d4d768e3-c5f0-4ce3-b4de-d58e84223c88\"\n" +
                    "}");

            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            result = serverDataSource.loadDeviceID();

            String deviceID = "d4d768e3-c5f0-4ce3-b4de-d58e84223c88";

            assertEquals(deviceID, result.getDeviceID());

            assertEquals(deviceID, serverDataSource.getDeviceID());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void loadRemoteUsers_verifyResult() {

        HttpResponse httpResponse = new HttpResponse(404, "");

        try {
            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            UsersLoadOperationResult result = serverDataSource.loadRemoteUsers();

            verify(okHttpUtil).remoteCallMethod(any(HttpRequest.class));

            assertEquals(OperationResultType.NETWORK_EXCEPTION, result.getOperationResult().getOperationResultType());

            httpResponse.setResponseCode(200);

            httpResponse.setResponseData("[\n" +
                    "  {\n" +
                    "    \"type\": \"local\",\n" +
                    "    \"uuid\": \"cd4b312a-1988-4427-9242-c0d5554fa465\",\n" +
                    "    \"username\": \"伊朗\",\n" +
                    "    \"avatar\": null,\n" +
                    "    \"email\": null,\n" +
                    "    \"isAdmin\": true,\n" +
                    "    \"isFirstUser\": true,\n" +
                    "    \"home\": \"37019d94-0df9-4ffb-92eb-29f65eba379a\",\n" +
                    "    \"library\": \"66f9d261-914a-492c-9a6d-64b5ad670cb1\",\n" +
                    "    \"unixUID\": 2000\n" +
                    "  }\n" +
                    "]");

            HttpResponse secondResponse = new HttpResponse(200, "[\n" +
                    "  {\n" +
                    "    \"uuid\": \"cd4b312a-1988-4427-9242-c0d5554fa465\",\n" +
                    "    \"username\": \"伊朗\",\n" +
                    "    \"avatar\": null,\n" +
                    "    \"unixUID\": 2000\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"uuid\": \"afd28dfe-c584-4a1b-8f7f-432ab28b97ee\",\n" +
                    "    \"username\": \"阿尔及利亚\",\n" +
                    "    \"avatar\": null,\n" +
                    "    \"unixUID\": 2001\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"uuid\": \"e15c1cc0-acb6-4393-a939-f908c156ccca\",\n" +
                    "    \"username\": \"塞浦路斯\",\n" +
                    "    \"avatar\": null,\n" +
                    "    \"unixUID\": 2002\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"uuid\": \"71ba95d1-ed32-49b6-80d5-cd66107fd49a\",\n" +
                    "    \"username\": \"斯里兰卡\",\n" +
                    "    \"avatar\": null,\n" +
                    "    \"unixUID\": 2003\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"uuid\": \"8267be69-854d-4f14-bb35-d801cfc144e0\",\n" +
                    "    \"username\": \"乌干达\",\n" +
                    "    \"avatar\": null,\n" +
                    "    \"unixUID\": 2004\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"uuid\": \"96c40eca-6d9d-43e9-87b3-799503a2d1bf\",\n" +
                    "    \"username\": \"留尼旺\",\n" +
                    "    \"avatar\": null,\n" +
                    "    \"unixUID\": 2005\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"uuid\": \"a816a43b-a1a8-4dc2-a91f-4976bf185d69\",\n" +
                    "    \"username\": \"赤道几内亚 \",\n" +
                    "    \"avatar\": null,\n" +
                    "    \"unixUID\": 2006\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"uuid\": \"0a7fd057-006f-489e-a4ef-d73d638ab5ed\",\n" +
                    "    \"username\": \"巴勒斯坦\",\n" +
                    "    \"avatar\": null,\n" +
                    "    \"unixUID\": 2007\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"uuid\": \"8a7083ab-bba6-4399-8237-d86070b75b6b\",\n" +
                    "    \"username\": \"津巴布韦\",\n" +
                    "    \"avatar\": null,\n" +
                    "    \"unixUID\": 2008\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"uuid\": \"52fcef11-7d17-4aac-8028-ad62f4e8c5c1\",\n" +
                    "    \"username\": \"圣马力诺\",\n" +
                    "    \"avatar\": null,\n" +
                    "    \"unixUID\": 2009\n" +
                    "  }\n" +
                    "]");

            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse).thenReturn(secondResponse);

            result = serverDataSource.loadRemoteUsers();

            verify(okHttpUtil, times(3)).remoteCallMethod(any(HttpRequest.class));

            List<User> users = result.getUsers();

            assertEquals(10, users.size());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void loadRemoteMedias_verifyResult() {

        HttpResponse httpResponse = new HttpResponse(404, "");

        try {
            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            MediasLoadOperationResult result = serverDataSource.loadAllRemoteMedias();

            assertEquals(OperationResultType.NETWORK_EXCEPTION, result.getOperationResult().getOperationResultType());

            httpResponse.setResponseCode(200);
            httpResponse.setResponseData("[\n" +
                    "  {\n" +
                    "    \"digest\": \"90c56e640b1113c6f3331b858e41ebdb1c099ded9c5a97901eae8629c8368100\",\n" +
                    "    \"type\": \"JPEG\",\n" +
                    "    \"format\": \"JPEG\",\n" +
                    "    \"width\": 6000,\n" +
                    "    \"height\": 4000,\n" +
                    "    \"exifOrientation\": 1,\n" +
                    "    \"exifDateTime\": \"2015:04:05 10:28:06\",\n" +
                    "    \"exifMake\": \"NIKON CORPORATION\",\n" +
                    "    \"exifModel\": \"NIKON D7100\",\n" +
                    "    \"size\": 6804340,\n" +
                    "    \"sharing\": 1\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"digest\": \"aed6660139810baf9b205257f08bbb76614ba5e8732ad0315cf798db8b5da393\",\n" +
                    "    \"type\": \"JPEG\",\n" +
                    "    \"format\": \"JPEG\",\n" +
                    "    \"width\": 6000,\n" +
                    "    \"height\": 4000,\n" +
                    "    \"exifOrientation\": 1,\n" +
                    "    \"exifDateTime\": \"2015:04:05 10:31:34\",\n" +
                    "    \"exifMake\": \"NIKON CORPORATION\",\n" +
                    "    \"exifModel\": \"NIKON D7100\",\n" +
                    "    \"size\": 7742987,\n" +
                    "    \"sharing\": 1\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"digest\": \"cc2dd66e1420169f0aba803d2e2ca54ead43db0ba63290d4649cca1e666767bd\",\n" +
                    "    \"type\": \"JPEG\",\n" +
                    "    \"format\": \"JPEG\",\n" +
                    "    \"width\": 6000,\n" +
                    "    \"height\": 4000,\n" +
                    "    \"exifOrientation\": 1,\n" +
                    "    \"exifDateTime\": \"2015:04:05 10:32:48\",\n" +
                    "    \"exifMake\": \"NIKON CORPORATION\",\n" +
                    "    \"exifModel\": \"NIKON D7100\",\n" +
                    "    \"size\": 6785961,\n" +
                    "    \"sharing\": 1\n" +
                    "  }" +
                    "]");

            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            result = serverDataSource.loadAllRemoteMedias();

            List<Media> medias = result.getMedias();

            assertNotNull(medias);

            Media media = medias.get(0);

            assertEquals("90c56e640b1113c6f3331b858e41ebdb1c099ded9c5a97901eae8629c8368100", media.getUuid());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void loadRemoteMediaShares_verifyResult() {

        HttpResponse httpResponse = new HttpResponse(404, "");

        try {
            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            MediaSharesLoadOperationResult result = serverDataSource.loadAllRemoteMediaShares();

            assertEquals(OperationResultType.NETWORK_EXCEPTION, result.getOperationResult().getOperationResultType());

            httpResponse.setResponseCode(200);
            httpResponse.setResponseData("[\n" +
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
                    "]");

            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            result = serverDataSource.loadAllRemoteMediaShares();

            List<MediaShare> mediaShares = result.getMediaShares();

            assertNotNull(mediaShares);

            MediaShare mediaShare = mediaShares.get(0);

            assertEquals("0336459b-d541-45d3-8f54-26b64ba9e600", mediaShare.getUuid());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void loadRemoteFolder_verifyResult() {

        HttpResponse httpResponse = new HttpResponse(404, "");

        try {
            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            FilesLoadOperationResult result = serverDataSource.loadRemoteFolder("");

            assertEquals(OperationResultType.NETWORK_EXCEPTION, result.getOperationResult().getOperationResultType());

            httpResponse.setResponseCode(200);
            httpResponse.setResponseData("[\n" +
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
                    "  }\n" +
                    "]");

            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            result = serverDataSource.loadRemoteFolder("");

            List<AbstractRemoteFile> abstractRemoteFiles = result.getFiles();

            assertNotNull(abstractRemoteFiles);

            AbstractRemoteFile abstractRemoteFile = abstractRemoteFiles.get(0);

            assertEquals("5d463eac-f73f-4987-a3e9-bb68fee726e0", abstractRemoteFile.getUuid());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //TODO: mock FileUtil writeResponseBodyToFolder

    @Ignore
    public void downloadRemoteFile_verifyResult() {

        try {

            Retrofit retrofit = Mockito.mock(Retrofit.class);

            FileDownloadUploadInterface fileDownloadUploadInterface = Mockito.mock(FileDownloadUploadInterface.class);

            ResponseBody response = Mockito.mock(ResponseBody.class);

            when(fileDownloadUploadInterface.downloadFile(anyString()).execute().body()).thenReturn(response);

            when(retrofit.create(FileDownloadUploadInterface.class)).thenReturn(fileDownloadUploadInterface);

            when(retrofitInstance.getRetrofitInstance(anyString(), anyString())).thenReturn(retrofit);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void loadRemoteFileRootShares() {

        HttpResponse httpResponse = new HttpResponse(404, "");

        try {
            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            FileSharesLoadOperationResult result = serverDataSource.loadRemoteFileRootShares();

            assertEquals(OperationResultType.NETWORK_EXCEPTION, result.getOperationResult().getOperationResultType());

            httpResponse.setResponseCode(200);
            httpResponse.setResponseData("[]");

            HttpResponse secondHttpResponse = new HttpResponse(200, "[\n" +
                    "  {\n" +
                    "    \"uuid\": \"37019d94-0df9-4ffb-92eb-29f65eba379a\",\n" +
                    "    \"type\": \"folder\",\n" +
                    "    \"owner\": [\n" +
                    "      \"cd4b312a-1988-4427-9242-c0d5554fa465\"\n" +
                    "    ],\n" +
                    "    \"writelist\": [],\n" +
                    "    \"readlist\": [\n" +
                    "      \"5da92303-33a1-4f79-8d8f-a7b6becde6c3\"\n" +
                    "    ],\n" +
                    "    \"mtime\": 1491362936008,\n" +
                    "    \"root\": \"37019d94-0df9-4ffb-92eb-29f65eba379a\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"uuid\": \"66f9d261-914a-492c-9a6d-64b5ad670cb1\",\n" +
                    "    \"type\": \"folder\",\n" +
                    "    \"owner\": [\n" +
                    "      \"cd4b312a-1988-4427-9242-c0d5554fa465\"\n" +
                    "    ],\n" +
                    "    \"writelist\": [],\n" +
                    "    \"readlist\": [],\n" +
                    "    \"mtime\": 1491450564829,\n" +
                    "    \"root\": \"66f9d261-914a-492c-9a6d-64b5ad670cb1\"\n" +
                    "  }\n" +
                    "]");

            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse).thenReturn(secondHttpResponse);

            result = serverDataSource.loadRemoteFileRootShares();

            verify(okHttpUtil, times(3)).remoteCallMethod(any(HttpRequest.class));

            List<AbstractRemoteFile> files = result.getFiles();

            assertNotNull(files);

            assertEquals(1, files.size());

            AbstractRemoteFile file = files.get(0);

            assertEquals("37019d94-0df9-4ffb-92eb-29f65eba379a", file.getUuid());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void loadToken_verifyResult() {

        String gateway = "192.168.5.95";

        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1dWlkIjoiY2Q0YjMxMmEtMTk4OC00NDI3LTkyNDItYzBkNTU1NGZhNDY1In0.CysqwCxOYb8J4h0vyJSpaU7HiyrpS5YRAe2e3zYo8kI";

        LoadTokenParam param = new LoadTokenParam(gateway, "", "");

        HttpResponse httpResponse = new HttpResponse(404, "");

        try {
            when(okHttpUtil.remoteCallMethod(any(HttpRequest.class))).thenReturn(httpResponse);

            TokenLoadOperationResult result = serverDataSource.loadToken(param);

            assertEquals(OperationResultType.NETWORK_EXCEPTION, result.getOperationResult().getOperationResultType());

            httpResponse.setResponseCode(200);
            httpResponse.setResponseData("{\n" +
                    "  \"type\": \"JWT\",\n" +
                    "  \"token\": \"" + token + "\"\n" +
                    "}");

            result = serverDataSource.loadToken(param);

            assertEquals(gateway, serverDataSource.getGateway());

            assertEquals(token, serverDataSource.getToken());

            assertEquals(token, result.getToken());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
