package com.winsun.fruitmix.anim.imageloadpattern;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;

/**
 * Created by Administrator on 2017/5/27.
 */

public class AnimateColorMatrixUtil {

    public void startAnimation(final Drawable drawable, int duration) {

        AlphaSatColorMatrixEvaluator evaluator = new AlphaSatColorMatrixEvaluator();
        final AnimateColorMatrixColorFilter filter = new AnimateColorMatrixColorFilter(evaluator.getColorMatrix());
        drawable.setColorFilter(filter.getColorFilter());

        ObjectAnimator animator = ObjectAnimator.ofObject(filter, "colorMatrix", evaluator,
                evaluator.getColorMatrix());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                drawable.setColorFilter(filter.getColorFilter());
            }
        });

        animator.setInterpolator(new FastOutSlowInInterpolator());

        animator.setDuration(duration);
        animator.start();
    }

    /// Thanks to @DavidCrawford \
    /// see http://stackoverflow.com/a/27301389/2573335
    private class AnimateColorMatrixColorFilter {
        private ColorMatrixColorFilter mFilter;
        private ColorMatrix mMatrix;

        public AnimateColorMatrixColorFilter(ColorMatrix matrix) {
            setColorMatrix(matrix);
        }

        public ColorMatrixColorFilter getColorFilter() {
            return mFilter;
        }

        public void setColorMatrix(ColorMatrix matrix) {
            mMatrix = matrix;
            mFilter = new ColorMatrixColorFilter(matrix);
        }

        public ColorMatrix getColorMatrix() {
            return mMatrix;
        }
    }

}
