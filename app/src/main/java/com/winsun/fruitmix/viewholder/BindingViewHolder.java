package com.winsun.fruitmix.viewholder;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Administrator on 2017/6/27.
 */

public class BindingViewHolder extends RecyclerView.ViewHolder {

    private final ViewDataBinding viewDataBinding;

    public BindingViewHolder(ViewDataBinding viewDataBinding) {
        super(viewDataBinding.getRoot());
        this.viewDataBinding = viewDataBinding;
    }

    public ViewDataBinding getViewDataBinding() {
        return viewDataBinding;
    }
}
