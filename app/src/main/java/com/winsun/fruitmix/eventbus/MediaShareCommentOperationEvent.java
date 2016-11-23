package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.util.OperationResultType;

/**
 * Created by Administrator on 2016/11/1.
 */

public class MediaShareCommentOperationEvent extends OperationEvent {

    private Comment comment;
    private String imageUUID;

    public MediaShareCommentOperationEvent(String action, OperationResultType operationResultType, Comment comment, String imageUUID) {
        super(action, operationResultType);
        this.comment = comment;
        this.imageUUID = imageUUID;
    }

    public Comment getComment() {
        return comment;
    }

    public String getImageUUID() {
        return imageUUID;
    }
}
