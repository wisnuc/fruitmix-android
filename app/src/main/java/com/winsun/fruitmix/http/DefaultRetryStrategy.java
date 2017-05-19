package com.winsun.fruitmix.http;

/**
 * Created by Administrator on 2017/5/17.
 */

public class DefaultRetryStrategy implements RetryStrategy {

    private static final int DEFAULT_MAX_RETRY_COUNT = 1;

    private int mCurrentRetryCount;
    private int mMaxRetryCount;

    public DefaultRetryStrategy() {

        this(DEFAULT_MAX_RETRY_COUNT);

    }

    public DefaultRetryStrategy(int mMaxRetryCount) {
        this.mMaxRetryCount = mMaxRetryCount;
    }

    @Override
    public boolean needRetry() {

        mCurrentRetryCount++;

        return mCurrentRetryCount <= mMaxRetryCount;
    }


}
