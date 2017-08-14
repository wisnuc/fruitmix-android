package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by Administrator on 2017/8/7.
 */

public class AutoScrollRecyclerView extends RecyclerView {

    public static final String TAG = AutoScrollRecyclerView.class.getSimpleName();

    private int scrollPosition;
    private boolean moved = false;

    public AutoScrollRecyclerView(Context context) {
        super(context);

        init();
    }

    public AutoScrollRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public AutoScrollRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init(){

        addOnScrollListener(new AutoScrollListener());

    }


    public void smoothToPosition(int position){

        if(position < 0 || position > getAdapter().getItemCount()){

            throw new IndexOutOfBoundsException();

        }

        scrollPosition = position;

        stopScroll();

        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();

        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

        if (position <= firstVisibleItemPosition) {
            this.smoothScrollToPosition(position);
        } else if (position <= lastVisibleItemPosition) {
            int top = this.getChildAt(position - firstVisibleItemPosition).getTop();
            this.smoothScrollBy(0, top);
        } else {
            this.smoothScrollToPosition(position);
            moved = true;
        }

    }


    private class AutoScrollListener extends OnScrollListener{

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            LinearLayoutManager gridLayoutManager = (LinearLayoutManager) getLayoutManager();

            if(moved){
                handleMoved(gridLayoutManager);
            }

        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            LinearLayoutManager gridLayoutManager = (LinearLayoutManager) getLayoutManager();

            if (moved && newState == RecyclerView.SCROLL_STATE_IDLE) {
                handleMoved(gridLayoutManager);

            }


        }

        private void handleMoved(LinearLayoutManager linearLayoutManager) {
            moved = false;
            int n = scrollPosition - linearLayoutManager.findFirstVisibleItemPosition();
            if (0 <= n && n < getChildCount()) {
                int top = getChildAt(n).getTop();
                smoothScrollBy(0, top);
            }
        }

    }


}
