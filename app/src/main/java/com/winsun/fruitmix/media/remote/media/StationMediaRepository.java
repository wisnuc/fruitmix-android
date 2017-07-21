package com.winsun.fruitmix.media.remote.media;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.LocalCache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2017/7/18.
 */

public class StationMediaRepository {

    private static StationMediaRepository instance;

    private StationMediaDBDataSource stationMediaDBDataSource;

    private StationMediaRemoteDataSource stationMediaRemoteDataSource;

    ConcurrentMap<String, Media> mediaConcurrentMap;

    boolean cacheDirty = true;

    private StationMediaRepository(StationMediaDBDataSource stationMediaDBDataSource, StationMediaRemoteDataSource stationMediaRemoteDataSource) {
        this.stationMediaDBDataSource = stationMediaDBDataSource;
        this.stationMediaRemoteDataSource = stationMediaRemoteDataSource;

        mediaConcurrentMap = new ConcurrentHashMap<>();
    }

    public static StationMediaRepository getInstance(StationMediaDBDataSource stationMediaDBDataSource, StationMediaRemoteDataSource stationMediaRemoteDataSource) {

        if (instance == null)
            instance = new StationMediaRepository(stationMediaDBDataSource, stationMediaRemoteDataSource);

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public void setCacheDirty() {
        cacheDirty = true;
    }

    public void getMedia(final BaseLoadDataCallback<Media> callback) {

        if (!cacheDirty) {
            callback.onSucceed(new ArrayList<>(mediaConcurrentMap.values()), new OperationSuccess());
            return;
        }

        stationMediaDBDataSource.getMedia(new BaseLoadDataCallbackImpl<Media>() {
            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                mediaConcurrentMap.clear();

                mediaConcurrentMap.putAll(LocalCache.BuildMediaMapKeyIsUUID(data));

                callback.onSucceed(data, operationResult);

                cacheDirty = false;
            }

            @Override
            public void onFail(OperationResult operationResult) {
                super.onFail(operationResult);

                cacheDirty = false;
            }
        });

        stationMediaRemoteDataSource.getMedia(new BaseLoadDataCallbackImpl<Media>() {

            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                mediaConcurrentMap.clear();

                mediaConcurrentMap.putAll(LocalCache.BuildMediaMapKeyIsUUID(data));

                stationMediaDBDataSource.clearAllMedias();
                stationMediaDBDataSource.insertMedias(data);

                callback.onSucceed(data, operationResult);

            }
        });
    }

}
