package com.winsun.fruitmix.refactor.presenter;

import android.content.Intent;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.business.callback.MediaOperationCallback;
import com.winsun.fruitmix.refactor.contract.MoreMediaContract;

import java.util.List;

/**
 * Created by Administrator on 2017/2/16.
 */

public class MoreMediaPresenterImpl implements MoreMediaContract.MoreMediaPresenter {

    private MoreMediaContract.MoreMediaView mView;

    private DataRepository mRepository;

    private MediaShare mMediaShare;
    private List<Media> mMedias;

    public MoreMediaPresenterImpl(DataRepository repository,String mediaShareUUID) {
        mRepository = repository;

        mMediaShare = mRepository.loadMediaShareFromMemory(mediaShareUUID);
    }

    @Override
    public void attachView(MoreMediaContract.MoreMediaView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void handleBackEvent() {
        mView.finishActivity();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void loadMediaInMediaShare() {

        mView.showLoadingUI();

        mRepository.loadMediaInMediaShareFromMemory(mMediaShare, new MediaOperationCallback.LoadMediasCallback() {
            @Override
            public void onLoadSucceed(OperationResult operationResult, List<Media> medias) {

                mMedias = medias;
                mView.dismissLoadingUI();

                if (mMedias.isEmpty()) {
                    mView.showNoContentUI();
                    mView.dismissContentUI();
                } else {
                    mView.showContentUI();
                    mView.dismissNoContentUI();
                    mView.showMedias(medias);
                }

            }

            @Override
            public void onLoadFail(OperationResult operationResult) {

            }
        });
    }
}
