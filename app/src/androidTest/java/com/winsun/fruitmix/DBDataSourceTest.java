package com.winsun.fruitmix;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.winsun.fruitmix.data.db.DBDataSource;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.model.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/4/1.
 */

@RunWith(AndroidJUnit4.class)
public class DBDataSourceTest {

    private DBDataSource dbDataSource;

    private static final String USER_NAME_1 = "user1";
    private static final String USER_NAME_2 = "user2";
    private static final String USER_NAME_3 = "user3";

    public static final String USER_UUID_1 = "userUUID1";

    public static final String MEDIA_UUID_1 = "mediaUUID1";
    public static final String MEDIA_UUID_2 = "mediaUUID2";
    public static final String MEDIA_UUID_3 = "mediaUUID3";

    public static final String MEDIA_SHARE_UUID_1 = "mediaShareUUID1";
    public static final String MEDIA_SHARE_UUID_2 = "mediaShareUUID2";
    public static final String MEDIA_SHARE_UUID_3 = "mediaShareUUID3";

    public static final String MEDIA_SHARE_TITLE = "mediaShareTitle";
    public static final String MEDIA_SHARE_DESC = "mediaShareDesc";

    public static final String DEVICE_ID = "deviceID";

    public static final String TOKEN = "token";

    public static final String GATEWAY = "gateway";

    public static final String MEDIA_MINI_THUMB = "media_mini_thumb";

    public static final String MEDIA_UPLOADED_DEVICE_ID_1 = "media_uploaded_device_id1";
    public static final String MEDIA_UPLOADED_DEVICE_ID_2 = "media_uploaded_device_id2";

    public static final String FILE_NAME = "fileName";
    public static final long FILE_SIZE = 1000;
    public static final String FILE_UUID = "fileUUID";

    @Before
    public void setup() {
        dbDataSource = DBDataSource.getInstance(InstrumentationRegistry.getTargetContext());
    }

    @After
    public void cleanUp() {

        dbDataSource.deleteAllRemoteMediaShare();
        dbDataSource.deleteAllRemoteMedia();
        dbDataSource.deleteAllLocalMedia();
        dbDataSource.deleteAllRemoteUsers();
        dbDataSource.deleteToken();
        dbDataSource.deleteDeviceID();

    }

    private void initUser(User user) {
        user.setAvatar("");
        user.setDefaultAvatar("");
        user.setUuid("");
        user.setUserName("");
        user.setDefaultAvatar("");
        user.setDefaultAvatarBgColor(0);
        user.setEmail("");
        user.setHome("");
        user.setLibrary("");
    }

    private void initMedia(Media media) {
        media.setUploadedDeviceIDs("");
        media.setUuid("");
        media.setMiniThumb("");
        media.setOrientationNumber(0);
        media.setBelongingMediaShareUUID("");
        media.setDate("");
        media.setHeight("");
        media.setWidth("");
        media.setThumb("");
        media.setType("");
        media.setTime("");
        media.setDate("");
    }

    private void initMediaShare(MediaShare mediaShare) {

        mediaShare.setDate("");
        mediaShare.setUuid("");
        mediaShare.setDesc("");
        mediaShare.setTime("");
        mediaShare.setCoverImageUUID("");
        mediaShare.setCreatorUUID("");
        mediaShare.setShareDigest("");
        mediaShare.setTitle("");
    }

    private void initMediaShareContent(MediaShareContent mediaShareContent) {
        mediaShareContent.setMediaUUID("");
        mediaShareContent.setId("");
        mediaShareContent.setTime("");
        mediaShareContent.setAuthor("");
    }

    @Test
    public void insertUsers_retrieveUsers() {

        List<User> users = new ArrayList<>();

        User user = new User();
        initUser(user);
        user.setUserName(USER_NAME_1);
        users.add(user);

        user = new User();
        initUser(user);
        user.setUserName(USER_NAME_2);
        users.add(user);

        user = new User();
        initUser(user);
        user.setUserName(USER_NAME_3);
        users.add(user);

        dbDataSource.insertRemoteUsers(users);

        users = dbDataSource.loadRemoteUsers().getUsers();

        assertEquals(3, users.size());

        assertEquals(USER_NAME_1, users.get(0).getUserName());
        assertEquals(USER_NAME_2, users.get(1).getUserName());
        assertEquals(USER_NAME_3, users.get(2).getUserName());
    }

