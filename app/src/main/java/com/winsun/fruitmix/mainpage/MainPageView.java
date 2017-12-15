package com.winsun.fruitmix.mainpage;

import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;

/**
 * Created by Administrator on 2017/6/26.
 */

public interface MainPageView extends BaseView{

    void gotoEquipmentManageActivity();

    void gotoUserManageActivity();

    void gotoSettingActivity();

    void gotoAccountManageActivity();

    void gotoFileDownloadActivity();

    void gotoDownloadManageActivity();

    void gotoConfirmInviteUserActivity();

    void loggedInUserItemOnClick(LoggedInUser loggedInUser);

    void quitApp();

    int getCurrentPage();

    void setCurrentPage(int page);

    void showMediaHideFile();

    void closeDrawer();

    String getString(int resID);

}
