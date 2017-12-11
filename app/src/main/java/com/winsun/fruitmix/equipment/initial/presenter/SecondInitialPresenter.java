package com.winsun.fruitmix.equipment.initial.presenter;

import android.content.Context;

import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.BaseOperateUserPresenter;
import com.winsun.fruitmix.user.OperateUserViewModel;
import com.winsun.fruitmix.user.datasource.UserDataRepository;

/**
 * Created by Administrator on 2017/12/9.
 */

public class SecondInitialPresenter extends BaseOperateUserPresenter {

    public SecondInitialPresenter() {
    }

    public boolean checkUserNameAndPassword(Context context, OperateUserViewModel operateUserViewModel) {

        final String userName = operateUserViewModel.getUserName();

        if (!checkOperateUserName(context, userName, operateUserViewModel))
            return false;

        final String password = operateUserViewModel.getUserPassword();

        String confirmPassword = operateUserViewModel.getUserConfirmPassword();

        return checkOperateUserPassword(context, password, confirmPassword, operateUserViewModel);

    }

}
