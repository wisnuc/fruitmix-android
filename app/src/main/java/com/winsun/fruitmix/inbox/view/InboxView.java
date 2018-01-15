package com.winsun.fruitmix.inbox.view;

import android.content.Context;

import com.winsun.fruitmix.interfaces.BaseView;

/**
 * Created by Administrator on 2018/1/11.
 */

public interface InboxView extends BaseView{

    Context getContext();

    String getQuantityString(int resID,int quantity);

    String getQuantityString(int resID,int quantity,Object...formatArgs);
}
