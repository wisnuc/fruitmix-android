package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

/**
 * Created by Administrator on 2017/2/21.
 */

public interface FileDownloadFragmentContract {

    interface FileDownloadFragmentView extends BaseView{


    }

    interface FileDownloadFragmentPresenter extends BasePresenter<FileDownloadFragmentView>{

        void handleTitle();

        boolean handleBackPressedOrNot();

        void fileMainMenuOnClick();

    }

}
