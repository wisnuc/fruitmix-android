package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/8/9.
 */

public class SingleFileComment extends UserComment {

    private AbstractFile file;

    public SingleFileComment(User creator, long time, AbstractFile file) {
        super(creator, time);
        this.file = file;
    }

    public AbstractFile getFile() {
        return file;
    }
}
