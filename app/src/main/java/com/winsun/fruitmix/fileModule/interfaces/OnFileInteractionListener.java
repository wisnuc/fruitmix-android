package com.winsun.fruitmix.fileModule.interfaces;

import android.view.View;

/**
 * Created by Administrator on 2016/10/27.
 */

public interface OnFileInteractionListener {
    void changeFilePageToFileFragment();

    void changeFilePageToFileShareFragment();

    void changeFilePageToFileDownloadFragment();

    void setToolbarTitle(String title);

    void setNavigationIcon(int id);

    void setNavigationOnClickListener(View.OnClickListener onClickListener);

    void setDefaultNavigationOnClickListener();
}
