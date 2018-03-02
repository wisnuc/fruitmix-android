package com.winsun.fruitmix.list;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.IImageLoadListener;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.NewPhotoGridlayoutItemBinding;
import com.winsun.fruitmix.databinding.ToolbarLayoutBinding;
import com.winsun.fruitmix.databinding.VideoItemBinding;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.mediaModule.PhotoSliderActivity;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.mediaModule.viewmodel.MediaViewModel;
import com.winsun.fruitmix.mediaModule.viewmodel.PhotoItemViewModel;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.util.Util;
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

    private String stationID;

    private ImageLoader mImageLoader;

    private MediaListAdapter mMediaListAdapter;

    public static final int SPAN_COUNT = 3;

    private int mScreenWidth;
    private int mItemWidth;

    private ToolbarLayoutBinding mToolbarLayoutBinding;

    private Activity containerActivity;

    private boolean mSelectMode = false;

    private List<MediaViewModel> mMediaViewModels;

    private HttpRequestFactory mHttpRequestFactory;

    public MediaListPresenter(ToolbarLayoutBinding toolbarLayoutBinding, MediaComment mediaComment,
                              ImageLoader imageLoader, Activity activity, IPhotoListListener photoListListener) {
        mToolbarLayoutBinding = toolbarLayoutBinding;
        mMedias = mediaComment.getMedias();

        groupUUID = mediaComment.getGroupUUID();

        stationID = mediaComment.getStationID();

        mImageLoader = imageLoader;

        mHttpRequestFactory = InjectHttp.provideHttpRequestFactory(mToolbarLayoutBinding.getRoot().getContext());

        mMediaListAdapter = new MediaListAdapter();

        calcScreenWidth(activity);
        calcPhotoItemWidth(activity);

        containerActivity = activity;

        mPhotoListListener = photoListListener;

        mMediaViewModels = new ArrayList<>();

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

        for (Media media : mMedias) {
            mMediaViewModels.add(new MediaViewModel(media));
        }

        mMediaListAdapter.setMedias(mMediaViewModels);
        mMediaListAdapter.notifyDataSetChanged();

    }

    private MediaListAdapter getMediaListAdapter() {
        return mMediaListAdapter;
    }

    public List<Media> getSelectedMedias() {

        List<Media> selectedMedias = new ArrayList<>();

        for (MediaViewModel mediaViewModel : mMediaViewModels) {
            if (mediaViewModel.isSelected()) {

                Media media = mediaViewModel.getMedia();

                String mediaUUID = media.getUuid();
                if (mediaUUID.isEmpty()) {
                    mediaUUID = Util.calcSHA256OfFile(media.getOriginalPhotoPath());
                    media.setUuid(mediaUUID);
                }

                selectedMedias.add(media);
            }
        }


        return selectedMedias;
    }

    public void setSelectMode(boolean selectMode) {

        mSelectMode = selectMode;

        if (mSelectMode) {
            clearSelectedPhoto();

        }

        mMediaListAdapter.notifyDataSetChanged();
    }

    private void clearSelectedPhoto() {

        if (mMediaViewModels == null || mMediaViewModels.size() == 0)
            return;

        MediaViewModel mediaViewModel;

        for (int i = 0; i < mMediaViewModels.size(); i++) {

            mediaViewModel = mMediaViewModels.get(i);

            if (mediaViewModel != null)
                mediaViewModel.setSelected(false);
        }

    }

    private int mSelectCount;

    private void calcSelectedPhoto() {

        int selectCount = 0;

        for (MediaViewModel mediaViewModel : mMediaViewModels) {
            if (mediaViewModel.isSelected()) {

                selectCount++;

            }

        }

        mSelectCount = selectCount;

    }

    private class MediaListAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<MediaViewModel> mMediaViewModels;

        private static final int VIEW_TYPE_CONTENT = 0x1001;

        private static final int VIEW_TYPE_VIDEO = 0x1002;

        public MediaListAdapter() {
            mMediaViewModels = new ArrayList<>();

            setHasStableIds(true);
        }

        public void setMedias(List<MediaViewModel> mediaViewModels) {

            mMediaViewModels.clear();

            mMediaViewModels.addAll(mediaViewModels);

        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            BindingViewHolder bindingViewHolder;

            NewPhotoGridlayoutItemBinding binding = NewPhotoGridlayoutItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                    parent, false);

            switch (viewType) {

                case VIEW_TYPE_CONTENT:

                    bindingViewHolder = new PhotoHolder(binding);

                    break;
                case VIEW_TYPE_VIDEO:

                    bindingViewHolder = new VideoViewHolder(binding);
                    break;

                default:

                    throw new IllegalArgumentException("onCreateViewHolder,enter default case,some error occur");

            }

            return bindingViewHolder;
        }


        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            if (holder instanceof PhotoHolder) {
                PhotoHolder photoHolder = (PhotoHolder) holder;
                photoHolder.refreshView(mMediaViewModels.get(position), mMediaViewModels);
            } else if (holder instanceof VideoViewHolder) {
                VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
                videoViewHolder.refreshView(mMediaViewModels.get(position), mMediaViewModels);
            }

