package com.winsun.fruitmix.user.manage;

import android.content.Intent;
import android.widget.BaseAdapter;

/**
 * Created by Administrator on 2017/6/21.
 */

public interface UserMangePresenter {

    void refreshView();

    void refreshUserFromCache();

    void onDestroy();

    void addUser();

    BaseAdapter getAdapter();
}
