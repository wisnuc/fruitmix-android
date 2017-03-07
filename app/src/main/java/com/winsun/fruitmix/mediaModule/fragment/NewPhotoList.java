package com.winsun.fruitmix.mediaModule.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.anim.BaseAnimationListener;
import com.winsun.fruitmix.mediaModule.CreateAlbumActivity;
import com.winsun.fruitmix.mediaModule.PhotoSliderActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.mediaModule.interfaces.Page;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.mediaModule.model.NewPhotoListDataLoader;
import com.winsun.fruitmix.model.ImageGifLoaderInstance;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.sin3hz.fastjumper.FastJumper;
import io.github.sin3hz.fastjumper.callback.LinearScrollCalculator;
import io.github.sin3hz.fastjumper.callback.SpannableCallback;

/**
 * Created by Administrator on 2016/7/28.
 */
public class NewPhotoList implements Page {

    public static final String TAG = NewPhotoList.class.getSimpleName();

    private Activity containerActivity;
    private View view;

    @BindView(R.id.photo_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout mNoContentLayout;
    @BindView(R.id.no_content_imageview)
    ImageView noContentImageView;

    private int mSpanCount = 3;

    private PhotoRecycleAdapter mPhotoRecycleAdapter;

    private FastJumper mFastJumper;
    private SpannableCallback mJumperCallback;
    private SpannableCallback.ScrollCalculator mLinearScrollCalculator;
    private SpannableCallback.ScrollCalculator mScrollCalculator;
    private RecyclerView.LayoutManager mLayoutManager;

    private int mItemWidth;

    private Map<String, List<Media>> mMapKeyIsDateValueIsPhotoList;

    //TODO use SparseArray or ArrayMap to optimize memory use effect

    private SparseArray<String> mMapKeyIsPhotoPositionValueIsPhotoDate;
    private SparseArray<Media> mMapKeyIsPhotoPositionValueIsPhoto;

    private List<Media> medias;

    private int mScreenWidth;

    private boolean mIsFling = false;

    private boolean mSelectMode = false;

    private List<IPhotoListListener> mPhotoListListeners;

    private int mSelectCount;

    private int mAdapterItemTotalCount;

    private boolean mUseAnim = false;

    private ImageLoader mImageLoader;

    private float mOldSpan = 0;
    private ScaleGestureDetector mPinchScaleDetector;

    private Bundle reenterState;

    private List<String> alreadySelectedImageKeyArrayList;

    public NewPhotoList(Activity activity) {
        containerActivity = activity;

        view = LayoutInflater.from(containerActivity.getApplicationContext()).inflate(R.layout.new_photo_layout, null);

        ButterKnife.bind(this, view);

        noContentImageView.setImageResource(R.drawable.no_photo);

        mPhotoListListeners = new ArrayList<>();

        calcScreenWidth();

        mPinchScaleDetector = new ScaleGestureDetector(containerActivity, new PinchScaleListener());

        NewPhotoListScrollListener mScrollListener = new NewPhotoListScrollListener();
        mRecyclerView.addOnScrollListener(mScrollListener);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mPinchScaleDetector.onTouchEvent(event);

                return false;
            }
        });

        mPhotoRecycleAdapter = new PhotoRecycleAdapter();

        setupFastJumper();
        mRecyclerView.setAdapter(mPhotoRecycleAdapter);
        setupLayoutManager();

    }

    private void initImageLoader() {

        ImageGifLoaderInstance imageGifLoaderInstance = ImageGifLoaderInstance.INSTANCE;
        mImageLoader = imageGifLoaderInstance.getImageLoader(containerActivity);

    }

    public void addPhotoListListener(IPhotoListListener listListener) {
        mPhotoListListeners.add(listListener);
    }

    public void removePhotoListListener(IPhotoListListener listListener) {
        mPhotoListListeners.remove(listListener);
    }

    public void setSelectMode(boolean selectMode) {

        mSelectMode = selectMode;

        if (mSelectMode)
            clearSelectedPhoto();

        mUseAnim = true;

        mPhotoRecycleAdapter.notifyDataSetChanged();
    }

    public void setAlreadySelectedImageKeyArrayList(List<String> alreadySelectedImageKeyArrayList) {
        this.alreadySelectedImageKeyArrayList = alreadySelectedImageKeyArrayList;
    }

    @Override
    public void refreshView() {

        if (Util.isRemoteMediaLoaded() && Util.isLocalMediaInCameraLoaded() && Util.isLocalMediaInDBLoaded()) {

            initImageLoader();

            final NewPhotoListDataLoader loader = NewPhotoListDataLoader.INSTANCE;

            loader.retrieveData(new NewPhotoListDataLoader.OnPhotoListDataListener() {
                @Override
                public void onDataLoadFinished() {
                    doAfterReloadData(loader);
                }
            });
        } else {

            mLoadingLayout.setVisibility(View.VISIBLE);

        }

    }

    private void doAfterReloadData(NewPhotoListDataLoader loader) {

        List<String> mPhotoDateGroups = loader.getmPhotoDateGroups();
        mAdapterItemTotalCount = loader.getmAdapterItemTotalCount();
        mMapKeyIsDateValueIsPhotoList = loader.getmMapKeyIsDateValueIsPhotoList();
        mMapKeyIsPhotoPositionValueIsPhotoDate = loader.getmMapKeyIsPhotoPositionValueIsPhotoDate();
        mMapKeyIsPhotoPositionValueIsPhoto = loader.getmMapKeyIsPhotoPositionValueIsPhoto();
        medias = loader.getMedias();

        mLoadingLayout.setVisibility(View.GONE);
        if (mPhotoDateGroups.size() == 0) {
            mNoContentLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);

            for (IPhotoListListener listener : mPhotoListListeners)
                listener.onNoPhotoItem(true);

        } else {
            mNoContentLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mPhotoRecycleAdapter.notifyDataSetChanged();

            for (IPhotoListListener listener : mPhotoListListeners)
                listener.onNoPhotoItem(false);
        }

        clearSelectedPhoto();

    }

    private void setupFastJumper() {
        mLinearScrollCalculator = new LinearScrollCalculator(mRecyclerView) {

            @Override
            public int getItemHeight(int position) {
                return mPhotoRecycleAdapter.getItemHeight(position);
            }

            @Override
            public int getSpanSize(int position) {
                return mPhotoRecycleAdapter.getSpanSize(position);
            }

            @Override
            public int getSpanCount() {
                return mSpanCount;
            }
        };

        mJumperCallback = new SpannableCallback() {
            @Override
            public boolean isSectionEnable() {
                return true;
            }

            @Override
            public String getSection(int position) {
                return mPhotoRecycleAdapter.getSectionForPosition(position);
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        mFastJumper = new FastJumper(mJumperCallback);

    }

    private void setupGridLayoutManager() {
        calcPhotoItemWidth();
        GridLayoutManager glm = new GridLayoutManager(containerActivity, mSpanCount);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mPhotoRecycleAdapter.getSpanSize(position);
            }
        });
        mLayoutManager = glm;
        mScrollCalculator = mLinearScrollCalculator;
    }

    private void setupLayoutManager() {
        mFastJumper.attachToRecyclerView(null);
        setupGridLayoutManager();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mJumperCallback.setScrollCalculator(mScrollCalculator);
        mFastJumper.attachToRecyclerView(mRecyclerView);
        mFastJumper.invalidate();
    }

    private void calcScreenWidth() {

        mScreenWidth = Util.calcScreenWidth(containerActivity);
    }

    private void calcPhotoItemWidth() {
        mItemWidth = mScreenWidth / mSpanCount - Util.dip2px(containerActivity, 5);
    }

    @Override
    public View getView() {
        return view;
    }

    public List<String> getSelectedImageThumbs() {

        List<String> selectedImageThumbs = new ArrayList<>();

        for (List<Media> mediaList : mMapKeyIsDateValueIsPhotoList.values()) {
            for (Media media : mediaList) {
                if (media.isSelected()) {

                    String mediaThumb = media.getThumb();

                    if (mediaThumb.length() != 0)
                        selectedImageThumbs.add(mediaThumb);
                }
            }
        }

        return selectedImageThumbs;
    }

    @NonNull
    public List<String> getSelectedImageUUIDs() {

        List<String> selectedImageUUIDs = new ArrayList<>();

        for (List<Media> mediaList : mMapKeyIsDateValueIsPhotoList.values()) {
            for (Media media : mediaList) {
                if (media.isSelected()) {

                    String mediaUUID = media.getUuid();
                    if (mediaUUID.isEmpty()) {
                        mediaUUID = Util.CalcSHA256OfFile(media.getThumb());
                    }

                    selectedImageUUIDs.add(mediaUUID);
                }
            }
        }

        if (alreadySelectedImageKeyArrayList != null) {
            for (String mediaUUID : alreadySelectedImageKeyArrayList) {
                if (!selectedImageUUIDs.contains(mediaUUID)) {
                    selectedImageUUIDs.add(mediaUUID);
                }
            }
        }

        return selectedImageUUIDs;
    }

    public void clearSelectedPhoto() {

        if (mMapKeyIsPhotoPositionValueIsPhoto == null || mMapKeyIsPhotoPositionValueIsPhoto.size() == 0)
            return;

        for (List<Media> mediaList : mMapKeyIsDateValueIsPhotoList.values()) {
            for (Media media : mediaList) {
                media.setSelected(false);
            }
        }
    }

    private void calcSelectedPhoto() {

        mSelectCount = 0;

        for (List<Media> mediaList : mMapKeyIsDateValueIsPhotoList.values()) {
            for (Media media : mediaList) {
                if (media.isSelected())
                    mSelectCount++;
            }
        }

    }

    public void createAlbum(List<String> selectKeys) {
        Intent intent = new Intent();
        intent.setClass(containerActivity, CreateAlbumActivity.class);

        LocalCache.mediaKeysInCreateAlbum.addAll(selectKeys);

        containerActivity.startActivityForResult(intent, Util.KEY_CREATE_ALBUM_REQUEST_CODE);

        clearSelectedPhoto();
    }

    public void createShare(List<String> selectMediaKeys) {

        FNAS.createRemoteMediaShare(containerActivity, createMediaShare(selectMediaKeys));

        clearSelectedPhoto();
    }

    private MediaShare createMediaShare(List<String> selectMediaKeys) {

        MediaShare mediaShare = new MediaShare();
        mediaShare.setUuid(Util.createLocalUUid());

        List<MediaShareContent> mediaShareContents = new ArrayList<>();

        for (String mediaKey : selectMediaKeys) {
            MediaShareContent mediaShareContent = new MediaShareContent();
            mediaShareContent.setMediaUUID(mediaKey);
            mediaShareContent.setAuthor(FNAS.userUUID);
            mediaShareContent.setTime(String.valueOf(System.currentTimeMillis()));
            mediaShareContents.add(mediaShareContent);

        }

        mediaShare.initMediaShareContents(mediaShareContents);

        mediaShare.setCoverImageUUID(selectMediaKeys.get(0));
        mediaShare.setTitle("");
        mediaShare.setDesc("");
        for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
            mediaShare.addViewer(userUUID);
        }
        mediaShare.addMaintainer(FNAS.userUUID);
        mediaShare.setCreatorUUID(FNAS.userUUID);
        mediaShare.setTime(String.valueOf(System.currentTimeMillis()));
        mediaShare.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(mediaShare.getTime()))));
        mediaShare.setArchived(false);
        mediaShare.setAlbum(false);
        mediaShare.setLocal(true);

        return mediaShare;

    }

    @Override
    public void onDidAppear() {

        refreshView();

    }

    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

        if (reenterState != null) {

            int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
            int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);
            String currentMediaKey = reenterState.getString(Util.CURRENT_MEDIA_KEY);

            if (initialPhotoPosition != currentPhotoPosition) {

                names.clear();
                sharedElements.clear();

                Media media = null;
                Media currentMedia = null;

                int size = mMapKeyIsPhotoPositionValueIsPhoto.size();

                for (int i = 0; i < size; i++) {
                    media = mMapKeyIsPhotoPositionValueIsPhoto.valueAt(i);
                    if (media.getKey().equals(currentMediaKey))
                        currentMedia = media;
                }

                if (currentMedia == null) return;

                View newSharedElement = mRecyclerView.findViewWithTag(findPhotoTag(currentMedia));
                String sharedElementName = currentMedia.getKey();

                names.add(sharedElementName);
                sharedElements.put(sharedElementName, newSharedElement);
            }

        }
        reenterState = null;

    }

    public void onActivityReenter(int resultCode, Intent data) {
        reenterState = new Bundle(data.getExtras());
        int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
        int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);
        String currentMediaKey = reenterState.getString(Util.CURRENT_MEDIA_KEY);

        if (initialPhotoPosition != currentPhotoPosition) {

            int scrollToPosition = 0;

            Media media = null;

            int size = mMapKeyIsPhotoPositionValueIsPhoto.size();

            for (int i = 0; i < size; i++) {
                media = mMapKeyIsPhotoPositionValueIsPhoto.valueAt(i);
                if (media.getKey().equals(currentMediaKey))
                    scrollToPosition = mMapKeyIsPhotoPositionValueIsPhoto.keyAt(i);
            }

            mRecyclerView.smoothScrollToPosition(scrollToPosition);

//            ActivityCompat.startPostponedEnterTransition(containerActivity);

            ActivityCompat.postponeEnterTransition(containerActivity);
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
                    mRecyclerView.requestLayout();
                    ActivityCompat.startPostponedEnterTransition(containerActivity);

                    return true;
                }
            });
        }

    }

    private String findPhotoTag(Media media) {
        return media.getImageThumbUrl(containerActivity);
    }

    private class PhotoRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_HEAD = 0;
        private static final int VIEW_TYPE_CONTENT = 1;

        private int mSubHeaderHeight = containerActivity.getResources().getDimensionPixelSize(R.dimen.photo_title_height);

        PhotoRecycleAdapter() {
            setHasStableIds(true);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int type = getItemViewType(position);
            switch (type) {
                case VIEW_TYPE_HEAD:
                    PhotoGroupHolder groupHolder = (PhotoGroupHolder) holder;
                    groupHolder.refreshView(position);
                    break;
                case VIEW_TYPE_CONTENT:
                    PhotoHolder photoHolder = (PhotoHolder) holder;
                    photoHolder.refreshView(position);
                    break;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_HEAD: {
                    View view = LayoutInflater.from(containerActivity).inflate(R.layout.new_photo_title_item, parent, false);
                    return new PhotoGroupHolder(view);
                }
                case VIEW_TYPE_CONTENT: {
                    View view = LayoutInflater.from(containerActivity).inflate(R.layout.new_photo_gridlayout_item, parent, false);
                    return new PhotoHolder(view);
                }
            }

            return null;
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            super.onViewRecycled(holder);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
            return super.onFailedToRecycleView(holder);
        }

        @Override
        public int getItemCount() {
            return mAdapterItemTotalCount;
        }

        @Override
        public int getItemViewType(int position) {
            if (mMapKeyIsPhotoPositionValueIsPhotoDate.indexOfKey(position) >= 0)
                return VIEW_TYPE_HEAD;
            else
                return VIEW_TYPE_CONTENT;
        }

        int getSpanSize(int position) {
            if (getItemViewType(position) == VIEW_TYPE_HEAD) {
                return mSpanCount;
            } else {
                return 1;
            }
        }

        int getItemHeight(int position) {
            if (getItemViewType(position) == VIEW_TYPE_HEAD) {
                return mItemWidth;
            } else {
                return mSubHeaderHeight;
            }
        }

        String getSectionForPosition(int position) {
            String title;

            if (position < 0) {
                position = 0;
            }
            if (position >= getItemCount()) {
                position = getItemCount() - 1;
            }

            if (mMapKeyIsPhotoPositionValueIsPhotoDate.indexOfKey(position) >= 0)
                title = mMapKeyIsPhotoPositionValueIsPhotoDate.get(position);
            else {
                title = mMapKeyIsPhotoPositionValueIsPhoto.get(position).getDate();
            }

            if (title.contains("1916-01-01")) {
                return containerActivity.getString(R.string.unknown_time);
            } else {
                String[] titleSplit = title.split("-");
                return titleSplit[0] + "年" + titleSplit[1] + "月";
            }

        }
    }

    class PhotoGroupHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.photo_group_tv)
        TextView mPhotoTitle;

        @BindView(R.id.photo_title_select_img)
        ImageView mPhotoTitleSelectImg;

        @BindView(R.id.photo_title_layout)
        LinearLayout mPhotoTitleLayout;

        @BindView(R.id.spacing_layout)
        View mSpacingLayout;

        @BindView(R.id.spacing_second_layout)
        View mSpacingSecondLayout;

        PhotoGroupHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void refreshView(int groupPosition) {

            final String date = mMapKeyIsPhotoPositionValueIsPhotoDate.get(groupPosition);

            if (groupPosition == 0) {
                mSpacingLayout.setVisibility(View.GONE);
                mSpacingSecondLayout.setVisibility(View.GONE);
            } else {
                mSpacingLayout.setVisibility(View.VISIBLE);
                mSpacingSecondLayout.setVisibility(View.VISIBLE);
            }

            if (date.equals("1916-01-01")) {
                mPhotoTitle.setText(containerActivity.getString(R.string.unknown_time));
            } else {
                mPhotoTitle.setText(date);
            }

            if (mSelectMode) {

                if (!isPhotoTitleSelectImgVisible()) {
                    if (mUseAnim) {
                        showPhotoTitleSelectImgAnim();
                    } else
                        showPhotoTitleSelectImg();
                }

                List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(date);
                int selectNum = 0;
                for (Media media : mediaList) {
                    if (media.isSelected())
                        selectNum++;
                }
                if (selectNum == mediaList.size())
                    mPhotoTitleSelectImg.setSelected(true);
                else
                    mPhotoTitleSelectImg.setSelected(false);

            } else {

                if (isPhotoTitleSelectImgVisible()) {
                    if (mUseAnim) {
                        dismissPhotoTitleSelectImgAnim();
                    } else
                        dismissPhotoTitleSelectImg();
                }
            }


            mPhotoTitleLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectMode) {

                        List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(date);
                        boolean selected = mPhotoTitleSelectImg.isSelected();

                        int unSelectNumInList = 0;

                        for (Media media : mediaList) {
                            if (!media.isSelected())
                                unSelectNumInList++;
                        }

                        calcSelectedPhoto();

                        if (!selected && unSelectNumInList + mSelectCount > Util.MAX_PHOTO_SIZE) {
                            Toast.makeText(containerActivity, containerActivity.getString(R.string.max_select_photo), Toast.LENGTH_SHORT).show();

                            int newSelectItemCount = 0;
                            int newSelectItemTotalCount = Util.MAX_PHOTO_SIZE - mSelectCount;
                            if (newSelectItemTotalCount != 0) {

                                for (Media media : mediaList) {

                                    if (!media.isSelected()) {
                                        media.setSelected(true);
                                        newSelectItemCount++;
                                        if (newSelectItemCount == newSelectItemTotalCount)
                                            break;
                                    }

                                }
                            }

                        } else {
                            mPhotoTitleSelectImg.setSelected(!selected);

                            for (Media media : mediaList)
                                media.setSelected(!selected);
                        }

                        mPhotoRecycleAdapter.notifyDataSetChanged();

                        calcSelectedPhoto();

                        for (IPhotoListListener listListener : mPhotoListListeners) {
                            listListener.onPhotoItemClick(mSelectCount);
                        }
                    }
                }
            });

        }

        private boolean isPhotoTitleSelectImgVisible() {
            return mPhotoTitleSelectImg.getVisibility() == View.VISIBLE;
        }

        private void showPhotoTitleSelectImgAnim() {
            mPhotoTitleSelectImg.setVisibility(View.VISIBLE);

            Animation animation = AnimationUtils.loadAnimation(containerActivity, R.anim.show_right_item_anim);

            mPhotoTitleSelectImg.startAnimation(animation);

        }

        private void dismissPhotoTitleSelectImgAnim() {
            Animation animation = AnimationUtils.loadAnimation(containerActivity, R.anim.dismiss_right_item_anim);
            animation.setAnimationListener(new BaseAnimationListener() {

                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);

                    mPhotoTitleSelectImg.setVisibility(View.GONE);

                }
            });

            mPhotoTitleSelectImg.startAnimation(animation);

        }

        private void showPhotoTitleSelectImg() {
            mPhotoTitleSelectImg.setVisibility(View.VISIBLE);
        }

        private void dismissPhotoTitleSelectImg() {
            mPhotoTitleSelectImg.setVisibility(View.GONE);
        }

    }

    class PhotoHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.photo_iv)
        NetworkImageView mPhotoIv;

        @BindView(R.id.photo_item_layout)
        RelativeLayout mImageLayout;

        @BindView(R.id.photo_select_img)
        ImageView mPhotoSelectedIv;

        PhotoHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void refreshView(int position) {

            final Media currentMedia = mMapKeyIsPhotoPositionValueIsPhoto.get(position);

            if (currentMedia == null) return;

            // because alreadySelectedImageKeyArrayList is media share content,it must be uuid about media now
            if (alreadySelectedImageKeyArrayList != null && alreadySelectedImageKeyArrayList.contains(currentMedia.getUuid()))
                currentMedia.setSelected(true);

            mImageLoader.setTag(position);

            if (!mIsFling) {
                String imageUrl = currentMedia.getImageThumbUrl(containerActivity);
                mImageLoader.setShouldCache(!currentMedia.isLocal());

                if (currentMedia.isLocal())
                    mPhotoIv.setOrientationNumber(currentMedia.getOrientationNumber());

                mPhotoIv.setBackgroundResource(R.drawable.placeholder_photo);

                mPhotoIv.setTag(imageUrl);
                mPhotoIv.setDefaultImageResId(R.drawable.placeholder_photo);
                mPhotoIv.setImageUrl(imageUrl, mImageLoader);
            } else {
                mPhotoIv.setDefaultImageResId(R.drawable.placeholder_photo);
                mPhotoIv.setImageUrl(null, mImageLoader);
            }

            List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(currentMedia.getDate());
            int mediaInListPosition = getPosition(mediaList, currentMedia);

            setPhotoItemMargin(mediaInListPosition);

            if (mSelectMode) {
                boolean selected = currentMedia.isSelected();
                if (selected) {
                    int selectMargin = Util.dip2px(containerActivity, 20);
                    setPhotoIvLayoutParams(selectMargin);
                    mPhotoSelectedIv.setVisibility(View.VISIBLE);
                } else {
                    setPhotoIvLayoutParams(0);
                    mPhotoSelectedIv.setVisibility(View.GONE);
                }
            } else {

                currentMedia.setSelected(false);

                setPhotoIvLayoutParams(0);
                mPhotoSelectedIv.setVisibility(View.GONE);
            }

            mImageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectMode) {

                        if (!currentMedia.isSharing()) {
                            Toast.makeText(containerActivity, containerActivity.getString(R.string.photo_not_sharing), Toast.LENGTH_SHORT).show();
                            return;
                        } else if (alreadySelectedImageKeyArrayList != null && alreadySelectedImageKeyArrayList.contains(currentMedia.getUuid())) {
                            Toast.makeText(containerActivity, containerActivity.getString(R.string.already_select_media), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        calcSelectedPhoto();

                        boolean selected = currentMedia.isSelected();

                        if (!selected && mSelectCount >= Util.MAX_PHOTO_SIZE) {

                            Toast.makeText(containerActivity, containerActivity.getString(R.string.max_select_photo), Toast.LENGTH_SHORT).show();

                            return;
                        }

                        if (selected) {
                            int selectMargin = Util.dip2px(containerActivity, 20);
                            setPhotoIvLayoutParams(selectMargin);
                            mPhotoSelectedIv.setVisibility(View.VISIBLE);
                        } else {
                            setPhotoIvLayoutParams(0);
                            mPhotoSelectedIv.setVisibility(View.GONE);
                        }

                        currentMedia.setSelected(!selected);
                        mPhotoRecycleAdapter.notifyDataSetChanged();

                        calcSelectedPhoto();

                        for (IPhotoListListener listListener : mPhotoListListeners) {
                            listListener.onPhotoItemClick(mSelectCount);
                        }

                    } else {

                        int initialPhotoPosition;

                        int size = medias.size();

                        for (initialPhotoPosition = 0; initialPhotoPosition < size; initialPhotoPosition++) {

                            Media media = medias.get(initialPhotoPosition);

                            if (media.getKey().equals(currentMedia.getKey()))
                                break;

                        }

                        PhotoSliderActivity.setMediaList(medias);

                        Intent intent = new Intent();
                        intent.putExtra(Util.INITIAL_PHOTO_POSITION, initialPhotoPosition);
                        intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, false);
                        intent.setClass(containerActivity, PhotoSliderActivity.class);

                        if (mPhotoIv.isLoaded()) {

                            ViewCompat.setTransitionName(mPhotoIv, currentMedia.getKey());

                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, mPhotoIv, currentMedia.getKey());

                            containerActivity.startActivity(intent, options.toBundle());
                        } else {

                            intent.putExtra(Util.KEY_NEED_TRANSITION, false);
                            containerActivity.startActivity(intent);

                        }

                    }

                    Log.d(TAG, "image key:" + currentMedia.getKey());
                }
            });

            mImageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    for (IPhotoListListener listListener : mPhotoListListeners) {
                        listListener.onPhotoItemLongClick();
                    }

                    currentMedia.setSelected(true);

                    mSelectCount = 1;

                    return true;
                }
            });

        }

        private void setPhotoItemMargin(int mediaInListPosition) {
            GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) mImageLayout.getLayoutParams();

            params.height = mItemWidth;

            int normalMargin = Util.dip2px(containerActivity, 2.5f);

            if ((mediaInListPosition + 1) % mSpanCount == 0) {
                params.setMargins(normalMargin, normalMargin, normalMargin, 0);
            } else {
                params.setMargins(normalMargin, normalMargin, 0, 0);
            }

            mImageLayout.setLayoutParams(params);
        }

        private void setPhotoIvLayoutParams(int margin) {
            RelativeLayout.LayoutParams photoParams = (RelativeLayout.LayoutParams) mPhotoIv.getLayoutParams();
            photoParams.setMargins(margin, margin, margin, margin);
            mPhotoIv.setLayoutParams(photoParams);
        }

        private int getPosition(List<Media> mediaList, Media media) {

            int position = 0;
            int size = mediaList.size();

            for (int i = 0; i < size; i++) {
                Media media1 = mediaList.get(i);

                if (media.getKey().equals(media1.getKey())) {
                    position = i;
                }
            }
            return position;
        }
    }

    private class NewPhotoListScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

