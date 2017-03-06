package com.winsun.fruitmix.business.callback;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.MediaFragmentDataLoader;

import java.util.List;

/**
 * Created by Administrator on 2017/2/7.
 */

public interface MediaOperationCallback {

    interface LoadMediasCallback{

        void onLoadSucceed(OperationResult operationResult, List<Media> medias);

        void onLoadFail(OperationResult operationResult);

    }

    interface HandleMediaForMediaFragmentCallback{

        void onOperateFinished(MediaFragmentDataLoader loader);

    }

}
