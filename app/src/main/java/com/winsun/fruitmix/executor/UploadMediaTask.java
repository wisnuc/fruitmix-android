package com.winsun.fruitmix.executor;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.util.Util;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2016/9/19.
 */
public class UploadMediaTask implements Callable<Boolean> {

    private Media media;

    public UploadMediaTask(Media media) {
        this.media = media;
    }

    @Override
    public Boolean call() throws Exception {

        boolean result = media.uploadIfNotDone();

        if(result){
            Intent localPhotoIntent = new Intent(Util.LOCAL_PHOTO_UPLOAD_STATE_CHANGED);
            LocalBroadcastManager.getInstance(Util.APPLICATION_CONTEXT).sendBroadcast(localPhotoIntent);
        }

        return result;
    }

}
