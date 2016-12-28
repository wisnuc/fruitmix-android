package com.winsun.fruitmix.model.operationResult;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.model.OperationResultType;

/**
 * Created by Administrator on 2016/11/23.
 */

public class OperationLocalMediaShareUploading extends OperationResult {

    @Override
    public String getResultMessage(Context context) {
        return context.getString(R.string.local_media_share_uploading);
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.LOCAL_MEDIA_SHARE_UPLOADING;
    }
}
