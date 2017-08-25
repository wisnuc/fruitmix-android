package com.winsun.fruitmix.media.remote.media;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.user.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/18.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class StationMediaRepositoryTest {

    private StationMediaRepository stationMediaRepository;

    @Mock
    private StationMediaRemoteDataSource stationMediaRemoteDataSource;

    @Mock
    private StationMediaDBDataSource stationMediaDBDataSource;

    private String testMediaUUID = "testMediaUUID";
    private String testMedia2UUID = "testMedia2UUID";

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        stationMediaRepository = StationMediaRepository.getInstance(stationMediaDBDataSource, stationMediaRemoteDataSource);

    }

    @After
    public void clean() {
        StationMediaRepository.destroyInstance();
    }


    private Media createMedia(String mediaUUID) {

        Media media = new Media();
        media.setUuid(mediaUUID);

        return media;
    }


    @Test
    public void getMediaRetrieveOrder() {

        stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        verify(stationMediaRemoteDataSource).getMedia(any(BaseLoadDataCallback.class));

    }

    @Test
    public void getMediaWhenCacheDirty() {

        stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        ArgumentCaptor<BaseLoadDataCallback<Media>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationMediaRemoteDataSource).getMedia(captor.capture());

        captor.getValue().onSucceed(Collections.<Media>emptyList(), new OperationSuccess());

        verify(stationMediaRemoteDataSource, times(1)).getMedia(any(BaseLoadDataCallback.class));

        stationMediaRepository.setCacheDirty();

        stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        verify(stationMediaRemoteDataSource, times(2)).getMedia(any(BaseLoadDataCallback.class));

    }

    @Test
    public void getMediaTwiceWhenCacheDirtyAndHasNotNewMedia() {

        Media media = createMedia(testMediaUUID);

        stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        ArgumentCaptor<BaseLoadDataCallback<Media>> firstCallCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationMediaRemoteDataSource).getMedia(firstCallCaptor.capture());

        firstCallCaptor.getValue().onSucceed(Collections.singletonList(media), new OperationSuccess());

        stationMediaRepository.setCacheDirty();

        stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        ArgumentCaptor<BaseLoadDataCallback<Media>> secondCallCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationMediaRemoteDataSource, times(2)).getMedia(secondCallCaptor.capture());

        secondCallCaptor.getValue().onSucceed(Collections.singletonList(media), new OperationSuccess());

        verify(stationMediaDBDataSource, times(1)).clearAllMedias();
        verify(stationMediaDBDataSource, times(1)).insertMedias(ArgumentMatchers.<Media>anyCollection());

    }

    @Test
    public void getMediaTwiceWhenCacheDirtyAndHasNewMedia() {

        Media media = createMedia(testMediaUUID);

        stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        ArgumentCaptor<BaseLoadDataCallback<Media>> firstCallCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationMediaRemoteDataSource).getMedia(firstCallCaptor.capture());

        firstCallCaptor.getValue().onSucceed(Collections.singletonList(media), new OperationSuccess());

        stationMediaRepository.setCacheDirty();

        List<Media> medias = new ArrayList<>();
        medias.add(createMedia(testMediaUUID));
        medias.add(createMedia(testMedia2UUID));

        stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        ArgumentCaptor<BaseLoadDataCallback<Media>> secondCallCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationMediaRemoteDataSource, times(2)).getMedia(secondCallCaptor.capture());

        secondCallCaptor.getValue().onSucceed(medias, new OperationSuccess());

        verify(stationMediaDBDataSource, times(2)).clearAllMedias();
        verify(stationMediaDBDataSource, times(2)).insertMedias(ArgumentMatchers.<Media>anyCollection());

    }


    @Test
    public void getMedia_RetrieveFromStationFailThenRetrieveFromDBSucceed() {

        stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        assertEquals(true, stationMediaRepository.cacheDirty);

        ArgumentCaptor<BaseLoadDataCallback<Media>> stationMediaRemoteDataCaptor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationMediaRemoteDataSource).getMedia(stationMediaRemoteDataCaptor.capture());

        stationMediaRemoteDataCaptor.getValue().onFail(new OperationIOException());

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
