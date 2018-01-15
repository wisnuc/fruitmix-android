package com.winsun.fruitmix.inbox.data.model;

import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.model.UserComment;

import java.util.List;

/**
 * Created by Administrator on 2018/1/15.
 */

public class GroupFileComment extends GroupUserComment {

    private List<AbstractFile> mAbstractFiles;

    public GroupFileComment(UserComment userComment, String groupUUID, String groupName, List<AbstractFile> abstractFiles) {
        super(userComment, groupUUID, groupName);
        mAbstractFiles = abstractFiles;
    }

    public List<AbstractFile> getAbstractFiles() {
        return mAbstractFiles;
    }

}
