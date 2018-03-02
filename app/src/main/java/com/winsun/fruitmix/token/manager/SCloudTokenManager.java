package com.winsun.fruitmix.token.manager;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.token.data.TokenDataSource;
import com.winsun.fruitmix.token.param.SCloudTokenParam;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2018/3/1.
 */

public class SCloudTokenManager extends TokenManager<SCloudTokenParam> {

    private String mSCloudToken;

    public SCloudTokenManager(SCloudTokenParam tokenParam, TokenDataSource tokenDataSource) {
        super(tokenParam, tokenDataSource);
    }

    @Override
    public synchronized void resetToken() {

        mSCloudToken = null;

    }

    @Override
    public synchronized void getToken(final BaseLoadDataCallback<String> callback) {

        if (mSCloudToken == null || mSCloudToken.isEmpty()) {

            mTokenDataSource.getSCloudTokenThroughStationTokenWithoutThreadChange(mTokenParam.getCurrentUserGUID(), new BaseLoadDataCallback<String>() {
                @Override
                public void onSucceed(List<String> data, OperationResult operationResult) {

                    callback.onSucceed(data, operationResult);

                }

                @Override
                public void onFail(OperationResult operationResult) {

                    callback.onFail(operationResult);

                }
            });


        } else {

            callback.onSucceed(Collections.singletonList(mSCloudToken), new OperationSuccess());

        }

    }

}
