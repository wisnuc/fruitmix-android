package com.winsun.fruitmix.refactor.contract;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.fileModule.model.BottomMenuItem;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;

/**
 * Created by Administrator on 2017/2/21.
 */

public interface FileDownloadFragmentContract {

    interface FileDownloadFragmentView {

        String getString(int resID);

        void showDownloadingContent(List<FileDownloadItem> fileDownloadItems);

        void showDownloadedContent(List<FileDownloadItem> fileDownloadItems,boolean selectMode);

        void showNoEnoughSpaceToast();

        Dialog getBottomSheetDialog(List<BottomMenuItem> bottomMenuItems);

        View getView();

        void onDestroyView();
    }

    interface FileDownloadFragmentPresenter extends BasePresenter<FileDownloadFragmentView>{

        void handleTitle();

        boolean handleBackPressedOrNot();

        void fileMainMenuOnClick();

        void loadDownloadedFile();

        void registerFileDownloadStateChangedListener();

        void unregisterFileDownloadStateChangedListener();

        boolean checkIsInSelectedFiles(String fileUUID);

        void toggleFileInSelectedFile(String fileUUID);

        void openFileIfDownloaded(Context context,FileDownloadItem fileDownloadItem);
    }

}
