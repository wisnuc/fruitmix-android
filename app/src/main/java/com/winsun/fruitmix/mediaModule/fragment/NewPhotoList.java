package com.winsun.fruitmix.mediaModule.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.mediaModule.CreateAlbumActivity;
import com.winsun.fruitmix.mediaModule.PhotoSliderActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.mediaModule.interfaces.OnMediaFragmentInteractionListener;
import com.winsun.fruitmix.mediaModule.interfaces.Page;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

    private OnMediaFragmentInteractionListener listener;

    private Activity containerActivity;
    private View view;

    @BindView(R.id.photo_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout mNoContentLayout;

    private int mSpanCount = 3;

    private PhotoRecycleAdapter mPhotoRecycleAdapter;

    private FastJumper mFastJumper;
    private SpannableCallback mJumperCallback;
    private SpannableCallback.ScrollCalculator mLinearScrollCalculator;
    private SpannableCallback.ScrollCalculator mScrollCalculator;
    private RecyclerView.LayoutManager mLayoutManager;

    private int mItemWidth;

    private List<String> mPhotoDateGroups;

    private Map<String, List<Media>> mMapKeyIsDateValueIsPhotoList;

    private Map<Integer, String> mMapKeyIsPhotoPositionValueIsPhotoDate;
    private Map<Integer, Media> mMapKeyIsPhotoPositionValueIsPhoto;

    private int mScreenWidth;

    private boolean mIsFling = false;

    private boolean mSelectMode = false;

    private List<IPhotoListListener> mPhotoListListeners;

    private int mSelectCount;

    private int mAdapterItemTotalCount;

    private boolean mHasFlung = false;

    private ImageLoader mImageLoader;

    private float mOldSpan = 0;
    private ScaleGestureDetector mPinchScaleDetector;

    private Bundle reenterState;

    private List<String> alreadySelectedImageUUIDArrayList;

    public NewPhotoList(Activity activity, OnMediaFragmentInteractionListener listener) {
        containerActivity = activity;

        this.listener = listener;

        view = View.inflate(containerActivity, R.layout.new_photo_layout, null);

        ButterKnife.bind(this, view);

        initImageLoader();

        mPhotoDateGroups = new ArrayList<>();
        mMapKeyIsDateValueIsPhotoList = new HashMap<>();

        mMapKeyIsPhotoPositionValueIsPhotoDate = new HashMap<>();
        mMapKeyIsPhotoPositionValueIsPhoto = new HashMap<>();

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
        RequestQueue mRequestQueue = RequestQueueInstance.getInstance(containerActivity).getRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);
    }

    public void addPhotoListListener(IPhotoListListener listListener) {
        mPhotoListListeners.add(listListener);
    }

    public void removePhotoListListener(IPhotoListListener listListener) {
        mPhotoListListeners.remove(listListener);
    }

    public void setSelectMode(boolean selectMode) {
        mSelectMode = selectMode;

        mPhotoRecycleAdapter.notifyDataSetChanged();
    }

    public void setAlreadySelectedImageUUIDArrayList(List<String> alreadySelectedImageUUIDArrayList) {
        this.alreadySelectedImageUUIDArrayList = alreadySelectedImageUUIDArrayList;
    }

    @Override
    public void refreshView() {

        mLoadingLayout.setVisibility(View.VISIBLE);

        if (listener != null && !listener.isRemoteMediaLoaded()) {
            return;
        }

        reloadData();

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

    private void reloadData() {

        String date;
        List<Media> mediaList;

        mPhotoDateGroups.clear();
        mMapKeyIsDateValueIsPhotoList.clear();

        mMapKeyIsPhotoPositionValueIsPhotoDate.clear();
        mMapKeyIsPhotoPositionValueIsPhoto.clear();

        List<String> localUploadMediaUUID = new ArrayList<>();

        for (Media media : LocalCache.LocalMediaMapKeyIsThumb.values()) {

            localUploadMediaUUID.add(media.getUuid());

            date = media.getTime().substring(0, 10);
            if (mMapKeyIsDateValueIsPhotoList.containsKey(date)) {
                mediaList = mMapKeyIsDateValueIsPhotoList.get(date);
            } else {
                mPhotoDateGroups.add(date);
                mediaList = new ArrayList<>();
                mMapKeyIsDateValueIsPhotoList.put(date, mediaList);
            }

            Media localMedia = media.cloneSelf();
            localMedia.setLocal(true);
            localMedia.setTitle(date);
            localMedia.setSelected(false);

            mediaList.add(localMedia);
        }

        for (Media media : LocalCache.RemoteMediaMapKeyIsUUID.values()) {

            if (localUploadMediaUUID.contains(media.getUuid()))
                continue;

            date = media.getTime().substring(0, 10);
            if (mMapKeyIsDateValueIsPhotoList.containsKey(date)) {
                mediaList = mMapKeyIsDateValueIsPhotoList.get(date);
            } else {
                mPhotoDateGroups.add(date);
                mediaList = new ArrayList<>();
                mMapKeyIsDateValueIsPhotoList.put(date, mediaList);
            }

            Media remoteMedia = media.cloneSelf();
            remoteMedia.setLocal(false);
            remoteMedia.setTitle(date);
            remoteMedia.setSelected(false);

            mediaList.add(remoteMedia);

        }

        Collections.sort(mPhotoDateGroups, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return -lhs.compareTo(rhs);
            }
        });

        calcPhotoPositionNumber();

    }

    private void calcPhotoPositionNumber() {

        int titlePosition = 0;
        int photoListSize;
        mAdapterItemTotalCount = 0;

        for (String title : mPhotoDateGroups) {
            mMapKeyIsPhotoPositionValueIsPhotoDate.put(titlePosition, title);

            mAdapterItemTotalCount++;

            List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(title);
            photoListSize = mediaList.size();
            mAdapterItemTotalCount += photoListSize;

//            Log.i(TAG, "titlePosition:" + titlePosition + " photoListSize:" + photoListSize + " photoListLineSize:" + photoListLineSize);

            for (int i = 0; i < photoListSize; i++) {
                mMapKeyIsPhotoPositionValueIsPhoto.put(titlePosition + 1 + i, mediaList.get(i));
            }

            titlePosition = mAdapterItemTotalCount;
        }

    }

    @NonNull
    public List<String> getSelectedImageUUIDs() {

        List<String> selectedImageUUIDs = new ArrayList<>();
        for (List<Media> mediaList : mMapKeyIsDateValueIsPhotoList.values()) {
            for (Media media : mediaList) {
                if (media.isSelected()) {
                    selectedImageUUIDs.add(media.getUuid());
                }
            }
        }

        return selectedImageUUIDs;
    }

    private void clearSelectedPhoto() {
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

    public void createAlbum(List<String> selectUUIDs) {
        Intent intent = new Intent();
        intent.setClass(containerActivity, CreateAlbumActivity.class);
        intent.putExtra(Util.KEY_NEW_SELECTED_IMAGE_UUID_ARRAY, selectUUIDs.toArray(new String[selectUUIDs.size()]));
        containerActivity.startActivityForResult(intent, Util.KEY_CREATE_ALBUM_REQUEST_CODE);
    }


    @Override
    public void onDidAppear() {

        refreshView();

    }

    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

        if (reenterState != null) {

            int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
            int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);
            String currentMediaUUID = reenterState.getString(Util.CURRENT_MEDIA_UUID);

            if (initialPhotoPosition != currentPhotoPosition) {

                names.clear();
                sharedElements.clear();

                Media media = null;

                for (Media media1 : mMapKeyIsPhotoPositionValueIsPhoto.values()) {
                    if (media1.getUuid().equals(currentMediaUUID))
                        media = media1;
                }

                if (media == null) return;

                View newSharedElement = mRecyclerView.findViewWithTag(findPhotoTag(media));
                String sharedElementName = media.getUuid();

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
        String currentMediaUUID = reenterState.getString(Util.CURRENT_MEDIA_UUID);

        if (initialPhotoPosition != currentPhotoPosition) {

            int scrollToPosition = 0;

            for (Map.Entry<Integer, Media> entry : mMapKeyIsPhotoPositionValueIsPhoto.entrySet()) {
                if (entry.getValue().getUuid().equals(currentMediaUUID))
                    scrollToPosition = entry.getKey();
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

    private int findPhotoPositionInRecyclerView(String photoDate, int photoPositionInDateList) {
        int photoDatePosition = 0;

        for (Map.Entry<Integer, String> entry : mMapKeyIsPhotoPositionValueIsPhotoDate.entrySet()) {
            if (photoDate.contains(entry.getValue())) {
                photoDatePosition = entry.getKey();
            }
        }

        return photoDatePosition + 1 + photoPositionInDateList;
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
            if (mMapKeyIsPhotoPositionValueIsPhotoDate.containsKey(position))
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

            if (mMapKeyIsPhotoPositionValueIsPhotoDate.containsKey(position))
                title = mMapKeyIsPhotoPositionValueIsPhotoDate.get(position);
            else {
                title = mMapKeyIsPhotoPositionValueIsPhoto.get(position).getTitle();
            }

            if (title.contains("1916-01-01")) {
                return containerActivity.getString(R.string.unknown_time_text);
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
                mPhotoTitle.setText(containerActivity.getString(R.string.unknown_time_text));
            } else {
                mPhotoTitle.setText(date);
            }

            if (mSelectMode) {
                mPhotoTitleSelectImg.setVisibility(View.VISIBLE);

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
                mPhotoTitleSelectImg.setVisibility(View.GONE);
            }


            mPhotoTitleLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectMode) {

                        boolean selected = mPhotoTitleSelectImg.isSelected();
                        mPhotoTitleSelectImg.setSelected(!selected);
                        List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(date);
                        for (Media media : mediaList)
                            media.setSelected(!selected);

                        mPhotoRecycleAdapter.notifyDataSetChanged();

                        calcSelectedPhoto();

                        for (IPhotoListListener listListener : mPhotoListListeners) {
                            listListener.onPhotoItemLongClick(mSelectCount);
                        }
                    }
                }
            });

        }

    }

    class PhotoHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.photo_iv)
        NetworkImageView mPhotoIv;

        @BindView(R.id.photo_item_layout)
        RelativeLayout mImageLayout;

        @BindView(R.id.photo_select_img)
        ImageView mPhotoSelectedIv;

        View view;

        PhotoHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }

        public void refreshView(int position) {

            final Media currentMedia = mMapKeyIsPhotoPositionValueIsPhoto.get(position);

            mImageLoader.setTag(position);

            if (!mIsFling) {
                String imageUrl = currentMedia.getImageThumbUrl(containerActivity);
                mImageLoader.setShouldCache(!currentMedia.isLocal());
                mPhotoIv.setTag(imageUrl);
                mPhotoIv.setDefaultImageResId(R.drawable.placeholder_photo);
                mPhotoIv.setImageUrl(imageUrl, mImageLoader);
            } else {
                mPhotoIv.setDefaultImageResId(R.drawable.placeholder_photo);
                mPhotoIv.setImageUrl(null, mImageLoader);
            }

            List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(currentMedia.getTitle());
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

            mPhotoIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectMode) {

                        if (!currentMedia.isSharing()) {
                            Toast.makeText(containerActivity, containerActivity.getString(R.string.photo_not_sharing), Toast.LENGTH_SHORT).show();
                            return;
                        } else if (alreadySelectedImageUUIDArrayList != null && alreadySelectedImageUUIDArrayList.contains(currentMedia.getUuid())) {
                            Toast.makeText(containerActivity, containerActivity.getString(R.string.already_select_media), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean selected = currentMedia.isSelected();

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

                        LocalCache.photoSliderList.clear();

                        int initialPhotoPosition = 0;
                        int i = 0;

                        // go to PhotoSliderActivity may be a little slow,continue check it,and add android-gif-drawable

                        for (Media media : mMapKeyIsPhotoPositionValueIsPhoto.values()) {

                            if (media.getUuid().equals(currentMedia.getUuid())) {
                                initialPhotoPosition = i;
                            } else {
                                i++;
                            }

                            LocalCache.photoSliderList.add(media);
                        }

                        Intent intent = new Intent();
                        intent.putExtra(Util.INITIAL_PHOTO_POSITION, initialPhotoPosition);
                        intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, false);
                        intent.setClass(containerActivity, PhotoSliderActivity.class);

                        if (mPhotoIv.isLoaded()) {

                            ViewCompat.setTransitionName(mPhotoIv, currentMedia.getUuid());

                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, mPhotoIv, currentMedia.getUuid());

                            containerActivity.startActivity(intent, options.toBundle());
                        } else {

                            intent.putExtra(Util.KEY_NEED_TRANSITION, false);
                            containerActivity.startActivity(intent);

                        }


                    }

                    Log.i(TAG, "image digest:" + currentMedia.getUuid());
                }
            });

            mPhotoIv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    currentMedia.setSelected(true);
                    mPhotoRecycleAdapter.notifyDataSetChanged();

                    calcSelectedPhoto();

                    for (IPhotoListListener listListener : mPhotoListListeners) {
                        listListener.onPhotoItemLongClick(mSelectCount);
                    }

                    return true;
                }
            });

        }

        private void setPhotoItemMargin(int mediaInListPosition) {
            GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) view.getLayoutParams();

            params.height = mItemWidth;

            int normalMargin = Util.dip2px(containerActivity, 2.5f);

            if ((mediaInListPosition + 1) % mSpanCount == 0) {
                params.setMargins(normalMargin, normalMargin, normalMargin, 0);
            } else {
                params.setMargins(normalMargin, normalMargin, 0, 0);
            }

            view.setLayoutParams(params);
        }

        private void setPhotoIvLayoutParams(int margin) {
            RelativeLayout.LayoutParams photoParams = (RelativeLayout.LayoutParams) mPhotoIv.getLayoutParams();
            photoParams.setMargins(margin, margin, margin, margin);
            mPhotoIv.setLayoutParams(photoParams);
        }

        private int getPosition(List<Media> mediaList, Media media) {

            int position = 0;

            for (int i = 0; i < mediaList.size(); i++) {
                Media media1 = mediaList.get(i);

                if (media.getUuid().equals(media1.getUuid())) {
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

            if (newState == RecyclerView.SCROLL_STATE_SETTLING) {

                mHasFlung = true;
                mIsFling = true;

            } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                mIsFling = false;

                mPhotoRecycleAdapter.notifyDataSetChanged();
            }
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
                    calcPhotoPositionNumber();
                    ((GridLayoutManager) mLayoutManager).setSpanCount(mSpanCount);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mPhotoRecycleAdapter.notifyItemRangeChanged(0, mPhotoRecycleAdapter.getItemCount());
                }

                Log.i(TAG, "pinch more");

            } else if (mOldSpan > newSpan + Util.dip2px(containerActivity, 2)) {

                if (mSpanCount < mSpanMaxCount) {
                    mSpanCount++;
                    calcPhotoItemWidth();
                    calcPhotoPositionNumber();
                    ((GridLayoutManager) mLayoutManager).setSpanCount(mSpanCount);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mPhotoRecycleAdapter.notifyItemRangeChanged(0, mPhotoRecycleAdapter.getItemCount());
                }

                Log.i(TAG, "pinch less");
            }

        }
    }

}
