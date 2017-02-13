package com.winsun.fruitmix.refactor.presenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.business.callback.MediaOperationCallback;
import com.winsun.fruitmix.refactor.contract.AlbumContentContract;
import com.winsun.fruitmix.util.Util;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/13.
 */

public class AlbumContentPresentImpl implements AlbumContentContract.AlbumContentPresenter {

    private AlbumContentContract.AlbumContentView mView;

    private MediaShare mMediaShare;
    private List<Media> mMedias;

    private DataRepository mRepository;

    private Bundle reenterState;

    private boolean isOperated = false;

    public AlbumContentPresentImpl(DataRepository repository, String mediaShareUUID) {

        mRepository = repository;

        mMediaShare = mRepository.loadMediaShareFromMemory(mediaShareUUID);

    }

    @Override
    public void toggleAlbumPublicState() {

    }

    @Override
    public void deleteCurrentAlbum() {

    }

    @Override
    public boolean preTreatItemOnOptionsItemSelected() {

        if (mView.isNetworkAlive()) {
            if (mMediaShare.isLocal()) {
                mView.showUploadingToast();
                return false;
            }
        } else {
            if (!mMediaShare.isLocal()) {
                mView.showNoNetwork();
                return false;
            }
        }

        if (mRepository.checkPermissionToOperateMediaShare(mMediaShare)) {
            return true;
        } else {
            mView.showNoOperationPermission();
            return false;
        }

    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        if (reenterState != null) {

            int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
            int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);

            if (initialPhotoPosition != currentPhotoPosition) {

                names.clear();
                sharedElements.clear();

                Media media = mMedias.get(currentPhotoPosition);

                String sharedElementName = media.getKey();
                View newSharedElement = mView.findViewWithTag(sharedElementName);

                names.add(sharedElementName);
                sharedElements.put(sharedElementName, newSharedElement);
            }

            reenterState = null;
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        reenterState = new Bundle(data.getExtras());
        int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
        int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);

        if (initialPhotoPosition != currentPhotoPosition) {

            mView.smoothScrollToPosition(currentPhotoPosition);

/*            ActivityCompat.postponeEnterTransition(AlbumPicContentActivity.this);
            mainGridView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mainGridView.getViewTreeObserver().removeOnPreDrawListener(this);
                    // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
                    mainGridView.requestLayout();


                    return true;
                }
            });*/
            mView.startPostponedEnterTransition();
        }
    }

    @Override
    public void setMediaShareTitle() {

        mView.setTitle(mMediaShare.getTitle());
    }

    @Override
    public void loadMediaInMediaShare() {

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
                    mView.showAlbumContent(mMedias);
                }

            }

            @Override
            public void onLoadFail(OperationResult operationResult) {

            }
        });


    }

    @Override
    public void attachView(AlbumContentContract.AlbumContentView view) {
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

        mView.finishActivity();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
