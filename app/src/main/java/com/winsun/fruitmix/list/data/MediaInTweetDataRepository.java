package com.winsun.fruitmix.list.data;

import android.util.Log;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.exception.NetworkException;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.media.CalcMediaDigestStrategy;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2018/2/27.
 */

public class MediaInTweetDataRepository extends BaseDataRepository implements MediaDataSourceRepository {

    public static final String TAG = MediaInTweetDataRepository.class.getSimpleName();

    private MediaComment mMediaComment;

    private MediaInTweetDataSource mMediaInTweetDataSource;

    private AtomicInteger downloadItemCount;

    public MediaInTweetDataRepository(ThreadManager threadManager, MediaComment mediaComment,
                                      MediaInTweetDataSource mediaInTweetDataSource) {
        super(threadManager);
        mMediaComment = mediaComment;
        mMediaInTweetDataSource = mediaInTweetDataSource;

        downloadItemCount = new AtomicInteger(0);
    }

    @Override
    public void getMedia(BaseLoadDataCallback<Media> callback) {

        callback.onSucceed(mMediaComment.getMedias(), new OperationSuccess());

    }

    @Override
    public void downloadMedia(final List<Media> medias, BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        downloadItemCount.set(medias.size());

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "call: begin retrieve original photo task in tweet");

                for (Media media : medias) {

                    Log.d(TAG, "call: media uuid:" + media.getUuid());

                    mMediaInTweetDataSource.downloadMedia(media, new BaseOperateCallback() {
                        @Override
                        public void onSucceed() {

                            int count = downloadItemCount.decrementAndGet();

                            checkCallback(count, runOnMainThreadCallback);

                        }

                        @Override
                        public void onFail(OperationResult operationResult) {

                            int count = downloadItemCount.decrementAndGet();

                            checkCallback(count, runOnMainThreadCallback);

                        }
                    });


                }


            }
        });
    }

    private void checkCallback(int count, BaseOperateDataCallback<Boolean> callback) {

        Log.d(TAG, "call: finish retrieve original photo task in tweet");

        if (count == 0)
            callback.onSucceed(true, new OperationSuccess());

    }


    @Override
    public void updateMedia(Media media) {

    }

    @Override
    public void updateVideo(Video video) {

    }

    @Override
    public void registerCalcDigestCallback(CalcMediaDigestStrategy.CalcMediaDigestCallback calcMediaDigestCallback) {

    }

    @Override
    public void unregisterCalcDigestCallback(CalcMediaDigestStrategy.CalcMediaDigestCallback calcMediaDigestCallback) {

    }

    @Override
    public void getLocalMedia(BaseLoadDataCallback<Media> callback) {

    }

    @Override
    public void getLocalMediaWithoutThreadChange(BaseLoadDataCallback<Media> callback) {

    }

    @Override
    public void getStationMediaForceRefresh(BaseLoadDataCallback<Media> callback) {

    }

    @Override
    public boolean clearAllStationMediasInDB() {
        return false;
    }

    @Override
    public void resetState() {

    }
}
