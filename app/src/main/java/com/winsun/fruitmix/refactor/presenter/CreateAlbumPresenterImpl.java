package com.winsun.fruitmix.refactor.presenter;

import android.app.Activity;
import android.content.Intent;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.business.callback.MediaShareOperationCallback;
import com.winsun.fruitmix.refactor.contract.CreateAlbumContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/2/16.
 */

public class CreateAlbumPresenterImpl implements CreateAlbumContract.CreateAlbumPresenter {

    private CreateAlbumContract.CreateAlbumView mView;

    private DataRepository mRepository;

    private List<String> mSelectMediaKeys;

    private boolean isOperated = false;

    public CreateAlbumPresenterImpl(DataRepository repository) {
        this.mRepository = repository;
        mSelectMediaKeys = mRepository.getMediaKeysInCreateAlbum();
    }

    @Override
    public void createAlbum(String title, String desc, boolean isPublic, boolean isMaintained) {
        mView.hideSoftInput();

        if (mView.isNetworkAlive()) {
            mView.showNoNetwork();

            handleBackEvent();
            return;
        }

        mView.showDialog();

        MediaShare mediaShare = mRepository.createMediaShareInMemory(true, isPublic, isMaintained, title, desc, mSelectMediaKeys);
        mRepository.clearMediaKeysInCreateAlbum();

        mRepository.createMediaShare(mediaShare, new MediaShareOperationCallback.OperateMediaShareCallback() {
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
    public void initView() {
        mView.setLayoutTitle(mView.getString(R.string.create_album));

        String mTitle = String.format(mView.getString(R.string.album_item_title), new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())));
        mView.setAlbumTitleHint(mTitle);

    }

    @Override
    public void attachView(CreateAlbumContract.CreateAlbumView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void handleBackEvent() {

        if (isOperated) {
            mView.setResult(Activity.RESULT_OK);
        } else {
            mView.setResult(Activity.RESULT_CANCELED);
        }
        mView.finishActivity();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
