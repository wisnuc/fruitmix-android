package com.winsun.fruitmix.media;

import android.util.Log;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.media.local.media.LocalMediaRepository;
import com.winsun.fruitmix.media.remote.media.StationMediaRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationMediaDataChanged;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/19.
 */

public class MediaDataSourceRepositoryImpl extends BaseDataRepository implements MediaDataSourceRepository {

    public static final String TAG = MediaDataSourceRepositoryImpl.class.getSimpleName();

    //TODO: check local media thumbnail exist

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

    private CalcMediaDigestStrategy.CalcMediaDigestCallback calcMediaDigestCallback;

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

    }

    @Override
    public void setCalcDigestCallback(CalcMediaDigestStrategy.CalcMediaDigestCallback calcDigestCallback) {
        this.calcMediaDigestCallback = calcDigestCallback;
    }

    @Override
    public void getLocalMedia(BaseLoadDataCallback<Media> mediaBaseLoadDataCallback) {

        final BaseLoadDataCallback<Media> runOnMainThreadCallback = createLoadCallbackRunOnMainThread(mediaBaseLoadDataCallback);

        if (getLocalMediaCallbackReturn)
            runOnMainThreadCallback.onSucceed(localMedias, new OperationSuccess());
        else
            localMediaRepository.getMedia(runOnMainThreadCallback);

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

                    if (calcMediaDigestCallback != null)
                        calcMediaDigestCallback.handleFinished();
                }

                @Override
                public void handleNothing() {

                    if (calcMediaDigestCallback != null)
                        calcMediaDigestCallback.handleNothing();

                }
            });

            localMediaRepository.setCalcMediaDigestStrategy(calcMediaDigestStrategy);

            hasSetCalcStrategy = true;
        }

        localMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>() {
            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                Log.d(TAG, "onSucceed: get media from local media repositroy");

                getLocalMediaCallbackReturn = true;

                localMedias = data;

                mediaDataChanged = operationResult.getOperationResultType() == OperationResultType.MEDIA_DATA_CHANGED;

                checkAllDataRetrieved(callback);
            }
        });

        if (!hasCallGetStationMedia) {

            stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>() {

                @Override
                public void onSucceed(List<Media> data, OperationResult operationResult) {
                    super.onSucceed(data, operationResult);

                    Log.d(TAG, "onSucceed: get media from station media repository");

                    getStationMediaCallbackReturn = true;

                    stationMedias = data;

                    checkAllDataRetrieved(callback);
                }

                @Override
                public void onFail(OperationResult operationResult) {
                    super.onFail(operationResult);

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
}
