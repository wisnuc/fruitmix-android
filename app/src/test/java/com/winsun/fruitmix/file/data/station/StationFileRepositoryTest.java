package com.winsun.fruitmix.file.data.station;

import android.support.annotation.NonNull;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.file.data.model.FinishedTaskItemWrapper;
import com.winsun.fruitmix.file.data.download.FileTaskManager;
import com.winsun.fruitmix.file.data.download.FinishedTaskItem;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadPendingState;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.FileTaskItem;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.file.data.model.RemoteFolder;
import com.winsun.fruitmix.file.data.upload.FileUploadFinishedState;
import com.winsun.fruitmix.file.data.upload.FileUploadItem;
import com.winsun.fruitmix.file.data.upload.FileUploadState;
import com.winsun.fruitmix.file.data.upload.FileUploadingState;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.mock.MockThreadManager;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.parser.HttpErrorBodyParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/19.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class StationFileRepositoryTest {

    //TODO:add UploadFileDataSource test

    private StationFileRepositoryImpl fileRepository;

    @Mock
    private StationFileDataSource stationFileDataSource;

    @Mock
    private DownloadedFileDataSource downloadedFileDataSource;

    @Mock
    private UploadFileDataSource uploadFileDataSource;

    @Mock
    private FileTaskManager fileTaskManager;

    @Mock
    private NetworkStateManager networkStateManager;

    @Captor
    private ArgumentCaptor<BaseOperateDataCallback<FileDownloadItem>> captor;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        fileRepository = StationFileRepositoryImpl.getInstance(fileTaskManager, stationFileDataSource, downloadedFileDataSource,
                uploadFileDataSource, new MockThreadManager());

    }

    @After
    public void clean() {
        StationFileRepositoryImpl.destroyInstance();
    }

    private String rootUUID = "";
    private String folderUUID = "";

    @Test
    public void testGetFileMethodCall() {

        fileRepository.getFile(rootUUID, folderUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>());

        verify(stationFileDataSource).getFile(anyString(), anyString(), any(BaseLoadDataCallback.class));

    }

    @Test
    public void testGetFileStationFileMapMemoryCache() {

        String testFolderUUID = "testFolderUUID";

        AbstractRemoteFile file = new RemoteFolder();

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        fileRepository.getFile(rootUUID, testFolderUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>());

        verify(stationFileDataSource).getFile(anyString(), anyString(), captor.capture());

        captor.getValue().onSucceed(Collections.singletonList(file), new OperationSuccess());

        assertEquals(1, fileRepository.stationFiles.size());

        assertEquals(testFolderUUID, fileRepository.stationFiles.get(0).getParentFolderUUID());

    }

    //NOTE:cache dirty logic is comment out

    @Ignore
    public void testSetCacheDirtyLogic() {

        String folderUUID = "testFolderUUID";

        String secondFolderUUID = "testSecondFolderUUID";

        String rootUUID = "rootUUID";

        BaseLoadDataCallback<AbstractRemoteFile> callback = new BaseLoadDataCallbackImpl<>();

        fileRepository.getFile(rootUUID, folderUUID, callback);

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationFileDataSource).getFile(anyString(), anyString(), captor.capture());

        captor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());

        assertFalse(fileRepository.cacheDirty);

        fileRepository.getFile(rootUUID, secondFolderUUID, callback);

        verify(stationFileDataSource, times(2)).getFile(anyString(), anyString(), captor.capture());

        assertTrue(fileRepository.cacheDirty);

        captor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());

        assertFalse(fileRepository.cacheDirty);

    }


    @Test
    public void testGetFileTwice() {

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        fileRepository.getFile(rootUUID, folderUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>());

        verify(stationFileDataSource).getFile(anyString(), anyString(), captor.capture());

        captor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());

        assertFalse(fileRepository.cacheDirty);

        fileRepository.getFile(rootUUID, folderUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>());

        verify(stationFileDataSource, times(2)).getFile(anyString(), anyString(), any(BaseLoadDataCallback.class));

    }

    @Test
    public void testDownloadFile() {

        FileDownloadItem fileDownloadItem = new FileDownloadItem("", 0, "");

        String currentUserUUID = "";

        try {
            fileRepository.downloadFile("", new FileDownloadPendingState(fileDownloadItem, fileRepository, currentUserUUID, networkStateManager),
                    new BaseOperateDataCallbackImpl<FileDownloadItem>());

            verify(stationFileDataSource).downloadFile(any(FileDownloadState.class), captor.capture());

            captor.getValue().onSucceed(fileDownloadItem, new OperationSuccess());

            verify(downloadedFileDataSource).insertFileTask(any(FinishedTaskItem.class));


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String testUUID1 = "testUUID1";

    @Test
    public void testDeleteDownloadedFileSucceed() {

        Collection<FinishedTaskItemWrapper> finishedTaskItemWrappers = new ArrayList<>();

        String testUUID2 = "testUUID2";

        finishedTaskItemWrappers.add(new FinishedTaskItemWrapper(testUUID1, "testName1"));
        finishedTaskItemWrappers.add(new FinishedTaskItemWrapper(testUUID2, "testName2"));

        when(uploadFileDataSource.deleteFileTask(anyString(), anyString())).thenReturn(false);
        when(downloadedFileDataSource.deleteDownloadedFile(anyString())).thenReturn(true);

        fileRepository.deleteFileFinishedTaskItems(finishedTaskItemWrappers, "", new BaseOperateDataCallbackImpl<Void>());

        InOrder inOrder = inOrder(downloadedFileDataSource, fileTaskManager);

        inOrder.verify(downloadedFileDataSource).deleteDownloadedFile(anyString());

        inOrder.verify(downloadedFileDataSource).deleteFileTask(anyString(), anyString());

        inOrder.verify(fileTaskManager).deleteFileTaskItem(eq(Collections.singletonList(testUUID1)));

        inOrder.verify(downloadedFileDataSource).deleteDownloadedFile(anyString());

        inOrder.verify(downloadedFileDataSource).deleteFileTask(anyString(), anyString());

        inOrder.verify(fileTaskManager).deleteFileTaskItem(eq(Collections.singletonList(testUUID2)));

    }

    @Test
    public void testDeleteDownloadedFileFail() {

        Collection<FinishedTaskItemWrapper> finishedTaskItemWrappers = new ArrayList<>();

        finishedTaskItemWrappers.add(new FinishedTaskItemWrapper("testUUID1", "testName1"));
        finishedTaskItemWrappers.add(new FinishedTaskItemWrapper("testUUID2", "testName2"));

        when(uploadFileDataSource.deleteFileTask(anyString(), anyString())).thenReturn(false);
        when(downloadedFileDataSource.deleteDownloadedFile(anyString())).thenReturn(false);

        fileRepository.deleteFileFinishedTaskItems(finishedTaskItemWrappers, "", new BaseOperateDataCallback<Void>() {
            @Override
            public void onSucceed(Void data, OperationResult result) {

            }

            @Override
            public void onFail(OperationResult result) {

            }
        });

        verify(downloadedFileDataSource, times(2)).deleteDownloadedFile(anyString());

        verify(downloadedFileDataSource, never()).deleteFileTask(anyString(), anyString());

        verify(fileTaskManager, never()).deleteFileTaskItem(ArgumentMatchers.<String>anyList());

    }

    @Test
    public void testDeleteUploadFileTaskSucceed() {

        String testFileName1 = "testFileName1";

        Collection<FinishedTaskItemWrapper> finishedTaskItemWrappers = new ArrayList<>();

        finishedTaskItemWrappers.add(new FinishedTaskItemWrapper(testUUID1, testFileName1));

        when(uploadFileDataSource.deleteFileTask(eq(testUUID1), anyString())).thenReturn(true);

        fileRepository.deleteFileFinishedTaskItems(finishedTaskItemWrappers, "", new BaseOperateDataCallbackImpl<Void>());

        verify(uploadFileDataSource).deleteFileTask(eq(testUUID1), eq(""));

        verify(downloadedFileDataSource, never()).deleteDownloadedFile(anyString());

        verify(downloadedFileDataSource, never()).deleteFileTask(anyString(), anyString());

        verify(fileTaskManager).deleteFileTaskItem(ArgumentMatchers.<String>anyList());

    }

    @Test
    public void testFillAllFinishTaskItemIntoFileTaskManager() {

        String testUserUUID = "";

        FinishedTaskItem finishedTaskItem = new FinishedTaskItem(new FileDownloadItem());

        FinishedTaskItem finishedTaskItem1 = new FinishedTaskItem(new FileUploadItem());

        when(downloadedFileDataSource.getCurrentLoginUserFileFinishedTaskItem(eq(testUserUUID))).thenReturn(Collections.singletonList(finishedTaskItem));

        when(uploadFileDataSource.getCurrentLoginUserFileFinishedTaskItem(eq(testUserUUID))).thenReturn(Collections.singletonList(finishedTaskItem1));

        fileRepository.fillAllFinishTaskItemIntoFileTaskManager(testUserUUID);

        verify(downloadedFileDataSource).getCurrentLoginUserFileFinishedTaskItem(eq(testUserUUID));
        verify(uploadFileDataSource).getCurrentLoginUserFileFinishedTaskItem(eq(testUserUUID));

        verify(fileTaskManager, times(2)).addFinishedFileTaskItem(any(FileTaskItem.class));

    }

    @Test
    public void testInsertFileUploadTask() {

        fileRepository.insertFileUploadTask(new FileUploadItem(), "");

        verify(uploadFileDataSource).insertFileTask(any(FinishedTaskItem.class));

    }

    @Test
    public void testUploadFileWithProgressSucceed() {

        LocalFile localFile = new LocalFile();

        FileUploadState fileUploadState = new FileUploadingState(new FileUploadItem());

        when(stationFileDataSource.uploadFileWithProgress(any(LocalFile.class), any(FileUploadState.class), anyString(), anyString()))
                .thenReturn(new OperationSuccess());

        fileRepository.uploadFileWithProgress(localFile, fileUploadState, "", "", "");

        verify(stationFileDataSource).uploadFileWithProgress(eq(localFile), eq(fileUploadState), eq(""), eq(""));


    }

    @Test
    public void testUploadFileWithProgress403Error() {

        LocalFile localFile = new LocalFile();

        FileUploadItem fileUploadItem = new FileUploadItem();

        FileUploadState fileUploadState = new FileUploadingState(fileUploadItem);

        when(stationFileDataSource.uploadFileWithProgress(any(LocalFile.class), any(FileUploadState.class), anyString(), anyString()))
                .thenReturn(new OperationNetworkException(new HttpResponse(403, getJSONArrayStringWhenEEXIST(HttpErrorBodyParser.UPLOAD_FILE_EXIST_CODE))));

        OperationResult result = fileRepository.uploadFileWithProgress(localFile, fileUploadState, "", "", "");

        verify(stationFileDataSource).uploadFileWithProgress(eq(localFile), eq(fileUploadState), eq(""), eq(""));

        assertTrue(result instanceof OperationSuccess);

        assertTrue(fileUploadItem.getFileUploadState() instanceof FileUploadFinishedState);

    }

    @NonNull
    public static String getJSONArrayStringWhenEEXIST(String message) {
        JSONArray jsonArray = new JSONArray();

        JSONObject jsonObject = new JSONObject();

        JSONObject error = new JSONObject();

        try {
            error.put("message", message);

            jsonObject.put("error", error);

            jsonArray.put(jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray.toString();
    }


}
