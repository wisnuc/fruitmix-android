package com.winsun.fruitmix.presenter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.SharedElementCallback;
import android.transition.Transition;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.business.DataRepository;
import com.winsun.fruitmix.component.GifTouchNetworkImageView;
import com.winsun.fruitmix.contract.OriginalMediaContract;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.CustomTransitionListener;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

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

    private String mCurrentMediaShareTime;

    private float x, y, lastX, lastY;

    public OriginalMediaPresenterImpl(List<Media> medias, DataRepository repository, int initialPhotoPosition, boolean needTransition, boolean transitionMediaNeedShowThumb, String currentMediaShareTime) {
        mMedias = new ArrayList<>(medias.size());
        mMedias.addAll(medias);

        mAlreadyLoadedMedias = new ArrayList<>();

        mRepository = repository;
        this.initialPhotoPosition = initialPhotoPosition;

        this.needTransition = needTransition;
        this.transitionMediaNeedShowThumb = transitionMediaNeedShowThumb;
        mCurrentMediaShareTime = currentMediaShareTime;
    }

    @Override
    public void setTitle(int position) {

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
                imageTag = getImageUrl(isThumb, media);

                sharedElements.put(imageKey, mView.findViewWithTag(imageTag));

                Log.d(TAG, "onMapSharedElements: media key:" + imageKey + " imageTag:" + imageTag);
            }
        }
    }


    private String getImageUrl(boolean isThumb, Media media) {
        String currentUrl;

        if (isThumb) {
            currentUrl = mRepository.loadImageThumbUrl(media);
        } else {
            currentUrl = mRepository.loadImageOriginalUrl(media);
        }

        return currentUrl;
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
        setTitle(initialPhotoPosition);
    }

    @Override
    public void finishAfterTransition() {
        willReturn = true;
        Intent intent = new Intent();
        intent.putExtra(Util.INITIAL_PHOTO_POSITION, initialPhotoPosition);
        intent.putExtra(Util.CURRENT_PHOTO_POSITION, currentPhotoPosition);

        intent.putExtra(Util.CURRENT_MEDIA_KEY, mMedias.get(currentPhotoPosition).getKey());
        intent.putExtra(Util.CURRENT_MEDIASHARE_TIME, mCurrentMediaShareTime);
        mView.setResult(RESULT_OK, intent);

    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        // Note that system bars will only be "visible" if none of the
        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
            // TODO: The system bars are visible. Make any desired
            // adjustments to your UI, such as showing the action bar or
            // other navigational controls.

            if (!sInEdit) {
                convertEditState();
            }

        } else {
            // TODO: The system bars are NOT visible. Make any desired
            // adjustments to your UI, such as hiding the action bar or
            // other navigational controls.

            if (sInEdit) {
                convertEditState();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && mIsFullScreen) {
            mView.hideSystemUI();
        }
    }

    @Override
    public void handleMediaLoaded(Context context, String mediaUrl, Media media, View view) {
        if (media.isLocal()) {
            handleLocalMediaLoaded(media);
        } else {
            handleRemoteMediaLoaded(context, mediaUrl, view, media);
        }
    }

    @Override
    public void instantiateItem(final Context context, int position, GifTouchNetworkImageView view) {

        if (mMedias.size() > position && position > -1) {

            Media media = mMedias.get(position);

            Log.d(TAG, "instantiateItem: orientationNumber:" + media.getOrientationNumber());

            if (position == initialPhotoPosition)
                mView.setTransitionName(view, media.getKey());

            if (transitionMediaNeedShowThumb && !media.isLocal()) {
                mRepository.loadThumbMediaToGifTouchNetworkImageView(context, media, view);
            } else {
                mRepository.loadOriginalMediaToGifTouchNetworkImageView(context, media, view);
            }

            mView.addOnTouchListener(view,new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    handleTouchEvent(context,event, (GifTouchNetworkImageView) v);

                    return false;
                }
            });
        }
    }

    @Override
    public int getMediasCount() {
        if (mMedias.size() == 0) {
            return 0;
        } else {
            return mMedias.size();
        }
    }

    private void handleLocalMediaLoaded(Media media) {
        if (mView.isCurrentViewPage(initialPhotoPosition) && needTransition) {
            mView.startPostponedEnterTransition();
        }

        if (!media.isLoaded()) {
            media.setLoaded(true);

            mAlreadyLoadedMedias.add(media);
        }
    }

    private void handleRemoteMediaLoaded(Context context, String url, View view, Media media) {
        if (isImageThumb(url)) {

            handleThumbLoaded(context, view, media);

        } else {

            handleOriginalMediaLoaded(media);

        }
    }

    private boolean isImageThumb(String imageUrl) {
        return imageUrl.contains("thumb");
    }

    private void handleOriginalMediaLoaded(Media media) {
        if (!transitionMediaNeedShowThumb && needTransition) {
            mView.startPostponedEnterTransition();
            transitionMediaNeedShowThumb = true;
        }

        if (!media.isLoaded()) {
            media.setLoaded(true);

            mAlreadyLoadedMedias.add(media);
        }

    }

    private void handleThumbLoaded(Context context, View view, Media media) {
        if (mView.isCurrentViewPage(initialPhotoPosition) && needTransition) {
            mView.startPostponedEnterTransition();

            startLoadCurrentImageAfterTransition(context, view, media);

        } else {

            startLoadingOriginalPhoto(context, view, media);
        }

    }

    private void startLoadCurrentImageAfterTransition(final Context context, final View view, final Media media) {
        if (Util.checkRunningOnLollipopOrHigher()) {

            mView.addTransitionListener(new CustomTransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);

                    startLoadingOriginalPhoto(context, view, media);

                }
            });

        } else {
            startLoadingOriginalPhoto(context, view, media);
        }
    }

    private void startLoadingOriginalPhoto(Context context, View view, Media media) {

        mRepository.loadOriginalMediaToGifTouchNetworkImageView(context, media, (GifTouchNetworkImageView) view);

    }

    private void handleTouchEvent(Context context, MotionEvent event, GifTouchNetworkImageView view) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x = event.getRawX();
            y = event.getRawY();
            lastX = x;
            lastY = y;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            lastX = event.getRawX();
            lastY = event.getRawY();

            //Log.i(TAG, "handleTouchEvent: action move lastX" + lastX + " lastY:" + lastY + " y:" + y + " x:" + x);

            if (!view.isZoomed() && lastY > y) {
                view.setTranslationY(lastY - y);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {

            if (Math.abs(lastY - y) + Math.abs(lastX - x) < 10) {

                view.setNeedFitImageToView(false);
                convertEditState();
                toggleFullScreenState();

            } else if (lastY - y > Util.dip2px(context, 30)) {

                view.setNeedFitImageToView(true);
                if (!view.isZoomed())
                    mView.finishActivity();
            } else {

                view.setNeedFitImageToView(true);
                if (!view.isZoomed())
                    view.setTranslationY(0);
            }

        }

    }



    private void toggleFullScreenState() {

        mIsFullScreen = !mIsFullScreen;
        if (mIsFullScreen) {
            mView.hideSystemUI();
        } else {
            mView.showSystemUI();
        }
    }


    private void convertEditState() {
        sInEdit = !sInEdit;
        if (sInEdit) {
            mView.setHeaderVisibility(View.VISIBLE);
            mView.setFooterVisibility(View.VISIBLE);
        } else {
            mView.setHeaderVisibility(View.INVISIBLE);
            mView.setFooterVisibility(View.INVISIBLE);
        }
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

        if (needTransition) {
            mView.finishAfterTransition();
        } else {
            mView.finishActivity();
        }

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
