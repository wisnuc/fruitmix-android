package com.winsun.fruitmix.refactor.data.dataOperationResult;

import com.winsun.fruitmix.mediaModule.model.MediaShare;

import java.util.List;

/**
 * Created by Administrator on 2017/2/8.
 */

public class MediaSharesLoadOperationResult extends DataOperationResult {

    private List<MediaShare> mediaShares;

    public List<MediaShare> getMediaShares() {
        return mediaShares;
    }

    public void setMediaShares(List<MediaShare> mediaShares) {
        this.mediaShares = mediaShares;
    }
}
