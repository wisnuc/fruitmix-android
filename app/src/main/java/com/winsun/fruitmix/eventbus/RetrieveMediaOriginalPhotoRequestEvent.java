package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;

import java.util.List;

/**
 * Created by Administrator on 2017/3/24.
 */

public class RetrieveMediaOriginalPhotoRequestEvent extends RequestEvent {

    private List<Media> medias;

    public RetrieveMediaOriginalPhotoRequestEvent(OperationType operationType, OperationTargetType operationTargetType, List<Media> medias) {
        super(operationType, operationTargetType);
        this.medias = medias;
    }

    public List<Media> getMedias() {
        return medias;
    }
}
