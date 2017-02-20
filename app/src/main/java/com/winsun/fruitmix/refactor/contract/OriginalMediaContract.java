package com.winsun.fruitmix.refactor.contract;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.SharedElementCallback;
import android.transition.Transition;
import android.view.MotionEvent;
import android.view.View;

import com.winsun.fruitmix.component.GifTouchNetworkImageView;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface OriginalMediaContract {

    interface OriginalMediaView extends BaseView {

        void setDate(String date);

        void setHeaderVisibility(int visibility);

        void setFooterVisibility(int visibility);

        void setReturnResizeVisibility(int visibility);

        void setReturnResizeOnClickListener(View.OnClickListener listener);

        View findViewWithTag(String tag);

        String getString(int resID);

        void setEnterSharedElementCallback(SharedElementCallback sharedElementCallback);

        void postponeEnterTransition();

        void startPostponedEnterTransition();

        String getImageUrl(boolean isThumb, Media media);

        void setResult(int result, Intent intent);

        void hideSystemUI();

        void showSystemUI();

        void finishActivity();

        void finishAfterTransition();

        boolean isCurrentViewPage(int viewPosition);

        void addTransitionListener(Transition.TransitionListener listener);

        void addOnTouchListener(GifTouchNetworkImageView view,View.OnTouchListener listener);

        void setTransitionName(View view,String transitionName);
    }

    interface OriginalMediaPresenter extends BasePresenter<OriginalMediaView> {

        void setTitle(int position);

        void initView();

        void finishAfterTransition();

        void onSystemUiVisibilityChange(int visibility);

        void onWindowFocusChanged(boolean hasFocus);

        void handleMediaLoaded(Context context,String mediaUrl,Media media,View view);

        void instantiateItem(Context context,int position,GifTouchNetworkImageView view);

        int getMediasCount();

    }

}
