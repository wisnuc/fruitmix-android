package com.winsun.fruitmix.refactor.presenter;

import android.app.Activity;
import android.content.Intent;

import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.business.callback.MediaShareOperationCallback;
import com.winsun.fruitmix.refactor.contract.ModifyAlbumContract;
import com.winsun.fruitmix.util.Util;

import java.util.Collection;

/**
 * Created by Administrator on 2017/2/16.
 */

public class ModifyAlbumPresenterImpl implements ModifyAlbumContract.ModifyAlbumPresenter {

    private ModifyAlbumContract.ModifyAlbumView mView;

    private DataRepository mRepository;

    private MediaShare mMediaShare;

    private boolean isOperated = false;

    public ModifyAlbumPresenterImpl(DataRepository repository, String mediaShareUUID) {
        this.mRepository = repository;

        mMediaShare = mRepository.loadMediaShareFromMemory(mediaShareUUID);
    }

    @Override
    public void modifyAlbum(String title, String desc, boolean isPublic, boolean isMaintained) {

        mView.hideSoftInput();

        if (mView.isNetworkAlive()) {
            mView.showNoNetwork();

            handleBackEvent();
            return;
        }

        MediaShare cloneMediaShare = mMediaShare.cloneMyself();

        mView.showDialog();

        mRepository.modifyMediaShare(createRequestData(cloneMediaShare, isPublic, isMaintained, title, desc), cloneMediaShare, new MediaShareOperationCallback.OperateMediaShareCallback() {
            @Override
            public void onOperateSucceed(OperationResult operationResult, MediaShare mediaShare) {

                mView.dismissDialog();
                isOperated = true;
                handleBackEvent();
            }

            @Override
            public void onOperateFail(OperationResult operationResult) {

                mView.dismissDialog();
                handleBackEvent();
            }
        });
    }

    private String createRequestData(MediaShare mediaShare, boolean isPublic, boolean isMaintained, String title, String desc) {
        String requestData;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");

        Collection<String> userUUIDs = mRepository.loadAllUserUUIDInMemory();

        boolean currentIsPublic = mediaShare.getViewersListSize() != 0;
        if (currentIsPublic != isPublic) {
            if (currentIsPublic) {

                for (String userUUID : userUUIDs) {
                    mediaShare.addViewer(userUUID);
                }

                stringBuilder.append(mediaShare.createStringOperateViewersInMediaShare(Util.ADD));

            } else {

                stringBuilder.append(mediaShare.createStringOperateViewersInMediaShare(Util.DELETE));

                mediaShare.clearViewers();
            }

            stringBuilder.append(",");
        }

        boolean currentIsMaintained = mediaShare.checkMaintainersListContainUserUUID(mRepository.loadCurrentLoginUserUUIDInMemory());

        if (isMaintained != currentIsMaintained) {

            if (isMaintained) {

                for (String userUUID : userUUIDs) {
                    mediaShare.addMaintainer(userUUID);
                }

                stringBuilder.append(mediaShare.createStringOperateMaintainersInMediaShare(Util.ADD));

            } else {

                stringBuilder.append(mediaShare.createStringOperateMaintainersInMediaShare(Util.DELETE));

                mediaShare.clearMaintainers();
            }

            stringBuilder.append(",");

        }

        if (!mediaShare.getTitle().equals(title) || !mediaShare.getDesc().equals(desc)) {

            mediaShare.setTitle(title);
            mediaShare.setDesc(desc);

            stringBuilder.append(mediaShare.createStringReplaceTitleTextAboutMediaShare());

            stringBuilder.append(",");
        }

        requestData = stringBuilder.substring(0, stringBuilder.length() - 1);

        requestData += "]";

        if (requestData.length() <= 2) {
            return null;
        }

        return requestData;
    }

    @Override
    public void initView() {

        mView.setAlbumTitle(mMediaShare.getTitle());
        mView.setDescription(mMediaShare.getDesc());
        mView.setIsPublic(mMediaShare.getViewersListSize() != 0);

        mView.setIsMaintained(mMediaShare.checkMaintainersListContainUserUUID(mRepository.loadCurrentLoginUserUUIDInMemory()));
    }

    @Override
    public void attachView(ModifyAlbumContract.ModifyAlbumView view) {
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
