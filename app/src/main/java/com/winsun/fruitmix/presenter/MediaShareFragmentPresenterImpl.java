package com.winsun.fruitmix.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.business.DataRepository;
import com.winsun.fruitmix.business.callback.MediaShareOperationCallback;
import com.winsun.fruitmix.contract.MediaShareFragmentContract;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/13.
 */

public class MediaShareFragmentPresenterImpl implements MediaShareFragmentContract.MediaShareFragmentPresenter {

    private MediaShareFragmentContract.MediaShareFragmentView mView;

    private DataRepository mRepository;

    private List<MediaShare> mMediaShares;

    private Bundle reenterState;

    private MediaShareOperationCallback.LoadMediaSharesCallback mCallback;

    public MediaShareFragmentPresenterImpl(DataRepository repository) {
        mRepository = repository;

        mMediaShares = new ArrayList<>();
    }

    @Override
    public List<Media> loadMedias(List<String> imageKeys) {
        ArrayList<Media> picList;
        Media picItem;

        picList = new ArrayList<>();

        for (String aStArr : imageKeys) {

            picItem = mRepository.loadMediaFromMemory(aStArr);
            if (picItem == null)
                picItem = new Media();

            picItem.setSelected(false);

            picList.add(picItem);
        }

        return picList;

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        reenterState = new Bundle(data.getExtras());
        int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
        int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);

        if (initialPhotoPosition != currentPhotoPosition) {

            mView.startPostponedEnterTransition();

        }
    }

    private int findShareItemPosition(String currentMediaShareTime) {

        int returnPosition = 0;
        int size = mMediaShares.size();
        for (int i = 0; i < size; i++) {
            MediaShare mediaShare = mMediaShares.get(i);
            String mediaShareTime = mediaShare.getTime();
            if (currentMediaShareTime.equals(mediaShareTime)) {
                returnPosition = i;
                break;
            }
        }

        return returnPosition;
    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        if (reenterState != null) {

            int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
            int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);
            String currentMediaShareTime = reenterState.getString(Util.CURRENT_MEDIASHARE_TIME);
            if (initialPhotoPosition != currentPhotoPosition) {

                names.clear();
                sharedElements.clear();

                int currentMediaSharePosition = findShareItemPosition(currentMediaShareTime);

                List<String> currentMediaUUIDs = mMediaShares.get(currentMediaSharePosition).getMediaKeyInMediaShareContents();

                String currentMediaUUID = currentMediaUUIDs.get(currentPhotoPosition);

                View currentSharedElementView = mView.findViewWithMedia(mRepository.loadMediaFromMemory(currentMediaUUID));

                names.add(currentMediaUUID);
                sharedElements.put(currentMediaUUID, currentSharedElementView);

            }

        }
        reenterState = null;

    }

    @Override
    public void refreshData() {
        loadMediaShares();
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

    private void loadMediaShares() {

        mView.showLoadingUI();

        mCallback = new MediaShareOperationCallback.LoadMediaSharesCallback() {
            @Override
            public void onLoadSucceed(OperationResult operationResult, Collection<MediaShare> mediaShares) {

                if (mView == null) return;

                mMediaShares.clear();

                for (MediaShare mediaShare : mediaShares) {

                    if (mRepository.isMediaSharePublic(mediaShare)) {
                        mMediaShares.add(mediaShare);
                    }
                }

                sortMediaShareList(mMediaShares);

                mView.dismissLoadingUI();
                if (mMediaShares.isEmpty()) {
                    mView.showNoContentUI();
                    mView.dismissContentUI();
                } else {
                    mView.dismissNoContentUI();
                    mView.showContentUI();
                    mView.showMediaShares(mMediaShares);
                }

            }

            @Override
            public void onLoadFail(OperationResult operationResult) {

            }
        };

        mRepository.loadMediaSharesInThread(mCallback);
        mRepository.registerTimeRetrieveMediaShareCallback(mCallback);
    }

    @Override
    public User loadUser(String userUUID) {
        return mRepository.loadUserFromMemory(userUUID);
    }

    @Override
    public Media loadMedia(String mediaKey) {
        return mRepository.loadMediaFromMemory(mediaKey);
    }

    @Override
    public void loadThumbMediaToView(Context context, Media media, NetworkImageView view) {
        mRepository.loadThumbMediaToNetworkImageView(context, media, view);
    }

    @Override
    public void loadOriginalMediaToView(Context context, Media media, NetworkImageView view) {
        mRepository.loadOriginalMediaToNetworkImageView(context, media, view);
    }

    @Override
    public String loadImageThumbUrl(Media media) {
        return mRepository.loadImageThumbUrl(media);
    }

    @Override
    public void attachView(MediaShareFragmentContract.MediaShareFragmentView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;

        mRepository.unregisterTimeRetrieveMediaShareCallback(mCallback);
    }

    @Override
    public void handleBackEvent() {

    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
