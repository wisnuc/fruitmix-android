package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.util.OperationResult;

/**
 * Created by Administrator on 2016/10/27.
 */

public class RetrieveFileOperationEvent {

    private String action;
    private OperationResult operationResult;
    private String folderUUID;

    public RetrieveFileOperationEvent(String action, OperationResult operationResult,String folderUUID) {
        this.action = action;
        this.operationResult = operationResult;
        this.folderUUID = folderUUID;
    }

    public RetrieveFileOperationEvent() {
    }

    public OperationResult getOperationResult() {
        return operationResult;
    }

    public String getAction() {
        return action;
    }

    public String getFolderUUID() {
        return folderUUID;
    }
}
