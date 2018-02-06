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

    void clearGroups();

    void getAllUserCommentByGroupUUID(String groupUUID, BaseLoadDataCallback<UserComment> callback);

    void insertUserComment(String groupUUID, UserComment userComment,BaseOperateCallback callback);

    void updateGroupProperty(String groupUUID,String property,String newValue,BaseOperateCallback callback);

    void addUsersInGroup(String groupUUID,List<String> userGUIDs,BaseOperateCallback callback);

    void deleteUsersInGroup(String groupUUID,List<String> userGUIDs,BaseOperateCallback callback);

    Pin insertPin(String groupUUID, Pin pin);

    boolean modifyPin(String groupUUID, String pinName,String pinUUID);

    boolean deletePin(String groupUUID,String pinUUID);

    boolean insertMediaToPin(Collection<Media> medias, String groupUUID, String pinUUID);

    boolean insertFileToPin(Collection<AbstractFile> files, String groupUUID, String pinUUID);

    boolean updatePinInGroup(Pin pin, String groupUUID);

    Pin getPinInGroup(String pinUUID, String groupUUID);

}
