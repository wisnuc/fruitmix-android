package com.winsun.fruitmix.list;

import android.app.Activity;
import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.databinding.ActivityBaseToolbarBinding;
import com.winsun.fruitmix.databinding.ActivityMediaListBinding;
import com.winsun.fruitmix.databinding.NewPhotoGridlayoutItemBinding;
import com.winsun.fruitmix.databinding.SinglePhotoBinding;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.mediaModule.PhotoSliderActivity;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.viewmodel.MediaViewModel;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BaseBindingViewHolder;
import com.winsun.fruitmix.viewholder.BaseRecyclerViewHolder;
import com.winsun.fruitmix.viewholder.BindingViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/18.
 */

public class MediaListPresenter {

    public static final String TAG = MediaListPresenter.class.getSimpleName();

    private List<Media> mMedias;

    private String groupUUID;

    private ImageLoader mImageLoader;

    private MediaListAdapter mMediaListAdapter;

    public static final int SPAN_COUNT = 3;

    private int mScreenWidth;
    private int mItemWidth;

    private ActivityBaseToolbarBinding mActivityBaseToolbarBinding;

    private Activity containerActivity;

    public MediaListPresenter(ActivityBaseToolbarBinding activityBaseToolbarBinding, MediaComment mediaComment, ImageLoader imageLoader, Activity activity) {
        mActivityBaseToolbarBinding = activityBaseToolbarBinding;
        mMedias = mediaComment.getMedias();
        groupUUID = mediaComment.getGroupUUID();
        mImageLoader = imageLoader;

        mMediaListAdapter = new MediaListAdapter();

        calcScreenWidth(activity);
        calcPhotoItemWidth(activity);

        containerActivity = activity;

    }

    public void onDestroy() {

        containerActivity = null;

    }


    private void calcScreenWidth(Activity activity) {

        mScreenWidth = Util.calcScreenWidth(activity);
    }

    private void calcPhotoItemWidth(Context context) {
        mItemWidth = mScreenWidth / SPAN_COUNT - Util.dip2px(context, 5);
    }

    public void refreshView(Context context, RecyclerView recyclerView) {

        recyclerView.setLayoutManager(new GridLayoutManager(context, SPAN_COUNT));

        recyclerView.setAdapter(getMediaListAdapter());

        mMediaListAdapter.setMedias(mMedias);
        mMediaListAdapter.notifyDataSetChanged();

    }

    private MediaListAdapter getMediaListAdapter() {
        return mMediaListAdapter;
    }

    private class MediaListAdapter extends RecyclerView.Adapter<MediaViewHolder> {

        private List<Media> mMedias;

        public MediaListAdapter() {
            mMedias = new ArrayList<>();
        }

        public void setMedias(List<Media> medias) {
            mMedias.clear();
            mMedias.addAll(medias);
        }

        @Override
        public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            NewPhotoGridlayoutItemBinding binding = NewPhotoGridlayoutItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                    parent, false);

            return new MediaViewHolder(binding);
        }


        @Override
        public void onBindViewHolder(MediaViewHolder holder, int position) {

            holder.refreshView(mMedias.get(position), mMedias);

        }

        @Override
        public int getItemCount() {
            return mMedias.size();
        }
    }

    private class MediaViewHolder extends BindingViewHolder {

        MediaViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        void refreshView(final Media media, final List<Media> medias) {

            final NewPhotoGridlayoutItemBinding binding = (NewPhotoGridlayoutItemBinding) getViewDataBinding();

            Context context = binding.getRoot().getContext();

            MediaUtil.setMediaImageUrl(media, binding.photoIv, media.getImageThumbUrl(context, groupUUID), mImageLoader);

            int temporaryPosition = 0;

            temporaryPosition = getMediaPosition(medias, media);

            setPhotoItemMargin(temporaryPosition, binding.photoItemLayout, context);

            binding.photoItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PhotoSliderActivity.startPhotoSliderActivity(mActivityBaseToolbarBinding.toolbarLayout.toolbar,containerActivity,medias,
                            groupUUID,SPAN_COUNT,binding.photoIv,media);

                }
            });

        }


    }

    private int getMediaPosition(List<Media> medias, Media media) {

        int position = 0;
        int size = medias.size();

        for (int i = 0; i < size; i++) {
            Media media1 = medias.get(i);

            if (media.getKey().equals(media1.getKey())) {
                position = i;
                break;
            }
        }
        return position;
    }

    private void setPhotoItemMargin(int mediaInListPosition, ViewGroup viewGroup, Context context) {

        int normalMargin = Util.dip2px(context, 2.5f);

        int height = mItemWidth;

        if ((mediaInListPosition + 1) % SPAN_COUNT == 0) {

            Util.setMarginAndHeight(viewGroup, height, normalMargin, normalMargin, normalMargin, 0);

        } else {

            Util.setMarginAndHeight(viewGroup, height, normalMargin, normalMargin, 0, 0);

        }

    }


}
