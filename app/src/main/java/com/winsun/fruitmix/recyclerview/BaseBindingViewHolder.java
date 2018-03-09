package com.winsun.fruitmix.recyclerview;

import android.databinding.ViewDataBinding;

/**
 * Created by Administrator on 2016/11/17.
 */

public abstract class BaseBindingViewHolder extends BindingViewHolder {

    public BaseBindingViewHolder(ViewDataBinding viewDataBinding) {
        super(viewDataBinding);
    }

    public abstract void refreshView(int position);
}
