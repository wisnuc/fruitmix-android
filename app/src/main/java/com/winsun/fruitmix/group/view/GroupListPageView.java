package com.winsun.fruitmix.group.view;

import com.winsun.fruitmix.interfaces.BaseView;

/**
 * Created by Administrator on 2017/7/24.
 */

public interface GroupListPageView {

    String getString(int resID,Object... formatArgs);

    void gotoGroupContentActivity(String groupUUID,String groupName);

}
