package com.winsun.fruitmix.refactor.business.callback;

import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/2/7.
 */

public interface FileShareOperationCallback {

    interface LoadFileShareCallback{

        void onLoadSucceed(OperationResult result, Collection<AbstractRemoteFile> files);

        void onLoadFail(OperationResult result);

    }

}
