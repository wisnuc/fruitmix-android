package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.user.User;

import java.util.List;

/**
 * Created by Administrator on 2017/8/9.
 */

public class FileComment extends TextComment {

    private List<AbstractFile> files;

    public FileComment(String uuid, User creator, long time, String groupUUID, List<AbstractFile> files) {
        super(uuid, creator, time, groupUUID);
        this.files = files;
    }

    public List<AbstractFile> getFiles() {
        return files;
    }
}
