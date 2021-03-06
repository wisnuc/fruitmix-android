package com.winsun.fruitmix.media;

import android.util.Log;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.eventbus.RetrieveVideoThumbnailEvent;
import com.winsun.fruitmix.media.local.media.LocalMediaRepository;
import com.winsun.fruitmix.media.remote.media.StationMediaRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationFail;
import com.winsun.fruitmix.model.operationResult.OperationMediaDataChanged;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/19.
 */

public class MediaDataSourceRepositoryImpl extends BaseDataRepository implements MediaDataSourceRepository {

    public static final String TAG = MediaDataSourceRepositoryImpl.class.getSimpleName();

    private static MediaDataSourceRepositoryImpl ourInstance;

    private LocalMediaRepository localMediaRepository;
    private StationMediaRepository stationMediaRepository;

    private CalcMediaDigestStrategy calcMediaDigestStrategy;

    private FilterLocalStationMediaStrategy filterLocalStationMediaStrategy;

    private boolean getLocalMediaCallbackReturn = false;
    private boolean getStationMediaCallbackReturn = false;

    private boolean hasSetCalcStrategy = false;
    private boolean hasCallGetStationMedia = false;

    private List<Media> localMedias;
    private List<Media> stationMedias;

    private boolean mediaDataChanged = false;

    private List<CalcMediaDigestStrategy.CalcMediaDigestCallback> calcMediaDigestCallbacks;

    public static MediaDataSourceRepositoryImpl getInstance(LocalMediaRepository localMediaRepository, StationMediaRepository stationMediaRepository,
                                                            CalcMediaDigestStrategy calcMediaDigestStrategy, ThreadManager threadManager) {

        if (ourInstance == null)
            ourInstance = new MediaDataSourceRepositoryImpl(localMediaRepository, stationMediaRepository, calcMediaDigestStrategy, threadManager);
        return ourInstance;
    }

    public static void destroyInstance() {
        ourInstance = null;
    }

    private MediaDataSourceRepositoryImpl(LocalMediaRepository localMediaRepository, StationMediaRepository stationMediaRepository,
                                          CalcMediaDigestStrategy calcMediaDigestStrategy, ThreadManager threadManager) {
        super(threadManager);
        this.localMediaRepository = localMediaRepository;
        this.stationMediaRepository = stationMediaRepository;
        this.calcMediaDigestStrategy = calcMediaDigestStrategy;

        filterLocalStationMediaStrategy = FilterLocalStationMediaStrategy.getInstance();

        localMedias = new ArrayList<>();
        stationMedias = new ArrayList<>();

        calcMediaDigestCallbacks = new ArrayList<>();
    }

    @Override
    public void registerCalcDigestCallback(CalcMediaDigestStrategy.CalcMediaDigestCallback calcMediaDigestCallback) {

        calcMediaDigestCallbacks.add(calcMediaDigestCallback);

    }

    @Override
    public void unregisterCalcDigestCallback(CalcMediaDigestStrategy.CalcMediaDigestCallback calcMediaDigestCallback) {

        calcMediaDigestCallbacks.remove(calcMediaDigestCallback);

    }

    @Override
    public void getLocalMedia(BaseLoadDataCallback<Media> mediaBaseLoadDataCallback) {

        final BaseLoadDataCallback<Media> runOnMainThreadCallback = createLoadCallbackRunOnMainThread(mediaBaseLoadDataCallback);

        Log.d(TAG, "getLocalMedia: size: " + localMedias.size());

        if (getLocalMediaCallbackReturn)
            runOnMainThreadCallback.onSucceed(localMedias, new OperationSuccess());
        else
            runOnMainThreadCallback.onFail(new OperationFail("get local media callback is not return"));

    }