    @Test
    public void insertMediaShares_retrieveMediaShares() {

        List<MediaShare> mediaShares = new ArrayList<>();

        MediaShare mediaShare = new MediaShare();
        initMediaShare(mediaShare);
        mediaShare.setUuid(MEDIA_SHARE_UUID_1);
        mediaShares.add(mediaShare);

        mediaShare = new MediaShare();
        initMediaShare(mediaShare);
        mediaShare.setUuid(MEDIA_SHARE_UUID_2);
        mediaShares.add(mediaShare);

        mediaShare = new MediaShare();
        initMediaShare(mediaShare);
        mediaShare.setUuid(MEDIA_SHARE_UUID_3);
        mediaShares.add(mediaShare);

        dbDataSource.insertRemoteMediaShares(mediaShares);

        mediaShares = dbDataSource.loadAllRemoteMediaShares().getMediaShares();

        assertEquals(3, mediaShares.size());
        assertEquals(MEDIA_SHARE_UUID_1, mediaShares.get(0).getUuid());
        assertEquals(MEDIA_SHARE_UUID_2, mediaShares.get(1).getUuid());
        assertEquals(MEDIA_SHARE_UUID_3, mediaShares.get(2).getUuid());

    }

    private List<Media> generateMedias() {
        List<Media> medias = new ArrayList<>();

        Media media = new Media();
        initMedia(media);
        media.setUuid(MEDIA_UUID_1);
        medias.add(media);

        media = new Media();
        initMedia(media);
        media.setUuid(MEDIA_UUID_2);
        medias.add(media);

        media = new Media();
        initMedia(media);
        media.setUuid(MEDIA_UUID_3);
        medias.add(media);

        return medias;
    }

    @Test
    public void insertRemoteMedias_retrieveRemoteMedias() {

        List<Media> medias = generateMedias();

        dbDataSource.insertRemoteMedias(medias);

        medias = dbDataSource.loadAllRemoteMedias().getMedias();

        assertMedias(medias);

    }

    @Test
    public void insertLocalMedias_retrieveLocalMedias() {
        List<Media> medias = generateMedias();

        dbDataSource.insertLocalMedias(medias);

        medias = dbDataSource.loadAllLocalMedias().getMedias();

        assertMedias(medias);
    }

    private void assertMedias(List<Media> medias) {
        assertEquals(3, medias.size());
        assertEquals(MEDIA_UUID_1, medias.get(0).getUuid());
        assertEquals(MEDIA_UUID_2, medias.get(1).getUuid());
        assertEquals(MEDIA_UUID_3, medias.get(2).getUuid());
    }

    @Test
    public void modifyRemoteMediaShare_retrieveRemoteMediaShare() {

        MediaShare mediaShare = new MediaShare();
        initMediaShare(mediaShare);
        mediaShare.setUuid(MEDIA_SHARE_UUID_1);

        dbDataSource.insertRemoteMediaShare(mediaShare);

        mediaShare.setTitle(MEDIA_SHARE_TITLE);
        mediaShare.setDesc(MEDIA_SHARE_DESC);

        mediaShare.addViewer(USER_NAME_3);

        dbDataSource.modifyRemoteMediaShare(null, mediaShare);

        mediaShare = dbDataSource.loadRemoteMediaShare(MEDIA_SHARE_UUID_1);

        assertEquals(MEDIA_SHARE_TITLE, mediaShare.getTitle());
        assertEquals(MEDIA_SHARE_DESC, mediaShare.getDesc());

        assertThat(mediaShare.getViewersListSize() == 1, is(true));

        String viewer = mediaShare.getViewers().get(0);

        assertEquals(USER_NAME_3, viewer);
    }

