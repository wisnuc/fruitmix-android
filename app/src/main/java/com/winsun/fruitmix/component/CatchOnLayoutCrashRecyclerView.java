package com.winsun.fruitmix.component;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by Administrator on 2017/9/6.
 */

public class CatchOnLayoutCrashRecyclerView extends RecyclerView {

    public CatchOnLayoutCrashRecyclerView(Context context) {
        super(context);
    }

    public CatchOnLayoutCrashRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CatchOnLayoutCrashRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        try {
            super.onLayout(changed, l, t, r, b);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