//            holder.refreshView();

        }

        @Override
        public int getItemCount() {
            return mMediaViewModels.size();
        }

        @Override
        public int getItemViewType(int position) {

            MediaViewModel mediaViewModel = mMediaViewModels.get(position);

            if (mediaViewModel != null) {

                Media media = mediaViewModel.getMedia();

                if (media instanceof Video)
                    return VIEW_TYPE_VIDEO;
                else
                    return VIEW_TYPE_CONTENT;

            } else
                return VIEW_TYPE_CONTENT;


        }
    }

    private class MediaViewHolder extends BindingViewHolder {

        MediaViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        void refreshView(final Media media, final List<MediaViewModel> medias) {

            final NewPhotoGridlayoutItemBinding binding = (NewPhotoGridlayoutItemBinding) getViewDataBinding();

            Context context = binding.getRoot().getContext();

            MediaUtil.setMediaImageUrl(media, binding.photoIv,
                    media.getImageThumbUrl(mHttpRequestFactory, new GroupRequestParam(groupUUID, stationID),""),
                    mImageLoader);

            int temporaryPosition = 0;

            temporaryPosition = getMediaPosition(medias, media);

            setPhotoItemMargin(temporaryPosition, binding.photoItemLayout, context);

            binding.photoItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PhotoSliderActivity.startPhotoSliderActivity(mToolbarLayoutBinding.toolbar, containerActivity, medias,
                            groupUUID, stationID, SPAN_COUNT, binding.photoIv, media);

                }
            });

        }


    }

    private int getMediaPosition(List<MediaViewModel> medias, Media media) {

        int position = 0;
        int size = medias.size();

        for (int i = 0; i < size; i++) {
            Media media1 = medias.get(i).getMedia();

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


    private class BaseMediaHolder extends BindingViewHolder {

        public BaseMediaHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        void checkMediaSelected(MediaViewModel mediaViewModel) {


        }

        PhotoItemViewModel getPhotoItemViewModel(PhotoItemViewModel prePhotoItemViewModel) {

            final PhotoItemViewModel photoItemViewModel;

            if (prePhotoItemViewModel != null) {

                photoItemViewModel = prePhotoItemViewModel;

            } else {

                photoItemViewModel = new PhotoItemViewModel();

            }

            return photoItemViewModel;

        }

        void refreshPhotoSelectImg(MediaViewModel mediaViewModel, PhotoItemViewModel photoItemViewModel) {

            photoItemViewModel.showPhotoSelectImg.set(mediaViewModel.isSelected());

        }

        void resetGIFAndCloudOffIcon(PhotoItemViewModel photoItemViewModel) {

            photoItemViewModel.showGifCorner.set(false);

            photoItemViewModel.showCloudOff.set(false);

        }

        void refreshGIFAndCloudOffIcon(PhotoItemViewModel photoItemViewModel, Media currentMedia) {

            photoItemViewModel.showGifCorner.set(MediaUtil.checkMediaIsGif(currentMedia));

            photoItemViewModel.showCloudOff.set(false);

        }

    }


    public class PhotoHolder extends BaseMediaHolder {

        NetworkImageView mPhotoIv;

        RelativeLayout mImageLayout;

        ImageView photoSelectImg;

        private NewPhotoGridlayoutItemBinding binding;

        private Media currentMedia;

        PhotoHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            binding = (NewPhotoGridlayoutItemBinding) viewDataBinding;

            mPhotoIv = binding.photoIv;

            mImageLayout = binding.photoItemLayout;

            photoSelectImg = binding.photoSelectImg;

        }

        public void refreshView(final MediaViewModel mediaViewModel, final List<MediaViewModel> mediaViewModels) {

            if (mediaViewModel == null) return;

            currentMedia = mediaViewModel.getMedia();

            Context context = binding.getRoot().getContext();

            Log.d(TAG, "PhotoHolder refreshView: media key: " + currentMedia.getKey());

            checkMediaSelected(mediaViewModel);

            PhotoItemViewModel prePhotoItemViewModel = binding.getPhotoItemViewModel();

            final PhotoItemViewModel photoItemViewModel = getPhotoItemViewModel(prePhotoItemViewModel);

            refreshPhotoSelectImg(mediaViewModel, photoItemViewModel);

            binding.setPhotoItemViewModel(photoItemViewModel);

            binding.executePendingBindings();

            mPhotoIv.registerImageLoadListener(new IImageLoadListener() {
                @Override
                public void onImageLoadFinish(String url, View view) {

                    refreshGIFAndCloudOffIcon(photoItemViewModel, currentMedia);

                }

                @Override
                public void onImageLoadFail(String url, View view) {

                }
            });

            MediaUtil.setMediaImageUrl(currentMedia, binding.photoIv,
                    currentMedia.getImageThumbUrl(mHttpRequestFactory, new GroupRequestParam(groupUUID, stationID),""), mImageLoader);

            int temporaryPosition = 0;

            temporaryPosition = getMediaPosition(mediaViewModels, currentMedia);

            setPhotoItemMargin(temporaryPosition, binding.photoItemLayout, context);

            setMediaSelectImg(mPhotoIv, mediaViewModel, photoItemViewModel.showPhotoSelectImg);

//            getViewDataBinding().setVariable(BR.showPhotoSelectImg, showPhotoSelectImg);
//            getViewDataBinding().executePendingBindings();

            mImageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectMode) {

                        handleMediaOnClickWhenSelectMode(mediaViewModel, mPhotoIv, photoItemViewModel.showPhotoSelectImg);

                    } else {

                        PhotoSliderActivity.startPhotoSliderActivity(mToolbarLayoutBinding.toolbar, containerActivity, mediaViewModels,
                                groupUUID, stationID, SPAN_COUNT, binding.photoIv, currentMedia);

                    }

                    Log.d(TAG, "image key:" + currentMedia.getKey());
                }
            });

            mImageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    return handleMediaOnLongClick(mediaViewModel, mPhotoIv, photoItemViewModel.showPhotoSelectImg);

                }
            });

        }

    }

    public class VideoViewHolder extends BaseMediaHolder {

        VideoItemBinding binding;

        ViewGroup viewGroup;

        NetworkImageView networkImageView;

        TextView durationTv;

        VideoViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            binding = (VideoItemBinding) viewDataBinding;

            viewGroup = binding.videoLayout;
            networkImageView = binding.videoNetworkImageview;
            durationTv = binding.duration;
        }

        public void refreshView(final MediaViewModel mediaViewModel, final List<MediaViewModel> mediaViewModels) {

            if (mediaViewModel == null) return;

            final Video video = (Video) mediaViewModel.getMedia();

            checkMediaSelected(mediaViewModel);

            PhotoItemViewModel prePhotoItemViewModel = binding.getPhotoItemViewModel();

            final PhotoItemViewModel photoItemViewModel = getPhotoItemViewModel(prePhotoItemViewModel);

            refreshPhotoSelectImg(mediaViewModel, photoItemViewModel);

            binding.setPhotoItemViewModel(photoItemViewModel);

            binding.executePendingBindings();

            binding.setVideo(video);

            setMediaSelectImg(networkImageView, mediaViewModel, photoItemViewModel.showPhotoSelectImg);

            int temporaryPosition = 0;

            if (mediaViewModels == null) {

                Log.d(TAG, "refreshView: media list is null,currentVideo getDateWithoutHourMinSec:" + video.getDateWithoutHourMinSec());

            } else {

                temporaryPosition = getMediaPosition(mediaViewModels, video);

            }

            final int mediaInListPosition = temporaryPosition;

            setPhotoItemMargin(mediaInListPosition, viewGroup);

            if (video.isLocal() && video.getThumb().isEmpty() && video.getMiniThumbPath().isEmpty())
                return;

            HttpRequest httpRequest;

            httpRequest = video.getImageThumbUrl(mHttpRequestFactory, new GroupRequestParam(groupUUID, stationID),"");

            networkImageView.registerImageLoadListener(new IImageLoadListener() {
                @Override
                public void onImageLoadFinish(String url, View view) {
                    refreshGIFAndCloudOffIcon(photoItemViewModel, video);
                }

                @Override
                public void onImageLoadFail(String url, View view) {

                }
            });

            MediaUtil.setMediaImageUrl(video, networkImageView, httpRequest, mImageLoader);

            viewGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mSelectMode) {

                        handleMediaOnClickWhenSelectMode(mediaViewModel, networkImageView, photoItemViewModel.showPhotoSelectImg);

                    } else {

//                        PlayVideoActivity.startPlayVideoActivity(containerActivity, video);

                        PhotoSliderActivity.startPhotoSliderActivity(mToolbarLayoutBinding.toolbar, containerActivity, mediaViewModels,
                                groupUUID, stationID, SPAN_COUNT, networkImageView, video);

                    }

                }
            });

            viewGroup.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return handleMediaOnLongClick(mediaViewModel, networkImageView, photoItemViewModel.showPhotoSelectImg);
                }
            });

        }

    }


    private void setMediaSelectImg(View view, MediaViewModel mediaViewModel, ObservableBoolean showPhotoSelectImg) {

        if (mSelectMode) {
            boolean selected = mediaViewModel.isSelected();
            if (selected && view.getScaleX() == 1) {
                scalePhoto(view, true);

                showPhotoSelectImg.set(true);
//                    photoSelectImg.setVisibility(View.VISIBLE);

            } else if (!selected && view.getScaleX() != 1) {

                restorePhoto(view, true);

                showPhotoSelectImg.set(false);
//                    photoSelectImg.setVisibility(View.INVISIBLE);

            }
        } else {

            mediaViewModel.setSelected(false);

            if (view.getScaleX() != 1) {
                restorePhoto(view, true);

                showPhotoSelectImg.set(false);
//                    photoSelectImg.setVisibility(View.INVISIBLE);
            }

        }
    }

    private Animator scaleAnimator;

    private void scalePhoto(View view, boolean immediate) {

        if (scaleAnimator != null)
            scaleAnimator.cancel();

        scaleAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.photo_scale);
        scaleAnimator.setTarget(view);

        if (immediate) {
            scaleAnimator.setDuration(0);
        }

        scaleAnimator.start();
    }

    private void restorePhoto(View view, boolean immediate) {

        if (scaleAnimator != null)
            scaleAnimator.cancel();

        scaleAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.photo_restore);
        scaleAnimator.setTarget(view);

        if (immediate) {
            scaleAnimator.setDuration(0);
        }

        scaleAnimator.start();

    }

    private void handleMediaOnClickWhenSelectMode(MediaViewModel mediaViewModel, View view, ObservableBoolean showPhotoSelectImg) {

        calcSelectedPhoto();

        boolean selected = mediaViewModel.isSelected();

        if (!selected && mSelectCount >= Util.MAX_PHOTO_SIZE) {

            Toast.makeText(containerActivity, containerActivity.getString(R.string.max_select_photo), Toast.LENGTH_SHORT).show();

            return;
        }

        selected = !selected;

        if (selected) {
            scalePhoto(view, false);

            showPhotoSelectImg.set(true);

        } else {
            restorePhoto(view, false);

            showPhotoSelectImg.set(false);

        }

        mediaViewModel.setSelected(selected);

        mMediaListAdapter.notifyDataSetChanged();

        calcSelectedPhoto();

        onPhotoItemClick();

    }

    private IPhotoListListener mPhotoListListener;

    private void onPhotoItemClick() {

        if (mPhotoListListener != null)
            mPhotoListListener.onPhotoItemClick(mSelectCount);

    }

    private boolean handleMediaOnLongClick(MediaViewModel mediaViewModel, View view, ObservableBoolean showPhotoSelectImg) {
        if (mSelectMode)
            return true;

        if (mPhotoListListener != null)
            mPhotoListListener.onPhotoItemLongClick();

        mediaViewModel.setSelected(true);

        mSelectCount = 1;

        scalePhoto(view, false);

        showPhotoSelectImg.set(true);

        return true;
    }

    private void setPhotoItemMargin(int mediaInListPosition, ViewGroup viewGroup) {

        int normalMargin = Util.dip2px(containerActivity, 2.5f);

        int height = mItemWidth;

        if ((mediaInListPosition + 1) % SPAN_COUNT == 0) {

            Util.setMarginAndHeight(viewGroup, height, normalMargin, normalMargin, normalMargin, 0);

        } else {

            Util.setMarginAndHeight(viewGroup, height, normalMargin, normalMargin, 0, 0);

        }

    }


}
