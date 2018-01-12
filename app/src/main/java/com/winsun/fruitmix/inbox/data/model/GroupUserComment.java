package com.winsun.fruitmix.inbox.data.model;

import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2018/1/11.
 */

public class GroupUserComment {

    private UserComment mUserComment;

    private String groupUUID;
    private String groupName;

    public GroupUserComment(UserComment userComment, String groupUUID, String groupName) {
        mUserComment = userComment;
        this.groupUUID = groupUUID;
        this.groupName = groupName;
    }

    public UserComment getUserComment() {
        return mUserComment;
    }

    public String getGroupUUID() {
        return groupUUID;
    }

    public String getGroupName() {
        return groupName;
    }


}
