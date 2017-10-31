package com.winsun.fruitmix.invitation;

import android.content.Context;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.interfaces.BaseView;

/**
 * Created by Administrator on 2017/10/13.
 */

public interface ConfirmInviteUserView extends BaseView {

    String getString(int resID);

    Context getContext();

    void setInviteUserFabVisibility(int visibility);

}
