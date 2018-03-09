package com.winsun.fruitmix.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Administrator on 2016/11/17.
 */

public abstract class BaseRecyclerViewHolder extends RecyclerView.ViewHolder {

    public BaseRecyclerViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void refreshView(int position);
}
