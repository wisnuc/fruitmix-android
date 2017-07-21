package com.winsun.fruitmix.media.local.media;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.media.CalcMediaDigestStrategy;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
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

    private static LocalMediaRepository instance;

    private LocalMediaAppDBDataSource localMediaAppDBDataSource;
    private LocalMediaSystemDBDataSource localMediaSystemDBDataSource;

    private CalcMediaDigestStrategy calcMediaDigestStrategy;

    ConcurrentMap<String, Media> mediaConcurrentMapKeyIsOriginalPath;

    private boolean hasGetMediaFromAppDB = false;

    private LocalMediaRepository(LocalMediaAppDBDataSource localMediaAppDBDataSource,
                                 LocalMediaSystemDBDataSource localMediaSystemDBDataSource) {
        this.localMediaAppDBDataSource = localMediaAppDBDataSource;
        this.localMediaSystemDBDataSource = localMediaSystemDBDataSource;

        mediaConcurrentMapKeyIsOriginalPath = new ConcurrentHashMap<>();
    }

    public static LocalMediaRepository getInstance(LocalMediaAppDBDataSource localMediaAppDBDataSource,
                                                   LocalMediaSystemDBDataSource localMediaSystemDBDataSource) {

        if (instance == null)
            instance = new LocalMediaRepository(localMediaAppDBDataSource, localMediaSystemDBDataSource);

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

            localMediaAppDBDataSource.getMedia(new BaseLoadDataCallbackImpl<Media>() {
                @Override
                public void onSucceed(List<Media> data, OperationResult operationResult) {
                    super.onSucceed(data, operationResult);

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
        localMediaSystemDBDataSource.getMedia(mediaConcurrentMapKeyIsOriginalPath.keySet(), new BaseLoadDataCallbackImpl<Media>() {

            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                mediaConcurrentMapKeyIsOriginalPath.putAll(LocalCache.BuildMediaMapKeyIsThumb(data));

                callback.onSucceed(new ArrayList<>(mediaConcurrentMapKeyIsOriginalPath.values()), new OperationSuccess());

                Collection<Media> result = data;

                if (calcMediaDigestStrategy != null)
                    result = calcMediaDigestStrategy.handleMedia(data);

                localMediaAppDBDataSource.insertMedias(result);

            }
        });
    }


}
