package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2017/10/29.
 */

public class RetrieveVideoThumbnailEvent extends OperationEvent {

    private Media media;

    public RetrieveVideoThumbnailEvent(String action, OperationResult operationResult, Media media) {
        super(action, operationResult);
        this.media = media;
    }

    public Media getMedia() {
        return media;
    }
}

