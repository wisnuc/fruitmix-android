package com.winsun.fruitmix.stations;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/14.
 */

public class StationsRepository extends BaseDataRepository implements StationsDataSource {

    private static StationsRepository instance;

    private StationsDataSource stationsDataSource;

    List<Station> stations;

    String currentGUID;

    public static StationsRepository getInstance(ThreadManager threadManager,StationsDataSource stationsDataSource) {

        if (instance == null)
            instance = new StationsRepository(threadManager,stationsDataSource);

        return instance;
    }

    public static void destroyInstance(){
        instance = null;
    }

    public StationsRepository(ThreadManager threadManager, StationsDataSource stationsDataSource) {
        super(threadManager);
        this.stationsDataSource = stationsDataSource;

        stations = new ArrayList<>();
        currentGUID = "";
    }

    @Override
    public void getStationsByWechatGUID(final String guid, final BaseLoadDataCallback<Station> callback) {

        final BaseLoadDataCallback<Station> runOnMainThread = createLoadCallbackRunOnMainThread(callback);

        if(currentGUID.equals(guid)){

            runOnMainThread.onSucceed(stations,new OperationSuccess());
            return;

        }

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                stationsDataSource.getStationsByWechatGUID(guid, new BaseLoadDataCallback<Station>() {
                    @Override
                    public void onSucceed(List<Station> data, OperationResult operationResult) {

                        stations.clear();
                        stations.addAll(data);

                        currentGUID = guid;

                        runOnMainThread.onSucceed(data,operationResult);

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        runOnMainThread.onFail(operationResult);
                    }
                });

            }
        });

    }
}
