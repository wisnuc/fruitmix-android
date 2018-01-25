package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.user.User;

import java.util.List;

/**
 * Created by Administrator on 2017/8/8.
 */

public class MediaComment extends TextComment {

    private List<Media> medias;

    public MediaComment(String uuid, User creator, long time,String groupUUID, List<Media> medias) {
        super(uuid,creator, time,groupUUID);
        this.medias = medias;
    }

    public List<Media> getMedias() {
        return medias;
    }

}
