package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.util.OperationResultType;

/**
 * Created by Administrator on 2016/10/27.
 */

public class RetrieveFileOperationEvent extends OperationEvent{

    private String folderUUID;

    public RetrieveFileOperationEvent(String action, OperationResultType operationResultType, String folderUUID) {
        super(action, operationResultType);
        this.folderUUID = folderUUID;
    }

    public String getFolderUUID() {
        return folderUUID;
    }
}
