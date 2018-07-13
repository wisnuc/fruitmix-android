package com.winsun.fruitmix.file.data.station;

import android.support.annotation.NonNull;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteFolder;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.mock.MockThreadManager;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/19.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class StationFileRepositoryTest {

    private StationFileRepositoryImpl fileRepository;

    @Mock
    private StationFileDataSource stationFileDataSource;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        fileRepository = StationFileRepositoryImpl.getInstance(stationFileDataSource, new MockThreadManager());

    }

    @After
    public void clean() {
        StationFileRepositoryImpl.destroyInstance();
    }

    private String rootUUID = "";
    private String folderUUID = "";
    private String folderName = "";

    @Test
    public void testGetFileMethodCall() {

        fileRepository.getFile(rootUUID, folderUUID, folderName, new BaseLoadDataCallbackImpl<AbstractRemoteFile>());

        verify(stationFileDataSource).getFile(anyString(), anyString(), any(BaseLoadDataCallback.class));

    }

    @Test
    public void testGetFileStationFileMapMemoryCache() {

        String testFolderUUID = "testFolderUUID";
        String testFolderName = "testFolderName";

        AbstractRemoteFile file = new RemoteFolder();

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        fileRepository.getFile(rootUUID, testFolderUUID, testFolderName, new BaseLoadDataCallbackImpl<AbstractRemoteFile>());

        verify(stationFileDataSource).getFile(anyString(), anyString(), captor.capture());

        captor.getValue().onSucceed(Collections.singletonList(file), new OperationSuccess());

    }

    //NOTE:cache dirty logic is comment out

    @Ignore
    public void testSetCacheDirtyLogic() {

        String folderUUID = "testFolderUUID";
        String folderName = "testFolderName";

        String secondFolderUUID = "testSecondFolderUUID";
        String secondFolderName = "testSecondFolderName";

        String rootUUID = "rootUUID";

        BaseLoadDataCallback<AbstractRemoteFile> callback = new BaseLoadDataCallbackImpl<>();

        fileRepository.getFile(rootUUID, folderUUID, folderName, callback);

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationFileDataSource).getFile(anyString(), anyString(), captor.capture());

        captor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());

        assertFalse(fileRepository.cacheDirty);

        fileRepository.getFile(rootUUID, secondFolderUUID, secondFolderName, callback);

        verify(stationFileDataSource, times(2)).getFile(anyString(), anyString(), captor.capture());

        assertTrue(fileRepository.cacheDirty);

        captor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());

        assertFalse(fileRepository.cacheDirty);

    }


    @Test
    public void testGetFileTwice() {

        ArgumentCaptor<BaseLoadDataCallback<AbstractRemoteFile>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        fileRepository.getFile(rootUUID, folderUUID, folderName, new BaseLoadDataCallbackImpl<AbstractRemoteFile>());

        verify(stationFileDataSource).getFile(anyString(), anyString(), captor.capture());

        captor.getValue().onSucceed(Collections.<AbstractRemoteFile>emptyList(), new OperationSuccess());

        assertFalse(fileRepository.cacheDirty);

        fileRepository.getFile(rootUUID, folderUUID, folderName, new BaseLoadDataCallbackImpl<AbstractRemoteFile>());

        verify(stationFileDataSource, times(2)).getFile(anyString(), anyString(), any(BaseLoadDataCallback.class));

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
