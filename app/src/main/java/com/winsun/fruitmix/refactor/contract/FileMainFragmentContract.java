package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface FileMainFragmentContract {

    interface FileMainFragmentView extends BaseView {

        void resetBottomNavigationItemCheckState();

        void setBottomNavigationItemChecked(int position);

        void setViewPagerCurrentItem(int position);

        void setTitleText(String titleText);

        void setFileMainMenuVisibility(int visibility);

    }

    interface FileMainFragmentPresenter extends BasePresenter<FileMainFragmentView> {

        void onShareNavigationItemSelected();

        void onFileNavigationItemSelected();

        void onDownloadNavigationItemSelected();

        void onPageFileSelect();

        void onPageFileShareSelect();

        void onPageFileDownloadSelect();

        void onToolbarClick();
    }

}
