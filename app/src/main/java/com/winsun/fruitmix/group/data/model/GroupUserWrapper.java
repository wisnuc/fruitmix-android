package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2018/3/14.
 */

public class GroupUserWrapper {

    private User mUser;
    private String mGroupUUID;

    public GroupUserWrapper(User user, String groupUUID) {
        mUser = user;
        mGroupUUID = groupUUID;
    }

    public String getGroupUUID() {
        return mGroupUUID;
    }

    public User getUser() {
        return mUser;
    }
}
