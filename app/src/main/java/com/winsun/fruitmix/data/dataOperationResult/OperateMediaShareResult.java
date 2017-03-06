package com.winsun.fruitmix.data.dataOperationResult;

import com.winsun.fruitmix.mediaModule.model.MediaShare;

/**
 * Created by Administrator on 2017/2/27.
 */

public class OperateMediaShareResult extends DataOperationResult {

    private MediaShare mediaShare;

    public MediaShare getMediaShare() {
        return mediaShare;
    }

    public void setMediaShare(MediaShare mediaShare) {
        this.mediaShare = mediaShare;
    }
}
