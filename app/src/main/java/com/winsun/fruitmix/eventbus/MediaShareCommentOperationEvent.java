package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2016/11/1.
 */

public class MediaShareCommentOperationEvent extends OperationEvent {

    private Comment comment;
    private String imageUUID;

    public MediaShareCommentOperationEvent(String action, OperationResult operationResult, Comment comment, String imageUUID) {
        super(action, operationResult);
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
