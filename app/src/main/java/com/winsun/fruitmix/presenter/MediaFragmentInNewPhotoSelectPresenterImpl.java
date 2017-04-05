package com.winsun.fruitmix.presenter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.business.DataRepository;
import com.winsun.fruitmix.business.callback.MediaOperationCallback;
import com.winsun.fruitmix.contract.MediaFragmentContract;
import com.winsun.fruitmix.contract.NewPhotoSelectContract;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.MediaFragmentDataLoader;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/2.
 */

public class MediaFragmentInNewPhotoSelectPresenterImpl implements MediaFragmentContract.MediaFragmentPresenter {

    public static final String TAG = MediaFragmentInNewPhotoSelectPresenterImpl.class.getSimpleName();

    private NewPhotoSelectContract.NewPhotoSelectPresenter newPhotoSelectPresenter;

    private MediaFragmentContract.MediaFragmentView mView;

    private DataRepository mRepository;

    private List<String> mPhotoDateGroups;

    private Map<String, List<Media>> mMapKeyIsDateValueIsPhotoList;

    private SparseArray<Media> mMapKeyIsPhotoPositionValueIsPhoto;

    private List<String> mAlreadySelectedImageKeyArrayList;

    private int mSelectCount;

    public void setNewPhotoSelectPresenter(NewPhotoSelectContract.NewPhotoSelectPresenter newPhotoSelectPresenter, DataRepository repository, List<String> alreadySelectedImageKeyArrayList) {
        this.newPhotoSelectPresenter = newPhotoSelectPresenter;

        mRepository = repository;

        mAlreadySelectedImageKeyArrayList = alreadySelectedImageKeyArrayList;
    }

    @Override
    public void setItemWidth(int itemWidth) {
    }

    @Override
    public void setSelectCountText(String selectCountText) {

        newPhotoSelectPresenter.setTitle(selectCountText);
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

        mView.setFABVisibility(View.GONE);

    }

    @Override
    public void quitChooseMode() {
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

        if (!mView.isNetworkAlive()){
            mView.showNoNetwork();
            return;
        }

        List<String> selectMediaKeys = getSelectedImageKeys();
        if (selectMediaKeys.size() == 0) {
            mView.showSelectNothingToast();
        }

        createAlbum(selectMediaKeys);

    }

    private void createAlbum(List<String> selectMediaKeys) {

        mRepository.insertMediaKeysInCreateAlbum(selectMediaKeys);
        clearSelectPhoto();

        mView.startCreateAlbumActivity();

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
    public void shareBtnOnClick() {
    }

    @Override
    public void fabOnClick() {
    }

    @Override
    public boolean isSelectState() {
        return true;
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
    }

    @Override
    public void onCreate() {
        mRepository.loadMediasInThread(new MediaOperationCallback.LoadMediasCallback() {
            @Override
            public void onLoadSucceed(OperationResult operationResult, List<Media> medias) {

                if (mView == null) return;

                Log.i(TAG, "onLoadSucceed: media size:" + medias.size());

                showMedias(medias);
            }

            @Override
            public void onLoadFail(OperationResult operationResult) {

            }
        });
    }

    @Override
    public void onResume() {
    }

    @Override
    public void loadThumbMediaToView(Context context, Media media, NetworkImageView view) {
        mRepository.loadThumbMediaToNetworkImageView(media, view);
    }

    @Override
    public void loadSmallThumbMediaToView(Context context, Media media, NetworkImageView view) {

    }

    @Override
    public void cancelLoadMediaToView(NetworkImageView view) {
        view.setDefaultImageResId(R.drawable.placeholder_photo);
        view.setImageUrl(null, null);
    }

    private void showMedias(final Collection<Media> medias) {
        mRepository.handleMediasForMediaFragment(medias, new MediaOperationCallback.HandleMediaForMediaFragmentCallback() {
            @Override
            public void onOperateFinished(MediaFragmentDataLoader loader) {

                if (mView == null) return;

                mPhotoDateGroups = loader.getPhotoDateGroups();
                mMapKeyIsPhotoPositionValueIsPhoto = loader.getMapKeyIsPhotoPositionValueIsPhoto();
                mMapKeyIsDateValueIsPhotoList = loader.getMapKeyIsDateValueIsPhotoList();

                clearSelectPhoto();

                mView.dismissLoadingUI();
                if (mPhotoDateGroups.size() != 0) {
                    mView.dismissNoContentUI();
                    mView.showContentUI();
                    mView.showMedias(loader);

                } else {
                    mView.showNoContentUI();
                    mView.dismissContentUI();

                }

            }
        });
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
    public void handleBackEvent() {
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
    }
}
