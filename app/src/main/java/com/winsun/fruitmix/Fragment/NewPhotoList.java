package com.winsun.fruitmix.Fragment;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.CreateAlbumActivity;
import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.PhotoSliderActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.services.CreateRemoteMediaShareService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.sin3hz.fastjumper.FastJumper;
import io.github.sin3hz.fastjumper.callback.LinearScrollCalculator;
import io.github.sin3hz.fastjumper.callback.SpannableCallback;

/**
 * Created by Administrator on 2016/7/28.
 */
public class NewPhotoList implements NavPagerActivity.Page {

    public static final String TAG = NewPhotoList.class.getSimpleName();

    Activity containerActivity;
    private View view;

    @BindView(R.id.photo_recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout mNoContentLayout;

    private int mSpanCount = 3;
    private int mSpanMaxCount = 6;
    private int mSpanMinCount = 2;

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

    private ProgressDialog mDialog;

    private int mScreenWidth;

    private boolean mIsFling = false;
    private NewPhotoListScrollListener mScrollListener;

    private boolean mSelectMode = false;

    private List<IPhotoListListener> mPhotoListListeners;

    private int mSelectCount;

    private int mAdapterItemTotalCount;

    private boolean mHasFlung = false;
    private RequestQueue mRequestQueue;

    private ImageLoader mImageLoader;

    private float mOldSpan = 0;
    private ScaleGestureDetector mPinchScaleDetector;

    private Bundle reenterState;

    public NewPhotoList(Activity activity) {
        containerActivity = activity;

        view = View.inflate(containerActivity, R.layout.new_photo_layout, null);
        ButterKnife.bind(this, view);

        mRequestQueue = RequestQueueInstance.REQUEST_QUEUE_INSTANCE.getmRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);

        mPhotoDateGroups = new ArrayList<>();
        mMapKeyIsDateValueIsPhotoList = new HashMap<>();

        mMapKeyIsPhotoPositionValueIsPhotoDate = new HashMap<>();
        mMapKeyIsPhotoPositionValueIsPhoto = new HashMap<>();

        mPhotoListListeners = new ArrayList<>();

        calcScreenWidth();

        mPinchScaleDetector = new ScaleGestureDetector(containerActivity, new PinchScaleListener());

        mScrollListener = new NewPhotoListScrollListener();
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

    @Override
    public void refreshView() {

        mLoadingLayout.setVisibility(View.VISIBLE);

        LocalCache.LoadLocalData();
        reloadData();

        mLoadingLayout.setVisibility(View.INVISIBLE);
        if (mPhotoDateGroups.size() == 0) {
            mNoContentLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);

            for (IPhotoListListener listener : mPhotoListListeners)
                listener.onNoPhotoItem(true);

        } else {
            mNoContentLayout.setVisibility(View.INVISIBLE);
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
        DisplayMetrics metric = new DisplayMetrics();
        containerActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);

        mScreenWidth = metric.widthPixels;
    }

