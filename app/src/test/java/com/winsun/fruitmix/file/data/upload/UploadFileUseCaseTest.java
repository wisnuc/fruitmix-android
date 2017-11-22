package com.winsun.fruitmix.file.data.upload;

import android.support.annotation.NonNull;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.file.data.station.StationFileRepositoryTest;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.model.operationResult.OperationFail;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.network.NetworkState;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.parser.HttpErrorBodyParser;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.FileTool;
import com.winsun.fruitmix.util.FileUtil;

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

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Administrator on 2017/11/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class UploadFileUseCaseTest {

    //TODO:create upload file use case test

    private UploadFileUseCase mUploadFileUseCase;

    @Mock
    private UserDataRepository mUserDataRepository;

    @Mock
    private StationFileRepository mStationFileRepository;

    @Mock
    private SystemSettingDataSource mSystemSettingDataSource;

    @Mock
    private NetworkStateManager mNetworkStateManager;

    @Mock
    private FileTool mFileTool;

    private String uploadFolderName;
    private String fileTemporaryFolderPath;

    private String testUserUUID = "testUserUUID";
    private String testUserHome = "testUserHome";

    private String testRootFolderUUID = "testRootFolderUUID";
    private String testUploadFolderUUID = "testUploadFolderUUID";

    private String testUploadFileUUID = "testUploadFileUUID";


    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        uploadFolderName = "";

        fileTemporaryFolderPath = "";

        mUploadFileUseCase = new UploadFileUseCase(mUserDataRepository, mStationFileRepository, mSystemSettingDataSource,
                mNetworkStateManager, mFileTool, uploadFolderName, fileTemporaryFolderPath);

    }

    @After
    public void tearDown() throws Exception {

    }

    private User createUser() {

        User user = new User();
        user.setUuid(testUserUUID);
        user.setHome(testUserHome);

        return user;

    }

    @Test
    public void testAutoUploadFileWhenFileExist() {

        prepareTest();

        FileUploadItem fileUploadItem = getFileUploadItem();

        FileUploadState fileUploadState = new FileUploadingState(fileUploadItem);

        mUploadFileUseCase.updateFile(fileUploadState);

        setUploadFolderExist();

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> getUploadFolderCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUploadFolderUUID), getUploadFolderCaptor.capture());

        AbstractRemoteFile uploadFolder = new RemoteFile();
        ((RemoteFile) uploadFolder).setFileHash(testUploadFileUUID);

        getUploadFolderCaptor.getValue().onSucceed(Collections.singletonList(uploadFolder), new OperationSuccess());

        assertTrue(fileUploadItem.getFileUploadState() instanceof FileUploadFinishedState);

        verify(mStationFileRepository).insertFileUploadTask(eq(fileUploadItem), eq(testUserUUID));

        verify(mStationFileRepository, never()).createFolderWithoutCreateNewThread(anyString(), anyString(), anyString(), any(BaseOperateDataCallback.class));

    }

    @NonNull
    private FileUploadItem getFileUploadItem() {
        FileUploadItem fileUploadItem = new FileUploadItem();
        fileUploadItem.setFileUUID(testUploadFileUUID);
        return fileUploadItem;
    }

    private void prepareTest() {
        when(mSystemSettingDataSource.getCurrentLoginUserUUID()).thenReturn(testUserUUID);

        when(mUserDataRepository.getUserByUUID(eq(testUserUUID))).thenReturn(createUser());
    }

    private void setUploadFolderExist() {
        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> getRootFolderCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUserHome), getRootFolderCaptor.capture());

        AbstractRemoteFile file = new RemoteFile();
        file.setName(UploadFileUseCase.UPLOAD_PARENT_FOLDER_NAME);
        file.setUuid(testRootFolderUUID);

        getRootFolderCaptor.getValue().onSucceed(Collections.singletonList(file), new OperationSuccess());

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> getUploadParentFolderCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testRootFolderUUID), getUploadParentFolderCaptor.capture());

        AbstractRemoteFile uploadFolder = new RemoteFile();
        uploadFolder.setName(mUploadFileUseCase.getUploadFolderName());
        uploadFolder.setUuid(testUploadFolderUUID);

        getUploadParentFolderCaptor.getValue().onSucceed(Collections.singletonList(uploadFolder), new OperationSuccess());
    }

    @Test
    public void testCheckAutoUploadCondition() {

        prepareTest();

        NetworkState networkState = new NetworkState(false, true);

        when(mNetworkStateManager.getNetworkState()).thenReturn(networkState);

        when(mSystemSettingDataSource.getOnlyAutoUploadWhenConnectedWithWifi()).thenReturn(true);

        FileUploadItem fileUploadItem = getFileUploadItem();

        FileUploadState fileUploadState = new FileUploadingState(fileUploadItem);

        mUploadFileUseCase.updateFile(fileUploadState);

        setUploadFolderExist();

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> getUploadFolderCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUploadFolderUUID), getUploadFolderCaptor.capture());

        getUploadFolderCaptor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());

        verify(mNetworkStateManager).getNetworkState();

        verify(mSystemSettingDataSource).getOnlyAutoUploadWhenConnectedWithWifi();

        assertTrue(fileUploadItem.getFileUploadState() instanceof FileUploadErrorState);

        assertFalse(mUploadFileUseCase.needRetryForEEXIST);

    }


    @Test
    public void testAutoUploadFileSucceed() {

        prepareTest();

        NetworkState networkState = new NetworkState(true, true);

        when(mNetworkStateManager.getNetworkState()).thenReturn(networkState);

        when(mSystemSettingDataSource.getOnlyAutoUploadWhenConnectedWithWifi()).thenReturn(true);

        when(mStationFileRepository.uploadFileWithProgress(any(LocalFile.class), any(FileUploadState.class), anyString(),
                anyString(), anyString())).thenReturn(new OperationSuccess());

        when(mStationFileRepository.insertFileUploadTask(any(FileUploadItem.class), anyString())).thenReturn(true);

        when(mFileTool.copyFileToDir(anyString(), anyString())).thenReturn(true);

        String testFilePath = "testFilePath";
        String testFileName = "testFileName";
        long testFileSize = 1024;

        FileUploadItem fileUploadItem = getFileUploadItem();
        fileUploadItem.setFilePath(testFilePath);
        fileUploadItem.setFileSize(testFileSize);
        fileUploadItem.setFileName(testFileName);

        FileUploadState fileUploadState = new FileUploadingState(fileUploadItem);
        fileUploadState.setFilePath(testFilePath);

        mUploadFileUseCase.updateFile(fileUploadState);

        setUploadFolderExist();

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> getUploadFolderCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUploadFolderUUID), getUploadFolderCaptor.capture());

        getUploadFolderCaptor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());

        verify(mFileTool).copyFileToDir(eq(testFilePath), eq(fileTemporaryFolderPath));

        verify(mStationFileRepository).uploadFileWithProgress(any(LocalFile.class), eq(fileUploadState), eq(testUserHome),
                eq(testUploadFolderUUID), eq(testUserUUID));

        verify(mStationFileRepository).insertFileUploadTask(eq(fileUploadItem), eq(testUserUUID));

        String copyFilePath = fileTemporaryFolderPath + File.separator + testFileName;

        verify(mFileTool).copyFileToDir(eq(copyFilePath), eq(FileUtil.getDownloadFileStoreFolderPath()));

        verify(mFileTool).deleteFile(eq(copyFilePath));

    }

    @Test
    public void testAutoUploadFileFail() {

        prepareTest();

        NetworkState networkState = new NetworkState(true, true);

        when(mNetworkStateManager.getNetworkState()).thenReturn(networkState);

        when(mSystemSettingDataSource.getOnlyAutoUploadWhenConnectedWithWifi()).thenReturn(true);

        when(mStationFileRepository.uploadFileWithProgress(any(LocalFile.class), any(FileUploadState.class), anyString(),
                anyString(), anyString())).thenReturn(new OperationFail("test fail"));

        when(mStationFileRepository.insertFileUploadTask(any(FileUploadItem.class), anyString())).thenReturn(true);

        when(mFileTool.copyFileToDir(anyString(), anyString())).thenReturn(true);

        String testFilePath = "testFilePath";
        String testFileName = "testFileName";
        long testFileSize = 1024;

        FileUploadItem fileUploadItem = getFileUploadItem();
        fileUploadItem.setFilePath(testFilePath);
        fileUploadItem.setFileSize(testFileSize);
        fileUploadItem.setFileName(testFileName);

        FileUploadState fileUploadState = new FileUploadingState(fileUploadItem);
        fileUploadState.setFilePath(testFilePath);

        mUploadFileUseCase.updateFile(fileUploadState);

        setUploadFolderExist();

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> getUploadFolderCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUploadFolderUUID), getUploadFolderCaptor.capture());

        getUploadFolderCaptor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());

        verify(mFileTool).copyFileToDir(eq(testFilePath), eq(fileTemporaryFolderPath));

        verify(mStationFileRepository).uploadFileWithProgress(any(LocalFile.class), eq(fileUploadState), eq(testUserHome),
                eq(testUploadFolderUUID), eq(testUserUUID));

        verify(mStationFileRepository, never()).insertFileUploadTask(eq(fileUploadItem), eq(testUserUUID));

        String copyFilePath = fileTemporaryFolderPath + File.separator + testFileName;

        verify(mFileTool, never()).copyFileToDir(eq(copyFilePath), eq(FileUtil.getDownloadFileStoreFolderPath()));

        verify(mFileTool).deleteFile(eq(copyFilePath));

    }

    @Test
    public void testUploadFolderNotExistCreateFolder() {

        prepareTest();

        FileUploadItem fileUploadItem = getFileUploadItem();
        FileUploadState fileUploadState = new FileUploadingState(fileUploadItem);

        mUploadFileUseCase.updateFile(fileUploadState);

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> getRootFolderCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUserHome), getRootFolderCaptor.capture());

        getRootFolderCaptor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());

        ArgumentCaptor<BaseOperateDataCallback<HttpResponse>> createRootFolderCallback = ArgumentCaptor.forClass(BaseOperateDataCallback.class);

        verify(mStationFileRepository).createFolderWithoutCreateNewThread(eq(UploadFileUseCase.UPLOAD_PARENT_FOLDER_NAME),
                eq(testUserHome), eq(testUserHome), createRootFolderCallback.capture());

        HttpResponse httpResponse = new HttpResponse();

        httpResponse.setResponseData("[{\"number\":0,\"op\":\"mkdir\",\"name\":\"来自Huawei-Nexus 6P\",\"data\":{\"uuid\":\"" + testRootFolderUUID + "\",\"type\":\"directory\",\"name\":\"" + UploadFileUseCase.UPLOAD_PARENT_FOLDER_NAME + "\",\"mtime\":1503366446508}}]");

        createRootFolderCallback.getValue().onSucceed(httpResponse, new OperationSuccess());

        verify(mStationFileRepository).createFolderWithoutCreateNewThread(eq(mUploadFileUseCase.getUploadFolderName()), eq(testUserHome),
                eq(testRootFolderUUID), any(BaseOperateDataCallback.class));

    }

    @Test
    public void testUploadFolderNotExistCreateFolderReturnEEXIST() {

        prepareTest();

        FileUploadItem fileUploadItem = getFileUploadItem();
        FileUploadState fileUploadState = new FileUploadingState(fileUploadItem);

        mUploadFileUseCase.updateFile(fileUploadState);

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> getRootFolderCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUserHome), getRootFolderCaptor.capture());

        getRootFolderCaptor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());

        ArgumentCaptor<BaseOperateDataCallback<HttpResponse>> createRootFolderCallback = ArgumentCaptor.forClass(BaseOperateDataCallback.class);

        verify(mStationFileRepository).createFolderWithoutCreateNewThread(eq(UploadFileUseCase.UPLOAD_PARENT_FOLDER_NAME),
                eq(testUserHome), eq(testUserHome), createRootFolderCallback.capture());

        createRootFolderCallback.getValue().onFail(new OperationNetworkException(new HttpResponse(403,
                StationFileRepositoryTest.getJSONArrayStringWhenEEXIST(HttpErrorBodyParser.UPLOAD_FILE_EXIST_CODE))));

        assertTrue(mUploadFileUseCase.needRetryForEEXIST);

    }


}
