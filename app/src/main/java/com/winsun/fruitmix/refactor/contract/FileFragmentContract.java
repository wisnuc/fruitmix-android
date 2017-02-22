package com.winsun.fruitmix.refactor.contract;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.fileModule.model.BottomMenuItem;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;

/**
 * Created by Administrator on 2017/2/21.
 */

public interface FileFragmentContract {

    interface FileFragmentView extends BaseView{

        String getString(int resID);

        boolean isShowingLoadingUI();

        boolean isHidden();

        void showContents(List<AbstractRemoteFile> files,boolean selectMode);

        Dialog getBottomSheetDialog(List<BottomMenuItem> bottomMenuItems);

        void showOpenFileFailToast();

        void showNoWriteExternalStoragePermission();

        void checkWriteExternalStoragePermission();

        View getView();

        void onDestroyView();
    }

    interface FileFragmentPresenter extends BasePresenter<FileFragmentView>{

        void onResume();

        void onDestroyView();

        void handleTitle();

        void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

        boolean handleBackPressedOrNot();

        void openAbstractRemoteFolder(AbstractRemoteFile file);

        void addSelectedFiles(List<AbstractRemoteFile> files);

        void refreshSelectMode(boolean selectMode);

        boolean checkIsInSelectedFiles(AbstractRemoteFile file);

        void toggleFileInSelectedFile(AbstractRemoteFile file);

        void itemMenuOnClick(Context context,AbstractRemoteFile file);

        void fileItemOnClick(Context context,AbstractRemoteFile abstractRemoteFile);

        void fileMainMenuOnClick();

        void downloadSelectedFiles();

    }


}
