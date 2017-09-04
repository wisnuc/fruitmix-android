package com.winsun.fruitmix.media.local.media;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.media.CalcMediaDigestStrategy;
import com.winsun.fruitmix.mediaModule.model.Media;
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

    public void getMedia(final BaseLoadDataCallback<Media> callback) {

        if (!hasGetMediaFromAppDB) {

            Log.d(TAG, "getMedia: start get media from app db ");

            localMediaAppDBDataSource.getMedia(new BaseLoadDataCallbackImpl<Media>() {
                @Override
                public void onSucceed(List<Media> data, OperationResult operationResult) {
                    super.onSucceed(data, operationResult);

                    Log.d(TAG, "onSucceed: finish get media from app db");

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

    private void getLocalMediaInSystemDB(final BaseLoadDataCallback<Media> callback) {

        Log.d(TAG, "getLocalMediaInSystemDB: start get media from system db");

        localMediaSystemDBDataSource.getMedia(mediaConcurrentMapKeyIsOriginalPath.keySet(), new BaseLoadDataCallbackImpl<Media>() {

            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                Log.d(TAG, "onSucceed: finish get media from system db");

                mediaConcurrentMapKeyIsOriginalPath.putAll(LocalCache.BuildMediaMapKeyIsThumb(data));

                callback.onSucceed(new ArrayList<>(mediaConcurrentMapKeyIsOriginalPath.values()), operationResult);

                calcMediaDigest(data);

            }
        });
    }

    private void calcMediaDigest(final List<Media> medias) {

        threadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                calcMediaDigestInThread(medias);
            }
        });

    }

    private void calcMediaDigestInThread(List<Media> data) {

        Collection<Media> result = data;

        if (calcMediaDigestStrategy != null)
            result = calcMediaDigestStrategy.handleMedia(data);

        localMediaAppDBDataSource.insertMedias(result);
    }

    public boolean updateMedia(Media media) {
        return localMediaAppDBDataSource.updateMedia(media);
    }


}
