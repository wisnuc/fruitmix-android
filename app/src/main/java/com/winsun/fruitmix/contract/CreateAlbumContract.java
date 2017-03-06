package com.winsun.fruitmix.contract;

import com.winsun.fruitmix.common.BasePresenter;
import com.winsun.fruitmix.common.BaseView;
import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface CreateAlbumContract {

    interface CreateAlbumView extends BaseView {

        void setLayoutTitle(String title);

        void setAlbumTitleHint(String hint);

        String getString(int resID);

        void finishActivity();

        void setResult(int result);

        void showOperationResultToast(OperationResult result);
    }

    interface CreateAlbumPresenter extends BasePresenter<CreateAlbumView> {

        void createAlbum(String title, String desc, boolean isPublic, boolean isMaintained);

        void initView();
    }

}
