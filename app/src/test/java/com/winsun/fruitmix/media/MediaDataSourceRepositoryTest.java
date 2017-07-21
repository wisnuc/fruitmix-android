package com.winsun.fruitmix.media;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.media.local.media.LocalMediaRepository;
import com.winsun.fruitmix.media.remote.media.StationMediaRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by Administrator on 2017/7/19.
 */

public class MediaDataSourceRepositoryTest {

    @Mock
    private LocalMediaRepository localMediaRepository;

    @Mock
    private StationMediaRepository stationMediaRepository;

    @Mock
    private CalcMediaDigestStrategy calcMediaDigestStrategy;

    private MediaDataSourceRepository mediaDataSourceRepository;

    @Mock
    private BaseLoadDataCallback<Media> callback;

    @Captor
    private ArgumentCaptor<BaseLoadDataCallback<Media>> localMediaLoadCallbackCaptor;

    @Captor
    private ArgumentCaptor<BaseLoadDataCallback<Media>> stationMediaLoadCallbackCaptor;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        mediaDataSourceRepository = MediaDataSourceRepository.getInstance(localMediaRepository, stationMediaRepository, calcMediaDigestStrategy);

    }

    @After
    public void clean() {
        MediaDataSourceRepository.destroyInstance();
    }

    @Test
    public void testGetMediaCallbackNotCall() {

        mediaDataSourceRepository.getMedia(callback);

        verify(localMediaRepository).setCalcMediaDigestStrategy(any(CalcMediaDigestStrategy.class));

        verify(calcMediaDigestStrategy).setCalcMediaDigestCallback(any(CalcMediaDigestStrategy.CalcMediaDigestCallback.class));

        verify(localMediaRepository).getMedia(any(BaseLoadDataCallback.class));

        verify(stationMediaRepository).getMedia(any(BaseLoadDataCallback.class));

        verify(callback, never()).onSucceed(ArgumentMatchers.<Media>anyList(), any(OperationResult.class));

    }

    @Test
    public void testGetMediaCallbackCall() {

        mediaDataSourceRepository.getMedia(callback);

        verify(localMediaRepository).getMedia(localMediaLoadCallbackCaptor.capture());

        verify(stationMediaRepository).getMedia(stationMediaLoadCallbackCaptor.capture());

        localMediaLoadCallbackCaptor.getValue().onSucceed(Collections.<Media>emptyList(), new OperationSuccess());
        stationMediaLoadCallbackCaptor.getValue().onSucceed(Collections.<Media>emptyList(), new OperationSuccess());

        verify(callback).onSucceed(ArgumentMatchers.<Media>anyList(), any(OperationResult.class));

    }

    @Test
    public void testGetMediaTwice() {

        mediaDataSourceRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        mediaDataSourceRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        verify(localMediaRepository).setCalcMediaDigestStrategy(any(CalcMediaDigestStrategy.class));

        verify(calcMediaDigestStrategy).setCalcMediaDigestCallback(any(CalcMediaDigestStrategy.CalcMediaDigestCallback.class));

        verify(stationMediaRepository).getMedia(any(BaseLoadDataCallback.class));

        verify(localMediaRepository, times(2)).getMedia(any(BaseLoadDataCallback.class));

    }


}
