package com.winsun.fruitmix.contract;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.winsun.fruitmix.common.BasePresenter;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface MediaMainFragmentContract {

    interface MediaMainFragmentView {

        void resetBottomNavigationItemCheckState();

        void setBottomNavigationItemChecked(int position);

        void setViewPageCurrentItem(int position);

        void setTitleText(int resID);

        void setTitleText(String titleText);

        void setToolbarNavigationIcon(int resID);

        void setSelectModeBtnVisibility(int visibility);

        void showBottomNavAnim();

        void dismissBottomNavAnim();

        void setToolbarNavigationOnClickListener(View.OnClickListener listener);

        int getCurrentViewPageItem();

        View getView();

        void onResume();

        void onDestroyView();
    }

    interface MediaMainFragmentPresenter extends BasePresenter<MediaMainFragmentView> {

        void setmMediaFragmentPresenter(MediaFragmentContract.MediaFragmentPresenter mMediaFragmentPresenter);

        void setmMediaShareFragmentPresenter(MediaShareFragmentContract.MediaShareFragmentPresenter mMediaShareFragmentPresenter);

        void setmAlbumFragmentPresenter(AlbumFragmentContract.AlbumFragmentPresenter mAlbumFragmentPresenter);

        void onCreate(Context context);

        void onCreateView();

        void onResume();

        void setViewPageCurrentItem(int position);

        void refreshShareFragment();

        void selectModeBtnClick();

        void switchDrawerOpenState();

        void lockDrawer();

        void unlockDrawer();

        void onNavigationItemSelected(int itemID);

        void onPageSelected(int position);

        void setTitleText(int resID);

        void setTitleText(String titleText);

        void setToolbarNavigationIcon(int resID);

        void setSelectModeBtnVisibility(int visibility);

        void showBottomNavAnim();

        void dismissBottomNavAnim();

        void setToolbarNavigationOnClickListener(View.OnClickListener listener);

        boolean handleBackPressedOrNot();

        void onActivityReenter(int resultCode, Intent data);

        boolean isResumed();

        void onMapSharedElements(List<String> names, Map<String, View> sharedElements);

        void setHidden(boolean hidden);
    }

}
