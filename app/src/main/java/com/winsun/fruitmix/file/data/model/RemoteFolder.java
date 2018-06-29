package com.winsun.fruitmix.file.data.model;

import com.winsun.fruitmix.R;

/**
 * Created by Administrator on 2016/10/25.
 */

public class RemoteFolder extends AbstractRemoteFile {

    public RemoteFolder() {

        setFileTypeResID(R.drawable.folder_icon);
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public String getDateText() {
        if (getTime() == 0)
            return "";
        else {
            return super.getDateText();
        }
    }

    @Override
    public String getTimeText() {
        if (getTime() == 0)
            return "";
        else {
            return super.getTimeText();
        }
    }

    @Override
    public AbstractFile copySelf() {

        RemoteFolder folder = new RemoteFolder();

        folder.setName(getName());
        folder.setFileTypeResID(getFileTypeResID());
        folder.setSize(getSize());
        folder.setTime(getTime());
        folder.setUuid(getUuid());
        folder.setParentFolderName(getParentFolderName());
        folder.setParentFolderPath(getParentFolderPath());
        folder.setParentFolderUUID(getParentFolderUUID());
        folder.setRootFolderUUID(getRootFolderUUID());

        return folder;

    }
}
