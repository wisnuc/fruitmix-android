package com.winsun.fruitmix.refactor.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.View;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.business.callback.MediaOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.MediaShareOperationCallback;
import com.winsun.fruitmix.refactor.contract.MediaFragmentContract;
import com.winsun.fruitmix.refactor.contract.MediaMainFragmentContract;
import com.winsun.fruitmix.refactor.model.MediaFragmentDataLoader;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/10.
 */

public class MediaFragmentPresenterImpl implements MediaFragmentContract.MediaFragmentPresenter {

    private MediaFragmentContract.MediaFragmentView mView;

    private MediaMainFragmentContract.MediaMainFragmentPresenter mMediaMainFragmentPresenter;

    private DataRepository mRepository;

    private List<String> mPhotoDateGroups;

    private Map<String, List<Media>> mMapKeyIsDateValueIsPhotoList;

    private SparseArray<Media> mMapKeyIsPhotoPositionValueIsPhoto;

    private List<String> mAlreadySelectedImageKeyArrayList;

    private int mSelectCount;

    private boolean mSelectMode = false;

    private Bundle reenterState;
    private boolean mPhotoListRefresh = false;

    private boolean mFabExpand = false;

    public MediaFragmentPresenterImpl(MediaMainFragmentContract.MediaMainFragmentPresenter mediaMainFragmentPresenter, DataRepository repository,List<String> alreadySelectedImageKeyArrayList) {
        mMediaMainFragmentPresenter = mediaMainFragmentPresenter;
        mRepository = repository;
        mAlreadySelectedImageKeyArrayList = alreadySelectedImageKeyArrayList;
    }

    private void clearSelectPhoto() {
        if (mMapKeyIsPhotoPositionValueIsPhoto == null) return;

        for (List<Media> mediaList : mMapKeyIsDateValueIsPhotoList.values()) {
            for (Media media : mediaList) {
                media.setSelected(false);
            }
        }
    }

    @Override
    public void setSelectCountText(String selectCountText) {
        mMediaMainFragmentPresenter.setTitleText(selectCountText);
    }

    @Override
    public int getSelectCount() {
        return mSelectCount;
    }

    @Override
    public void calcSelectedPhoto() {

        mSelectCount = 0;

        for (List<Media> mediaList : mMapKeyIsDateValueIsPhotoList.values()) {
            for (Media media : mediaList) {
                if (media.isSelected())
                    mSelectCount++;
            }
        }

    }

