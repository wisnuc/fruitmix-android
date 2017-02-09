package com.winsun.fruitmix.refactor.presenter;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.business.callback.OperationCallback;
import com.winsun.fruitmix.refactor.business.callback.UserOperationCallback;
import com.winsun.fruitmix.refactor.contract.FileMainFragmentContract;
import com.winsun.fruitmix.refactor.contract.MainPageContract;
import com.winsun.fruitmix.refactor.contract.MediaMainFragmentContract;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/9.
 */

public class MainPagePresenterImpl implements MainPageContract.MainPagePresenter {

    public static final String TAG = MainPagePresenterImpl.class.getSimpleName();

    private MainPageContract.MainPageView mView;
    private DataRepository mRepository;

    private MediaMainFragmentContract.MediaMainFragmentPresenter mMediaMainFragmentPresenter;
    private FileMainFragmentContract.FileMainFragmentPresenter mFileMainFragmentPresenter;

    private static final int PAGE_FILE = 1;
    private static final int PAGE_MEDIA = 0;

    private int currentPage = PAGE_MEDIA;

    private static final int TIME_INTERNAL = 2 * 1000;
    private long backPressedTimeMillis = 0;

    public MainPagePresenterImpl(DataRepository repository, MediaMainFragmentContract.MediaMainFragmentPresenter mediaMainFragmentPresenter, FileMainFragmentContract.FileMainFragmentPresenter fileMainFragmentPresenter) {
        mRepository = repository;

        mMediaMainFragmentPresenter = mediaMainFragmentPresenter;
        mFileMainFragmentPresenter = fileMainFragmentPresenter;
    }

    private void onUserManageNavigationItemSelected() {
        mView.gotoUserManageActivity();
    }

    private void onLogoutNavigationItemSelected() {

        mView.showDialog();

        mRepository.logout(new OperationCallback() {
            @Override
            public void onOperationSucceed(OperationResult result) {

                if (mView == null)
                    return;

                mView.dismissDialog();

                mView.gotoEquipmentActivity();
                mView.finishActivity();
            }
        });

    }

    private void onFileNavigationItemSelected() {

        if (currentPage == PAGE_MEDIA) {

            currentPage = PAGE_FILE;
            mView.setFileItemMenuTitle(R.string.my_photo);
            mView.setFileItemMenuIcon(R.drawable.ic_photo);
            mView.hideMediaAndShowFileFragment();

        } else {

            currentPage = PAGE_MEDIA;
            mView.setFileItemMenuTitle(R.string.my_file);
            mView.setFileItemMenuIcon(R.drawable.ic_folder);
            mView.showMediaAndHideFileFragment();

        }

    }

    @Override
    public void onNavigationItemSelected(int itemId) {
        switch (itemId) {
            case R.id.user_manage:
                onUserManageNavigationItemSelected();
                break;
            case R.id.logout:
                onLogoutNavigationItemSelected();
                break;
            case R.id.file:
                onFileNavigationItemSelected();
                break;
        }
        mView.closeDrawer();
    }

    @Override
    public void switchDrawerOpenState() {
        mView.switchDrawerOpenState();
    }

    @Override
    public void lockDrawer() {
        mView.lockDrawer();
    }

    @Override
    public void unlockDrawer() {
        mView.unlockDrawer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult: " + (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ? "true" : "false"));

        if (currentPage == PAGE_FILE)
            mFileMainFragmentPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

        if (currentPage == PAGE_MEDIA && mMediaMainFragmentPresenter.isResumed())
            mMediaMainFragmentPresenter.onActivityReenter(resultCode, data);
    }

    @Override
    public boolean isDrawerOpen() {
        return mView.isDrawerOpen();
    }

    @Override
    public void closeDrawer() {
        mView.closeDrawer();
    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        if (currentPage == PAGE_MEDIA)
            mMediaMainFragmentPresenter.onMapSharedElements(names, sharedElements);
    }

    @Override
    public void attachView(MainPageContract.MainPageView view) {
        mView = view;
    }

    @Override
    public void detachView() {

        mView.dismissDialog();

        mRepository.shutdownFixedThreadPoolNow();

        mView = null;
    }

    @Override
    public void startMission() {
        mView.setVersionNameText(mView.getVersionName());

        mRepository.loadCurrentUser(new UserOperationCallback.LoadCurrentUserCallback() {
            @Override
            public void onLoadSucceed(OperationResult operationResult, User user) {
                mView.refreshUserInNavigationView(user);
            }

            @Override
            public void onLoadFail(OperationResult operationResult) {

            }
        });

    }

    @Override
    public void handleBackEvent() {
        if (currentPage == PAGE_FILE) {
            if (mFileMainFragmentPresenter.handleBackPressedOrNot()) {
                mFileMainFragmentPresenter.handleBackEvent();
            } else {
                finishApp();
            }
        } else if (currentPage == PAGE_MEDIA) {

            if (mMediaMainFragmentPresenter.handleBackPressedOrNot()) {
                mMediaMainFragmentPresenter.handleBackEvent();
            } else {
                finishApp();
            }

        } else {
            finishApp();
        }
    }

    private void finishApp() {

        if (System.currentTimeMillis() - backPressedTimeMillis < TIME_INTERNAL) {
            mView.finishActivity();
        } else {
            mView.showFinishAppToast();
        }

        backPressedTimeMillis = System.currentTimeMillis();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        if (currentPage == PAGE_MEDIA)
            mMediaMainFragmentPresenter.handleOnActivityResult(requestCode, resultCode, data);
    }
}
