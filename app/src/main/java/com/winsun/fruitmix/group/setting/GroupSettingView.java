package com.winsun.fruitmix.group.setting;

import android.content.Context;

import com.winsun.fruitmix.interfaces.BaseView;

/**
 * Created by Administrator on 2018/2/1.
 */

public interface GroupSettingView extends BaseView{

    Context getContext();

    void addUserBtnOnClick();

    void deleteUserBtnOnClick();
}
