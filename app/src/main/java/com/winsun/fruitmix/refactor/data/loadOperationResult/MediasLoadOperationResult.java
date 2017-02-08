package com.winsun.fruitmix.refactor.data.loadOperationResult;

import com.winsun.fruitmix.mediaModule.model.Media;

import java.util.List;

/**
 * Created by Administrator on 2017/2/8.
 */

public class MediasLoadOperationResult extends LoadOperationResult {

    private List<Media> medias;

    public List<Media> getMedias() {
        return medias;
    }

    public void setMedias(List<Media> medias) {
        this.medias = medias;
    }
}
