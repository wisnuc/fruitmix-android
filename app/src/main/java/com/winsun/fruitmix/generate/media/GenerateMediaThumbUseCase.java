package com.winsun.fruitmix.generate.media;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.util.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2017/8/18.
 */

public class GenerateMediaThumbUseCase {

    public static final String TAG = GenerateMediaThumbUseCase.class.getSimpleName();

    public static GenerateMediaThumbUseCase instance;

    private MediaDataSourceRepository mediaDataSourceRepository;

    private ThreadManager threadManager;

    private boolean mStopGenerateThumb = false;

    private boolean mStopGenerateMiniThumb = false;

    public static GenerateMediaThumbUseCase getInstance(MediaDataSourceRepository mediaDataSourceRepository) {

        if (instance == null)
            instance = new GenerateMediaThumbUseCase(mediaDataSourceRepository);

        return instance;
    }

    private GenerateMediaThumbUseCase(MediaDataSourceRepository mediaDataSourceRepository) {
        this.mediaDataSourceRepository = mediaDataSourceRepository;

        threadManager = ThreadManagerImpl.getInstance();
    }

    public void startGenerateMediaThumb() {

        Log.d(TAG, "startGenerateMediaThumb: ");

        getMedia(new BaseLoadDataCallback<Media>() {
            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {

                List<Media> needOperateMedias = new ArrayList<>();

                for (Media media : data) {
                    if (media.isLocal() && media.getThumb().isEmpty()) {
                        needOperateMedias.add(media);
                    }
                }

                Log.d(TAG, "onSucceed: needOperateMedias size" + needOperateMedias.size());

                startGenerateMediaThumbInThread(needOperateMedias);


            }

            @Override
            public void onFail(OperationResult operationResult) {

            }
        });


    }

    public void startGenerateMediaMiniThumb() {

        Log.d(TAG, "startGenerateMediaMiniThumb: ");

        getMedia(new BaseLoadDataCallback<Media>() {
            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {

                List<Media> needOperateMedias = new ArrayList<>();

                for (Media media : data) {
                    if (media.isLocal() && media.getMiniThumbPath().isEmpty()) {
                        needOperateMedias.add(media);
                    }
                }

                Log.d(TAG, "onSucceed: needOperateMiniMedias size" + needOperateMedias.size());

                startGenerateMediaMiniThumbInThread(needOperateMedias);

            }

            @Override
            public void onFail(OperationResult operationResult) {

            }
        });

    }

    private void getMedia(BaseLoadDataCallback<Media> callback) {

        mediaDataSourceRepository.getLocalMedia(callback);

    }

    private void startGenerateMediaThumbInThread(List<Media> data) {
        if (data == null || data.isEmpty())
            return;

        for (final Media media : data) {

            if (media.isLocal() && media.getThumb().isEmpty()) {

                threadManager.runOnGenerateThumbThread(new Runnable() {
                    @Override
                    public void run() {

                        if (mStopGenerateThumb)
                            return;

                        boolean result = FileUtil.writeBitmapToLocalPhotoThumbnailFolder(media);

                        if (result && !mStopGenerateThumb) {

                            mediaDataSourceRepository.updateMedia(media);

                        }

                    }
                });


            }

        }
    }

    public void stopGenerateLocalPhotoThumbnail() {

        mStopGenerateThumb = true;

        threadManager.stopGenerateThumbThreadNow();
    }

    private void startGenerateMediaMiniThumbInThread(List<Media> data) {
        if (data == null || data.isEmpty())
            return;

        for (final Media media : data) {

            if (media.isLocal() && media.getMiniThumbPath().isEmpty()) {

                threadManager.runOnGenerateMiniThumbThread(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {

                        if (mStopGenerateMiniThumb)
                            return false;

                        boolean result = FileUtil.writeBitmapToLocalPhotoMiniThumbnailFolder(media);

                        if (result && !mStopGenerateMiniThumb) {

                            mediaDataSourceRepository.updateMedia(media);

                        }

                        return true;
                    }
                });


            }

        }
    }

    public void stopGenerateLocalPhotoMiniThumbnail() {

        mStopGenerateMiniThumb = true;

        threadManager.stopGenerateMiniThumbThreadNow();
    }


}
