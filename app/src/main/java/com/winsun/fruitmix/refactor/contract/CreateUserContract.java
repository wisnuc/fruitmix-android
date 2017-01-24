package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.refactor.common.BaseView;

/**
 * Created by Administrator on 2017/1/24.
 */

public interface CreateUserContract {

    interface View extends BaseView {

        void showDialog();

        void dismissDialog();

        void showEmptyUserName();

        void showNotUniqueUserName();

        void showEmptyUserPassword();

        void showEmptyConfirmUserPassword();

        void showNotSamePassword();

    }


}
