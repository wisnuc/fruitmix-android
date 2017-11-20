package com.winsun.fruitmix.file.data.upload;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
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

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Administrator on 2017/11/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class UploadFileUseCaseTest {

    //TODO:create upload file usecase test

    private UploadFileUseCase mUploadFileUseCase;

    @Mock
    private UserDataRepository mUserDataRepository;

    @Mock
    private StationFileRepository mStationFileRepository;

    @Mock
    private SystemSettingDataSource mSystemSettingDataSource;

    @Mock
    private NetworkStateManager mNetworkStateManager;

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
                mNetworkStateManager, uploadFolderName, fileTemporaryFolderPath);

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

        when(mSystemSettingDataSource.getCurrentLoginUserUUID()).thenReturn(testUserUUID);

        when(mUserDataRepository.getUserByUUID(eq(testUserUUID))).thenReturn(createUser());

        FileUploadItem fileUploadItem = new FileUploadItem();
        fileUploadItem.setFileUUID(testUploadFileUUID);

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

        FileUploadState fileUploadState = new FileUploadingState(new FileUploadItem());

        mUploadFileUseCase.updateFile(fileUploadState);


    }


    @Test
    public void testAutoUploadFileWhen403EEXISTOccur() {


    }


}
