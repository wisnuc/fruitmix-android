package com.winsun.fruitmix.login;

import android.databinding.ObservableField;

/**
 * Created by Administrator on 2017/8/14.
 */

public class LoginViewModel {

    public final ObservableField<String> userName = new ObservableField<>();
    public final ObservableField<String> userNameFirstLetter = new ObservableField<>();

    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
