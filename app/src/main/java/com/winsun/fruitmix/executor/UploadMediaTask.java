package com.winsun.fruitmix.executor;

import android.content.Context;

import com.winsun.fruitmix.mediaModule.model.Media;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2016/9/19.
 */
public class UploadMediaTask implements Callable<Boolean> {

    private Media media;
    private Context context;

    public UploadMediaTask(Context context, Media media) {
        this.media = media;
        this.context = context;
    }

    @Override
    public Boolean call() throws Exception {

        media.uploadIfNotDone(context);

        return true;
    }

}
