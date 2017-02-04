package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface UserManageContract {

    interface UserManageView extends BaseView{

        void showUsers(List<User> users);

    }

    interface UserManagePresenter extends BasePresenter<UserManageView>{

        void addUserBtnClick();

    }

}
