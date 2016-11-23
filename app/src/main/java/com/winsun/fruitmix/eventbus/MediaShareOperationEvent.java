package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.util.OperationResultType;

/**
 * Created by Administrator on 2016/11/1.
 */

public class MediaShareOperationEvent extends OperationEvent {

    private MediaShare mediaShare;

    public MediaShareOperationEvent(String action, OperationResultType operationResultType, MediaShare mediaShare) {
        super(action, operationResultType);
        this.mediaShare = mediaShare;
    }

    public MediaShare getMediaShare() {
        return mediaShare;
    }
}
