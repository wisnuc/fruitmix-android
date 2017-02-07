package com.winsun.fruitmix.refactor.common.callback;

import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.List;

/**
 * Created by Administrator on 2017/2/7.
 */

public interface FileOperationCallback {

    interface LoadFileOperationCallback{

        void onLoadSucceed(OperationResult result, List<AbstractRemoteFile> files);

        void onLoadFail(OperationResult result);

    }

    

}
