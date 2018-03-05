package com.winsun.fruitmix.list.data;

import android.util.Log;

import com.winsun.fruitmix.base.data.BaseDataOperator;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.exception.NetworkException;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

/**
 * Created by Administrator on 2018/3/5.
 */

public class MediaInTweetDataSourceWrapper implements MediaInTweetDataSource {

    public static final String TAG = MediaInTweetDataSourceWrapper.class.getSimpleName();

    private MediaInTweetRemoteDataSource mMediaInTweetRemoteDataSource;

    private BaseDataOperator mBaseDataOperator;

    public MediaInTweetDataSourceWrapper(MediaInTweetRemoteDataSource mediaInTweetRemoteDataSource, BaseDataOperator baseDataOperator) {
        mMediaInTweetRemoteDataSource = mediaInTweetRemoteDataSource;
        mBaseDataOperator = baseDataOperator;
    }

    @Override
    public void downloadMedia(final Media media, final BaseOperateCallback callback) {

        mBaseDataOperator.preConditionCheck(false,new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                try {
                    handleGetSCloudToken();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NetworkException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFail(OperationResult operationResult) {

                try {
                    handleGetSCloudToken();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NetworkException e) {
                    e.printStackTrace();
                }

            }

            private void handleGetSCloudToken() throws IOException, NetworkException {
                boolean result = mMediaInTweetRemoteDataSource.downloadMedia(media);

                Log.d(TAG, "download media result: " + result);

                callback.onSucceed();
            }
        });

    }
}
