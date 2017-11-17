package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.file.data.download.DownloadedFileWrapper;
import com.winsun.fruitmix.file.data.download.FinishedTaskItem;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadPendingState;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteFolder;
import com.winsun.fruitmix.mock.MockThreadManager;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/19.
 */

public class StationFileRepositoryTest {

    private StationFileRepositoryImpl fileRepository;

    @Mock
    private StationFileDataSource stationFileDataSource;

    @Mock
    private DownloadedFileDataSource downloadedFileDataSource;

    @Captor
    private ArgumentCaptor<BaseOperateDataCallback<FileDownloadItem>> captor;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        fileRepository = StationFileRepositoryImpl.getInstance(stationFileDataSource, downloadedFileDataSource,new MockThreadManager());

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

    //cache dirty logic is comment out

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
            fileRepository.downloadFile("", new FileDownloadPendingState(fileDownloadItem, fileRepository, currentUserUUID), new BaseOperateDataCallbackImpl<FileDownloadItem>());

            verify(stationFileDataSource).downloadFile(any(FileDownloadState.class), captor.capture());

            captor.getValue().onSucceed(fileDownloadItem, new OperationSuccess());

            verify(downloadedFileDataSource).insertDownloadedFileRecord(any(FinishedTaskItem.class));


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testDeleteDownloadedFileSucceed() {

        Collection<DownloadedFileWrapper> downloadedFileWrappers = new ArrayList<>();

        downloadedFileWrappers.add(new DownloadedFileWrapper("testUUID1", "testName1"));
        downloadedFileWrappers.add(new DownloadedFileWrapper("testUUID2", "testName2"));

        when(downloadedFileDataSource.deleteDownloadedFile(anyString())).thenReturn(true);

        fileRepository.deleteDownloadedFile(downloadedFileWrappers, "", new BaseOperateDataCallbackImpl<Void>());

        InOrder inOrder = inOrder(downloadedFileDataSource);

        inOrder.verify(downloadedFileDataSource).deleteDownloadedFile(anyString());

        inOrder.verify(downloadedFileDataSource).deleteDownloadedFileRecord(anyString(), anyString());

        inOrder.verify(downloadedFileDataSource).deleteDownloadedFile(anyString());

        inOrder.verify(downloadedFileDataSource).deleteDownloadedFileRecord(anyString(), anyString());

    }


    @Test
    public void testDeleteDownloadedFileFail() {

        Collection<DownloadedFileWrapper> downloadedFileWrappers = new ArrayList<>();

        downloadedFileWrappers.add(new DownloadedFileWrapper("testUUID1", "testName1"));
        downloadedFileWrappers.add(new DownloadedFileWrapper("testUUID2", "testName2"));

        when(downloadedFileDataSource.deleteDownloadedFile(anyString())).thenReturn(false);

        fileRepository.deleteDownloadedFile(downloadedFileWrappers, "", new BaseOperateDataCallback<Void>() {
            @Override
            public void onSucceed(Void data, OperationResult result) {

            }

            @Override
            public void onFail(OperationResult result) {

            }
        });

        verify(downloadedFileDataSource, times(2)).deleteDownloadedFile(anyString());

        verify(downloadedFileDataSource, never()).deleteDownloadedFileRecord(anyString(), anyString());

    }


}
