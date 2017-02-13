package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

/**
 * Created by Administrator on 2017/2/6.
 */

public interface SplashContract {

    interface SplashView extends BaseView {

        void emptyCacheToken();

        void welcome();
    }

    interface SplashPresenter extends BasePresenter<SplashView> {

        void createDownloadFileStoreFolder();

        void loadToken();

    }

}