    @Override
    public void enterChooseMode() {

        mSelectMode = true;

        mMediaMainFragmentPresenter.setToolbarNavigationIcon(R.drawable.ic_back);
        mMediaMainFragmentPresenter.setToolbarNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitChooseMode();
            }
        });

        mMediaMainFragmentPresenter.setSelectModeBtnVisibility(View.GONE);
        mMediaMainFragmentPresenter.dismissBottomNavAnim();
        mMediaMainFragmentPresenter.setTitleText(R.string.choose_photo);
        mMediaMainFragmentPresenter.lockDrawer();

        mView.setFABVisibility(View.VISIBLE);
        mView.notifyDataSetChangedUseAnim();
    }

    @Override
    public void quitChooseMode() {

        mSelectMode = false;
        clearSelectPhoto();

        mMediaMainFragmentPresenter.setToolbarNavigationIcon(R.drawable.menu);
        mMediaMainFragmentPresenter.setToolbarNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaMainFragmentPresenter.switchDrawerOpenState();
            }
        });

        mMediaMainFragmentPresenter.setSelectModeBtnVisibility(View.VISIBLE);
        mMediaMainFragmentPresenter.showBottomNavAnim();
        mMediaMainFragmentPresenter.setTitleText(R.string.photo);
        mMediaMainFragmentPresenter.unlockDrawer();

        mFabExpand = false;
        mView.collapseFab();
        mView.setFABVisibility(View.GONE);

        mView.notifyDataSetChangedUseAnim();
    }

    @NonNull
    private List<String> getSelectedImageKeys() {

        List<String> selectedImageKeys = new ArrayList<>();

        for (List<Media> mediaList : mMapKeyIsDateValueIsPhotoList.values()) {
            for (Media media : mediaList) {
                if (media.isSelected()) {

                    String mediaUUID = media.getUuid();
                    if (mediaUUID.isEmpty()) {
                        mediaUUID = Util.CalcSHA256OfFile(media.getThumb());
                    }

                    selectedImageKeys.add(mediaUUID);
                }
            }
        }

        if (mAlreadySelectedImageKeyArrayList != null) {
            for (String mediaUUID : mAlreadySelectedImageKeyArrayList) {
                if (!selectedImageKeys.contains(mediaUUID)) {
                    selectedImageKeys.add(mediaUUID);
                }
            }
        }

        return selectedImageKeys;
    }

    @Override
    public void albumBtnOnClick() {

        if (!mView.isNetworkAlive())
            mView.showNoNetwork();

        List<String> selectMediaKeys = getSelectedImageKeys();
        if (selectMediaKeys.size() == 0) {
            mView.showSelectNothingToast();
        }
        quitChooseMode();

        createAlbum(selectMediaKeys);

    }

    @Override
    public void shareBtnOnClick() {

        if (!mView.isNetworkAlive())
            mView.showNoNetwork();

        List<String> selectMediaKeys = getSelectedImageKeys();
        if (selectMediaKeys.size() == 0) {
            mView.showSelectNothingToast();
        }
        quitChooseMode();

        mView.showDialog();
        createMediaShare(selectMediaKeys);

    }

    @Override
    public void fabOnClick() {

        if (mFabExpand) {
            mFabExpand = false;
            mView.collapseFab();
        } else {
            mFabExpand = true;
            mView.expandFab();
        }

    }

    private void createMediaShare(List<String> selectMediaKeys) {
        mRepository.createMediaShare(mRepository.createMediaShareInMemory(false,true,false,"","",selectMediaKeys), new MediaShareOperationCallback.OperateMediaShareCallback() {
            @Override
            public void onOperateSucceed(OperationResult operationResult, MediaShare mediaShare) {

                mView.dismissDialog();

                mMediaMainFragmentPresenter.setViewPageCurrentItem(MediaMainFragmentPresenterImpl.PAGE_SHARE);
            }

            @Override
            public void onOperateFail(OperationResult operationResult) {

                mView.dismissDialog();
            }
        });
    }

    private void createAlbum(List<String> selectMediaKeys) {

        mRepository.insertMediaKeysInCreateAlbum(selectMediaKeys);
        clearSelectPhoto();

        mView.startCreateAlbumActivity();

    }

    @Override
    public boolean isSelectState() {
        return mSelectMode;
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

        reenterState = new Bundle(data.getExtras());
        int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
        int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);
        String currentMediaKey = reenterState.getString(Util.CURRENT_MEDIA_KEY);

        if (initialPhotoPosition != currentPhotoPosition) {

            int scrollToPosition = 0;

            Media media;

            int size = mMapKeyIsPhotoPositionValueIsPhoto.size();

            for (int i = 0; i < size; i++) {
                media = mMapKeyIsPhotoPositionValueIsPhoto.valueAt(i);
                if (media.getKey().equals(currentMediaKey))
                    scrollToPosition = mMapKeyIsPhotoPositionValueIsPhoto.keyAt(i);
            }

            mView.smoothScrollToPosition(scrollToPosition);

            mView.startPostponedEnterTransition();

        }

    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        if (reenterState != null) {

            int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
            int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);
            String currentMediaKey = reenterState.getString(Util.CURRENT_MEDIA_KEY);

            if (initialPhotoPosition != currentPhotoPosition) {

                names.clear();
                sharedElements.clear();

                Media media;
                Media currentMedia = null;

                int size = mMapKeyIsPhotoPositionValueIsPhoto.size();

                for (int i = 0; i < size; i++) {
                    media = mMapKeyIsPhotoPositionValueIsPhoto.valueAt(i);
                    if (media.getKey().equals(currentMediaKey))
                        currentMedia = media;
                }

                if (currentMedia == null) return;

                View newSharedElement = mView.findViewByMedia(currentMedia);
                String sharedElementName = currentMedia.getKey();

                names.add(sharedElementName);
                sharedElements.put(sharedElementName, newSharedElement);
            }

        }
        reenterState = null;

    }

    @Override
    public void onResume(){
        if (mPhotoListRefresh) {
            mRepository.loadLocalMediaInCameraInThread(new MediaOperationCallback.LoadMediasCallback() {
                @Override
                public void onLoadSucceed(OperationResult operationResult, List<Media> medias) {
                    showMedias(medias);
                }

                @Override
                public void onLoadFail(OperationResult operationResult) {

                }
            });
        }
    }

    @Override
    public void attachView(MediaFragmentContract.MediaFragmentView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void onCreate() {

        mRepository.loadMediasInThread(new MediaOperationCallback.LoadMediasCallback() {
            @Override
            public void onLoadSucceed(OperationResult operationResult, List<Media> medias) {

                mPhotoListRefresh = true;

                showMedias(medias);
            }

            @Override
            public void onLoadFail(OperationResult operationResult) {

            }
        });
    }

    private void showMedias(final Collection<Media> medias) {
        mRepository.handleMediasForMediaFragment(medias, new MediaOperationCallback.HandleMediaForMediaFragmentCallback() {
            @Override
            public void onOperateFinished(MediaFragmentDataLoader loader) {
                mPhotoDateGroups = loader.getPhotoDateGroups();
                mMapKeyIsPhotoPositionValueIsPhoto = loader.getMapKeyIsPhotoPositionValueIsPhoto();
                mMapKeyIsDateValueIsPhotoList = loader.getMapKeyIsDateValueIsPhotoList();

                clearSelectPhoto();

                mView.dismissLoadingUI();
                if(mPhotoDateGroups.size() == 0){
                    mView.dismissNoContentUI();
                    mView.showContentUI();
                    mView.showMedias(loader);

                    mMediaMainFragmentPresenter.setSelectModeBtnVisibility(View.VISIBLE);
                }else {
                    mView.showNoContentUI();
                    mView.dismissContentUI();

                    mMediaMainFragmentPresenter.setSelectModeBtnVisibility(View.INVISIBLE);
                }

            }
        });
    }

    @Override
    public void handleBackEvent() {

    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
