package com.winsun.fruitmix.modify.user;


import com.winsun.fruitmix.user.OperateUserViewModel;

/**
 * Created by Administrator on 2017/10/19.
 */

public class ModifyUserPasswordViewModel extends OperateUserViewModel {

    private String userOriginalPassword;

    public ModifyUserPasswordViewModel() {
        super();
        userOriginalPassword = "";
    }

    public String getUserOriginalPassword() {
        return userOriginalPassword;
    }

    public void setUserOriginalPassword(String userOriginalPassword) {
        this.userOriginalPassword = userOriginalPassword;
    }
}
