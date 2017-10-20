package com.winsun.fruitmix.user;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

/**
 * Created by Administrator on 2017/10/19.
 */

public class OperateUserViewModel {

    private String userName;
    private String userPassword;
    private String userConfirmPassword;

    public OperateUserViewModel() {

        userName = "";
        userPassword = "";
        userConfirmPassword = "";

    }

    public final ObservableBoolean userNameErrorEnable = new ObservableBoolean(false);
    public final ObservableBoolean userPasswordErrorEnable = new ObservableBoolean(false);
    public final ObservableBoolean userConfirmPasswordErrorEnable = new ObservableBoolean(false);

    public final ObservableField<String> userNameError = new ObservableField<>();
    public final ObservableField<String> userPasswordError = new ObservableField<>();
    public final ObservableField<String> userConfirmPasswordError = new ObservableField<>();


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserConfirmPassword() {
        return userConfirmPassword;
    }

    public void setUserConfirmPassword(String userConfirmPassword) {
        this.userConfirmPassword = userConfirmPassword;
    }

}
