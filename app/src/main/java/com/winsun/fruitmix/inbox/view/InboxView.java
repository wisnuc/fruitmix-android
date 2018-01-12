package com.winsun.fruitmix.inbox.view;

import android.content.Context;

/**
 * Created by Administrator on 2018/1/11.
 */

public interface InboxView {

    Context getContext();

    String getString(int resID);

    String getString(int resID,Object... formatArgs);

}
