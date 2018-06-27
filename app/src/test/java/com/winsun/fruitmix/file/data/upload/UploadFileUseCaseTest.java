package com.winsun.fruitmix.file.data.upload;

import android.support.annotation.NonNull;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
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
import com.winsun.fruitmix.model.operationResult.OperationSuccessWithFile;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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

    @Ignore
    public void testAutoUploadFileWhenFileExist() {

        prepareTest();

        NetworkState networkState = new NetworkState(true, true);

        when(mNetworkStateManager.getNetworkState()).thenReturn(networkState);

        when(mSystemSettingDataSource.getOnlyAutoUploadWhenConnectedWithWifi()).thenReturn(true);

        FileUploadItem fileUploadItem = getFileUploadItem();

        FileUploadState fileUploadState = new FileUploadingState(fileUploadItem);

        setUploadFolderExist();

        AbstractRemoteFile uploadFolder = new RemoteFile();
        ((RemoteFile) uploadFolder).setFileHash(testUploadFileUUID);

        when(mStationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome), eq(testUploadFolderUUID),eq(""))).thenReturn(new OperationSuccessWithFile(Collections.singletonList(uploadFolder)));

        mUploadFileUseCase.updateFile(fileUploadState);

        verifyUploadFolderExist();

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUploadFolderUUID),eq(""));

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

        when(mFileTool.getTemporaryUploadFolderPath(anyString(), anyString())).thenReturn("");
    }

    private void setUploadFolderExist() {

        AbstractRemoteFile file = new RemoteFile();
        file.setName(UploadFileUseCase.UPLOAD_PARENT_FOLDER_NAME);
        file.setUuid(testRootFolderUUID);

        when(mStationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome), eq(testUserHome),eq(""))).thenReturn(new OperationSuccessWithFile(Collections.singletonList(file)));

        AbstractRemoteFile uploadFolder = new RemoteFile();
        uploadFolder.setName(mUploadFileUseCase.getUploadFolderName());
        uploadFolder.setUuid(testUploadFolderUUID);

        when(mStationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome), eq(testRootFolderUUID),eq(""))).thenReturn(new OperationSuccessWithFile(Collections.singletonList(uploadFolder)));

    }

    private void verifyUploadFolderExist() {

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUserHome),eq(""));

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testRootFolderUUID),eq(""));

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

        verify(mNetworkStateManager).getNetworkState();

        verify(mSystemSettingDataSource).getOnlyAutoUploadWhenConnectedWithWifi();

        assertTrue(fileUploadItem.getFileUploadState() instanceof FileUploadPendingState);

        assertFalse(mUploadFileUseCase.needRetryForCreateFolderEEXIST);

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

        when(mFileTool.copyFileToDir(anyString(), anyString(), anyString())).thenReturn(true);

        when(mStationFileRepository.uploadFileWithProgress(any(LocalFile.class), any(FileUploadState.class), anyString(), anyString(), anyString()))
                .thenReturn(new OperationSuccess());

        setUploadFolderExist();

        when(mStationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome),eq(testUploadFolderUUID),eq(""))).thenReturn(new OperationSuccessWithFile(Collections.<AbstractRemoteFile>emptyList()));

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

        verify(mFileTool).copyFileToDir(eq(testFilePath), eq(testFileName), eq(fileTemporaryFolderPath));

        verifyUploadFolderExist();

/*
        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> getUploadFolderCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUploadFolderUUID), getUploadFolderCaptor.capture());

        getUploadFolderCaptor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());
*/

        verify(mStationFileRepository).uploadFileWithProgress(any(LocalFile.class), eq(fileUploadState), eq(testUserHome),
                eq(testUploadFolderUUID), eq(testUserUUID));

        verify(mStationFileRepository).insertFileUploadTask(eq(fileUploadItem), eq(testUserUUID));

        String copyFilePath = fileTemporaryFolderPath + File.separator + testFileName;

        verify(mFileTool).copyFileToDir(eq(copyFilePath), eq(testFileName), eq(FileUtil.getDownloadFileStoreFolderPath()));

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

        when(mFileTool.copyFileToDir(anyString(), anyString(), anyString())).thenReturn(true);

        when(mStationFileRepository.uploadFileWithProgress(any(LocalFile.class), any(FileUploadState.class), anyString(), anyString(), anyString()))
                .thenReturn(new OperationFail(""));

        setUploadFolderExist();

        when(mStationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome),eq(testUploadFolderUUID),eq(""))).thenReturn(new OperationSuccessWithFile(Collections.<AbstractRemoteFile>emptyList()));

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

        verify(mFileTool).copyFileToDir(eq(testFilePath), eq(testFileName), eq(fileTemporaryFolderPath));

        verifyUploadFolderExist();

