package com.winsun.fruitmix.viewholder;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Administrator on 2016/11/17.
 */

public abstract class BaseBindingViewHolder extends BindingViewHolder {

    public BaseBindingViewHolder(ViewDataBinding viewDataBinding) {
        super(viewDataBinding);
    }

    public abstract void refreshView(int position);
}
