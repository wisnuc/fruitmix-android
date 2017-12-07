package com.winsun.fruitmix.model.operationResult;

import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;

import java.util.List;

/**
 * Created by Administrator on 2017/12/6.
 */

public class OperationSuccessWithFile extends OperationSuccess {

    private List<AbstractRemoteFile> mList;

    public OperationSuccessWithFile(List<AbstractRemoteFile> list) {
        mList = list;
    }

    public List<AbstractRemoteFile> getList() {
        return mList;
    }
}
