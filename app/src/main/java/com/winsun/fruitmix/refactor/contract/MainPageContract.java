package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface MainPageContract {

    interface MainPageView extends BaseView {

        void refreshUserInNavigationView(User user);

        void setVersionNameText(String versionNameText);

        void switchDrawerOpenState();

        void lockDrawer();

        void unlockDrawer();

        void closeDrawer();

    }

    interface MainPagePresenter extends BasePresenter<MainPageView> {

        void onUserManageNavigationItemSelected();

        void onLogoutNavigationItemSelected();

        void onFileNavigationItemSelected();

    }

}
