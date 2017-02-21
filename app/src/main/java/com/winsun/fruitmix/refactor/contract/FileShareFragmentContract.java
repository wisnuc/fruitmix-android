package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

/**
 * Created by Administrator on 2017/2/21.
 */

public interface FileShareFragmentContract {

    interface FileShareFragmentView extends BaseView{


    }

    interface FileShareFragmentPresenter extends BasePresenter<FileShareFragmentView>{

        void handleTitle();

        boolean handleBackPressedOrNot();

    }

}
