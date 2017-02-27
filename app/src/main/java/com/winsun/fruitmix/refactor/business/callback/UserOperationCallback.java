package com.winsun.fruitmix.refactor.business.callback;

import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/2/7.
 */

public interface UserOperationCallback {

    interface LoadUsersCallback {

        void onLoadSucceed(OperationResult operationResult, List<User> users);

        void onLoadFail(OperationResult operationResult);

    }

    interface LoadCurrentUserCallback {

        void onLoadSucceed(OperationResult operationResult, User user);

        void onLoadFail(OperationResult operationResult);

    }

    interface OperateUserCallback {

        void onOperateSucceed(OperationResult operationResult, User user);

        void onOperateFail(OperationResult operationResult);

    }

}
