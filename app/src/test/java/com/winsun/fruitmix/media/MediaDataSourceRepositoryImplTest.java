package com.winsun.fruitmix.media;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.media.local.media.LocalMediaRepository;
import com.winsun.fruitmix.media.remote.media.StationMediaRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * Created by Administrator on 2017/7/19.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class MediaDataSourceRepositoryImplTest {

    @Mock
    private LocalMediaRepository localMediaRepository;

    @Mock
    private StationMediaRepository stationMediaRepository;

    @Mock
    private CalcMediaDigestStrategy calcMediaDigestStrategy;

    private MediaDataSourceRepositoryImpl mediaDataSourceRepositoryImpl;

    @Mock
    private BaseLoadDataCallback<Media> callback;

    @Captor
    private ArgumentCaptor<BaseLoadDataCallback<Media>> localMediaLoadCallbackCaptor;

    @Captor
    private ArgumentCaptor<BaseLoadDataCallback<Media>> stationMediaLoadCallbackCaptor;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        mediaDataSourceRepositoryImpl = MediaDataSourceRepositoryImpl.getInstance(localMediaRepository, stationMediaRepository, calcMediaDigestStrategy);

    }

    @After
    public void clean() {
        MediaDataSourceRepositoryImpl.destroyInstance();
    }

    @Test
    public void testGetMediaCallbackNotCall() {

        mediaDataSourceRepositoryImpl.getMedia(callback);

        verify(localMediaRepository).setCalcMediaDigestStrategy(any(CalcMediaDigestStrategy.class));

        verify(calcMediaDigestStrategy).setCalcMediaDigestCallback(any(CalcMediaDigestStrategy.CalcMediaDigestCallback.class));

        verify(localMediaRepository).getMedia(any(BaseLoadDataCallback.class));

        verify(stationMediaRepository).getMedia(any(BaseLoadDataCallback.class));

        verify(callback, never()).onSucceed(ArgumentMatchers.<Media>anyList(), any(OperationResult.class));

    }

    @Test
    public void testGetMediaCallbackCall() {

        mediaDataSourceRepositoryImpl.getMedia(callback);

        verify(localMediaRepository).getMedia(localMediaLoadCallbackCaptor.capture());

        verify(stationMediaRepository).getMedia(stationMediaLoadCallbackCaptor.capture());

        localMediaLoadCallbackCaptor.getValue().onSucceed(Collections.<Media>emptyList(), new OperationSuccess());
        stationMediaLoadCallbackCaptor.getValue().onSucceed(Collections.<Media>emptyList(), new OperationSuccess());

        verify(callback).onSucceed(ArgumentMatchers.<Media>anyList(), any(OperationResult.class));

    }

    @Test
    public void testGetMediaTwice() {

        mediaDataSourceRepositoryImpl.getMedia(new BaseLoadDataCallbackImpl<Media>());

        mediaDataSourceRepositoryImpl.getMedia(new BaseLoadDataCallbackImpl<Media>());

        verify(localMediaRepository).setCalcMediaDigestStrategy(any(CalcMediaDigestStrategy.class));

        verify(calcMediaDigestStrategy).setCalcMediaDigestCallback(any(CalcMediaDigestStrategy.CalcMediaDigestCallback.class));

        verify(stationMediaRepository).getMedia(any(BaseLoadDataCallback.class));

        verify(localMediaRepository, times(2)).getMedia(any(BaseLoadDataCallback.class));

    }

    @Test
    public void testGetStationMediaForceRefresh() {

        mediaDataSourceRepositoryImpl.getStationMediaForceRefresh(new BaseLoadDataCallbackImpl<Media>());

        verify(stationMediaRepository).setCacheDirty();

        verify(stationMediaRepository).getMedia(any(BaseLoadDataCallback.class));

    }


}
