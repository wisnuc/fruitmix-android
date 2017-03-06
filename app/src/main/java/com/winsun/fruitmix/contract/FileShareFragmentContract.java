package com.winsun.fruitmix.contract;

import android.view.View;

import com.winsun.fruitmix.common.BasePresenter;
import com.winsun.fruitmix.common.BaseView;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;

import java.util.List;

/**
 * Created by Administrator on 2017/2/21.
 */

public interface FileShareFragmentContract {

    interface FileShareFragmentView extends BaseView {

        String getString(int resID);

        boolean isShowingLoadingUI();

        void showContent(List<AbstractRemoteFile> files);

        View getView();

        void onDestroyView();

        void onResume();

    }

    interface FileShareFragmentPresenter extends BasePresenter<FileShareFragmentView> {

        void handleTitle();

        boolean handleBackPressedOrNot();

        void onResume();

        void onDestroyView();

        String getFileShareOwnerName(AbstractRemoteFile file);

        void openFolder(AbstractRemoteFile abstractRemoteFile);

    }

}
