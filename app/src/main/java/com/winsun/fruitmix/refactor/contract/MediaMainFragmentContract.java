package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

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

    }

    interface MediaMainFragmentPresenter extends BasePresenter<MediaMainFragmentView>{

        void onShareNavigationItemSelected();

        void onMediaNavigationItemSelected();

        void onAlbumNavigationItemSelected();

        void onPageMediaSelect();

        void onPageMediaShareSelect();

        void onPageAlbumSelect();

        void onToolbarClick();

    }

}
