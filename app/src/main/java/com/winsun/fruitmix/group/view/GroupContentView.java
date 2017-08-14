package com.winsun.fruitmix.group.view;

import android.content.Context;

import com.winsun.fruitmix.interfaces.BaseView;

/**
 * Created by Administrator on 2017/7/27.
 */

public interface GroupContentView extends BaseView {

    void smoothToChatListPosition(int position);

    Context getContext();

    void showCreatePing();

    void showPinContent(String groupUUID,String pinUUID);

}
