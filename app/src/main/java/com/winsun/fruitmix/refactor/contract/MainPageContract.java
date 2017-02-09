package com.winsun.fruitmix.refactor.contract;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface MainPageContract {

    interface MainPageView extends BaseView {

        void refreshUserInNavigationView(User user);

        void setVersionNameText(String versionNameText);

        void switchDrawerOpenState();

        void lockDrawer();

        void unlockDrawer();

        void closeDrawer();

        void gotoUserManageActivity();

        void gotoEquipmentActivity();

        String getVersionName();

        void setFileItemMenuTitle(int resID);

        void setFileItemMenuIcon(int resID);

        void showMediaAndHideFileFragment();

        void hideMediaAndShowFileFragment();

        void finishActivity();

        void showFinishAppToast();

        boolean isDrawerOpen();

    }

    interface MainPagePresenter extends BasePresenter<MainPageView> {

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
