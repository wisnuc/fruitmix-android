package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.util.OperationResultType;

/**
 * Created by Administrator on 2016/11/11.
 */

public class MediaOperationEvent extends OperationEvent {
    public MediaOperationEvent(String action, OperationResultType operationResultType) {
        super(action, operationResultType);
    }
}
