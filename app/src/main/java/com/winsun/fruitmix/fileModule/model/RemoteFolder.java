package com.winsun.fruitmix.fileModule.model;

import android.content.Context;

import com.winsun.fruitmix.util.FNAS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */

public class RemoteFolder extends AbstractRemoteFile {

    private List<AbstractRemoteFile> abstractRemoteFiles;

    public RemoteFolder(){
        abstractRemoteFiles = new ArrayList<>();
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public void openAbstractRemoteFile(Context context) {

        FNAS.retrieveRemoteFile(context,getUuid());

    }

    @Override
    public List<AbstractRemoteFile> listChildAbstractRemoteFileList() {
        return Collections.unmodifiableList(abstractRemoteFiles);
    }

    @Override
    public void initChildAbstractRemoteFileList(List<AbstractRemoteFile> abstractRemoteFiles) {
        this.abstractRemoteFiles.addAll(abstractRemoteFiles);
    }

}
