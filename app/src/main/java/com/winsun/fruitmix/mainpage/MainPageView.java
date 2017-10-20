package com.winsun.fruitmix.mainpage;

import com.winsun.fruitmix.logged.in.user.LoggedInUser;

/**
 * Created by Administrator on 2017/6/26.
 */

public interface MainPageView {

    void gotoEquipmentManageActivity();

    void gotoUserManageActivity();

    void gotoSettingActivity();

    void gotoAccountManageActivity();

    void gotoFileDownloadActivity();

    void gotoConfirmInviteUserActivity();

    void loggedInUserItemOnClick(LoggedInUser loggedInUser);

    void logout();

    int getCurrentPage();

    void setCurrentPage(int page);

    void showMediaHideFile();

    void closeDrawer();

}
