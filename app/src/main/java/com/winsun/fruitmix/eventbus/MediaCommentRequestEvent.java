package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;

/**
 * Created by Administrator on 2016/11/9.
 */

public class MediaCommentRequestEvent extends RequestEvent {

    private String imageUUID;
    private Comment comment;

    public MediaCommentRequestEvent(OperationType operationType, OperationTargetType operationTargetType, String imageUUID, Comment comment) {
        super(operationType, operationTargetType);
        this.imageUUID = imageUUID;
        this.comment = comment;
    }

    public Comment getComment() {
        return comment;
    }

    public String getImageUUID() {
        return imageUUID;
    }
}
