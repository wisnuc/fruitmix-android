package com.winsun.fruitmix.file.model;

import android.content.Context;

import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */

public class RemoteFile extends AbstractRemoteFile {

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public void openAbstractRemoteFile(Context context) {

    }

    @Override
    public List<AbstractRemoteFile> listChildAbstractRemoteFileList() {
        throw new UnsupportedOperationException("File can not call list operation");
    }

    @Override
    public void initChildAbstractRemoteFileList(List<AbstractRemoteFile> abstractRemoteFiles) {
        throw new UnsupportedOperationException("File can not call this operation");
    }

}
