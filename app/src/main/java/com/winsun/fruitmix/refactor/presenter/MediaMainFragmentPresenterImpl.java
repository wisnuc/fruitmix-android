package com.winsun.fruitmix.refactor.presenter;

import android.content.Intent;
import android.view.View;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.refactor.contract.AlbumFragmentContract;
import com.winsun.fruitmix.refactor.contract.MainPageContract;
import com.winsun.fruitmix.refactor.contract.MediaFragmentContract;
import com.winsun.fruitmix.refactor.contract.MediaMainFragmentContract;
import com.winsun.fruitmix.refactor.contract.MediaShareFragmentContract;
import com.winsun.fruitmix.util.Util;

import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2017/2/9.
 */

public class MediaMainFragmentPresenterImpl implements MediaMainFragmentContract.MediaMainFragmentPresenter {

    private MediaMainFragmentContract.MediaMainFragmentView mView;

    private MainPageContract.MainPagePresenter mMainPagePresenter;

    private MediaFragmentContract.MediaFragmentPresenter mMediaFragmentPresenter;
    private MediaShareFragmentContract.MediaShareFragmentPresenter mMediaShareFragmentPresenter;
    private AlbumFragmentContract.AlbumFragmentPresenter mAlbumFragmentPresenter;

    static final int PAGE_SHARE = 0;
    private static final int PAGE_PHOTO = 1;
    private static final int PAGE_ALBUM = 2;

    public MediaMainFragmentPresenterImpl(MainPageContract.MainPagePresenter mainPagePresenter) {

        mMainPagePresenter = mainPagePresenter;
        mainPagePresenter.setMediaMainFragmentPresenter(this);
    }

    public void setmMediaFragmentPresenter(MediaFragmentContract.MediaFragmentPresenter mMediaFragmentPresenter) {
        this.mMediaFragmentPresenter = mMediaFragmentPresenter;
    }

    public void setmMediaShareFragmentPresenter(MediaShareFragmentContract.MediaShareFragmentPresenter mMediaShareFragmentPresenter) {
        this.mMediaShareFragmentPresenter = mMediaShareFragmentPresenter;
    }

    public void setmAlbumFragmentPresenter(AlbumFragmentContract.AlbumFragmentPresenter mAlbumFragmentPresenter) {
        this.mAlbumFragmentPresenter = mAlbumFragmentPresenter;
    }

    @Override
    public void setViewPageCurrentItem(int position) {
        mView.setViewPageCurrentItem(position);
    }

    @Override
    public void selectModeBtnClick() {

        if (mView.getCurrentViewPageItem() == PAGE_PHOTO)
            mMediaFragmentPresenter.enterChooseMode();

    }

    @Override
    public void switchDrawerOpenState() {
        mMainPagePresenter.switchDrawerOpenState();
    }

    @Override
    public void lockDrawer() {
        mMainPagePresenter.lockDrawer();
    }

    @Override
    public void unlockDrawer() {
        mMainPagePresenter.unlockDrawer();
    }

    public void onNavigationItemSelected(int itemID) {
        switch (itemID) {
            case R.id.share:
                mView.setViewPageCurrentItem(PAGE_SHARE);
                break;
            case R.id.photo:
                mView.setViewPageCurrentItem(PAGE_PHOTO);
                break;
            case R.id.album:
                mView.setViewPageCurrentItem(PAGE_ALBUM);
                break;
        }
    }

    @Override
    public void onPageSelected(int position) {

        switch (position) {
            case PAGE_ALBUM:
                mView.setSelectModeBtnVisibility(View.INVISIBLE);
                mView.showBottomNavAnim();
                break;
            case PAGE_PHOTO:
                mView.setSelectModeBtnVisibility(View.VISIBLE);
                mView.showBottomNavAnim();
                break;
            case PAGE_SHARE:
                mView.setSelectModeBtnVisibility(View.INVISIBLE);
                mView.showBottomNavAnim();
                break;
        }

        mView.resetBottomNavigationItemCheckState();
        mView.setBottomNavigationItemChecked(position);
    }

    @Override
    public void setTitleText(int resID) {
        mView.setTitleText(resID);
    }

    @Override
    public void setTitleText(String titleText) {
        mView.setTitleText(titleText);
    }

    @Override
    public void setToolbarNavigationIcon(int resID) {
        mView.setToolbarNavigationIcon(resID);
    }

    @Override
    public void setSelectModeBtnVisibility(int visibility) {
        mView.setSelectModeBtnVisibility(visibility);
    }

    @Override
    public void showBottomNavAnim() {
        mView.showBottomNavAnim();
    }

    @Override
    public void dismissBottomNavAnim() {
        mView.dismissBottomNavAnim();
    }

    @Override
    public void setToolbarNavigationOnClickListener(View.OnClickListener listener) {
        mView.setToolbarNavigationOnClickListener(listener);
    }

    @Override
    public boolean handleBackPressedOrNot() {
        return mView.getCurrentViewPageItem() == PAGE_PHOTO && mMediaFragmentPresenter.isSelectState();
    }


    @Override
    public void handleBackEvent() {
        if (mView.getCurrentViewPageItem() == PAGE_PHOTO) {
            mMediaFragmentPresenter.quitChooseMode();
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

        int currentViewPageItem = mView.getCurrentViewPageItem();

        if (currentViewPageItem == PAGE_PHOTO) {
            mMediaFragmentPresenter.onActivityReenter(resultCode, data);
        } else if (currentViewPageItem == PAGE_SHARE) {
            mMediaShareFragmentPresenter.onActivityReenter(resultCode, data);
        }

    }

    @Override
    public boolean isResumed() {
        return mView.isResumed();
    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        int currentViewPageItem = mView.getCurrentViewPageItem();

        if (currentViewPageItem == PAGE_PHOTO) {
            mMediaFragmentPresenter.onMapSharedElements(names, sharedElements);
        } else if (currentViewPageItem == PAGE_SHARE) {
            mMediaShareFragmentPresenter.onMapSharedElements(names, sharedElements);
        }
    }

    @Override
    public void attachView(MediaMainFragmentContract.MediaMainFragmentView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void onCreate() {
        if (mView.isHidden())
            return;

        mMediaFragmentPresenter.onCreate();
    }

    @Override
    public void onCreateView() {
        mView.setViewPageCurrentItem(PAGE_PHOTO);
    }

    @Override
    public void onResume() {
        if (mView.isHidden())
            return;

        mMediaFragmentPresenter.onResume();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

        if ((requestCode == Util.KEY_ALBUM_CONTENT_REQUEST_CODE || requestCode == Util.KEY_CREATE_ALBUM_REQUEST_CODE || requestCode == Util.KEY_CHOOSE_PHOTO_REQUEST_CODE) && resultCode == RESULT_OK) {

            mView.setTitleText(R.string.album);
            mView.setSelectModeBtnVisibility(View.GONE);

            mMediaShareFragmentPresenter.refreshData();
            mAlbumFragmentPresenter.refreshData();

            mView.setViewPageCurrentItem(PAGE_ALBUM);


        } else if (requestCode == Util.KEY_CREATE_SHARE_REQUEST_CODE && resultCode == RESULT_OK) {

            mView.setTitleText(R.string.share_text);
            mView.setSelectModeBtnVisibility(View.GONE);

            mMediaShareFragmentPresenter.refreshData();

            mView.setViewPageCurrentItem(PAGE_SHARE);
        }

    }
}
