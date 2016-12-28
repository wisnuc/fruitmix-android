package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;

/**
 * Created by Administrator on 2016/11/9.
 */

public class MediaRequestEvent extends RequestEvent {

    private Media media;

    public MediaRequestEvent(OperationType operationType, OperationTargetType operationTargetType,Media media) {
        super(operationType, operationTargetType);
        this.media = media;
    }

    public Media getMedia() {
        return media;
    }
}
