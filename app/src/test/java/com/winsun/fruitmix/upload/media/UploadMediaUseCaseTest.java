package com.winsun.fruitmix.upload.media;

import android.support.annotation.NonNull;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.RemoteDatasParserUnitTest;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.file.data.station.StationFileRepositoryTest;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.media.CalcMediaDigestStrategy;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.mock.MockThreadManager;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.model.operationResult.OperationSuccessWithFile;
import com.winsun.fruitmix.network.NetworkState;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.parser.HttpErrorBodyParser;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Administrator on 2017/8/23.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class UploadMediaUseCaseTest {

    private UploadMediaUseCase uploadMediaUseCase;

    @Mock
    private MediaDataSourceRepository mediaDataSourceRepository;

    @Mock
    private StationFileRepository stationFileRepository;

    @Mock
    private UserDataRepository userDataRepository;

    @Mock
    private SystemSettingDataSource systemSettingDataSource;

    @Mock
    private CheckMediaIsUploadStrategy checkMediaIsUploadStrategy;

    @Mock
    private CheckMediaIsExistStrategy checkMediaIsExistStrategy;

    @Mock
    private CalcMediaDigestStrategy calcMediaDigestStrategy;

    @Mock
    private EventBus eventBus;

    @Mock
    private NetworkStateManager networkStateManager;

    private ThreadManager threadManager;

    private String testUserUUID;
    private String testUserHome;

    private String testUploadParentFolderUUID;
    private String testUploadFolderUUID;

    private String testUploadFolderName;

    private String testMediaUUID;
    private String testMediaOriginalPhotoPath;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        testUploadFolderName = "";

        threadManager = new MockThreadManager();

        uploadMediaUseCase = UploadMediaUseCase.getInstance(mediaDataSourceRepository, stationFileRepository,
                userDataRepository, threadManager, systemSettingDataSource, checkMediaIsUploadStrategy, checkMediaIsExistStrategy, testUploadFolderName, eventBus,
                calcMediaDigestStrategy, networkStateManager);

    }

    @After
    public void clean() {

        UploadMediaUseCase.destroyInstance();

    }

    @Test
    public void testStartUploadTwiceImmediately() {

        prepareStartUpload();

        uploadMediaUseCase.startUploadMedia();

        assertTrue(uploadMediaUseCase.mAlreadyStartUpload);

        verify(systemSettingDataSource).getCurrentLoginUserUUID();

        verify(userDataRepository).getUserByUUID(anyString());

        uploadMediaUseCase.startUploadMedia();

        assertTrue(uploadMediaUseCase.mAlreadyStartUpload);

        verify(systemSettingDataSource, times(1)).getCurrentLoginUserUUID();

        verify(userDataRepository, times(1)).getUserByUUID(anyString());

    }

    private void prepareStartUpload() {

        testUploadParentFolderUUID = "testUploadParentFolderUUID";
        testUploadFolderUUID = "testUploadFolderUUID";

        testUserUUID = "testUserUUID";
        testUserHome = "testUserHome";

        User user = new User();
        user.setUuid(testUserUUID);
        user.setHome(testUserHome);

        when(systemSettingDataSource.getCurrentLoginUserUUID()).thenReturn(testUserUUID);

        when(userDataRepository.getUserByUUID(anyString())).thenReturn(user);

        when(checkMediaIsExistStrategy.checkMediaIsExist(any(Media.class))).thenReturn(true);

        when(calcMediaDigestStrategy.isFinishCalcMediaDigest()).thenReturn(true);

        when(networkStateManager.getNetworkState()).thenReturn(new NetworkState(true, false));
    }

    private void assertStringIsEmpty(String param) {
        assertTrue(param.isEmpty());
    }

    @Test
    public void testUploadParentFolderNotExist() {

        prepareStartUpload();

        when(systemSettingDataSource.getAutoUploadOrNot()).thenReturn(true);

        when(checkMediaIsUploadStrategy.isMediaUploaded(any(Media.class))).thenReturn(false);

        when(stationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome),eq(testUserHome),eq("")))
                .thenReturn(new OperationSuccessWithFile(Collections.<AbstractRemoteFile>emptyList()));

        uploadMediaUseCase.startUploadMedia();

        ArgumentCaptor<BaseLoadDataCallback<Media>> getLocalMediaCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mediaDataSourceRepository).getLocalMediaWithoutThreadChange(getLocalMediaCaptor.capture());

        getLocalMediaCaptor.getValue().onSucceed(Collections.<Media>emptyList(), new OperationSuccess());

        assertStringIsEmpty(uploadMediaUseCase.uploadParentFolderUUID);
        assertStringIsEmpty(uploadMediaUseCase.uploadFolderUUID);

        assertNotNull(uploadMediaUseCase.uploadedMediaHashs);

        verify(checkMediaIsUploadStrategy).setUploadedMediaHashs(Collections.<String>emptyList());

        ArgumentCaptor<BaseOperateDataCallback<HttpResponse>> createUploadParentFolderCaptor = ArgumentCaptor.forClass(BaseOperateDataCallback.class);

        verify(stationFileRepository).createFolderWithoutCreateNewThread(eq(uploadMediaUseCase.UPLOAD_PARENT_FOLDER_NAME), eq(testUserHome), eq(testUserHome), createUploadParentFolderCaptor.capture());

        HttpResponse httpResponse = new HttpResponse();

        httpResponse.setResponseData("[{\"number\":0,\"op\":\"mkdir\",\"name\":\"来自Huawei-Nexus 6P\",\"data\":{\"uuid\":\"" + testUploadParentFolderUUID + "\",\"type\":\"directory\",\"name\":\"" + uploadMediaUseCase.UPLOAD_PARENT_FOLDER_NAME + "\",\"mtime\":1503366446508}}]");

        createUploadParentFolderCaptor.getValue().onSucceed(httpResponse, new OperationSuccess());

        ArgumentCaptor<BaseOperateDataCallback<HttpResponse>> createUploadFolderCaptor = ArgumentCaptor.forClass(BaseOperateDataCallback.class);

        verify(stationFileRepository).createFolderWithoutCreateNewThread(eq(uploadMediaUseCase.UPLOAD_FOLDER_NAME_PREFIX + testUploadFolderName), eq(testUserHome), eq(testUploadParentFolderUUID), createUploadFolderCaptor.capture());

        HttpResponse uploadFolderResponse = new HttpResponse();

        uploadFolderResponse.setResponseData("[{\"number\":0,\"op\":\"mkdir\",\"name\":\"来自Huawei-Nexus 6P\",\"data\":{\"uuid\":\"" + testUploadFolderUUID + "\",\"type\":\"directory\",\"name\":\"" + uploadMediaUseCase.UPLOAD_FOLDER_NAME_PREFIX + testUploadFolderName + "\",\"mtime\":1503366446508}}]");

        createUploadFolderCaptor.getValue().onSucceed(uploadFolderResponse, new OperationSuccess());

        assertTrue(uploadMediaUseCase.mStopUpload);
        assertFalse(uploadMediaUseCase.mAlreadyStartUpload);

    }

    @Test
    public void testUploadParentFolderExistButUploadFolderNotExist() {

        prepareStartUpload();

        when(systemSettingDataSource.getAutoUploadOrNot()).thenReturn(true);

        when(checkMediaIsUploadStrategy.isMediaUploaded(any(Media.class))).thenReturn(false);

        AbstractRemoteFile file = new RemoteFile();
        file.setName(UploadMediaUseCase.UPLOAD_PARENT_FOLDER_NAME);
        file.setUuid(testUploadParentFolderUUID);

        List<AbstractRemoteFile> files = new ArrayList<>();
        files.add(file);

        when(stationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome),eq(testUserHome),eq("")))
                .thenReturn(new OperationSuccessWithFile(files));

        when(stationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome),eq(testUploadParentFolderUUID),eq("")))
                .thenReturn(new OperationSuccessWithFile(Collections.<AbstractRemoteFile>emptyList()));

        uploadMediaUseCase.startUploadMedia();

        ArgumentCaptor<BaseLoadDataCallback<Media>> getLocalMediaCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mediaDataSourceRepository).getLocalMediaWithoutThreadChange(getLocalMediaCaptor.capture());

        getLocalMediaCaptor.getValue().onSucceed(Collections.<Media>emptyList(), new OperationSuccess());

        assertNotNull(uploadMediaUseCase.uploadedMediaHashs);

        verify(checkMediaIsUploadStrategy).setUploadedMediaHashs(Collections.<String>emptyList());

        ArgumentCaptor<BaseOperateDataCallback<HttpResponse>> createUploadFolderCaptor = ArgumentCaptor.forClass(BaseOperateDataCallback.class);

        verify(stationFileRepository).createFolderWithoutCreateNewThread(eq(uploadMediaUseCase.UPLOAD_FOLDER_NAME_PREFIX + testUploadFolderName), eq(testUserHome), eq(testUploadParentFolderUUID), createUploadFolderCaptor.capture());

        HttpResponse uploadFolderResponse = new HttpResponse();
        uploadFolderResponse.setResponseData(RemoteDatasParserUnitTest.localMkdirResult);

        createUploadFolderCaptor.getValue().onSucceed(uploadFolderResponse, new OperationSuccess());

        assertTrue(uploadMediaUseCase.mStopUpload);
        assertFalse(uploadMediaUseCase.mAlreadyStartUpload);

    }

    @Test
    public void testUploadParentFolderExistAndUploadFolderExist() {

        prepareStartUpload();

        Media media = createMedia();

        when(systemSettingDataSource.getAutoUploadOrNot()).thenReturn(true);

        when(checkMediaIsUploadStrategy.isMediaUploaded(any(Media.class))).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(true);

        when(stationFileRepository.uploadFile(any(LocalFile.class), anyString(), anyString())).thenReturn(new OperationSuccess());

        uploadWhenUploadParentFolderExistAndUploadFolderExist(Collections.singletonList(media));

        assertTrue(uploadMediaUseCase.mStopUpload);
        assertFalse(uploadMediaUseCase.mAlreadyStartUpload);

        assertEquals(1, uploadMediaUseCase.alreadyUploadedMediaCount);

    }

    private void uploadWhenUploadParentFolderExistAndUploadFolderExist(List<Media> medias) {

        AbstractRemoteFile file = new RemoteFile();
        file.setName(UploadMediaUseCase.UPLOAD_PARENT_FOLDER_NAME);
        file.setUuid(testUploadParentFolderUUID);

        List<AbstractRemoteFile> files = new ArrayList<>();
        files.add(file);

        when(stationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome),eq(testUserHome),eq("")))
                .thenReturn(new OperationSuccessWithFile(files));

        AbstractRemoteFile uploadFolderFile = new RemoteFile();
        uploadFolderFile.setName(UploadMediaUseCase.UPLOAD_FOLDER_NAME_PREFIX + testUploadFolderName);
        uploadFolderFile.setUuid(testUploadFolderUUID);

        List<AbstractRemoteFile> uploadParentFolders = new ArrayList<>();
        uploadParentFolders.add(uploadFolderFile);

        when(stationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome),eq(testUploadParentFolderUUID),eq("")))
                .thenReturn(new OperationSuccessWithFile(uploadParentFolders));

        AbstractRemoteFile uploadFile = new RemoteFile();
        ((RemoteFile) uploadFile).setFileHash(testMediaUUID);

        when(stationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome),eq(testUploadFolderUUID),eq("")))
                .thenReturn(new OperationSuccessWithFile(Collections.singletonList(uploadFile)));

        uploadMediaUseCase.startUploadMedia();

        ArgumentCaptor<BaseLoadDataCallback<Media>> getLocalMediaCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mediaDataSourceRepository).getLocalMediaWithoutThreadChange(getLocalMediaCaptor.capture());

        getLocalMediaCaptor.getValue().onSucceed(medias, new OperationSuccess());

        verify(stationFileRepository, never()).createFolderWithoutCreateNewThread(anyString(), anyString(), anyString(), any(BaseOperateDataCallback.class));

    }

    @Test
    public void testUploadFileOccur404() {

        prepareStartUpload();

        when(systemSettingDataSource.getAutoUploadOrNot()).thenReturn(true).thenReturn(false);

        when(checkMediaIsUploadStrategy.isMediaUploaded(any(Media.class))).thenReturn(false);

        when(stationFileRepository.uploadFile(any(LocalFile.class), anyString(), anyString())).thenReturn(new OperationNetworkException(new HttpResponse(404, "")));

        Media media = createMedia();

        uploadWhenUploadParentFolderExistAndUploadFolderExist(Collections.singletonList(media));

        assertStringIsEmpty(uploadMediaUseCase.uploadParentFolderUUID);
        assertStringIsEmpty(uploadMediaUseCase.uploadFolderUUID);

    }

    @Test
    public void testUploadFileOccur403EEXIST() {

        prepareStartUpload();

        when(systemSettingDataSource.getAutoUploadOrNot()).thenReturn(true);

        when(checkMediaIsUploadStrategy.isMediaUploaded(any(Media.class))).thenReturn(false);

        when(stationFileRepository.uploadFile(any(LocalFile.class), anyString(), anyString()))
                .thenReturn(new OperationNetworkException(new HttpResponse(403,
                        StationFileRepositoryTest.getJSONArrayStringWhenEEXIST(HttpErrorBodyParser.UPLOAD_FILE_EXIST_CODE))));

        Media media = createMedia();

        uploadWhenUploadParentFolderExistAndUploadFolderExist(Collections.singletonList(media));

        assertEquals(1, uploadMediaUseCase.alreadyUploadedMediaCount);

        assertTrue(uploadMediaUseCase.mStopUpload);
        assertFalse(uploadMediaUseCase.mAlreadyStartUpload);

    }

    @Test
    public void testUploadFileOccur403ButNotEEXIST() {

        prepareStartUpload();

        when(systemSettingDataSource.getAutoUploadOrNot()).thenReturn(true);

        when(checkMediaIsUploadStrategy.isMediaUploaded(any(Media.class))).thenReturn(false);

        when(stationFileRepository.uploadFile(any(LocalFile.class), anyString(), anyString()))
                .thenReturn(new OperationNetworkException(new HttpResponse(403, StationFileRepositoryTest.getJSONArrayStringWhenEEXIST(""))));

        Media media = createMedia();

        uploadWhenUploadParentFolderExistAndUploadFolderExist(Collections.singletonList(media));

        assertEquals(0, uploadMediaUseCase.alreadyUploadedMediaCount);

        assertTrue(uploadMediaUseCase.mStopUpload);
        assertFalse(uploadMediaUseCase.mAlreadyStartUpload);

    }


    @NonNull
    private Media createMedia() {
        testMediaUUID = "testMediaUUID";
        testMediaOriginalPhotoPath = "testMediaOriginalPhotoPath";

        Media media = new Media();
        media.setUuid(testMediaUUID);
        media.setOriginalPhotoPath(testMediaOriginalPhotoPath);
        return media;
    }

    @Test
    public void testStartUploadWhenUploadParentFolderUUIDNotNullAndUploadFolderUUIDNotNull() {

        prepareStartUpload();

        when(systemSettingDataSource.getAutoUploadOrNot()).thenReturn(true);

        when(checkMediaIsUploadStrategy.isMediaUploaded(any(Media.class))).thenReturn(false);

        uploadMediaUseCase.uploadParentFolderUUID = testUploadParentFolderUUID;
        uploadMediaUseCase.uploadFolderUUID = testUploadFolderUUID;

        uploadMediaUseCase.startUploadMedia();

        ArgumentCaptor<BaseLoadDataCallback<Media>> getLocalMediaCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mediaDataSourceRepository).getLocalMediaWithoutThreadChange(getLocalMediaCaptor.capture());

        getLocalMediaCaptor.getValue().onSucceed(Collections.<Media>emptyList(), new OperationSuccess());

        verify(stationFileRepository, never()).getFileWithoutCreateNewThread(anyString(), anyString(),anyString());

        verify(stationFileRepository, never()).createFolderWithoutCreateNewThread(anyString(), anyString(), anyString(), any(BaseOperateDataCallback.class));

        assertFalse(uploadMediaUseCase.mAlreadyStartUpload);
        assertTrue(uploadMediaUseCase.mStopUpload);

    }


    @Test
    public void testFirstStartUploadNoAutoUploadPermissionThenSecondStartUploadHasAutoUploadPermission() {

        prepareStartUpload();

        Media media = createMedia();

        when(systemSettingDataSource.getAutoUploadOrNot()).thenReturn(false);

        when(checkMediaIsUploadStrategy.isMediaUploaded(any(Media.class))).thenReturn(false);

        when(stationFileRepository.uploadFile(any(LocalFile.class), anyString(), anyString())).thenReturn(new OperationSuccess());

        uploadWhenUploadParentFolderExistAndUploadFolderExist(Collections.singletonList(media));

        verify(stationFileRepository, never()).uploadFile(any(LocalFile.class), anyString(), anyString());

        when(systemSettingDataSource.getAutoUploadOrNot()).thenReturn(true);

        secondCallUploadWhenUploadParentFolderExistAndUploadFolderExist(Collections.singletonList(media));

        verify(stationFileRepository).uploadFile(any(LocalFile.class), anyString(), anyString());

    }

    private void secondCallUploadWhenUploadParentFolderExistAndUploadFolderExist(List<Media> medias) {

        AbstractRemoteFile file = new RemoteFile();
        file.setName(UploadMediaUseCase.UPLOAD_PARENT_FOLDER_NAME);
        file.setUuid(testUploadParentFolderUUID);

        List<AbstractRemoteFile> files = new ArrayList<>();
        files.add(file);

        when(stationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome),eq(testUserHome),eq("")))
                .thenReturn(new OperationSuccessWithFile(files));

        AbstractRemoteFile uploadFolderFile = new RemoteFile();
        uploadFolderFile.setName(UploadMediaUseCase.UPLOAD_FOLDER_NAME_PREFIX + testUploadFolderName);
        uploadFolderFile.setUuid(testUploadFolderUUID);

        List<AbstractRemoteFile> uploadParentFolders = new ArrayList<>();
        uploadParentFolders.add(uploadFolderFile);

        when(stationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome),eq(testUploadParentFolderUUID),eq("")))
                .thenReturn(new OperationSuccessWithFile(uploadParentFolders));

        AbstractRemoteFile uploadFile = new RemoteFile();
        ((RemoteFile) uploadFile).setFileHash(testMediaUUID);

        when(stationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome),eq(testUploadFolderUUID),eq("")))
                .thenReturn(new OperationSuccessWithFile(Collections.singletonList(uploadFile)));

        uploadMediaUseCase.startUploadMedia();

        ArgumentCaptor<BaseLoadDataCallback<Media>> getLocalMediaCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mediaDataSourceRepository, times(2)).getLocalMediaWithoutThreadChange(getLocalMediaCaptor.capture());

        getLocalMediaCaptor.getValue().onSucceed(medias, new OperationSuccess());

        verify(stationFileRepository, times(2)).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUserHome),eq(""));

        verify(stationFileRepository, times(2)).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUploadParentFolderUUID),
                eq(""));

        verify(stationFileRepository, times(2)).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUploadFolderUUID),
                eq(""));

        verify(checkMediaIsUploadStrategy, times(2)).setUploadedMediaHashs(ArgumentMatchers.<String>anyList());

        verify(stationFileRepository, never()).createFolderWithoutCreateNewThread(anyString(), anyString(), anyString(), any(BaseOperateDataCallback.class));

    }

    @Test
    public void testGetUploadParentFolderUUIDFail() {

        prepareStartUpload();

        when(systemSettingDataSource.getAutoUploadOrNot()).thenReturn(true);

        when(checkMediaIsUploadStrategy.isMediaUploaded(any(Media.class))).thenReturn(false);

        when(stationFileRepository.getFileWithoutCreateNewThread(anyString(),anyString(),anyString())).thenReturn(new OperationIOException());

        uploadMediaUseCase.startUploadMedia();

        ArgumentCaptor<BaseLoadDataCallback<Media>> getLocalMediaCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mediaDataSourceRepository).getLocalMediaWithoutThreadChange(getLocalMediaCaptor.capture());

        getLocalMediaCaptor.getValue().onSucceed(Collections.<Media>emptyList(), new OperationSuccess());

        assertFalse(uploadMediaUseCase.mAlreadyStartUpload);
        assertTrue(uploadMediaUseCase.mStopUpload);

    }


    @Test
    public void testCreateFolderFail() {

        prepareStartUpload();

        when(systemSettingDataSource.getAutoUploadOrNot()).thenReturn(true);

        when(checkMediaIsUploadStrategy.isMediaUploaded(any(Media.class))).thenReturn(false);

        when(stationFileRepository.getFileWithoutCreateNewThread(anyString(),anyString(),anyString()))
                .thenReturn(new OperationSuccessWithFile(Collections.<AbstractRemoteFile>emptyList()));

        uploadMediaUseCase.startUploadMedia();

        ArgumentCaptor<BaseLoadDataCallback<Media>> getLocalMediaCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mediaDataSourceRepository).getLocalMediaWithoutThreadChange(getLocalMediaCaptor.capture());

        getLocalMediaCaptor.getValue().onSucceed(Collections.<Media>emptyList(), new OperationSuccess());

        ArgumentCaptor<BaseOperateDataCallback<HttpResponse>> createFolderCaptor = ArgumentCaptor.forClass(BaseOperateDataCallback.class);

        verify(stationFileRepository).createFolderWithoutCreateNewThread(anyString(), anyString(), anyString(), createFolderCaptor.capture());

        createFolderCaptor.getValue().onFail(new OperationIOException());

        assertFalse(uploadMediaUseCase.mAlreadyStartUpload);
        assertTrue(uploadMediaUseCase.mStopUpload);

    }

    @Test
    public void testUploadSameUUIDMedia() {

        prepareStartUpload();

        Media media = createMedia();

        Media sameUUIDMedia = createMedia();

        List<Media> medias = new ArrayList<>();
        medias.add(media);
        medias.add(sameUUIDMedia);

        when(systemSettingDataSource.getAutoUploadOrNot()).thenReturn(true);

        when(checkMediaIsUploadStrategy.isMediaUploaded(any(Media.class))).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(true);

        when(stationFileRepository.uploadFile(any(LocalFile.class), anyString(), anyString())).thenReturn(new OperationSuccess());

        uploadWhenUploadParentFolderExistAndUploadFolderExist(medias);

        assertTrue(uploadMediaUseCase.mStopUpload);
        assertFalse(uploadMediaUseCase.mAlreadyStartUpload);

        assertEquals(2, uploadMediaUseCase.alreadyUploadedMediaCount);

    }

}
