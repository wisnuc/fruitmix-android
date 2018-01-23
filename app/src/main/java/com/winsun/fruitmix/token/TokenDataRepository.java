package com.winsun.fruitmix.token;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.thread.manage.ThreadManager;

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
    public void getToken(final LoadTokenParam loadTokenParam, final BaseLoadDataCallback<String> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                tokenRemoteDataSource.getToken(loadTokenParam, createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void getToken(final String wechatCode, final BaseLoadDataCallback<WeChatTokenUserWrapper> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                tokenRemoteDataSource.getToken(wechatCode, createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void getTokenThroughWAToken(final BaseLoadDataCallback<String> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                tokenRemoteDataSource.getTokenThroughWAToken(createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void getWATokenThroughStationToken(final String userGUID, final BaseLoadDataCallback<String> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                tokenRemoteDataSource.getWATokenThroughStationToken(userGUID, createLoadCallbackRunOnMainThread(callback));
            }
        });

    }
}
