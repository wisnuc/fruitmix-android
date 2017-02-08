package com.winsun.fruitmix.refactor.data.loadOperationResult;

import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;

import java.util.List;

/**
 * Created by Administrator on 2017/2/8.
 */

public class FileSharesLoadOperationResult extends LoadOperationResult {

    private List<AbstractRemoteFile> files;

    public List<AbstractRemoteFile> getFiles() {
        return files;
    }

    public void setFiles(List<AbstractRemoteFile> files) {
        this.files = files;
    }
}
