package com.winsun.fruitmix.plugin.data;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.thread.manage.ThreadManager;

/**
 * Created by Administrator on 2017/12/22.
 */

public class PluginManageRepository extends BaseDataRepository implements PluginManageDataSource {

    private PluginManageDataSource mPluginManageDataSource;

    public PluginManageRepository(ThreadManager threadManager, PluginManageDataSource pluginManageDataSource) {
        super(threadManager);
        mPluginManageDataSource = pluginManageDataSource;
    }

    @Override
    public void getPluginStatus(final String type,final BaseLoadDataCallback<PluginStatus> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mPluginManageDataSource.getPluginStatus(type,createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void updatePluginStatus(final String type, final String action, final BaseOperateDataCallback<Void> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mPluginManageDataSource.updatePluginStatus(type, action,createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void getBTStatus(final BaseLoadDataCallback<PluginStatus> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mPluginManageDataSource.getBTStatus(createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void updateBTStatus(final String op, final BaseOperateDataCallback<Void> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mPluginManageDataSource.updateBTStatus(op,createOperateCallbackRunOnMainThread(callback));
            }
        });

    }
}