/*            if (newState == RecyclerView.SCROLL_STATE_SETTLING) {

                mIsFling = true;

            } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                if (mIsFling) {

                    mIsFling = false;

                    mPhotoRecycleAdapter.notifyDataSetChanged();
                }

            }*/
        }

    }

    private class PinchScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private int mSpanMaxCount = 6;
        private int mSpanMinCount = 2;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            mOldSpan = detector.getCurrentSpan();

            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            float newSpan = detector.getCurrentSpan();

            if (newSpan > mOldSpan + Util.dip2px(containerActivity, 2)) {

                if (mSpanCount > mSpanMinCount) {
                    mSpanCount--;
                    calcPhotoItemWidth();
                    NewPhotoListDataLoader.INSTANCE.calcPhotoPositionNumber();
                    ((GridLayoutManager) mLayoutManager).setSpanCount(mSpanCount);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mPhotoRecycleAdapter.notifyItemRangeChanged(0, mPhotoRecycleAdapter.getItemCount());
                }

                Log.i(TAG, "pinch more");

            } else if (mOldSpan > newSpan + Util.dip2px(containerActivity, 2)) {

                if (mSpanCount < mSpanMaxCount) {
                    mSpanCount++;
                    calcPhotoItemWidth();
                    NewPhotoListDataLoader.INSTANCE.calcPhotoPositionNumber();
                    ((GridLayoutManager) mLayoutManager).setSpanCount(mSpanCount);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mPhotoRecycleAdapter.notifyItemRangeChanged(0, mPhotoRecycleAdapter.getItemCount());
                }

                Log.i(TAG, "pinch less");
            }

        }
    }

}
