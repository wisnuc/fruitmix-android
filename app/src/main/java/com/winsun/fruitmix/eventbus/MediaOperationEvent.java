package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.operationResult.OperationResult;

/**
 * Created by Administrator on 2016/11/11.
 */

public class MediaOperationEvent extends OperationEvent {
    public MediaOperationEvent(String action, OperationResult operationResult) {
        super(action, operationResult);
    }
}
