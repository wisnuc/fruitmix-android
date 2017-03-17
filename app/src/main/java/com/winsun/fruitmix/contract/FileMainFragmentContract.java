package com.winsun.fruitmix.contract;

import android.support.annotation.NonNull;
import android.view.View;

import com.winsun.fruitmix.common.BasePresenter;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface FileMainFragmentContract {

    interface FileMainFragmentView {

        void resetBottomNavigationItemCheckState();

        void setBottomNavigationItemChecked(int position);

        void setViewPagerCurrentItem(int position);

        void setTitleText(String titleText);

        void setNavigationIcon(int resID);

        void setFileMainMenuVisibility(int visibility);

        int getCurrentPage();

        void setNavigationOnClickListener(View.OnClickListener listener);

    }

    interface FileMainFragmentPresenter extends BasePresenter<FileMainFragmentView> {

        void onResume();

        void setFileFragmentPresenter(FileFragmentContract.FileFragmentPresenter fileFragmentPresenter);

        void setFileShareFragmentPresenter(FileShareFragmentContract.FileShareFragmentPresenter fileShareFragmentPresenter);

        void setFileDownloadFragmentPresenter(FileDownloadFragmentContract.FileDownloadFragmentPresenter fileDownloadFragmentPresenter);

        void fileMainMenuOnClick();

        void setBottomNavigationItemChecked(int position);

        void setViewPagerCurrentItem(int position);

        void onNavigationItemSelected(int itemID);

        void onPageSelected(int position);

        boolean handleBackPressedOrNot();

        void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

        void initView();

        void setTitleText(String titleText);

        void setNavigationIcon(int resID);

        void setDefaultNavigationOnClickListener();

        void setNavigationOnClickListener(View.OnClickListener listener);

        void switchDrawerOpenState();

        boolean isHidden();
    }

}
