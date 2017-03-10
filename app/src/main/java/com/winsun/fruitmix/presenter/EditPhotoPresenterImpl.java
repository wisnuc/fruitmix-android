package com.winsun.fruitmix.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.business.DataRepository;
import com.winsun.fruitmix.business.callback.MediaOperationCallback;
import com.winsun.fruitmix.business.callback.MediaShareOperationCallback;
import com.winsun.fruitmix.contract.EditPhotoContract;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.Util;

import java.util.List;

/**
 * Created by Administrator on 2017/2/16.
 */

public class EditPhotoPresenterImpl implements EditPhotoContract.EditPhotoPresenter {

    private EditPhotoContract.EditPhotoView mView;

    private DataRepository mRepository;

    private MediaShare mMediaShare;
    private MediaShare mModifiedMediaShare;
    private List<Media> mMedias;

    private boolean isOperated = false;

    public EditPhotoPresenterImpl(DataRepository repository, String mediaShareUUID) {
        this.mRepository = repository;

        mMediaShare = mRepository.loadMediaShareFromMemory(mediaShareUUID);
        mModifiedMediaShare = mMediaShare.cloneMyself();

    }

    @Override
    public void modifyMediaInMediaShare() {

        if (!mView.isNetworkAlive()) {
            mView.showNoNetwork();
            return;
        }

        MediaShare diffContentsOriginalMediaShare = mMediaShare.cloneMyself();
        diffContentsOriginalMediaShare.clearMediaShareContents();
        diffContentsOriginalMediaShare.initMediaShareContents(mMediaShare.getDifferentMediaShareContentInCurrentMediaShare(mModifiedMediaShare));

        MediaShare diffContentsModifiedMediaShare = mModifiedMediaShare.cloneMyself();
        diffContentsModifiedMediaShare.clearMediaShareContents();
        diffContentsModifiedMediaShare.initMediaShareContents(mModifiedMediaShare.getDifferentMediaShareContentInCurrentMediaShare(mMediaShare));

        int diffOriginalMediaShareContentSize = diffContentsOriginalMediaShare.getMediaContentsListSize();
        int diffModifiedMediaShareContentSize = diffContentsModifiedMediaShare.getMediaContentsListSize();
        if (diffOriginalMediaShareContentSize == 0 && diffModifiedMediaShareContentSize == 0) {

            mView.finishActivity();
        }

        if (mModifiedMediaShare.getMediaContentsListSize() != 0) {
            mModifiedMediaShare.setCoverImageKey(mModifiedMediaShare.getFirstMediaDigestInMediaContentsList());
        } else {
            mModifiedMediaShare.setCoverImageKey("");
        }

        mView.showDialog();

        mRepository.modifyMediaInMediaShare(diffContentsOriginalMediaShare,diffContentsModifiedMediaShare, mModifiedMediaShare, new MediaShareOperationCallback.OperateMediaShareCallback() {
            @Override
            public void onOperateSucceed(OperationResult operationResult, MediaShare mediaShare) {
                mView.dismissDialog();
                mView.showOperationResultToast(operationResult);

                isOperated = true;
                handleBackEvent();
            }

            @Override
            public void onOperateFail(OperationResult operationResult) {
                mView.dismissDialog();
                mView.showOperationResultToast(operationResult);

                handleBackEvent();
            }
        });

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

    @Override
    public void removeContent(int position) {

        mModifiedMediaShare.removeMediaShareContent(position);
        mMedias.remove(position);

    }

    @Override
    public void loadMediaToView(Context context, Media media, NetworkImageView view) {
        mRepository.loadThumbMediaToNetworkImageView(context,media,view);
    }

    @Override
    public void attachView(EditPhotoContract.EditPhotoView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void handleBackEvent() {

        if (isOperated)
            mView.setResult(Activity.RESULT_OK);
        else
            mView.setResult(Activity.RESULT_CANCELED);

        mView.finishActivity();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
