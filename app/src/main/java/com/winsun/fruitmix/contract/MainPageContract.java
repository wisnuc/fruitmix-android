package com.winsun.fruitmix.contract;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.winsun.fruitmix.common.BasePresenter;
import com.winsun.fruitmix.common.BaseView;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.navigationItem.NavigationItemType;

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

        void gotoSettingActivity();

        void gotoAccountManageActivity();

        void showMediaAndHideFileFragment();

        void hideMediaAndShowFileFragment();

        void finishActivity();

        void showFinishAppToast();

        boolean isDrawerOpen();

        String getString(int resID);

        void setNavigationItemTypes(List<NavigationItemType> navigationItemTypes);

        void setLoggedInUser0AvatarVisibility(int visibility);

        void setLoggedInUser1AvatarVisibility(int visibility);

        void setNavigationHeaderArrowVisibility(int visibility);

        int getLoggedInUser0AvatarVisibility();

        int getLoggedInUser1AvatarVisibility();

        void setLoggedInUser0AvatarText(String text);

        void setLoggedInUser1AvatarText(String text);

        void setLoggedInUser0AvatarBackgroundResource(int resID);

        void setLoggedInUser1AvatarBackgroundResource(int resID);

        void setNavigationHeaderArrowImageResource(int resID);

        void setLoggedInUser0AvatarOnClickListener(View.OnClickListener onClickListener);

        void setLoggedInUser1AvatarOnClickListener(View.OnClickListener onClickListener);

        void setNavigationHeaderArrowOnClickListener(View.OnClickListener onClickListener);

        void showOperateFailed();

        void showAutoUploadAlreadyClose();

        void setUploadMediaPercentText(String text);
    }

    interface MainPagePresenter extends BasePresenter<MainPageView> {

        void initNavigationItemMenu();

        void loadCurrentUser();

        void setMediaMainFragmentPresenter(MediaMainFragmentContract.MediaMainFragmentPresenter presenter);

        void setFileMainFragmentPresenter(FileMainFragmentContract.FileMainFragmentPresenter presenter);

        void switchDrawerOpenState();

        void lockDrawer();

        void unlockDrawer();

        void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

        void onActivityReenter(int resultCode, Intent data);

        void closeDrawer();

        void onMapSharedElements(List<String> names, Map<String, View> sharedElements);

        void calcAlreadyUploadMediaCount();
    }

}
