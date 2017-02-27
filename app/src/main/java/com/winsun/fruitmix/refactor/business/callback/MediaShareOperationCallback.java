package com.winsun.fruitmix.refactor.business.callback;

import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/2/7.
 */

public interface MediaShareOperationCallback {

    interface LoadMediaSharesCallback{

        void onLoadSucceed(OperationResult operationResult, Collection<MediaShare> mediaShares);

        void onLoadFail(OperationResult operationResult);

    }

    interface OperateMediaShareCallback{

        void onOperateSucceed(OperationResult operationResult,MediaShare mediaShare);

        void onOperateFail(OperationResult operationResult);

    }

}
