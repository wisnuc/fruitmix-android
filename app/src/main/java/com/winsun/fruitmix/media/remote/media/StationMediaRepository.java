package com.winsun.fruitmix.media.remote.media;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.exception.NetworkException;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationMediaDataChanged;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2017/7/18.
 */

public class StationMediaRepository {

    public static final String TAG = StationMediaRepository.class.getSimpleName();

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

        Log.d(TAG, "getMedia: start get media from station," + Util.getCurrentFormatTime());

        stationMediaRemoteDataSource.getMedia(new BaseLoadDataCallbackImpl<Media>() {

            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                Log.d(StationMediaRepository.TAG, "onSucceed: finish get media from station," + Util.getCurrentFormatTime());

                cacheDirty = false;

                boolean hasNewMedia = false;

                for (Media media : data) {
                    if (!mediaConcurrentMap.containsKey(media.getUuid())) {
                        hasNewMedia = true;
                        break;
                    }
                }

                if (hasNewMedia) {

                    Log.d(StationMediaRepository.TAG, "onSucceed: get media from station has new media," + Util.getCurrentFormatTime());

                    mediaConcurrentMap.clear();

                    mediaConcurrentMap.putAll(MediaUtil.BuildMediaMapKeyIsUUID(data));

                    Log.d(StationMediaRepository.TAG, "onSucceed: finish put all media to mediaConcurrentMap,"+ Util.getCurrentFormatTime());

                    callback.onSucceed(data, new OperationMediaDataChanged());

                    stationMediaDBDataSource.clearAllMedias();
                    stationMediaDBDataSource.insertMedias(data);

                    Log.d(StationMediaRepository.TAG, "onSucceed: finish insert medias," + Util.getCurrentFormatTime());

                } else {

                    Log.d(StationMediaRepository.TAG, "onSucceed: get media no new media");

                    callback.onSucceed(data, operationResult);
                }

            }

            @Override
            public void onFail(OperationResult operationResult) {
                super.onFail(operationResult);

                getMediaFromDB(callback);
            }
        });
    }

    private void getMediaFromDB(final BaseLoadDataCallback<Media> callback) {
        stationMediaDBDataSource.getMedia(new BaseLoadDataCallbackImpl<Media>() {
            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                mediaConcurrentMap.clear();

                mediaConcurrentMap.putAll(MediaUtil.BuildMediaMapKeyIsUUID(data));

                cacheDirty = false;

                callback.onSucceed(data, operationResult);

            }

            @Override
            public void onFail(OperationResult operationResult) {
                super.onFail(operationResult);

                cacheDirty = false;

                callback.onFail(new OperationSQLException());

            }
        });
    }


    public void downloadMedia(Media media) {

        boolean result = false;

        try {
            result = stationMediaRemoteDataSource.downloadMedia(media);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NetworkException e) {

            Log.e(TAG, "downloadMedia: http error" + e.getHttpResponseCode());
            e.printStackTrace();
        }

        if (result)
            stationMediaDBDataSource.updateMedia(media);

    }

    public boolean clearAllStationMediasInDB() {

        return stationMediaDBDataSource.clearAllMedias();
    }

}
