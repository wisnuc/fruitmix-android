package com.winsun.fruitmix.util;

import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Administrator on 2017/7/27.
 */

@BindingMethods({
        @BindingMethod(type = View.class,
                attribute = "android:background",
                method = "setBackgroundResource"),

        @BindingMethod(type = ImageView.class,
                attribute = "android:src",
                method = "setImageResource"),
})

public class DataBindingSetting {

    @BindingAdapter("android:layout_marginLeft")
    public static void setLayoutMarginLeft(View view, float leftMargin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(Math.round(leftMargin), layoutParams.topMargin,
                layoutParams.rightMargin, layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginRight")
    public static void setLayoutMarginRight(View view, float rightMargin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                Math.round(rightMargin), layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginTop")
    public static void setLayoutMarginTop(View view, float topMargin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, Math.round(topMargin),
                layoutParams.rightMargin, layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginBottom")
    public static void setLayoutMarginBottom(View view, float bottomMargin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                layoutParams.rightMargin, Math.round(bottomMargin));
        view.setLayoutParams(layoutParams);
    }

    @BindingConversion
    public static ColorDrawable convertColorToDrawable(int color) {
        return new ColorDrawable(color);
    }

}
