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

    private String testVideoOriginalPath = "testVideoOriginalPath";
    private String testVideoOriginalPathFromSystemDB = "testVideoOriginalPathFromSystemDB";

    private Media createMediaFromAppDB() {
        Media media = new Media();
        media.setOriginalPhotoPath(testMediaOriginalPath);

        return media;
    }

    private Media createVideoFromAppDB() {
        Video video = new Video();
        video.setOriginalPhotoPath(testVideoOriginalPath);

        return video;
    }

    @Test
    public void getMediaFromAppDBSucceed() {

        Media media = createMediaFromAppDB();

        getMediaFromAppDBSucceed(Collections.singletonList(media));

    }

    public void getMediaFromAppDBSucceed(List<Media> medias) {

        localMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>());

        ArgumentCaptor<BaseLoadDataCallback<Media>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(localMediaAppDBDataSource).getMedia(captor.capture());

        captor.getValue().onSucceed(medias, new OperationSuccess());

        assertEquals(medias.size(), localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.size());

        assertNotNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(medias.get(0).getOriginalPhotoPath()));

    }

    @Test
    public void getMediaFromSystemDBSucceed_HasNoNewMediaOrVideoAndNoPreMediaDeleted() {

        getMediaFromAppDBSucceed();

        ArgumentCaptor<MediaInSystemDBLoadCallback> localMediaSystemDBLoadCallback = ArgumentCaptor.forClass(MediaInSystemDBLoadCallback.class);

        verify(localMediaSystemDBDataSource).getMedia(ArgumentMatchers.<String>anyCollection(), localMediaSystemDBLoadCallback.capture());

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
    public void getMediaFromSystemDBSucceed_HasNoNewMediaOrVideoAndPreMediaAlreadyDeleted() {

        getMediaFromAppDBSucceed();

        ArgumentCaptor<MediaInSystemDBLoadCallback> localMediaSystemDBLoadCallback = ArgumentCaptor.forClass(MediaInSystemDBLoadCallback.class);

        verify(localMediaSystemDBDataSource).getMedia(ArgumentMatchers.<String>anyCollection(), localMediaSystemDBLoadCallback.capture());

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
    public void getMediaFromSystemDB_HasNewMediaNoNewVideoAndPreMediaAlreadyDeleted() {

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
    public void getMediaFromSystemDB_HasNewMediaNoNewVideoAndNoPreMediaDeleted() {

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
    public void getMediaFromSystemDB_NoNewMediaHasNewVideoAndNoPreMediaOrVideoDeleted() {

        getMediaFromAppDBSucceed();

        Video video = new Video();
        video.setOriginalPhotoPath(testVideoOriginalPathFromSystemDB);

        ArgumentCaptor<MediaInSystemDBLoadCallback> localMediaSystemDBLoadCallback = ArgumentCaptor.forClass(MediaInSystemDBLoadCallback.class);

        verify(localMediaSystemDBDataSource).getMedia(ArgumentMatchers.<String>anyCollection(), localMediaSystemDBLoadCallback.capture());

        localMediaRepository.setCalcMediaDigestStrategy(calcMediaDigestStrategy);

        when(calcMediaDigestStrategy.handleMedia(ArgumentMatchers.<Video>anyCollection())).thenReturn(Collections.<Video>emptyList()).thenReturn(Collections.singletonList(video));

        localMediaSystemDBLoadCallback.getValue().onSucceed(Collections.singletonList(testMediaOriginalPath),
                Collections.<String>emptyList(), Collections.<Media>emptyList(), Collections.singletonList(video), new OperationSuccess());

        assertEquals(2, localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.size());

        assertNotNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testMediaOriginalPath));

        assertNotNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testVideoOriginalPathFromSystemDB));

        verify(calcMediaDigestStrategy, times(2)).handleMedia(ArgumentMatchers.<Media>anyCollection());

        verify(localMediaAppDBDataSource, never()).insertMedias(ArgumentMatchers.<Media>anyCollection());

        verify(calcMediaDigestStrategy).handleMedia(Collections.singletonList(video));

        verify(localMediaAppDBDataSource).insertVideos(ArgumentMatchers.<Video>anyCollection());

    }


    @Test
    public void getMediaFromSystemDB_NoNewMediaOrVideoAndPreVideoDeleted() {

        getMediaFromAppDBSucceed(Collections.singletonList(createVideoFromAppDB()));

        ArgumentCaptor<MediaInSystemDBLoadCallback> localMediaSystemDBLoadCallback = ArgumentCaptor.forClass(MediaInSystemDBLoadCallback.class);

        verify(localMediaSystemDBDataSource).getMedia(ArgumentMatchers.<String>anyCollection(), localMediaSystemDBLoadCallback.capture());

        localMediaRepository.setCalcMediaDigestStrategy(calcMediaDigestStrategy);

        when(calcMediaDigestStrategy.handleMedia(ArgumentMatchers.<Media>anyCollection())).thenReturn(Collections.<Media>emptyList());

        localMediaSystemDBLoadCallback.getValue().onSucceed(Collections.<String>emptyList(), Collections.<String>emptyList(),
                Collections.<Media>emptyList(), Collections.<Video>emptyList(), new OperationSuccess());

        assertEquals(0, localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.size());

        assertNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testVideoOriginalPath));

        assertNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testVideoOriginalPathFromSystemDB));

        verify(calcMediaDigestStrategy, times(2)).handleMedia(ArgumentMatchers.<Media>anyCollection());

        verify(localMediaAppDBDataSource, never()).insertMedias(ArgumentMatchers.<Media>anyCollection());

        verify(localMediaAppDBDataSource, never()).insertVideos(ArgumentMatchers.<Video>anyCollection());

    }

    @Test
    public void getMediaFromSystemDB_HasNewMediaAndVideoAndPreVideoOrMediaDeleted() {

        List<Media> medias = new ArrayList<>(2);
        medias.add(createMediaFromAppDB());
        medias.add(createVideoFromAppDB());

        getMediaFromAppDBSucceed(medias);

        ArgumentCaptor<MediaInSystemDBLoadCallback> localMediaSystemDBLoadCallback = ArgumentCaptor.forClass(MediaInSystemDBLoadCallback.class);

        verify(localMediaSystemDBDataSource).getMedia(ArgumentMatchers.<String>anyCollection(), localMediaSystemDBLoadCallback.capture());

        Media newMediaFromSystemDB = new Media();
        newMediaFromSystemDB.setOriginalPhotoPath(testMediaOriginalPathFromSystemDB);

        Video newVideoFromSystemDB = new Video();
        newVideoFromSystemDB.setOriginalPhotoPath(testVideoOriginalPathFromSystemDB);

        localMediaRepository.setCalcMediaDigestStrategy(calcMediaDigestStrategy);

        when(calcMediaDigestStrategy.handleMedia(ArgumentMatchers.<Media>anyCollection())).thenReturn(Collections.singleton(newMediaFromSystemDB))
                .thenReturn(Collections.<Media>singleton(newVideoFromSystemDB));

        localMediaSystemDBLoadCallback.getValue().onSucceed(Collections.<String>emptyList(), Collections.<String>emptyList(),
                Collections.singletonList(newMediaFromSystemDB), Collections.singletonList(newVideoFromSystemDB), new OperationSuccess());

        assertEquals(2, localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.size());

        assertNotNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testMediaOriginalPathFromSystemDB));

        assertNotNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testVideoOriginalPathFromSystemDB));

        assertNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testMediaOriginalPath));

        assertNull(localMediaRepository.mediaConcurrentMapKeyIsOriginalPath.get(testVideoOriginalPath));

        verify(calcMediaDigestStrategy, times(2)).handleMedia(ArgumentMatchers.<Media>anyCollection());

        verify(localMediaAppDBDataSource).insertMedias(ArgumentMatchers.<Media>anyCollection());

        verify(localMediaAppDBDataSource).insertVideos(ArgumentMatchers.<Video>anyCollection());
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