    @Test
    public void modifyMediaInRemoteMediaShare_retrieveRemoteMediaShare() {

        MediaShare mediaShare = new MediaShare();
        initMediaShare(mediaShare);
        mediaShare.setUuid(MEDIA_SHARE_UUID_1);

        MediaShareContent mediaShareContent = new MediaShareContent();
        initMediaShareContent(mediaShareContent);
        mediaShareContent.setMediaUUID(MEDIA_UUID_1);

        mediaShare.addMediaShareContent(mediaShareContent);

        mediaShareContent = new MediaShareContent();
        initMediaShareContent(mediaShareContent);
        mediaShareContent.setMediaUUID(MEDIA_UUID_2);

        mediaShare.addMediaShareContent(mediaShareContent);

        dbDataSource.insertRemoteMediaShare(mediaShare);

        MediaShare diffOriginalMediaShare = mediaShare.cloneMyself();

        diffOriginalMediaShare.removeMediaShareContent(mediaShareContent);

        MediaShare diffModifiedMediaShare = mediaShare.cloneMyself();

        diffModifiedMediaShare.clearMediaShareContents();

        mediaShareContent = new MediaShareContent();
        initMediaShareContent(mediaShareContent);
        mediaShareContent.setMediaUUID(MEDIA_UUID_3);

        diffModifiedMediaShare.addMediaShareContent(mediaShareContent);

        dbDataSource.modifyMediaInRemoteMediaShare(null, diffOriginalMediaShare, diffModifiedMediaShare, null);

        mediaShare = dbDataSource.loadRemoteMediaShare(MEDIA_SHARE_UUID_1);

        assertEquals(2, mediaShare.getMediaShareContentsListSize());

        assertEquals(MEDIA_UUID_2, mediaShare.getMediaShareContents().get(0).getMediaUUID());
        assertEquals(MEDIA_UUID_3, mediaShare.getMediaShareContents().get(1).getMediaUUID());

    }

    @Test
    public void deleteRemoteMediaShare_retrieveRemoteMediaShareFail() {

        MediaShare mediaShare = new MediaShare();
        initMediaShare(mediaShare);
        mediaShare.setUuid(MEDIA_SHARE_UUID_1);

        dbDataSource.insertRemoteMediaShare(mediaShare);

        dbDataSource.deleteRemoteMediaShare(mediaShare);

        mediaShare = dbDataSource.loadRemoteMediaShare(MEDIA_SHARE_UUID_1);

        assertNull(mediaShare);

    }

    @Test
    public void loadDeviceID_retrieveSavedDeviceID() {

        dbDataSource.insertDeviceID(DEVICE_ID);

        String deviceID = dbDataSource.loadDeviceID().getDeviceID();

        assertEquals(DEVICE_ID, deviceID);

    }

    @Test
    public void updateLocalMediaMiniThumb_retrieveLocalMedia() {

        Media media = new Media();
        initMedia(media);
        media.setUuid(MEDIA_UUID_1);

        dbDataSource.insertLocalMedias(Collections.singletonList(media));

        media.setMiniThumb(MEDIA_MINI_THUMB);

        dbDataSource.updateLocalMediaMiniThumb(media);

        media = dbDataSource.loadAllLocalMedias().getMedias().get(0);

        assertEquals(MEDIA_UUID_1, media.getUuid());
        assertEquals(MEDIA_MINI_THUMB, media.getMiniThumb());

    }

    @Test
    public void updateLocalMediaUploadedDeviceID_retrieveLocalMedia() {

        Media media = new Media();
        initMedia(media);
        media.setUuid(MEDIA_UUID_1);

        dbDataSource.insertLocalMedias(Collections.singletonList(media));

        media.setUploadedDeviceIDs(MEDIA_UPLOADED_DEVICE_ID_1);

        dbDataSource.updateLocalMediaUploadedDeviceID(media);

        media = dbDataSource.loadAllLocalMedias().getMedias().get(0);

        assertEquals(MEDIA_UUID_1, media.getUuid());
        assertEquals(MEDIA_UPLOADED_DEVICE_ID_1, media.getUploadedDeviceIDs());

        String uploadedDeviceID = MEDIA_UPLOADED_DEVICE_ID_1 + "," + MEDIA_UPLOADED_DEVICE_ID_2;

        media.setUploadedDeviceIDs(uploadedDeviceID);

        dbDataSource.updateLocalMediaUploadedDeviceID(media);

        media = dbDataSource.loadAllLocalMedias().getMedias().get(0);

        assertEquals(MEDIA_UUID_1, media.getUuid());
        assertEquals(uploadedDeviceID, media.getUploadedDeviceIDs());

    }

    //because load downloaded files record logic, ignore test

    @Ignore
    public void loadDownloadedFileRecord_retrieveSavedDownloadedFileRecord() {

        FileDownloadItem fileDownloadItem = new FileDownloadItem(FILE_NAME, FILE_SIZE, FILE_UUID);

        fileDownloadItem.setFileCreatorUUID(USER_UUID_1);

        dbDataSource.insertDownloadedFileRecord(fileDownloadItem);

        fileDownloadItem = dbDataSource.loadDownloadedFilesRecord(USER_UUID_1).getFileDownloadItems().get(0);

        assertEquals(FILE_UUID, fileDownloadItem.getFileUUID());
        assertEquals(FILE_NAME, fileDownloadItem.getFileName());
        assertEquals(FILE_SIZE, fileDownloadItem.getFileSize());

    }

