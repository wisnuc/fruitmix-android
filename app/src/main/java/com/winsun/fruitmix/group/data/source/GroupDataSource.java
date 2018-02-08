package com.winsun.fruitmix.group.data.source;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.model.Pin;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.mediaModule.model.Media;

import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/7/20.
 */

public interface GroupDataSource {

    void addGroup(PrivateGroup group, BaseOperateCallback callback);

    void getAllGroups(BaseLoadDataCallback<PrivateGroup> callback);

    void deleteGroup(GroupRequestParam groupRequestParam,BaseOperateCallback callback);

    void quitGroup(GroupRequestParam groupRequestParam,String currentUserGUID,BaseOperateCallback callback);

    void clearGroups();

    void getAllUserCommentByGroupUUID(GroupRequestParam groupRequestParam, BaseLoadDataCallback<UserComment> callback);

    void insertUserComment(GroupRequestParam groupRequestParam, UserComment userComment,BaseOperateCallback callback);

    void updateGroupProperty(GroupRequestParam groupRequestParam,String property,String newValue,BaseOperateCallback callback);

    void addUsersInGroup(GroupRequestParam groupRequestParam,List<String> userGUIDs,BaseOperateCallback callback);

    void deleteUsersInGroup(GroupRequestParam groupRequestParam,List<String> userGUIDs,BaseOperateCallback callback);

    Pin insertPin(String groupUUID, Pin pin);

    boolean modifyPin(String groupUUID, String pinName,String pinUUID);

    boolean deletePin(String groupUUID,String pinUUID);

    boolean insertMediaToPin(Collection<Media> medias, String groupUUID, String pinUUID);

    boolean insertFileToPin(Collection<AbstractFile> files, String groupUUID, String pinUUID);

    boolean updatePinInGroup(Pin pin, String groupUUID);

    Pin getPinInGroup(String pinUUID, String groupUUID);

}
