package com.winsun.fruitmix.contract;

import com.winsun.fruitmix.common.BasePresenter;
import com.winsun.fruitmix.common.BaseView;
import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2017/1/24.
 */

public interface CreateUserContract {

    interface CreateUserView extends BaseView {

        void showCorrectUserNameFormat();

        void showCorrectPasswordFormat();

        void showEmptyUserName();

        void showNotUniqueUserName();

        void showNotSamePassword();

        void handleCreateUserFail(OperationResult result);

        void handleCreateUserSucceed();

        void finishActivity();

        void hideSoftInput();

    }

    interface CreateUserPresenter extends BasePresenter<CreateUserView> {

        void loadUserNames();

        void createUser(String userName, String userPassword, String userConfirmPassword);

    }

}
