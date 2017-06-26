package com.winsun.fruitmix.user.manage;

import android.app.Activity;
import android.content.Intent;

import com.winsun.fruitmix.CreateUserActivity;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/6/21.
 */

public class UserManagePresenterImpl implements UserMangePresenter {

    private Activity activity;

    public UserManagePresenterImpl(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onDestroy() {
        activity = null;
    }

    @Override
    public void addUser() {
        Intent intent = new Intent(activity, CreateUserActivity.class);
        activity.startActivityForResult(intent, Util.KEY_CREATE_USER_REQUEST_CODE);
    }
}
