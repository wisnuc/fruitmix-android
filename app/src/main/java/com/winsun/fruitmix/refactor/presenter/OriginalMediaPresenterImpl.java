package com.winsun.fruitmix.refactor.presenter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.SharedElementCallback;
import android.util.Log;
import android.view.View;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.contract.OriginalMediaContract;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/16.
 */

public class OriginalMediaPresenterImpl implements OriginalMediaContract.OriginalMediaPresenter {

    public static final String TAG = OriginalMediaPresenterImpl.class.getSimpleName();

    private OriginalMediaContract.OriginalMediaView mView;

    private List<Media> mMedias;
    private List<Media> mAlreadyLoadedMedias;

    private DataRepository mRepository;

    private int initialPhotoPosition = 0;
    private int currentPhotoPosition = 0;
    private boolean willReturn = false;

    private boolean transitionMediaNeedShowThumb = true;

    private boolean needTransition = true;

    private boolean sInEdit = true;

    private boolean mIsFullScreen = false;

    public OriginalMediaPresenterImpl(List<Media> medias, DataRepository repository, int initialPhotoPosition, boolean needTransition, boolean transitionMediaNeedShowThumb) {
        mMedias = new ArrayList<>(medias.size());
        mMedias.addAll(medias);

        mRepository = repository;
        this.initialPhotoPosition = initialPhotoPosition;

        this.needTransition = needTransition;
        this.transitionMediaNeedShowThumb = transitionMediaNeedShowThumb;
    }

    @Override
    public void onPageChanged(int position) {

        currentPhotoPosition = position;

        if (mMedias.size() > position && position > -1) {
            Log.d(TAG, "image:" + mMedias.get(position));

            String title = mMedias.get(position).getTime();
            if (title == null || title.contains("1916-01-01")) {
                mView.setDate(mView.getString(R.string.unknown_time));
            } else {
                mView.setDate(title);
            }
        }

    }

    private void onMapSharedElementsImpl(List<String> names, Map<String, View> sharedElements) {
        if (willReturn) {
            if (initialPhotoPosition != currentPhotoPosition) {

                names.clear();
                sharedElements.clear();

                Media media = mMedias.get(currentPhotoPosition);

                String imageKey = media.getKey();
                names.add(imageKey);

                String imageTag;

                boolean isThumb = media.isLoaded();
                imageTag = mView.getImageUrl(isThumb, media);

                sharedElements.put(imageKey, mView.findViewWithTag(imageTag));

                Log.d(TAG, "onMapSharedElements: media key:" + imageKey + " imageTag:" + imageTag);
            }
        }
    }

    @Override
    public void initView() {

        if (needTransition) {
            mView.postponeEnterTransition();
            mView.setEnterSharedElementCallback(new SharedElementCallback() {

                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    super.onMapSharedElements(names, sharedElements);

                    onMapSharedElementsImpl(names, sharedElements);
                }
            });
        }

        refreshReturnResizeVisibility();
        onPageChanged(initialPhotoPosition);
    }


    private void refreshReturnResizeVisibility() {

        if (mRepository.getShowPhotoReturnTipsValue()) {
            mRepository.setShowPhotoReturnTipsValue(false);

            mView.setReturnResizeVisibility(View.VISIBLE);
            mView.setReturnResizeOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.setReturnResizeVisibility(View.GONE);
                }
            });
        }

    }

    @Override
    public void attachView(OriginalMediaContract.OriginalMediaView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void handleBackEvent() {

        resetMediaLoadedState();

    }

    private void resetMediaLoadedState() {
        for (Media media : mAlreadyLoadedMedias) {
            media.setLoaded(false);
        }
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
