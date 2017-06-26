package com.winsun.fruitmix.account.manage;

import android.app.Activity;
import android.content.Intent;

import com.winsun.fruitmix.EquipmentSearchActivity;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/6/22.
 */

public class AccountManagePresenterImpl implements AccountManagePresenter {

    private AccountManageView view;

    public AccountManagePresenterImpl(AccountManageView view) {
        this.view = view;
    }

    @Override
    public void onDestroy() {
        view = null;
    }

    @Override
    public void addAccount() {
        view.gotoEquipmentSearchActivity();
    }
}
