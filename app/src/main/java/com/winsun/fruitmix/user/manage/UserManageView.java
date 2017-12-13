package com.winsun.fruitmix.user.manage;

import android.content.Context;

import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/6/28.
 */

public interface UserManageView {

    void gotoCreateUserActivity();

    void gotoModifyUserStateActivity(User user);

    String getString(int resID);

    Context getContext();

}
