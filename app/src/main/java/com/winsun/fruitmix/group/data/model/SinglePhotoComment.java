package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/8/8.
 */

public class SinglePhotoComment extends UserComment {

    private Media media;

    public SinglePhotoComment(User creator, long time, Media media) {
        super(creator, time);
        this.media = media;
    }

    public Media getMedia() {
        return media;
    }
}
