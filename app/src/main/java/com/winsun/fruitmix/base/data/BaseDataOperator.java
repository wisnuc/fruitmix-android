package com.winsun.fruitmix.base.data;

import com.winsun.fruitmix.base.data.retry.DefaultHttpRetryStrategy;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.manager.TokenManager;

import java.util.List;

/**
 * Created by Administrator on 2018/3/1.
 */

public class BaseDataOperator {

    private SystemSettingDataSource mSystemSettingDataSource;

    private TokenManager mTokenManager;

    private SCloudTokenContainer mSCloudTokenContainer;

    private DefaultHttpRetryStrategy mDefaultHttpRetryStrategy;

    public BaseDataOperator(SystemSettingDataSource systemSettingDataSource, TokenManager tokenManager,
                            SCloudTokenContainer SCloudTokenContainer, DefaultHttpRetryStrategy defaultHttpRetryStrategy) {
        mSystemSettingDataSource = systemSettingDataSource;
        mTokenManager = tokenManager;
        mSCloudTokenContainer = SCloudTokenContainer;
        mDefaultHttpRetryStrategy = defaultHttpRetryStrategy;
    }

    public void preConditionCheck(boolean changeThread,final BaseOperateCallback callback) {

        if (mSystemSettingDataSource.getLoginWithWechatCodeOrNot()) {

            callback.onSucceed();

        } else {

            mTokenManager.getToken(changeThread,new BaseLoadDataCallback<String>() {
                @Override
                public void onSucceed(List<String> data, OperationResult operationResult) {

                    mSCloudTokenContainer.setSCloudToken(data.get(0));

                    callback.onSucceed();

                }

                @Override
                public void onFail(OperationResult operationResult) {

                    mSCloudTokenContainer.setSCloudToken("");

                    callback.onFail(operationResult);

                }
            });

        }

    }


    public boolean needRetryWhenFail(OperationResult result) {

        mDefaultHttpRetryStrategy.setOperationResult(result);

        return mDefaultHttpRetryStrategy.needRetry();

    }

}
