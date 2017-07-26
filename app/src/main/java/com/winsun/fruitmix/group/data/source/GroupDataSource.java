package com.winsun.fruitmix.group.data.source;

import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;

import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/7/20.
 */

public interface GroupDataSource {

    void addGroup(Collection<PrivateGroup> groups);

    List<PrivateGroup> getAllGroups();

    void clearGroups();

    PrivateGroup getGroupByUUID(String groupUUID);

    UserComment insertUserComment(String groupUUID,UserComment userComment);

}