    @Override
    public void getMedia(final BaseLoadDataCallback<Media> callback) {

        final BaseLoadDataCallback<Media> runOnMainThreadCallback = createLoadCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                getMediaInThread(runOnMainThreadCallback);

            }
        });

    }

    private void getMediaInThread(final BaseLoadDataCallback<Media> callback) {
        if (!hasSetCalcStrategy) {

            calcMediaDigestStrategy.setCalcMediaDigestCallback(new CalcMediaDigestStrategy.CalcMediaDigestCallback() {
                @Override
                public void handleFinished() {

                    for (CalcMediaDigestStrategy.CalcMediaDigestCallback calcMediaDigestCallback : calcMediaDigestCallbacks) {

                        if (calcMediaDigestCallback != null)
                            calcMediaDigestCallback.handleFinished();

                    }

                }

                @Override
                public void handleNothing() {

                    for (CalcMediaDigestStrategy.CalcMediaDigestCallback calcMediaDigestCallback : calcMediaDigestCallbacks) {

                        if (calcMediaDigestCallback != null)
                            calcMediaDigestCallback.handleNothing();

                    }

                }
            });

            localMediaRepository.setCalcMediaDigestStrategy(calcMediaDigestStrategy);

            hasSetCalcStrategy = true;
        }

        localMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>() {
            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                Log.d(MediaDataSourceRepositoryImpl.TAG, "onSucceed: get media from local media repositroy,data size: " + data.size());

                getLocalMediaCallbackReturn = true;

                localMedias = data;

                mediaDataChanged = operationResult.getOperationResultType() == OperationResultType.MEDIA_DATA_CHANGED;

                checkAllDataRetrieved(callback);
            }
        });

        if (!hasCallGetStationMedia) {

            stationMediaRepository.getMedia(new BaseLoadDataCallback<Media>() {

                @Override
                public void onSucceed(List<Media> data, OperationResult operationResult) {

                    Log.d(TAG, "onSucceed: get media from station media repository,data size: " + data.size());

                    getStationMediaCallbackReturn = true;

                    stationMedias = data;

                    checkAllDataRetrieved(callback);
                }

                @Override
                public void onFail(OperationResult operationResult) {

                    Log.d(TAG, "onFail: get media from station media repository,type: " + operationResult.getOperationResultType());

                    getStationMediaCallbackReturn = true;

                    stationMedias = Collections.emptyList();

                    checkAllDataRetrieved(callback);
                }
            });

            hasCallGetStationMedia = true;

        }
    }

    private void checkAllDataRetrieved(BaseLoadDataCallback<Media> callback) {

        if (getStationMediaCallbackReturn && getLocalMediaCallbackReturn) {

            OperationResult result;

            if (mediaDataChanged) {
                result = new OperationMediaDataChanged();
            } else
                result = new OperationSuccess();

            Log.d(TAG, "checkAllDataRetrieved: mediaDataChanged: " + mediaDataChanged);

            callback.onSucceed(filterLocalStationMediaStrategy.filter(localMedias, stationMedias), result);
        }

    }

    @Override
    public void downloadMedia(final List<Media> medias, BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "call: begin retrieve original photo task");

                for (Media media : medias) {

                    Log.d(TAG, "call: media uuid:" + media.getUuid());

                    stationMediaRepository.downloadMedia(media);
                }

                Log.d(TAG, "call: finish retrieve original photo task");

                runOnMainThreadCallback.onSucceed(true, new OperationSuccess());

//                EventBus.getDefault().post(new OperationEvent(Util.SHARED_PHOTO_THUMB_RETRIEVED, null));


            }
        });


    }

    @Override
    public void updateMedia(Media media) {

        if (media.isLocal()) {

            localMediaRepository.updateMedia(media);

        }

    }

    @Override
    public void updateVideo(Video video) {

        if (video.isLocal()) {

            EventBus.getDefault().post(new RetrieveVideoThumbnailEvent(Util.LOCAL_VIDEO_THUMBNAIL_RETRIEVED, new OperationSuccess(), video));

            localMediaRepository.updateVideo(video);

        }

    }

    @Override
    public void getStationMediaForceRefresh(BaseLoadDataCallback<Media> callback) {

        stationMediaRepository.setCacheDirty();

        hasCallGetStationMedia = false;

        getStationMediaCallbackReturn = false;

        getMedia(callback);

    }

    @Override
    public boolean clearAllStationMediasInDB() {
        return stationMediaRepository.clearAllStationMediasInDB();
    }

    @Override
    public void resetState() {

        getLocalMediaCallbackReturn = false;

        getStationMediaCallbackReturn = false;

        hasCallGetStationMedia = false;

        stationMediaRepository.setCacheDirty();

        localMediaRepository.resetState();

    }
}
