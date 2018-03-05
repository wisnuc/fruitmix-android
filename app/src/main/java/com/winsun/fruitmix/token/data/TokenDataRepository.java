package com.winsun.fruitmix.token.data;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.token.param.StationTokenParam;
import com.winsun.fruitmix.token.WeChatTokenUserWrapper;

/**
 * Created by Administrator on 2017/8/28.
 */

public class TokenDataRepository extends BaseDataRepository implements TokenDataSource {

    private TokenRemoteDataSource tokenRemoteDataSource;

    public TokenDataRepository(ThreadManager threadManager, TokenRemoteDataSource tokenRemoteDataSource) {
        super(threadManager);
        this.tokenRemoteDataSource = tokenRemoteDataSource;
    }

    @Override
    public void getStationToken(final StationTokenParam stationTokenParam, final BaseLoadDataCallback<String> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                tokenRemoteDataSource.getStationToken(stationTokenParam, createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void getCloudToken(final String wechatCode, final BaseLoadDataCallback<WeChatTokenUserWrapper> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                tokenRemoteDataSource.getCloudToken(wechatCode, createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void getStationTokenThroughCloudToken(final BaseLoadDataCallback<String> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                tokenRemoteDataSource.getStationTokenThroughCloudToken(createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void getSCloudTokenThroughStationTokenWithThreadChange(final String userGUID, final BaseLoadDataCallback<String> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                tokenRemoteDataSource.getSCloudTokenThroughStationTokenWithThreadChange(userGUID, createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void getSCloudTokenThroughStationTokenWithoutThreadChange(final String userGUID, final BaseLoadDataCallback<String> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                tokenRemoteDataSource.getSCloudTokenThroughStationTokenWithoutThreadChange(userGUID, callback);
            }
        });

    }
}
