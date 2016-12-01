package com.winsun.fruitmix.fileModule.interfaces;

import android.app.Dialog;
import android.view.View;

import com.winsun.fruitmix.fileModule.model.BottomMenuItem;

import java.util.List;

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
