package com.winsun.fruitmix.contract;

import com.winsun.fruitmix.common.BasePresenter;
import com.winsun.fruitmix.common.BaseView;
import com.winsun.fruitmix.model.User;

import java.util.List;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface UserManageContract {

    interface UserManageView extends BaseView {

        void showUsers(List<User> users);

        void finishActivity();

    }

    interface UserManagePresenter extends BasePresenter<UserManageView> {

        void addUserBtnClick();

        void initView();
    }

}
