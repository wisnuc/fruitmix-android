package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.util.OperationResult;

/**
 * Created by Administrator on 2016/10/27.
 */

public class RetrieveFileOperationEvent extends OperationEvent{

    private String folderUUID;

    public RetrieveFileOperationEvent(String action, OperationResult operationResult,String folderUUID) {
        super(action,operationResult);
        this.folderUUID = folderUUID;
    }

    public String getFolderUUID() {
        return folderUUID;
    }
}
