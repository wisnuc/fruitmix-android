package com.winsun.fruitmix.fileModule.interfaces;

import com.winsun.fruitmix.fileModule.model.BottomMenuItem;

import java.util.List;

/**
 * Created by Administrator on 2016/10/27.
 */

public interface OnFileFragmentInteractionListener {
    void changeFilePageToFileFragment();

    void changeFilePageToFileShareFragment();

    void changeFilePageToFileDownloadFragment();

    void showBottomSheetDialog(List<BottomMenuItem> bottomMenuItems);

    void dismissBottomSheetDialog();
}
