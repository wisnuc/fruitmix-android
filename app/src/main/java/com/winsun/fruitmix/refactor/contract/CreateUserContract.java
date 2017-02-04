package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

/**
 * Created by Administrator on 2017/1/24.
 */

public interface CreateUserContract {

    interface CreateUserView extends BaseView {

        void showEmptyUserName();

        void showNotUniqueUserName();

        void showEmptyUserPassword();

        void showEmptyConfirmUserPassword();

        void showNotSamePassword();

        void handleCreateUserFail();

        void handleCreateUserSucceed();

    }

    interface CreateUserPresenter extends BasePresenter<CreateUserView> {

        void createUser(String userName, String userPassword, String userConfirmPassword);

    }

}
