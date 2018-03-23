package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2018/3/23.
 */

public class GetNewCommentFinishedEvent extends OperationEvent {

    private String groupUUID;

    public GetNewCommentFinishedEvent(String action, OperationResult operationResult, String groupUUID) {
        super(action, operationResult);
        this.groupUUID = groupUUID;
    }

    public String getGroupUUID() {
        return groupUUID;
    }

}
