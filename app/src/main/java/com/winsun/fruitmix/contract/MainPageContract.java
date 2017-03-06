package com.winsun.fruitmix.contract;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.winsun.fruitmix.common.BasePresenter;
import com.winsun.fruitmix.common.BaseView;
import com.winsun.fruitmix.model.User;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface MainPageContract {

    interface MainPageView extends BaseView {

        void refreshUserInNavigationView(User user);

        void switchDrawerOpenState();

        void lockDrawer();

        void unlockDrawer();

        void closeDrawer();

        void gotoUserManageActivity();

        void gotoEquipmentActivity();

        void setFileItemMenuTitle(int resID);

        void setFileItemMenuIcon(int resID);

        void showMediaAndHideFileFragment();

        void hideMediaAndShowFileFragment();

        void finishActivity();

        void showFinishAppToast();

        boolean isDrawerOpen();

    }

    interface MainPagePresenter extends BasePresenter<MainPageView> {

        void loadCurrentUser();

        void setMediaMainFragmentPresenter(MediaMainFragmentContract.MediaMainFragmentPresenter presenter);

        void setFileMainFragmentPresenter(FileMainFragmentContract.FileMainFragmentPresenter presenter);

        void onNavigationItemSelected(int itemId);

        void switchDrawerOpenState();

        void lockDrawer();

        void unlockDrawer();

        void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

        void onActivityReenter(int resultCode, Intent data);

        boolean isDrawerOpen();

        void closeDrawer();

        void onMapSharedElements(List<String> names, Map<String, View> sharedElements);
    }

}
