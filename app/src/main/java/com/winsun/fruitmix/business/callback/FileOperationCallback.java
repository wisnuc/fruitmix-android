package com.winsun.fruitmix.business.callback;

import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.Collection;

/**
 * Created by Administrator on 2017/2/7.
 */

public interface FileOperationCallback {

    interface LoadFileOperationCallback{

        void onLoadSucceed(OperationResult result, Collection<AbstractRemoteFile> files);

        void onLoadFail(OperationResult result);

    }

}
