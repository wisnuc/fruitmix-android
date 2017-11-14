package com.winsun.fruitmix.user.manage;

import android.content.Context;

/**
 * Created by Administrator on 2017/6/28.
 */

public interface UserManageView {

    void gotoCreateUserActivity();

    String getString(int resID);

    Context getContext();

}
