package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;

/**
 * Created by Administrator on 2016/11/9.
 */

public class EditPhotoInMediaShareRequestEvent extends RequestEvent {

    private MediaShare originalMediaShare;
    private MediaShare modifiedMediaShare;

    public EditPhotoInMediaShareRequestEvent(OperationType operationType, OperationTargetType operationTargetType, MediaShare originalMediaShare, MediaShare modifiedMediaShare) {
        super(operationType, operationTargetType);
        this.originalMediaShare = originalMediaShare;
        this.modifiedMediaShare = modifiedMediaShare;
    }

    public MediaShare getOriginalMediaShare() {
        return originalMediaShare;
    }

    public MediaShare getModifiedMediaShare() {
        return modifiedMediaShare;
    }
}
