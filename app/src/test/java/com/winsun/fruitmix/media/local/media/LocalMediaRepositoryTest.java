package com.winsun.fruitmix.media.local.media;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.media.CalcMediaDigestStrategy;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.mock.MockThreadManager;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/18.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
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

        MockThreadManager threadManager = new MockThreadManager();

        localMediaRepository = LocalMediaRepository.getInstance(localMediaAppDBDataSource, localMediaSystemDBDataSource, threadManager);
    }

    @After
    public void clean() {
        LocalMediaRepository.destroyInstance();
    }

    private String testMediaOriginalPath = "testMediaOriginalPath";
    private String testMediaOriginalPathFromSystemDB = "testMediaOriginalPathFromSystemDB";

    private Media createMediaFromAppDB() {
        Media media = new Media();
        media.setOriginalPhotoPath(testMediaOriginalPath);

        return media;
    }

    @Test
    public void getMediaFromAppDBSucceed() {

        localMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        ArgumentCaptor<BaseLoadDataCallback<Media>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(localMediaAppDBDataSource).getMedia(captor.capture());

        Media media = createMediaFromAppDB();

        captor.getValue().onSucceed(Collections.singletonList(media), new OperationSuccess());

        assertEquals(1, localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.size());

        assertNotNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testMediaOriginalPath));

    }

    //TODO:add newVideo logic test

    @Test
    public void getMediaFromSystemDBSucceed_HasNoNewMediaAndNoPreMediaDeleted() {

        getMediaFromAppDBSucceed();

        ArgumentCaptor<MediaInSystemDBLoadCallback> localMediaSystemDBLoadCallback = ArgumentCaptor.forClass(MediaInSystemDBLoadCallback.class);

        verify(localMediaSystemDBDataSource).getMedia(ArgumentMatchers.<String>anyCollection(), localMediaSystemDBLoadCallback.capture());

        Media newMediaFromSystemDB = new Media();
        newMediaFromSystemDB.setOriginalPhotoPath(testMediaOriginalPathFromSystemDB);

        localMediaRepository.setCalcMediaDigestStrategy(calcMediaDigestStrategy);

        when(calcMediaDigestStrategy.handleMedia(ArgumentMatchers.<Media>anyCollection())).thenReturn(Collections.<Media>emptyList());

        localMediaSystemDBLoadCallback.getValue().onSucceed(Collections.singletonList(testMediaOriginalPath),
                Collections.<String>emptyList(), Collections.<Media>emptyList(), Collections.<Video>emptyList(), new OperationSuccess());

        assertEquals(1, localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.size());

        assertNotNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testMediaOriginalPath));

        assertNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testMediaOriginalPathFromSystemDB));

        verify(calcMediaDigestStrategy, times(2)).handleMedia(ArgumentMatchers.<Media>anyCollection());

        verify(localMediaAppDBDataSource, never()).insertMedias(ArgumentMatchers.<Media>anyCollection());

        verify(localMediaAppDBDataSource, never()).insertVideos(ArgumentMatchers.<Video>anyCollection());

    }

    @Test
    public void getMediaFromSystemDBSucceed_HasNoNewMediaAndPreMediaAlreadyDeleted() {

        getMediaFromAppDBSucceed();

        ArgumentCaptor<MediaInSystemDBLoadCallback> localMediaSystemDBLoadCallback = ArgumentCaptor.forClass(MediaInSystemDBLoadCallback.class);

        verify(localMediaSystemDBDataSource).getMedia(ArgumentMatchers.<String>anyCollection(), localMediaSystemDBLoadCallback.capture());

        Media newMediaFromSystemDB = new Media();
        newMediaFromSystemDB.setOriginalPhotoPath(testMediaOriginalPathFromSystemDB);

        localMediaRepository.setCalcMediaDigestStrategy(calcMediaDigestStrategy);

        when(calcMediaDigestStrategy.handleMedia(ArgumentMatchers.<Media>anyCollection())).thenReturn(Collections.<Media>emptyList());

        localMediaSystemDBLoadCallback.getValue().onSucceed(Collections.<String>emptyList(), Collections.<String>emptyList(),
                Collections.<Media>emptyList(), Collections.<Video>emptyList(), new OperationSuccess());

        assertEquals(0, localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.size());

        assertNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testMediaOriginalPath));

        assertNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testMediaOriginalPathFromSystemDB));

        verify(calcMediaDigestStrategy, times(2)).handleMedia(ArgumentMatchers.<Media>anyCollection());

        verify(localMediaAppDBDataSource, never()).insertMedias(ArgumentMatchers.<Media>anyCollection());

        verify(localMediaAppDBDataSource, never()).insertVideos(ArgumentMatchers.<Video>anyCollection());

    }

    @Test
    public void getMediaFromSystemDB_HasNewMediaAndPreMediaAlreadyDeleted() {

        getMediaFromAppDBSucceed();

        ArgumentCaptor<MediaInSystemDBLoadCallback> localMediaSystemDBLoadCallback = ArgumentCaptor.forClass(MediaInSystemDBLoadCallback.class);

        verify(localMediaSystemDBDataSource).getMedia(ArgumentMatchers.<String>anyCollection(), localMediaSystemDBLoadCallback.capture());

        Media newMediaFromSystemDB = new Media();
        newMediaFromSystemDB.setOriginalPhotoPath(testMediaOriginalPathFromSystemDB);

        localMediaRepository.setCalcMediaDigestStrategy(calcMediaDigestStrategy);

        when(calcMediaDigestStrategy.handleMedia(ArgumentMatchers.<Media>anyCollection())).thenReturn(Collections.singleton(newMediaFromSystemDB));

        localMediaSystemDBLoadCallback.getValue().onSucceed(Collections.<String>emptyList(), Collections.<String>emptyList(),
                Collections.singletonList(newMediaFromSystemDB), Collections.<Video>emptyList(), new OperationSuccess());

        assertEquals(1, localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.size());

        assertNotNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testMediaOriginalPathFromSystemDB));

        assertNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testMediaOriginalPath));

        verify(calcMediaDigestStrategy).handleMedia(Collections.singletonList(newMediaFromSystemDB));

        verify(localMediaAppDBDataSource).insertMedias(ArgumentMatchers.<Media>anyCollection());

    }

    @Test
    public void getMediaFromSystemDB_HasNewMediaAndNoPreMediaDeleted() {

        getMediaFromAppDBSucceed();

        ArgumentCaptor<MediaInSystemDBLoadCallback> localMediaSystemDBLoadCallback = ArgumentCaptor.forClass(MediaInSystemDBLoadCallback.class);

        verify(localMediaSystemDBDataSource).getMedia(ArgumentMatchers.<String>anyCollection(), localMediaSystemDBLoadCallback.capture());

        Media newMediaFromSystemDB = new Media();
        newMediaFromSystemDB.setOriginalPhotoPath(testMediaOriginalPathFromSystemDB);

        localMediaRepository.setCalcMediaDigestStrategy(calcMediaDigestStrategy);

        when(calcMediaDigestStrategy.handleMedia(ArgumentMatchers.<Media>anyCollection())).thenReturn(Collections.singleton(newMediaFromSystemDB));

        localMediaSystemDBLoadCallback.getValue().onSucceed(Collections.singletonList(testMediaOriginalPath), Collections.<String>emptyList(),
                Collections.singletonList(newMediaFromSystemDB), Collections.<Video>emptyList(), new OperationSuccess());

        assertEquals(2, localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.size());

        assertNotNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testMediaOriginalPathFromSystemDB));

        assertNotNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testMediaOriginalPath));

        verify(calcMediaDigestStrategy).handleMedia(Collections.singletonList(newMediaFromSystemDB));

        verify(localMediaAppDBDataSource).insertMedias(ArgumentMatchers.<Media>anyCollection());

    }

    @Test
    public void getMediaTwice() {

        ArgumentCaptor<BaseLoadDataCallback<Media>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        localMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        localMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        verify(localMediaAppDBDataSource).getMedia(captor.capture());

        captor.getValue().onSucceed(Collections.<Media>emptyList(), new OperationSuccess());

        verify(localMediaSystemDBDataSource, times(2)).getMedia(ArgumentMatchers.<String>anyCollection(), any(MediaInSystemDBLoadCallback.class));

    }

}
