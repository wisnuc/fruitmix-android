package com.winsun.fruitmix.contract;

import android.content.Context;

import com.winsun.fruitmix.common.BasePresenter;
import com.winsun.fruitmix.common.BaseView;

/**
 * Created by Administrator on 2017/3/2.
 */

public interface NewPhotoSelectContract {

    interface NewPhotoSelectView extends BaseView {

        void finishActivity();

        void setTitle(String title);

        void setResult(int result);

    }

    interface NewPhotoSelectPresenter extends BasePresenter<NewPhotoSelectView> {

        void handleSelectFinished();

        void initView(Context context);

        void setTitle(String title);

    }

}
