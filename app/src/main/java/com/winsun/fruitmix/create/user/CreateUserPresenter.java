package com.winsun.fruitmix.create.user;

import android.content.Context;

import com.winsun.fruitmix.CreateUserActivity;
import com.winsun.fruitmix.eventbus.OperationEvent;

/**
 * Created by Administrator on 2017/6/22.
 */

public interface CreateUserPresenter {

    void onDestroy();

    void createUser(Context context,CreateUserActivity.CreateUserViewModel createUserViewModel);

    void handleOperationEvent(OperationEvent operationEvent);
}
