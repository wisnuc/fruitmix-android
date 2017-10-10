package com.winsun.fruitmix.stations;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.model.operationResult.OperationFail;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/9/14.
 */

public class StationsRepository extends BaseDataRepository implements StationsDataSource {

    private static StationsRepository instance;

    private StationsDataSource stationsDataSource;

    public static StationsRepository getInstance(ThreadManager threadManager, StationsDataSource stationsDataSource) {

        if (instance == null)
            instance = new StationsRepository(threadManager, stationsDataSource);

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public StationsRepository(ThreadManager threadManager, StationsDataSource stationsDataSource) {
        super(threadManager);
        this.stationsDataSource = stationsDataSource;

    }

    @Override
    public void getStationsByWechatGUID(final String guid, final BaseLoadDataCallback<Station> callback) {

        final BaseLoadDataCallback<Station> runOnMainThread = createLoadCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                stationsDataSource.getStationsByWechatGUID(guid, new BaseLoadDataCallback<Station>() {
                    @Override
                    public void onSucceed(List<Station> data, OperationResult operationResult) {

                        if (data.isEmpty()) {
                            runOnMainThread.onFail(new OperationFail("未绑定nas用户，请绑定"));

                            return;
                        }

                        Iterator<Station> iterator = data.iterator();

                        while (iterator.hasNext()) {

                            Station station = iterator.next();

                            if (!station.isOnline())
                                iterator.remove();

                        }

                        if (data.isEmpty()) {

                            runOnMainThread.onFail(new OperationFail("绑定的设备都未在线,请确认"));

                            return;
                        }

                        runOnMainThread.onSucceed(data, operationResult);

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {
                        runOnMainThread.onFail(operationResult);
                    }
                });

            }
        });

    }

    @Override
    public void getStationInfoByStationAPI(final String ip,final BaseLoadDataCallback<Station> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                stationsDataSource.getStationInfoByStationAPI(ip,createLoadCallbackRunOnMainThread(callback));
            }
        });

    }
}
