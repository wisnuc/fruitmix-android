package com.winsun.fruitmix.group.data.source;

import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.model.Pin;
import com.winsun.fruitmix.mediaModule.model.Media;

import java.util.Collection;

/**
 * Created by Administrator on 2018/3/1.
 */

public interface PinDataSource {

    Pin insertPin(String groupUUID, Pin pin);

    boolean modifyPin(String groupUUID, String pinName,String pinUUID);

    boolean deletePin(String groupUUID,String pinUUID);

    boolean insertMediaToPin(Collection<Media> medias, String groupUUID, String pinUUID);

    boolean insertFileToPin(Collection<AbstractFile> files, String groupUUID, String pinUUID);

    boolean updatePinInGroup(Pin pin, String groupUUID);

    Pin getPinInGroup(String pinUUID, String groupUUID);

}
