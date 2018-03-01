package com.winsun.fruitmix.component.fab.menu;

import android.content.Context;

/**
 * Created by Administrator on 2018/2/28.
 */
public interface FabMenuItemOnClickListener {

    int ITEM_MEDIA = 1;
    int ITEM_FILE = 2;
    int ITEM_OTHER = 3;

    void systemShareBtnOnClick(Context context, int currentItem);

    void downloadFileBtnOnClick(Context context);

}