/*        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> getUploadFolderCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUploadFolderUUID), getUploadFolderCaptor.capture());

        getUploadFolderCaptor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());*/

        verify(mStationFileRepository).uploadFileWithProgress(any(LocalFile.class), eq(fileUploadState), eq(testUserHome),
                eq(testUploadFolderUUID), eq(testUserUUID));

        verify(mStationFileRepository, never()).insertFileUploadTask(eq(fileUploadItem), eq(testUserUUID));

        String copyFilePath = fileTemporaryFolderPath + File.separator + testFileName;

        verify(mFileTool, never()).copyFileToDir(eq(copyFilePath), eq(FileUtil.getDownloadFileStoreFolderPath()));

        verify(mFileTool, never()).deleteFile(eq(copyFilePath));

    }

    @Test
    public void testUploadFolderNotExistCreateFolder() {

        prepareTest();

        NetworkState networkState = new NetworkState(true, true);

        when(mNetworkStateManager.getNetworkState()).thenReturn(networkState);

        when(mSystemSettingDataSource.getOnlyAutoUploadWhenConnectedWithWifi()).thenReturn(true);

        when(mStationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome), eq(testUserHome),eq(""))).thenReturn(new OperationSuccessWithFile(Collections.<AbstractRemoteFile>emptyList()));

        FileUploadItem fileUploadItem = getFileUploadItem();
        FileUploadState fileUploadState = new FileUploadingState(fileUploadItem);

        mUploadFileUseCase.updateFile(fileUploadState);

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUserHome),eq(""));

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

        NetworkState networkState = new NetworkState(true, true);

        when(mNetworkStateManager.getNetworkState()).thenReturn(networkState);

        when(mSystemSettingDataSource.getOnlyAutoUploadWhenConnectedWithWifi()).thenReturn(true);

        when(mStationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome), eq(testUserHome),eq(""))).thenReturn(new OperationSuccessWithFile(Collections.<AbstractRemoteFile>emptyList()));

        FileUploadItem fileUploadItem = getFileUploadItem();
        FileUploadState fileUploadState = new FileUploadingState(fileUploadItem);

        mUploadFileUseCase.updateFile(fileUploadState);

        verify(mStationFileRepository).getFileWithoutCreateNewThread(eq(testUserHome), eq(testUserHome),eq(""));

        ArgumentCaptor<BaseOperateDataCallback<HttpResponse>> createRootFolderCallback = ArgumentCaptor.forClass(BaseOperateDataCallback.class);

        verify(mStationFileRepository).createFolderWithoutCreateNewThread(eq(UploadFileUseCase.UPLOAD_PARENT_FOLDER_NAME),
                eq(testUserHome), eq(testUserHome), createRootFolderCallback.capture());

        createRootFolderCallback.getValue().onFail(new OperationNetworkException(new HttpResponse(403,
                StationFileRepositoryTest.getJSONArrayStringWhenEEXIST(HttpErrorBodyParser.UPLOAD_FILE_EXIST_CODE))));

        assertTrue(mUploadFileUseCase.needRetryForCreateFolderEEXIST);

    }

    @Test
    public void testUploadFileWithProgress403Error() {

        prepareTest();

        NetworkState networkState = new NetworkState(true, true);

        when(mNetworkStateManager.getNetworkState()).thenReturn(networkState);

        when(mSystemSettingDataSource.getOnlyAutoUploadWhenConnectedWithWifi()).thenReturn(true);

        when(mStationFileRepository.uploadFileWithProgress(any(LocalFile.class), any(FileUploadState.class), anyString(), anyString(), anyString()))
                .thenReturn(new OperationNetworkException(new HttpResponse(403, StationFileRepositoryTest.getJSONArrayStringWhenEEXIST(HttpErrorBodyParser.UPLOAD_FILE_EXIST_CODE))))
                .thenReturn(new OperationSuccess());

        setUploadFolderExist();

        when(mStationFileRepository.getFileWithoutCreateNewThread(eq(testUserHome),eq(testUploadFolderUUID),eq(""))).thenReturn(new OperationSuccessWithFile(Collections.<AbstractRemoteFile>emptyList()));

        FileUploadItem fileUploadItem = getFileUploadItem();
        fileUploadItem.setFileName("test.txt");

        FileUploadState fileUploadState = new FileUploadingState(fileUploadItem);

        mUploadFileUseCase.updateFile(fileUploadState);

        verifyUploadFolderExist();

        verify(mStationFileRepository, times(2))
                .uploadFileWithProgress(any(LocalFile.class), eq(fileUploadState), anyString(), anyString(), anyString());


    }


}
