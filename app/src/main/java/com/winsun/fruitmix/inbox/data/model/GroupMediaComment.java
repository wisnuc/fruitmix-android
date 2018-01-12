package com.winsun.fruitmix.inbox.data.model;

import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.user.User;

import java.util.List;

/**
 * Created by Administrator on 2018/1/11.
 */

public class GroupMediaComment extends GroupUserComment {

    private List<Media> mMedias;

    public GroupMediaComment(UserComment userComment, String groupUUID, String groupName, List<Media> medias) {
        super(userComment, groupUUID, groupName);
        mMedias = medias;
    }

    public List<Media> getMedias() {
        return mMedias;
    }

}
