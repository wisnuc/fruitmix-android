package com.winsun.fruitmix.refactor.contract;

import android.content.Context;

import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

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
