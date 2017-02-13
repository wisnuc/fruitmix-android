package com.winsun.fruitmix.refactor.presenter;

import android.content.Intent;
import android.view.View;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.business.callback.MediaShareOperationCallback;
import com.winsun.fruitmix.refactor.contract.AlbumFragmentContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2017/2/13.
 */

public class AlbumFragmentPresenterImpl implements AlbumFragmentContract.AlbumFragmentPresenter {

    private AlbumFragmentContract.AlbumFragmentView mView;

    private DataRepository mRepository;

    private List<MediaShare> mAlbumList;

    private boolean isDataRefreshed = false;

    public AlbumFragmentPresenterImpl(DataRepository repository) {
        mRepository = repository;

        mAlbumList = new ArrayList<>();
    }

    @Override
    public void createAlbumBtnOnClick() {


    }

    private boolean preTreatOperateMediaShare(MediaShare mediaShare) {
        if (mView.isNetworkAlive()) {

            if (mRepository.checkPermissionToOperateMediaShare(mediaShare)) {
                return true;
            } else {
                mView.showNoOperatePermission();
                return false;
            }

        } else {
            mView.showNoNetwork();
            return false;
        }
    }

    @Override
    public void modifyMediaShare(MediaShare mediaShare) {

        if (!preTreatOperateMediaShare(mediaShare)) return;

        mView.showDialog();

        mRepository.modifyMediaShare(mediaShare, new MediaShareOperationCallback.OperateMediaShareCallback() {
            @Override
            public void onOperateSucceed(OperationResult operationResult, MediaShare mediaShare) {

                mView.dismissDialog();

                if (mView != null)
                    mView.showOperationResultToast(operationResult);

                mRepository.loadMediaShares(null);
            }

            @Override
            public void onOperateFail(OperationResult operationResult) {

                mView.dismissDialog();

                if (mView != null)
                    mView.showOperationResultToast(operationResult);
            }
        });
    }

    @Override
    public void deleteMediaShare(MediaShare mediaShare) {

        if (!preTreatOperateMediaShare(mediaShare)) return;

        mView.showDialog();

        mRepository.deleteMediaShare(mediaShare, new MediaShareOperationCallback.OperateMediaShareCallback() {
            @Override
            public void onOperateSucceed(OperationResult operationResult, MediaShare mediaShare) {

                mView.dismissDialog();

                if (mView != null)
                    mView.showOperationResultToast(operationResult);

                mRepository.loadMediaShares(null);
            }

            @Override
            public void onOperateFail(OperationResult operationResult) {

                mView.dismissDialog();

                if (mView != null)
                    mView.showOperationResultToast(operationResult);
            }
        });
    }

    @Override
    public void refreshData() {

        isDataRefreshed = true;
        loadMediaShares();
    }

    @Override
    public void onResume() {


    }

    private void sortMediaShareList(List<MediaShare> mediaShareList) {
        Collections.sort(mediaShareList, new Comparator<MediaShare>() {
            @Override
            public int compare(MediaShare lhs, MediaShare rhs) {

                long time1 = Long.parseLong(lhs.getTime());
                long time2 = Long.parseLong(rhs.getTime());
                if (time1 < time2)
                    return 1;
                else if (time1 > time2)
                    return -1;
                else return 0;
            }
        });
    }

    private void showTips() {
        if (mRepository.getShowAlbumTipsValue()) {
            mRepository.saveShowAlbumTipsValue(false);
            mView.setAlbumBalloonVisibility(View.VISIBLE);
            mView.setAlbumBalloonOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.setAlbumBalloonVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onCreate() {
        loadMediaShares();
    }

    @Override
    public Media loadMedia(String mediaKey) {
        return mRepository.loadMediaFromMemory(mediaKey);
    }

    @Override
    public User loadUser(String userUUID) {
        return mRepository.loadUserFromMemory(userUUID);
    }

    private void loadMediaShares() {
        mRepository.loadMediaShares(new MediaShareOperationCallback.LoadMediaSharesCallback() {
            @Override
            public void onLoadSucceed(OperationResult operationResult, List<MediaShare> mediaShares) {

                mAlbumList.clear();

                for (MediaShare mediaShare : mediaShares) {
                    if (mediaShare.isAlbum() && !mediaShare.isArchived()) {
                        mAlbumList.add(mediaShare);
                    }
                }

                sortMediaShareList(mAlbumList);

                if (mView != null) {
                    mView.dismissLoadingUI();
                    mView.setAddAlbumBtnVisibility(View.VISIBLE);
                    showTips();
                    if (mAlbumList.size() == 0) {
                        mView.showNoContentUI();
                        mView.dismissContentUI();
                    } else {
                        mView.dismissNoContentUI();
                        mView.showContentUI();
                        mView.showAlbums(mAlbumList);
                    }
                }

            }

            @Override
            public void onLoadFail(OperationResult operationResult) {

            }
        });
    }

    @Override
    public void attachView(AlbumFragmentContract.AlbumFragmentView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }


    @Override
    public void handleBackEvent() {

    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
