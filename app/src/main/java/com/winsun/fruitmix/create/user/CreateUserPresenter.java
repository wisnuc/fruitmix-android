package com.winsun.fruitmix.create.user;

import android.content.Context;

import com.winsun.fruitmix.CreateUserActivity;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.user.OperateUserViewModel;

/**
 * Created by Administrator on 2017/6/22.
 */

public interface CreateUserPresenter {

    void onDestroy();

    void createUser(Context context,OperateUserViewModel createUserViewModel);


}
