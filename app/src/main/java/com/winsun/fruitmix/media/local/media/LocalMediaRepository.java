package com.winsun.fruitmix.media.local.media;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.media.CalcMediaDigestStrategy;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.model.operationResult.OperationMediaDataChanged;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.util.LocalCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2017/7/18.
 */

public class LocalMediaRepository {

    public static final String TAG = LocalMediaRepository.class.getSimpleName();

    private static LocalMediaRepository instance;

    private LocalMediaAppDBDataSource localMediaAppDBDataSource;
    private LocalMediaSystemDBDataSource localMediaSystemDBDataSource;

    private CalcMediaDigestStrategy calcMediaDigestStrategy;

    ConcurrentMap<String, Media> mediaConcurrentMapKeyIsOriginalPath;

    private boolean hasGetMediaFromAppDB = false;

    private boolean gettingMediaFromAppDB = false;

    private ThreadManager threadManager;

    private LocalMediaRepository(LocalMediaAppDBDataSource localMediaAppDBDataSource,
                                 LocalMediaSystemDBDataSource localMediaSystemDBDataSource, ThreadManager threadManager) {
        this.localMediaAppDBDataSource = localMediaAppDBDataSource;
        this.localMediaSystemDBDataSource = localMediaSystemDBDataSource;
        this.threadManager = threadManager;

        mediaConcurrentMapKeyIsOriginalPath = new ConcurrentHashMap<>();
    }

