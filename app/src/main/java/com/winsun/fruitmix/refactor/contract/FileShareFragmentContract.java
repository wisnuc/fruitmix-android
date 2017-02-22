package com.winsun.fruitmix.refactor.contract;

import android.view.View;

import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;

/**
 * Created by Administrator on 2017/2/21.
 */

public interface FileShareFragmentContract {

    interface FileShareFragmentView extends BaseView{

        String getString(int resID);

        boolean isShowingLoadingUI();

        void showContent(List<AbstractRemoteFile> files);

        View getView();

        void onDestroyView();

    }

    interface FileShareFragmentPresenter extends BasePresenter<FileShareFragmentView>{

        void handleTitle();

        boolean handleBackPressedOrNot();

        void onResume();

        void onDestroyView();

        String getFileShareOwnerName(AbstractRemoteFile file);

        void openFolder(AbstractRemoteFile abstractRemoteFile);

    }

}
