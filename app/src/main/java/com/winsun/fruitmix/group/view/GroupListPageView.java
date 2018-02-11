package com.winsun.fruitmix.group.view;

import android.content.Context;

import com.winsun.fruitmix.interfaces.BaseView;

/**
 * Created by Administrator on 2017/7/24.
 */

public interface GroupListPageView {

    void showToast(String message);

    String getString(int resID);

    String getString(int resID, Object... formatArgs);

    void gotoGroupContentActivity(String groupUUID);

    Context getContext();

    void finishSwipeRefreshAnimation();

}
