package com.winsun.fruitmix.group.data.source;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.model.Pin;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.user.User;

import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/7/20.
 */

public interface GroupDataSource extends PinDataSource{

    void addGroup(PrivateGroup group, BaseOperateCallback callback);

    void getAllGroups(BaseLoadDataCallback<PrivateGroup> callback);

    void deleteGroup(GroupRequestParam groupRequestParam,BaseOperateCallback callback);

    void quitGroup(GroupRequestParam groupRequestParam,String currentUserGUID,BaseOperateCallback callback);

    void clearGroups();

    void getUserCommentRange(GroupRequestParam groupRequestParam,long first,long last,int count,BaseLoadDataCallback<UserComment> callback);

    void getAllUserCommentByGroupUUID(GroupRequestParam groupRequestParam, BaseLoadDataCallback<UserComment> callback);

    void insertUserComment(GroupRequestParam groupRequestParam, UserComment userComment,BaseOperateCallback callback);

    void updateGroupProperty(GroupRequestParam groupRequestParam,String property,String newValue,BaseOperateCallback callback);

    void addUsersInGroup(GroupRequestParam groupRequestParam, List<User> users, BaseOperateCallback callback);

    void deleteUsersInGroup(GroupRequestParam groupRequestParam,List<String> userGUIDs,BaseOperateCallback callback);


}
