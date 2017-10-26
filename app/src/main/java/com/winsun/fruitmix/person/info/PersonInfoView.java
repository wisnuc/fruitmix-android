package com.winsun.fruitmix.person.info;

import android.content.Context;

import com.winsun.fruitmix.interfaces.BaseView;

/**
 * Created by Administrator on 2017/10/19.
 */

public interface PersonInfoView extends BaseView {

    String getString(int resID);

    String getString(int resID,Object... formatArgs);

    Context getContext();

    void handleBindSucceed();

}
