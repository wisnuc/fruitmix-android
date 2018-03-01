package com.winsun.fruitmix.list.data;

import android.util.Log;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.exception.NetworkException;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.media.CalcMediaDigestStrategy;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2018/2/27.
 */

public class MediaInTweetDataSourceRepository extends BaseDataRepository implements MediaDataSourceRepository {

    public static final String TAG = MediaInTweetDataSourceRepository.class.getSimpleName();

    private MediaComment mMediaComment;

    private MediaInTweetRemoteDataSource mMediaInTweetRemoteDataSource;

    public MediaInTweetDataSourceRepository(ThreadManager threadManager, MediaComment mediaComment,
                                            MediaInTweetRemoteDataSource mediaInTweetRemoteDataSource) {
        super(threadManager);
        mMediaComment = mediaComment;
        mMediaInTweetRemoteDataSource = mediaInTweetRemoteDataSource;
    }

    @Override
    public void getMedia(BaseLoadDataCallback<Media> callback) {

        callback.onSucceed(mMediaComment.getMedias(), new OperationSuccess());

    }

    @Override
    public void downloadMedia(final List<Media> medias, BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "call: begin retrieve original photo task in tweet");

                for (Media media : medias) {

                    Log.d(TAG, "call: media uuid:" + media.getUuid());

                    try {
                        mMediaInTweetRemoteDataSource.downloadMedia(media);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NetworkException e) {
                        e.printStackTrace();
                    }

                }

                Log.d(TAG, "call: finish retrieve original photo task in tweet");

                runOnMainThreadCallback.onSucceed(true, new OperationSuccess());

            }
        });
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
