package com.winsun.fruitmix.group.data.source;

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

    void addGroup(Collection<PrivateGroup> groups);

    List<PrivateGroup> getAllGroups();

    void clearGroups();

    PrivateGroup getGroupByUUID(String groupUUID);

    UserComment insertUserComment(String groupUUID, UserComment userComment);

    Pin insertPin(String groupUUID, Pin pin);

    boolean modifyPin(String groupUUID, String pinName,String pinUUID);

    boolean deletePin(String groupUUID,String pinUUID);

    boolean insertMediaToPin(Collection<Media> medias, String groupUUID, String pinUUID);

    boolean insertFileToPin(Collection<AbstractFile> files, String groupUUID, String pinUUID);

    boolean updatePinInGroup(Pin pin, String groupUUID);

    Pin getPinInGroup(String pinUUID, String groupUUID);

}
