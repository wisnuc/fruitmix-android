package com.winsun.fruitmix.media.remote.media;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.user.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/18.
 */

public class StationMediaRepositoryTest {

    private StationMediaRepository stationMediaRepository;

    @Mock
    private StationMediaRemoteDataSource stationMediaRemoteDataSource;

    @Mock
    private StationMediaDBDataSource stationMediaDBDataSource;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        stationMediaRepository = StationMediaRepository.getInstance(stationMediaDBDataSource, stationMediaRemoteDataSource);

    }

    @After
    public void clean() {
        StationMediaRepository.destroyInstance();
    }

    @Test
    public void getMediaRetrieveOrder() {

        stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        InOrder inOrder = inOrder(stationMediaRemoteDataSource, stationMediaDBDataSource);

        inOrder.verify(stationMediaDBDataSource).getMedia(any(BaseLoadDataCallback.class));

        inOrder.verify(stationMediaRemoteDataSource).getMedia(any(BaseLoadDataCallback.class));

    }

    @Test
    public void getMediaWhenCacheDirty() {

        stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        ArgumentCaptor<BaseLoadDataCallback<Media>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationMediaDBDataSource).getMedia(captor.capture());

        captor.getValue().onSucceed(Collections.<Media>emptyList(), new OperationSuccess());

        stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        verify(stationMediaDBDataSource, times(1)).getMedia(any(BaseLoadDataCallback.class));
        verify(stationMediaRemoteDataSource, times(1)).getMedia(any(BaseLoadDataCallback.class));

        stationMediaRepository.setCacheDirty();

        stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        verify(stationMediaDBDataSource, times(2)).getMedia(any(BaseLoadDataCallback.class));
        verify(stationMediaRemoteDataSource, times(2)).getMedia(any(BaseLoadDataCallback.class));

    }

    private String testMediaUUID = "testMediaUUID";

    @Test
    public void getMedia_RetrieveFromDBSucceed() {

        stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        assertEquals(true, stationMediaRepository.cacheDirty);

        ArgumentCaptor<BaseLoadDataCallback<Media>> loadStationMediaRemoteDataCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationMediaDBDataSource).getMedia(loadStationMediaRemoteDataCaptor.capture());

        Media media = new Media();
        media.setUuid(testMediaUUID);

        loadStationMediaRemoteDataCaptor.getValue().onSucceed(Collections.singletonList(media), new OperationSuccess());

        assertEquals(1, stationMediaRepository.mediaConcurrentMap.size());

        Media result = stationMediaRepository.mediaConcurrentMap.get(testMediaUUID);

        assertNotNull(result);

        assertEquals(false, stationMediaRepository.cacheDirty);

    }

    @Test
    public void getMedia_RetrieveFromStationSucceed() {

        Media media = new Media();
        media.setUuid(testMediaUUID);

        stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        ArgumentCaptor<BaseLoadDataCallback<Media>> loadStationMediaDBDataCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationMediaDBDataSource).getMedia(loadStationMediaDBDataCaptor.capture());

        loadStationMediaDBDataCaptor.getValue().onSucceed(Collections.singletonList(media), new OperationSuccess());

        ArgumentCaptor<BaseLoadDataCallback<Media>> loadStationMediaRemoteDataCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationMediaRemoteDataSource).getMedia(loadStationMediaRemoteDataCaptor.capture());

        loadStationMediaRemoteDataCaptor.getValue().onSucceed(Collections.singletonList(media), new OperationSuccess());

        verify(stationMediaDBDataSource).clearAllMedias();

        verify(stationMediaDBDataSource).insertMedias(ArgumentMatchers.<Media>anyCollection());

        assertEquals(1, stationMediaRepository.mediaConcurrentMap.size());

        Media result = stationMediaRepository.mediaConcurrentMap.get(testMediaUUID);

        assertNotNull(result);

    }


}
