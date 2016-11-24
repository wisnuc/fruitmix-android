package com.winsun.fruitmix.executor;

import android.content.Context;

import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.OperationResultType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2016/9/19.
 */
public class UploadMediaTask implements Callable<Boolean> {

    private Media media;
    private Context context;

    public UploadMediaTask(Context context,Media media) {
        this.media = media;
        this.context = context;
    }

    @Override
    public Boolean call() throws Exception {

        boolean result = media.uploadIfNotDone(context);

        if(result){

            EventBus.getDefault().post(new OperationEvent(Util.LOCAL_PHOTO_UPLOAD_STATE_CHANGED, new OperationSuccess()));
        }

        return result;
    }

}
