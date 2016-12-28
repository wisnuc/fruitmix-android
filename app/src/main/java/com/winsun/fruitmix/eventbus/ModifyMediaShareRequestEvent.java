package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;

/**
 * Created by Administrator on 2016/11/9.
 */

public class ModifyMediaShareRequestEvent extends MediaShareRequestEvent {

    private String requestData;

    public ModifyMediaShareRequestEvent(OperationType operationType, OperationTargetType operationTargetType, MediaShare mediaShare, String requestData) {
        super(operationType, operationTargetType, mediaShare);
        this.requestData = requestData;
    }

    public String getRequestData() {
        return requestData;
    }
}