    private void calcPhotoItemWidth() {
        mItemWidth = mScreenWidth / mSpanCount - Util.dip2px(5);
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

        for (ConcurrentMap<String, String> map : LocalCache.LocalImagesMapKeyIsThumb.values()) {

            if (map.containsKey(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS) && map.get(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS).equals("true")) {
                continue;
            }

            date = map.get("mtime").substring(0, 10);
            if (mMapKeyIsDateValueIsPhotoList.containsKey(date)) {
                mediaList = mMapKeyIsDateValueIsPhotoList.get(date);
            } else {
                mPhotoDateGroups.add(date);
                mediaList = new ArrayList<>();
                mMapKeyIsDateValueIsPhotoList.put(date, mediaList);
            }

            Media media = new Media();
            media.setUuid(map.get("uuid"));
            media.setWidth(map.get("width"));
            media.setHeight(map.get("height"));
            media.setLocal(true);
            media.setTime(map.get("mtime"));
            media.setTitle(date);
            media.setThumb(map.get("thumb"));
            mediaList.add(media);
        }

        for (ConcurrentMap<String, String> map : LocalCache.MediasMap.values()) {

            date = map.get("mtime").substring(0, 10);
            if (mMapKeyIsDateValueIsPhotoList.containsKey(date)) {
                mediaList = mMapKeyIsDateValueIsPhotoList.get(date);
            } else {
                mPhotoDateGroups.add(date);
                mediaList = new ArrayList<>();
                mMapKeyIsDateValueIsPhotoList.put(date, mediaList);
            }

            Media media = new Media();
            media.setUuid(map.get("uuid"));
            media.setWidth(map.get("width"));
            media.setHeight(map.get("height"));
            media.setLocal(false);
            media.setTime(map.get("mtime").replace("T", " ").replace("Z", " "));
            media.setThumb("");
            media.setTitle(date);
            mediaList.add(media);
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
    public String getSelectedImageUUIDString() {

        StringBuilder builder = new StringBuilder();
        for (List<Media> mediaList : mMapKeyIsDateValueIsPhotoList.values()) {
            for (Media media : mediaList) {
                if (media.isSelected()) {
                    builder.append(",");
                    builder.append(media.getUuid());
                }
            }
        }

        String selectedUID = "";
        if (builder.length() >= 1) {
            selectedUID = builder.substring(1);
        }

        Log.i(TAG, "selectUID" + selectedUID);
        return selectedUID;
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

    public void createAlbum(String selectUID) {
        Intent intent = new Intent();
        intent.setClass(containerActivity, CreateAlbumActivity.class);
        intent.putExtra("selectedUIDStr", selectUID);
        containerActivity.startActivityForResult(intent, Util.KEY_CREATE_ALBUM_REQUEST_CODE);
    }

    public void createShare(final String selectUID) {
        new AsyncTask<Object, Object, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                mDialog = ProgressDialog.show(containerActivity, containerActivity.getString(R.string.operating_title), containerActivity.getString(R.string.loading_message), true, false);
            }

            @Override
            protected Boolean doInBackground(Object... params) {
                String data, viewers, selectUUID;
                String[] selectedUIDArr;
                int i;

                selectUUID = selectUID;
                selectedUIDArr = selectUUID.split(",");
                data = "";
                for (i = 0; i < selectedUIDArr.length; i++) {
                    data += ",{\\\"type\\\":\\\"media\\\",\\\"digest\\\":\\\"" + selectedUIDArr[i] + "\\\"}";
                }

                viewers = "";
                for (String key : LocalCache.UsersMap.keySet()) {
                    viewers += ",\\\"" + key + "\\\"";
                }
                if (viewers.length() == 0) {
                    viewers += ",";
                }
                Log.i("winsun viewer:", viewers);

                createAlbumInLocalAlbumDatabase(true, false, "", "", selectUUID);
                FNAS.loadLocalShare();

                return true;

            }

            @Override
            protected void onPostExecute(Boolean sSuccess) {

                mDialog.dismiss();

                if (Util.getNetworkState(containerActivity)) {
                    CreateRemoteMediaShareService.startActionCreateRemoteMediaShareTask(containerActivity);
                }
                if (sSuccess) {
                    if (containerActivity instanceof NavPagerActivity) {
                        ((NavPagerActivity) containerActivity).onActivityResult(Util.KEY_CREATE_SHARE_REQUEST_CODE, Activity.RESULT_OK, null);
                    }

                } else {
                    Snackbar.make(mRecyclerView, containerActivity.getString(R.string.operation_fail), Snackbar.LENGTH_SHORT).show();
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void createAlbumInLocalAlbumDatabase(boolean isPublic, boolean otherMaintianer, String title, String desc, String digest) {

        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

        MediaShare mediaShare = new MediaShare();
        mediaShare.setUuid(Util.createLocalUUid());

        Log.i(TAG, "create share digest:" + digest);

        mediaShare.setImageDigests(Arrays.asList(digest.split(",")));
        mediaShare.setTitle(title);
        mediaShare.setDesc(desc);


        if (isPublic) {
            mediaShare.setViewer(new ArrayList<>(LocalCache.UsersMap.keySet()));
        } else mediaShare.setViewer(new ArrayList<String>());

        if (otherMaintianer) {
            mediaShare.setMaintainer(new ArrayList<>(LocalCache.UsersMap.keySet()));
        } else {
            mediaShare.setMaintainer(Collections.singletonList(FNAS.userUUID));
        }

        mediaShare.setCreator(FNAS.userUUID);
        mediaShare.setTime(String.valueOf(System.currentTimeMillis()));
        mediaShare.setAlbum(false);
        dbUtils.insertLocalShare(mediaShare);

    }

    @Override
    public void onDidAppear() {

        refreshView();

    }

    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

        if (reenterState != null) {

            int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
            int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);
            String currentPhotoDate = reenterState.getString(Util.CURRENT_PHOTO_DATE);

            if (initialPhotoPosition != currentPhotoPosition) {

                names.clear();
                sharedElements.clear();

                Media media = mMapKeyIsPhotoPositionValueIsPhoto.get(findPhotoPositionInRecyclerView(currentPhotoDate, currentPhotoPosition));

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
        String currentPhotoDate = reenterState.getString(Util.CURRENT_PHOTO_DATE);


        if (initialPhotoPosition != currentPhotoPosition) {

            mRecyclerView.smoothScrollToPosition(findPhotoPositionInRecyclerView(currentPhotoDate, currentPhotoPosition));

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
        if (mMapKeyIsPhotoPositionValueIsPhotoDate.containsValue(photoDate)) {

            for (Map.Entry<Integer, String> entry : mMapKeyIsPhotoPositionValueIsPhotoDate.entrySet()) {
                if (entry.getValue().equals(photoDate)) {
                    photoDatePosition = entry.getKey();
                }
            }
        }

        return photoDatePosition + 1 + photoPositionInDateList;
    }

    private String findPhotoTag(Media media) {
        String tag = "";
        if (media.isLocal()) {

            tag = media.getThumb();

        } else if (!media.isLocal()) {


            int width = Integer.parseInt(media.getWidth());
            int height = Integer.parseInt(media.getHeight());

            int[] result = Util.formatPhotoWidthHeight(width, height);

            tag = String.format(containerActivity.getString(R.string.thumb_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + media.getUuid(), result[0], result[1]);
        }
        return tag;
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

        public int getSpanSize(int position) {
            if (getItemViewType(position) == VIEW_TYPE_HEAD) {
                return mSpanCount;
            } else {
                return 1;
            }
        }

        public int getItemHeight(int position) {
            if (getItemViewType(position) == VIEW_TYPE_HEAD) {
                return mItemWidth;
            } else {
                return mSubHeaderHeight;
            }
        }

        public String getSectionForPosition(int position) {
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

        public PhotoGroupHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void refreshView(int groupPosition) {

            final String date = mMapKeyIsPhotoPositionValueIsPhotoDate.get(groupPosition);
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

        public PhotoHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }

        public void refreshView(int position) {

            final Media media = mMapKeyIsPhotoPositionValueIsPhoto.get(position);

            mImageLoader.setTag(position);

            if (media.isLocal() && !mIsFling) {

                String url = media.getThumb();

                mImageLoader.setShouldCache(false);
                mPhotoIv.setTag(url);
                mPhotoIv.setDefaultImageResId(R.drawable.placeholder_photo);
                mPhotoIv.setImageUrl(url, mImageLoader);

            } else if (!media.isLocal() && !mIsFling) {


                int width = Integer.parseInt(media.getWidth());
                int height = Integer.parseInt(media.getHeight());

                int[] result = Util.formatPhotoWidthHeight(width, height);

                String url = String.format(containerActivity.getString(R.string.thumb_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + media.getUuid(), result[0], result[1]);

                mImageLoader.setShouldCache(true);
                mPhotoIv.setTag(url);
                mPhotoIv.setDefaultImageResId(R.drawable.placeholder_photo);
                mPhotoIv.setImageUrl(url, mImageLoader);


            } else if (mIsFling) {
                mPhotoIv.setDefaultImageResId(R.drawable.placeholder_photo);
                mPhotoIv.setImageUrl(null, mImageLoader);
            }

            GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) view.getLayoutParams();

            params.height = mItemWidth;

            int normalMargin = Util.dip2px(2.5f);

            params.setMargins(normalMargin, normalMargin, normalMargin, normalMargin);
            view.setLayoutParams(params);

            if (mSelectMode) {
                boolean selected = media.isSelected();
                RelativeLayout.LayoutParams photoParams = (RelativeLayout.LayoutParams) mPhotoIv.getLayoutParams();
                if (selected) {
                    int selectMargin = Util.dip2px(20);
                    photoParams.setMargins(selectMargin, selectMargin, selectMargin, selectMargin);
                    mPhotoIv.setLayoutParams(photoParams);
                    mPhotoSelectedIv.setVisibility(View.VISIBLE);
                } else {
                    photoParams.setMargins(0, 0, 0, 0);
                    mPhotoIv.setLayoutParams(photoParams);
                    mPhotoSelectedIv.setVisibility(View.INVISIBLE);
                }
            } else {

                media.setSelected(false);

                RelativeLayout.LayoutParams photoParams = (RelativeLayout.LayoutParams) mPhotoIv.getLayoutParams();
                photoParams.setMargins(0, 0, 0, 0);
                mPhotoIv.setLayoutParams(photoParams);
                mPhotoSelectedIv.setVisibility(View.INVISIBLE);
            }

            mPhotoIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectMode) {
                        boolean selected = media.isSelected();
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPhotoIv.getLayoutParams();
                        if (selected) {
                            int selectMargin = Util.dip2px(20);
                            params.setMargins(selectMargin, selectMargin, selectMargin, selectMargin);
                            mPhotoIv.setLayoutParams(params);
                            mPhotoSelectedIv.setVisibility(View.VISIBLE);
                        } else {
                            params.setMargins(0, 0, 0, 0);
                            mPhotoIv.setLayoutParams(params);
                            mPhotoSelectedIv.setVisibility(View.INVISIBLE);
                        }

                        media.setSelected(!selected);
                        mPhotoRecycleAdapter.notifyDataSetChanged();

                        calcSelectedPhoto();

                        for (IPhotoListListener listListener : mPhotoListListeners) {
                            listListener.onPhotoItemClick(mSelectCount);
                        }

                    } else {

                        int position = 0;

                        List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(media.getTitle());
                        List<Map<String, Object>> imgList = new ArrayList<>(mediaList.size());
                        Map<String, Object> map;
                        for (int i = 0; i < mediaList.size(); i++) {
                            Media media1 = mediaList.get(i);
                            map = new HashMap<>();
                            map.put("cacheType", media1.isLocal() ? "local" : "nas");
                            map.put("resID", R.drawable.default_img);
                            map.put("resHash", media1.getUuid());
                            map.put("thumb", media1.getThumb());
                            map.put("width", media1.getWidth());
                            map.put("height", media1.getHeight());
                            map.put("uuid", media1.getUuid());
                            map.put("mtime", media1.getTime());
                            map.put("selected", media1.isSelected() ? "1" : "0");
                            map.put("locked", "1");
                            imgList.add(map);

                            if (media.getUuid().equals(media1.getUuid())) {
                                position = i;
                            }
                        }

                        LocalCache.TransActivityContainer.put("imgSliderList", imgList);
                        Intent intent = new Intent();

//                        Log.i(TAG, "photo pos:" + position);
                        intent.putExtra(Util.INITIAL_PHOTO_POSITION, position);
                        intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, false);
                        intent.setClass(containerActivity, PhotoSliderActivity.class);

                        ViewCompat.setTransitionName(mPhotoIv, media.getUuid());

                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, mPhotoIv, media.getUuid());

                        containerActivity.startActivity(intent, options.toBundle());

                    }

                    Log.i(TAG, "image digest:" + media.getUuid());
                }
            });

            mPhotoIv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    media.setSelected(true);
                    mPhotoRecycleAdapter.notifyDataSetChanged();

                    calcSelectedPhoto();

                    for (IPhotoListListener listListener : mPhotoListListeners) {
                        listListener.onPhotoItemLongClick(mSelectCount);
                    }

                    return true;
                }
            });

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

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            mOldSpan = detector.getCurrentSpan();

            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            float newSpan = detector.getCurrentSpan();

            if (newSpan > mOldSpan + Util.dip2px(2)) {

                if (mSpanCount > mSpanMinCount) {
                    mSpanCount--;
                    calcPhotoItemWidth();
                    calcPhotoPositionNumber();
                    ((GridLayoutManager) mLayoutManager).setSpanCount(mSpanCount);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mPhotoRecycleAdapter.notifyItemRangeChanged(0, mPhotoRecycleAdapter.getItemCount());
                }

                Log.i(TAG, "pinch more");

            } else if (mOldSpan > newSpan + Util.dip2px(2)) {

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
