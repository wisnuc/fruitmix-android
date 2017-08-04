package com.winsun.fruitmix.media;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.media.local.media.LocalMediaRepository;
import com.winsun.fruitmix.media.remote.media.StationMediaRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationNoChanged;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/19.
 */

public class MediaDataSourceRepositoryImpl implements MediaDataSourceRepository {
    private static MediaDataSourceRepositoryImpl ourInstance;

    private LocalMediaRepository localMediaRepository;
    private StationMediaRepository stationMediaRepository;

    private CalcMediaDigestStrategy calcMediaDigestStrategy;

    private FilterLocalStationMediaStrategy filterLocalStationMediaStrategy;

    private boolean calcMediaDigestCallbackReturn = false;
    private boolean getLocalMediaCallbackReturn = false;
    private boolean getStationMediaCallbackReturn = false;

    private boolean hasSetCalcStrategy = false;
    private boolean hasCallGetStationMedia = false;

    private Collection<Media> localMedias;
    private Collection<Media> stationMedias;

    public static MediaDataSourceRepositoryImpl getInstance(LocalMediaRepository localMediaRepository, StationMediaRepository stationMediaRepository,
                                                            CalcMediaDigestStrategy calcMediaDigestStrategy) {

        if (ourInstance == null)
            ourInstance = new MediaDataSourceRepositoryImpl(localMediaRepository, stationMediaRepository, calcMediaDigestStrategy);
        return ourInstance;
    }

    public static void destroyInstance() {
        ourInstance = null;
    }

    private MediaDataSourceRepositoryImpl(LocalMediaRepository localMediaRepository, StationMediaRepository stationMediaRepository,
                                          CalcMediaDigestStrategy calcMediaDigestStrategy) {

        this.localMediaRepository = localMediaRepository;
        this.stationMediaRepository = stationMediaRepository;
        this.calcMediaDigestStrategy = calcMediaDigestStrategy;

        filterLocalStationMediaStrategy = FilterLocalStationMediaStrategy.getInstance();

        localMedias = new ArrayList<>();
        stationMedias = new ArrayList<>();

    }


    @Override
    public void getMedia(final BaseLoadDataCallback<Media> callback) {

        if (!hasSetCalcStrategy) {

            calcMediaDigestStrategy.setCalcMediaDigestCallback(new CalcMediaDigestStrategy.CalcMediaDigestCallback() {
                @Override
                public void handleFinished() {

                    calcMediaDigestCallbackReturn = true;
                }

                @Override
                public void handleNothing() {

                    calcMediaDigestCallbackReturn = true;

                }
            });

            localMediaRepository.setCalcMediaDigestStrategy(calcMediaDigestStrategy);

            hasSetCalcStrategy = true;
        }

        localMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>() {
            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                getLocalMediaCallbackReturn = true;

                localMedias = data;

                checkAllDataRetrieved(callback);
            }
        });

        if (!hasCallGetStationMedia) {

            stationMediaRepository.getMedia(new BaseLoadDataCallbackImpl<Media>() {

                @Override
                public void onSucceed(List<Media> data, OperationResult operationResult) {
                    super.onSucceed(data, operationResult);

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

        if (getStationMediaCallbackReturn && getLocalMediaCallbackReturn)
            callback.onSucceed(filterLocalStationMediaStrategy.filter(localMedias, stationMedias), new OperationSuccess());
        else
            callback.onFail(new OperationNoChanged());
    }

}
