package com.winsun.fruitmix.anim;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.Log;

/**
 * Created by Administrator on 2017/5/27.
 */

public enum ViewPagerTranslation {

    INSTANCE;

    public static final String TAG = ViewPagerTranslation.class.getSimpleName();

    private int oldDragPosition = 0;

    private Animator pagerAnimation;

    public void animatePagerTransition(final boolean forward, final ViewPager viewPager, int duration, int pageCount) {
        // if previous animation have not finished we can get exception
        if (pagerAnimation != null && viewPager.isFakeDragging()) {
            pagerAnimation.cancel();
        }

        if (pageCount == 0)
            return;

        pagerAnimation = getPagerTransitionAnimation(forward, viewPager, duration, pageCount);
        if (viewPager.beginFakeDrag()) {    // checking that started drag correctly
            pagerAnimation.start();
        }
    }

    private Animator getPagerTransitionAnimation(final boolean forward, final ViewPager viewPager, int duration, final int pageCount) {
        final ValueAnimator animator = ValueAnimator.ofInt(0, viewPager.getWidth() - 1);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                Log.d(TAG, "onAnimationEnd: ");

                if (viewPager.isFakeDragging()) {
                    viewPager.endFakeDrag();
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

                Log.d(TAG, "onAnimationCancel: ");

                if (viewPager.isFakeDragging()) {
                    viewPager.endFakeDrag();
                }

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

                Log.d(TAG, "onAnimationRepeat: ");

                viewPager.endFakeDrag();
                oldDragPosition = 0;
                viewPager.beginFakeDrag();
            }
        });

        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int dragPosition = (Integer) animation.getAnimatedValue();
                int dragOffset = dragPosition - oldDragPosition;
                oldDragPosition = dragPosition;
                viewPager.fakeDragBy(dragOffset * (forward ? -1 : 1));
            }
        });

        animator.setDuration(duration / pageCount);
        animator.setRepeatCount(pageCount);

        return animator;
    }


}
