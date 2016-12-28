package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;

/**
 * Created by Administrator on 2016/11/9.
 */

public class MediaShareRequestEvent extends RequestEvent{

    private MediaShare mediaShare;

    public MediaShareRequestEvent(OperationType operationType, OperationTargetType operationTargetType,MediaShare mediaShare) {
        super(operationType, operationTargetType);
        this.mediaShare = mediaShare;
    }

    public MediaShare getMediaShare() {
        return mediaShare;
    }
}
