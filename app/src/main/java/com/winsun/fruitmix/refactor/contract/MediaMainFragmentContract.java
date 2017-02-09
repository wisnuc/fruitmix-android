package com.winsun.fruitmix.refactor.contract;

import android.content.Intent;
import android.view.View;

import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface MediaMainFragmentContract {

    interface MediaMainFragmentView extends BaseView {

        void resetBottomNavigationItemCheckState();

        void setBottomNavigationItemChecked(int position);

        void setViewPagerCurrentItem(int position);

        void setTitleText(String titleText);

        void setToolbarNavigationIcon(int icon);

        void setChooseModeBtnVisibility(int visibility);

        void showBottomNavAnim();

        void dismissBottomNavAnim();

        MediaMainFragmentPresenter getPresenter();

    }

    interface MediaMainFragmentPresenter extends BasePresenter<MediaMainFragmentView>{

        void onNavigationItemSelected(int itemID);

        void onPageSelected(int position);

        void onToolbarClick();

        boolean handleBackPressedOrNot();

        void onActivityReenter(int resultCode, Intent data);

        boolean isResumed();

        void onMapSharedElements(List<String> names, Map<String, View> sharedElements);
    }

}