    @Ignore
    public void deleteDownloadedFileRecord_retrieveDownloadedFileRecord() {

        FileDownloadItem fileDownloadItem = new FileDownloadItem(FILE_NAME, FILE_SIZE, FILE_UUID);

        fileDownloadItem.setFileCreatorUUID(USER_UUID_1);

        dbDataSource.insertDownloadedFileRecord(fileDownloadItem);

        fileDownloadItem = dbDataSource.loadDownloadedFilesRecord(USER_UUID_1).getFileDownloadItems().get(0);

        assertEquals(FILE_UUID, fileDownloadItem.getFileUUID());
        assertEquals(FILE_NAME, fileDownloadItem.getFileName());
        assertEquals(FILE_SIZE, fileDownloadItem.getFileSize());

        dbDataSource.deleteDownloadedFileRecord(Collections.singletonList(FILE_UUID), USER_UUID_1);

        List<FileDownloadItem> fileDownloadItems = dbDataSource.loadDownloadedFilesRecord(USER_UUID_1).getFileDownloadItems();

        assertEquals(0, fileDownloadItems.size());

    }

    @Test
    public void insertToken_retrieveToken() {

        dbDataSource.insertToken(TOKEN);

        String token = dbDataSource.loadToken();

        assertEquals(TOKEN, token);

    }

    @Test
    public void deleteToken_retrieveToken() {

        dbDataSource.insertToken(TOKEN);

        String token = dbDataSource.loadToken();

        assertEquals(TOKEN, token);

        dbDataSource.deleteToken();

        token = dbDataSource.loadToken();

        assertNull(token);

    }

    @Test
    public void insertGateway_retrieveGateway() {

        dbDataSource.insertGateway(GATEWAY);

        String gateway = dbDataSource.loadGateway();

        assertEquals(GATEWAY, gateway);
    }

    @Test
    public void deleteAllRemoteMediaShare_retrieveRemoteMediaShare() {

        List<MediaShare> mediaShares = new ArrayList<>();

        MediaShare mediaShare = new MediaShare();
        initMediaShare(mediaShare);
        mediaShare.setUuid(MEDIA_SHARE_UUID_1);

        mediaShares.add(mediaShare);

        mediaShare = new MediaShare();
        initMediaShare(mediaShare);
        mediaShare.setUuid(MEDIA_SHARE_UUID_2);

        mediaShares.add(mediaShare);

        mediaShare = new MediaShare();
        initMediaShare(mediaShare);
        mediaShare.setUuid(MEDIA_SHARE_UUID_3);

        mediaShares.add(mediaShare);

        dbDataSource.insertRemoteMediaShares(mediaShares);

        dbDataSource.deleteAllRemoteMediaShare();

        mediaShares = dbDataSource.loadAllRemoteMediaShares().getMediaShares();

        assertEquals(0, mediaShares.size());

    }

    @Test
    public void deleteAllMedia_retrieveMedia() {

        List<Media> medias = new ArrayList<>();

        Media media = new Media();
        initMedia(media);
        media.setUuid(MEDIA_UUID_1);

        medias.add(media);

        media = new Media();
        initMedia(media);
        media.setUuid(MEDIA_UUID_2);

        medias.add(media);

        media = new Media();
        initMedia(media);
        media.setUuid(MEDIA_UUID_3);

        medias.add(media);

        dbDataSource.insertRemoteMedias(medias);

        dbDataSource.insertLocalMedias(medias);

        dbDataSource.deleteAllRemoteMedia();

        medias = dbDataSource.loadAllRemoteMedias().getMedias();

        assertEquals(0, medias.size());

        dbDataSource.deleteAllLocalMedia();

        medias = dbDataSource.loadAllLocalMedias().getMedias();

        assertEquals(0, medias.size());

    }

    @Test
    public void deleteAllRemoteUser_retrieveRemoteUser() {

        List<User> users = new ArrayList<>();

        User user = new User();
        initUser(user);
        users.add(user);

        user = new User();
        initUser(user);
        users.add(user);

        user = new User();
        initUser(user);
        users.add(user);

        dbDataSource.insertRemoteUsers(users);

        dbDataSource.deleteAllRemoteUsers();

        users = dbDataSource.loadRemoteUsers().getUsers();

        assertEquals(0, users.size());

    }


}
