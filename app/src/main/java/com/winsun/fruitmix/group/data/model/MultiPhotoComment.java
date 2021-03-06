package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.user.User;

import java.util.List;

/**
 * Created by Administrator on 2017/8/8.
 */

public class MultiPhotoComment extends UserComment {

    private List<Media> medias;

    public MultiPhotoComment(User creator, long time, List<Media> medias) {
        super(creator, time);
        this.medias = medias;
    }

    public List<Media> getMedias() {
        return medias;
    }
}
