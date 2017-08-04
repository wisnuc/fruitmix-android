package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.file.data.download.DownloadedItem;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadPendingState;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteFolder;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/19.
 */

public class FileRepositoryTest {

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

        fileRepository = StationFileRepositoryImpl.getInstance(stationFileDataSource, downloadedFileDataSource);

    }

    @After
    public void clean() {
        StationFileRepositoryImpl.destroyInstance();
    }

    private String rootUUID = "";
    private String folderUUID = "";

    @Test
    public void testGetFileMethodCall() {

        fileRepository.getFile(folderUUID, rootUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>());

        verify(stationFileDataSource).getFile(anyString(), anyString(), any(BaseLoadDataCallback.class));

    }

    @Test
    public void testGetFileStationFileMapMemoryCache() {

        String testFolderUUID = "testFolderUUID";

        AbstractRemoteFile file = new RemoteFolder();

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        fileRepository.getFile(testFolderUUID, rootUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>());

        verify(stationFileDataSource).getFile(anyString(), anyString(), captor.capture());

        captor.getValue().onSucceed(Collections.singletonList(file), new OperationSuccess());

        assertEquals(1, fileRepository.stationFileMap.size());

        assertNotNull(fileRepository.stationFileMap.get(testFolderUUID));

    }

    @Test
    public void testSetCacheDirtyLogic() {

        String folderUUID = "testFolderUUID";

        String secondFolderUUID = "testSecondFolderUUID";

        String rootUUID = "rootUUID";

        BaseLoadDataCallback<AbstractRemoteFile> callback = new BaseLoadDataCallbackImpl<>();

        fileRepository.getFile(folderUUID, rootUUID, callback);

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationFileDataSource).getFile(anyString(),anyString(), captor.capture());

        captor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());

        assertFalse(fileRepository.cacheDirty);

        fileRepository.getFile(secondFolderUUID, rootUUID, callback);

        verify(stationFileDataSource,times(2)).getFile(anyString(),anyString(), captor.capture());

        assertTrue(fileRepository.cacheDirty);

        captor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());

        assertFalse(fileRepository.cacheDirty);

    }


    @Test
    public void testGetFileTwice() {

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        fileRepository.getFile(folderUUID, rootUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>());

        verify(stationFileDataSource).getFile(anyString(), anyString(), captor.capture());

        captor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());

        assertFalse(fileRepository.cacheDirty);

        fileRepository.getFile(folderUUID, rootUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>());

        fileRepository.setCacheDirty();

        assertTrue(fileRepository.cacheDirty);

        fileRepository.getFile(folderUUID, rootUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>());

        verify(stationFileDataSource, times(2)).getFile(anyString(), anyString(), any(BaseLoadDataCallback.class));

    }


    @Test
    public void testDownloadFile() {

        FileDownloadItem fileDownloadItem = new FileDownloadItem("", 0, "");

        try {
            fileRepository.downloadFile("", new FileDownloadPendingState(fileDownloadItem), new BaseOperateDataCallbackImpl<FileDownloadItem>());

            verify(stationFileDataSource).downloadFile(any(FileDownloadState.class), captor.capture());

            captor.getValue().onSucceed(fileDownloadItem, new OperationSuccess());

            verify(downloadedFileDataSource).insertDownloadedFileRecord(any(DownloadedItem.class));


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
