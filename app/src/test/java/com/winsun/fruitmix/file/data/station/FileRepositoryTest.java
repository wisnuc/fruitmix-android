package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteFolder;
import com.winsun.fruitmix.file.data.station.FileRepository;
import com.winsun.fruitmix.file.data.station.StationFileDataSource;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/19.
 */

public class FileRepositoryTest {

    private FileRepository fileRepository;

    @Mock
    private StationFileDataSource stationFileDataSource;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        fileRepository = FileRepository.getInstance(stationFileDataSource);

    }

    @After
    public void clean(){
        FileRepository.destroyInstance();
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

}
