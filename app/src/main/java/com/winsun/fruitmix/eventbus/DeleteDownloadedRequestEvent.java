package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2016/11/21.
 */

public class DeleteDownloadedRequestEvent extends RequestEvent {

    private List<String> fileUUIDs;

    public DeleteDownloadedRequestEvent(OperationType operationType, OperationTargetType operationTargetType,List<String> fileUUIDs) {
        super(operationType, operationTargetType);

        this.fileUUIDs = fileUUIDs;
    }

    public List<String> getFileUUIDs() {
        return Collections.unmodifiableList(fileUUIDs);
    }
}