    public static LocalMediaRepository getInstance(LocalMediaAppDBDataSource localMediaAppDBDataSource,
                                                   LocalMediaSystemDBDataSource localMediaSystemDBDataSource, ThreadManager threadManager) {

        if (instance == null)
            instance = new LocalMediaRepository(localMediaAppDBDataSource, localMediaSystemDBDataSource, threadManager);

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public void setCalcMediaDigestStrategy(CalcMediaDigestStrategy calcMediaDigestStrategy) {
        this.calcMediaDigestStrategy = calcMediaDigestStrategy;
    }

    public void resetState() {

        hasGetMediaFromAppDB = false;

    }

    public void getMedia(final BaseLoadDataCallback<Media> callback) {

        if (!hasGetMediaFromAppDB) {

            Log.d(TAG, "getMedia: start get media from app db ");

            if (gettingMediaFromAppDB)
                return;
            else
                gettingMediaFromAppDB = true;

            localMediaAppDBDataSource.getMedia(new BaseLoadDataCallbackImpl<Media>() {
                @Override
                public void onSucceed(List<Media> data, OperationResult operationResult) {
                    super.onSucceed(data, operationResult);

                    Log.d(TAG, "onSucceed: finish get media from app db,data size: " + data.size());

                    mediaConcurrentMapKeyIsOriginalPath.clear();

                    mediaConcurrentMapKeyIsOriginalPath.putAll(LocalCache.BuildMediaMapKeyIsThumb(data));

                    getLocalMediaInSystemDB(callback);

                }
            });

            hasGetMediaFromAppDB = true;
        } else {

            getLocalMediaInSystemDB(callback);

        }

    }

    //TODO:fix bug:when media path is same but hash is different

    private void getLocalMediaInSystemDB(final BaseLoadDataCallback<Media> callback) {

        Log.d(TAG, "getLocalMediaInSystemDB: start get media from system db");

        localMediaSystemDBDataSource.getMedia(mediaConcurrentMapKeyIsOriginalPath.keySet(), new MediaInSystemDBLoadCallback() {

            public void onSucceed(List<String> currentAllMediaPathInSystemDB, List<String> currentAllVideoPathInSystemDB, List<Media> newMedia, List<Video> newVideos, OperationResult operationResult) {

                Log.d(TAG, "onSucceed: finish get media from system db,data size: " + currentAllMediaPathInSystemDB.size()
                        + " newMedia size: " + newMedia.size() + " newVideo size: " + newVideos.size());

                boolean hasDeleteMedia = false;

                boolean hasDeleteVideo = false;

                List<String> needDeleteMediaPaths = null;

                List<String> needDeleteVideoPaths = null;

                for (String mediaPathInAppDB : mediaConcurrentMapKeyIsOriginalPath.keySet()) {

                    if (!currentAllMediaPathInSystemDB.contains(mediaPathInAppDB) && !currentAllVideoPathInSystemDB.contains(mediaPathInAppDB)) {

                        Media media = mediaConcurrentMapKeyIsOriginalPath.remove(mediaPathInAppDB);

                        if (media instanceof Video) {

                            if (needDeleteVideoPaths == null)
                                needDeleteVideoPaths = new ArrayList<>();

                            needDeleteVideoPaths.add(mediaPathInAppDB);

                            mediaConcurrentMapKeyIsOriginalPath.remove(mediaPathInAppDB);

                            hasDeleteVideo = true;

                        } else {

                            if (needDeleteMediaPaths == null)
                                needDeleteMediaPaths = new ArrayList<>();

                            needDeleteMediaPaths.add(mediaPathInAppDB);

                            hasDeleteMedia = true;

                        }

                    }

                }

                Log.d(TAG, "onSucceed: finish check hasDeleteMedia: " + hasDeleteMedia
                        + " hasDeleteVideo: " + hasDeleteVideo);

                OperationResult result = operationResult;

                if (hasDeleteMedia || hasDeleteVideo || newMedia.size() > 0 || newVideos.size() > 0) {
                    result = new OperationMediaDataChanged();
                }

                mediaConcurrentMapKeyIsOriginalPath.putAll(LocalCache.BuildMediaMapKeyIsThumb(newMedia));

                mediaConcurrentMapKeyIsOriginalPath.putAll(LocalCache.BuildMediaMapKeyIsThumb(newVideos));

                List<Media> returnValue = new ArrayList<>(mediaConcurrentMapKeyIsOriginalPath.values());

                calcMediaDigest(newMedia, newVideos);

                callback.onSucceed(new ArrayList<>(returnValue), result);

                if (needDeleteMediaPaths != null && needDeleteMediaPaths.size() != 0) {
                    localMediaAppDBDataSource.deleteMediaByPath(needDeleteMediaPaths);
                }

                if (needDeleteVideoPaths != null && needDeleteVideoPaths.size() != 0) {
                    localMediaAppDBDataSource.deleteVideoByPath(needDeleteVideoPaths);
                }

                gettingMediaFromAppDB = false;

            }
        });
    }

    private void calcMediaDigest(final List<Media> medias, final List<Video> videos) {

        threadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                if (calcMediaDigestStrategy != null)
                    calcMediaDigestStrategy.setFinishCalcMediaDigest(false);

                Collection<Media> calcMediaResult = calcMediaDigestInThread(medias);

                Collection<Video> calcVideoResult = calcVideoDigestInThread(videos);

                if (calcMediaDigestStrategy != null)
                    calcMediaDigestStrategy.setFinishCalcMediaDigest(true);

                int size = calcMediaResult.size() + calcVideoResult.size();

                calcMediaDigestStrategy.notifyCalcFinished(size);

            }
        });

    }

    private Collection<Media> calcMediaDigestInThread(List<Media> data) {

        Collection<Media> result = data;

        if (calcMediaDigestStrategy != null)
            result = calcMediaDigestStrategy.handleMedia(data);

        if (result.size() != 0)
            localMediaAppDBDataSource.insertMedias(result);

        return result;
    }


    private Collection<Video> calcVideoDigestInThread(List<Video> data) {

        Collection<Video> result = data;

        if (calcMediaDigestStrategy != null)
            result = calcMediaDigestStrategy.handleMedia(data);

        if (result.size() != 0)
            localMediaAppDBDataSource.insertVideos(data);

        return result;
    }


    public boolean updateMedia(Media media) {

        Log.d(TAG, "updateMedia: " + media);

        return localMediaAppDBDataSource.updateMedia(media);
    }

    public boolean updateVideo(Video video) {

        Log.d(TAG, "updateVideo: " + video);

        return localMediaAppDBDataSource.updateVideo(video);
    }

}
