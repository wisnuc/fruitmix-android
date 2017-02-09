package com.winsun.fruitmix.refactor.presenter;

import android.content.Intent;
import android.view.View;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.refactor.contract.MediaMainFragmentContract;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/9.
 */

public class MediaMainFragmentPresenterImpl implements MediaMainFragmentContract.MediaMainFragmentPresenter {

    private MediaMainFragmentContract.MediaMainFragmentView mView;

    private static final int PAGE_SHARE = 0;
    private static final int PAGE_PHOTO = 1;
    private static final int PAGE_ALBUM = 2;

    public void onNavigationItemSelected(int itemID) {
        switch (itemID) {
            case R.id.share:
                mView.setViewPagerCurrentItem(PAGE_SHARE);
                break;
            case R.id.photo:
                mView.setViewPagerCurrentItem(PAGE_PHOTO);
                break;
            case R.id.album:
                mView.setViewPagerCurrentItem(PAGE_ALBUM);
                break;
        }
    }

    @Override
    public void onPageSelected(int position) {

        switch (position) {
            case PAGE_ALBUM:
                mView.setChooseModeBtnVisibility(View.INVISIBLE);
                mView.showBottomNavAnim();
                break;
            case PAGE_PHOTO:
                mView.setChooseModeBtnVisibility(View.VISIBLE);
                mView.showBottomNavAnim();
                break;
            case PAGE_SHARE:
                mView.setChooseModeBtnVisibility(View.INVISIBLE);
                mView.showBottomNavAnim();
                break;
        }

        mView.resetBottomNavigationItemCheckState();
        mView.setBottomNavigationItemChecked(position);
    }

    @Override
    public void onToolbarClick() {

    }

    @Override
    public boolean handleBackPressedOrNot() {
        return false;
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

    }

    @Override
    public boolean isResumed() {
        return false;
    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

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
    public void startMission() {

    }

    @Override
    public void handleBackEvent() {

    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
