package com.winsun.fruitmix;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GalleryTestActivity extends Activity {

    RecyclerView mRecyclerView;

    FloatingActionButton mBtn;

    private int mSpanCount = 3;
    private GridLayoutManager mManager;

    private Context mContext;

    private MyRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_test);

        mRecyclerView = findViewById(R.id.recyclerView);
        mBtn = findViewById(R.id.floatingactionbtn);

        mManager = new GridLayoutManager(this, mSpanCount);
        mRecyclerView.setLayoutManager(mManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mContext = this;

        mAdapter = new MyRecyclerAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpanCount++;
                mManager.setSpanCount(mSpanCount);
                mAdapter.notifyItemChanged(0,12);
            }
        });
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(View view) {
            super(view);
        }
    }

    class MyRecyclerAdapter extends RecyclerView.Adapter<MyViewHolder> {
        @Override
        public int getItemCount() {
            return 13;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.gallery_item, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);

            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
        }
    }

}
