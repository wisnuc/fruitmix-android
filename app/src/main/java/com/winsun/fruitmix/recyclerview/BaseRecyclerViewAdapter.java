package com.winsun.fruitmix.recyclerview;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/9.
 */

public abstract class BaseRecyclerViewAdapter<T extends RecyclerView.ViewHolder, E> extends RecyclerView.Adapter<T> {

    protected List<E> mItemList;

    public BaseRecyclerViewAdapter() {
        mItemList = new ArrayList<>();
    }

    public void setItemList(List<E> itemList) {
        mItemList.clear();
        mItemList.addAll(itemList);
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }
}
