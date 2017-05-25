package com.winsun.fruitmix.util;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;

/**
 * Created by Administrator on 2017/5/25.
 */

public class AnimatorBuilder {

    private Animator animator;

    public AnimatorBuilder(Context context, int animatorResID, Object target) {

        animator = AnimatorInflater.loadAnimator(context, animatorResID);

        animator.setTarget(target);

    }

    public AnimatorBuilder setDuration(int duration) {
        animator.setDuration(duration);

        return this;
    }

    public AnimatorBuilder addAdapter(AnimatorListenerAdapter animatorListenerAdapter) {
        animator.addListener(animatorListenerAdapter);

        return this;
    }

    public AnimatorBuilder setStartDelay(long startDelay){
        animator.setStartDelay(startDelay);

        return this;
    }

    public Animator getAnimator() {
        return animator;
    }

    public void startAnimator() {
        animator.start();
    }

}
