package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.user.User;

import java.util.List;

/**
 * Created by Administrator on 2017/8/9.
 */

public class MultiFileComment extends UserComment {

    private List<AbstractFile> files;

    public MultiFileComment(User creator, long time, List<AbstractFile> files) {
        super(creator, time);
        this.files = files;
    }

    public List<AbstractFile> getFiles() {
        return files;
    }
}
