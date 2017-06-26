package com.winsun.fruitmix.mainpage;

import com.winsun.fruitmix.model.LoggedInUser;

/**
 * Created by Administrator on 2017/6/26.
 */

public interface MainPageView {

    void gotoUserManageActivity();

    void gotoSettingActivity();

    void gotoAccountManageActivity();

    void loggedInUserItemOnClick(LoggedInUser loggedInUser);

    void logout();

    int getCurrentPage();

    void setCurrentPage(int page);

    void showMediaHideFile();

    void showFileHideMedia();

    void closeDrawer();
}
