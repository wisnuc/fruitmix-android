package com.winsun.fruitmix.upload.media;

import android.os.Handler;
import android.os.Message;

/**
 * Created by Administrator on 2017/11/9.
 */

public class RetryUploadHandler extends Handler {

    private UploadMediaUseCase uploadMediaUseCase;

    RetryUploadHandler(UploadMediaUseCase uploadMediaUseCase) {

        this.uploadMediaUseCase = uploadMediaUseCase;

    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        switch (msg.what) {
            case UploadMediaUseCase.RETRY_UPLOAD:

                if (uploadMediaUseCase != null)
                    uploadMediaUseCase.startUploadMedia();

                break;
        }

    }

}
