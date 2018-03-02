package com.winsun.fruitmix.base.data.retry;

import com.winsun.fruitmix.http.DefaultRetryStrategy;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.token.manager.TokenManager;

/**
 * Created by Administrator on 2018/3/1.
 */

public class RefreshTokenRetryStrategy extends DefaultHttpRetryStrategy {

    private TokenManager mTokenManager;

    public RefreshTokenRetryStrategy(TokenManager tokenManager) {
        mTokenManager = tokenManager;
    }

    public RefreshTokenRetryStrategy(int mMaxRetryCount, TokenManager tokenManager) {
        super(mMaxRetryCount);
        mTokenManager = tokenManager;
    }

    @Override
    public boolean needRetry() {

        boolean result = super.needRetry();

        if (result) {
            mTokenManager.resetToken();
        }

        return result;

    }

}
