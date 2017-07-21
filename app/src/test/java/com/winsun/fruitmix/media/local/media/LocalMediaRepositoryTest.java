package com.winsun.fruitmix.media.local.media;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.media.CalcMediaDigestStrategy;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/18.
 */

public class LocalMediaRepositoryTest {

    @Mock
    private LocalMediaAppDBDataSource localMediaAppDBDataSource;

    @Mock
    private LocalMediaSystemDBDataSource localMediaSystemDBDataSource;

    @Mock
    private CalcMediaDigestStrategy calcMediaDigestStrategy;

    private LocalMediaRepository localMediaRepository;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        localMediaRepository = LocalMediaRepository.getInstance(localMediaAppDBDataSource, localMediaSystemDBDataSource);
    }

    @After
    public void clean() {
        LocalMediaRepository.destroyInstance();
    }

    private String testMediaOriginalPath = "testMediaOriginalPath";
    private String testMediaOriginalPathFromSystemDB = "testMediaOriginalPathFromSystemDB";

    @Test
    public void getMediaFromAppDBSucceed() {

        localMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        ArgumentCaptor<BaseLoadDataCallback<Media>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(localMediaAppDBDataSource).getMedia(captor.capture());

        Media media = new Media();
        media.setOriginalPhotoPath(testMediaOriginalPath);

        captor.getValue().onSucceed(Collections.singletonList(media), new OperationSuccess());

        assertEquals(1, localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.size());

        assertNotNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testMediaOriginalPath));

    }

    @Test
    public void getMediaFromSystemDBSucceed() {

        getMediaFromAppDBSucceed();

        ArgumentCaptor<BaseLoadDataCallback<Media>> localMediaSystemDBLoadCallback = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(localMediaSystemDBDataSource).getMedia(ArgumentMatchers.<String>anyCollection(), localMediaSystemDBLoadCallback.capture());

        Media newMediaFromSystemDB = new Media();
        newMediaFromSystemDB.setOriginalPhotoPath(testMediaOriginalPathFromSystemDB);

        localMediaRepository.setCalcMediaDigestStrategy(calcMediaDigestStrategy);

        when(calcMediaDigestStrategy.handleMedia(ArgumentMatchers.<Media>anyCollection())).thenReturn(Collections.<Media>emptyList());

        localMediaSystemDBLoadCallback.getValue().onSucceed(Collections.singletonList(newMediaFromSystemDB), new OperationSuccess());

        assertEquals(2, localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.size());

        assertNotNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testMediaOriginalPathFromSystemDB));

        verify(calcMediaDigestStrategy).handleMedia(ArgumentMatchers.<Media>anyCollection());

        verify(localMediaAppDBDataSource).insertMedias(ArgumentMatchers.<Media>anyCollection());

    }

    @Test
    public void getMediaTwice() {

        ArgumentCaptor<BaseLoadDataCallback<Media>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        localMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        localMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        verify(localMediaAppDBDataSource).getMedia(captor.capture());

        captor.getValue().onSucceed(Collections.<Media>emptyList(), new OperationSuccess());

        verify(localMediaSystemDBDataSource, times(2)).getMedia(ArgumentMatchers.<String>anyCollection(), any(BaseLoadDataCallback.class));

    }

}
