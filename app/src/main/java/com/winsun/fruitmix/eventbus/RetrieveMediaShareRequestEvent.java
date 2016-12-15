package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;

/**
 * Created by Administrator on 2016/12/15.
 */

public class RetrieveMediaShareRequestEvent extends RequestEvent {

    private boolean loadMediaShareInDBWhenExceptionOccur = true;

    public RetrieveMediaShareRequestEvent(OperationType operationType, OperationTargetType operationTargetType,boolean loadMediaShareInDBWhenExceptionOccur) {
        super(operationType, operationTargetType);

        this.loadMediaShareInDBWhenExceptionOccur = loadMediaShareInDBWhenExceptionOccur;
    }

    public boolean isLoadMediaShareInDBWhenExceptionOccur() {
        return loadMediaShareInDBWhenExceptionOccur;
    }
}
