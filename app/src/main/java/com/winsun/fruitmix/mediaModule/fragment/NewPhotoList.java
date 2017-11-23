package com.winsun.fruitmix.mediaModule.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.databinding.NewPhotoGridlayoutItemBinding;
import com.winsun.fruitmix.databinding.NewPhotoLayoutBinding;
import com.winsun.fruitmix.databinding.NewPhotoTitleItemBinding;
import com.winsun.fruitmix.databinding.VideoItemBinding;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.media.CalcMediaDigestStrategy;
import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.PhotoSliderActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.interfaces.Page;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.NewPhotoListDataLoader;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.mediaModule.model.NewPhotoListViewModel;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.mediaModule.viewmodel.PhotoItemViewModel;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationMediaDataChanged;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.upload.media.InjectUploadMediaUseCase;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BindingViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.github.sin3hz.fastjumper.FastJumper;
import io.github.sin3hz.fastjumper.callback.LinearScrollCalculator;
import io.github.sin3hz.fastjumper.callback.SpannableCallback;

/**
 * Created by Administrator on 2016/7/28.
 */
public class NewPhotoList implements Page, IShowHideFragmentListener, ActiveView {

    public static final String TAG = NewPhotoList.class.getSimpleName();

    private Activity containerActivity;
    private View view;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private int mSpanCount = 4;

    private PhotoRecycleAdapter mPhotoRecycleAdapter;

    private FastJumper mFastJumper;
    private SpannableCallback mJumperCallback;
    private SpannableCallback.ScrollCalculator mScrollCalculator;
    private RecyclerView.LayoutManager mLayoutManager;

    private int mItemWidth;

    private Map<String, List<Media>> mMapKeyIsDateValueIsPhotoList;

    private Map<Integer, String> mMapKeyIsPhotoPositionValueIsPhotoDate;

    private SparseArray<Media> mMapKeyIsPhotoPositionValueIsPhoto;

    private List<Media> medias;

    private int mScreenWidth;

    private boolean mIsFling = false;

    private boolean mSelectMode = false;

    private IPhotoListListener mPhotoListListener;

    private int mSelectCount;

    private int mAdapterItemTotalCount;

    private boolean mUseAnim = false;

    private ImageLoader mImageLoader;

    private float mOldSpan = 0;
    private ScaleGestureDetector mPinchScaleDetector;

    private Bundle reenterState;

    private List<String> alreadySelectedImageKeysFromChooseActivity;

    private boolean mPreLoadPhoto = false;

    private boolean mCancelPreLoadPhoto = false;

    private Typeface mTypeface;

    private boolean mIsLoading = false;

    private boolean mIsLoaded = false;

    public static boolean mEnteringPhotoSlider = false;

    private NoContentViewModel noContentViewModel;
    private LoadingViewModel loadingViewModel;

    private NewPhotoListViewModel newPhotoListViewModel;

    private Animator scaleAnimator;

    private MediaDataSourceRepository mediaDataSourceRepository;

    private boolean hasCallStartUpload = false;

    private CalcMediaDigestStrategy.CalcMediaDigestCallback calcMediaDigestCallback;

    public NewPhotoList(Activity activity) {
        containerActivity = activity;

        medias = Collections.emptyList();

        mMapKeyIsDateValueIsPhotoList = Collections.emptyMap();

        mMapKeyIsPhotoPositionValueIsPhotoDate = Collections.emptyMap();

        mMapKeyIsPhotoPositionValueIsPhoto = new SparseArray<>();

        NewPhotoLayoutBinding binding = NewPhotoLayoutBinding.inflate(LayoutInflater.from(containerActivity.getApplicationContext()), null, false);

        view = binding.getRoot();

        noContentViewModel = new NoContentViewModel();
        noContentViewModel.setNoContentImgResId(R.drawable.no_file);
        noContentViewModel.setNoContentText(containerActivity.getString(R.string.no_photos));

        binding.setNoContentViewModel(noContentViewModel);

        loadingViewModel = new LoadingViewModel();

        binding.setLoadingViewModel(loadingViewModel);

        newPhotoListViewModel = new NewPhotoListViewModel();

        binding.setNewPhotoListViewModel(newPhotoListViewModel);

        mSwipeRefreshLayout = binding.swipeRefreshLayout;

        initSwipeRefreshLayout();

        mRecyclerView = binding.photoRecyclerview;

        calcScreenWidth();

        mPinchScaleDetector = new ScaleGestureDetector(containerActivity, new PinchScaleListener());

        mRecyclerView.addOnScrollListener(new NewPhotoListScrollListener());

        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mPinchScaleDetector.onTouchEvent(event);

                return false;
            }
        });

        mPhotoRecycleAdapter = new PhotoRecycleAdapter();

        setupFastJumper();

        setupRecyclerView();

        mTypeface = Typeface.createFromAsset(containerActivity.getAssets(), "fonts/Roboto-Medium.ttf");

        mediaDataSourceRepository = InjectMedia.provideMediaDataSourceRepository(containerActivity);

        calcMediaDigestCallback = new CalcMediaDigestStrategy.CalcMediaDigestCallback() {
            @Override
            public void handleFinished() {

                refreshViewForce();

            }

            @Override
            public void handleNothing() {

            }
        };

        mediaDataSourceRepository.registerCalcDigestCallback(calcMediaDigestCallback);

    }

    private void initSwipeRefreshLayout() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refreshStationMediaForce();

            }
        });
    }

    private void finishSwipeRefreshAnimation() {
        if (mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void show() {

//        MobclickAgent.onPageStart("PhotoFragment");
    }

    @Override
    public void hide() {
//        MobclickAgent.onPageEnd("PhotoFragment");
    }

    private void initImageLoader() {

        ImageGifLoaderInstance imageGifLoaderInstance = InjectHttp.provideImageGifLoaderInstance(containerActivity);
        mImageLoader = imageGifLoaderInstance.getImageLoader(containerActivity);

    }

    public void setPhotoListListener(IPhotoListListener listListener) {
        mPhotoListListener = listListener;
    }

    @Override
    public boolean canEnterSelectMode() {
        return medias.size() > 0;
    }

    public void setSelectMode(boolean selectMode) {

        mSelectMode = selectMode;

        if (mSelectMode) {
            clearSelectedPhoto();

            mSwipeRefreshLayout.setEnabled(false);

        } else {

            mSwipeRefreshLayout.setEnabled(true);

        }

        mUseAnim = true;

        mPhotoRecycleAdapter.notifyDataSetChanged();
    }

    public void setAlreadySelectedImageKeysFromChooseActivity(List<String> alreadySelectedImageKeysFromChooseActivity) {
        this.alreadySelectedImageKeysFromChooseActivity = alreadySelectedImageKeysFromChooseActivity;
    }

    private void refreshStationMediaForce() {

        mediaDataSourceRepository.getStationMediaForceRefresh(new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<Media>() {
            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {

                Log.d(TAG, "onSucceed: refresh station media force");

                handleGetMediaSucceed(data, new OperationMediaDataChanged());
            }

            @Override
            public void onFail(OperationResult operationResult) {

                finishSwipeRefreshAnimation();

            }
        }, this));

    }

    @Override
    public void refreshViewForce() {

        mediaDataSourceRepository.getStationMediaForceRefresh(new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<Media>() {
            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {

                Log.d(TAG, "onSucceed: refresh view force");

                handleGetMediaSucceed(data, new OperationMediaDataChanged());

            }

            @Override
            public void onFail(OperationResult operationResult) {

                Log.d(TAG, "onFail: refresh view force");

            }
        }, this));

    }

    //prevent load media from system db return 0,but UploadMediaUseCase load local media return correct count,then refresh view
    public void onUploadMediaCountChanged(int totalLocalMediaCount) {

        if (mIsLoaded) {

            if (medias.size() < totalLocalMediaCount) {

                mediaDataSourceRepository.getLocalMedia(new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<Media>() {
                    @Override
                    public void onSucceed(List<Media> data, OperationResult operationResult) {

                        handleGetMediaSucceed(data, new OperationMediaDataChanged());

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                    }
                }, this));

            }

        }

    }


    @Override
    public void refreshView() {

        if (mSelectMode)
            return;

        if (!mIsLoading)
            mIsLoading = true;
        else
            return;

        getMediaInThread();


//        if (Util.isRemoteMediaLoaded() && Util.isLocalMediaInCameraLoaded() && Util.isLocalMediaInDBLoaded()) {
//
//            initImageLoader();
//
//            final NewPhotoListDataLoader loader = NewPhotoListDataLoader.INSTANCE;
//
//            loader.retrieveData(new NewPhotoListDataLoader.OnPhotoListDataListener() {
//                @Override
//                public void onDataLoadFinished() {
//                    doAfterReloadData(loader);
//
//                    mIsLoaded = true;
//                }
//            });
//
//        } else {
//
//            loadingViewModel.showLoading.set(true);
//
//        }

    }

    private void getMediaInThread() {

        if (mPhotoListListener != null)
            mPhotoListListener.onNoPhotoItem(true);

        mediaDataSourceRepository.getMedia(new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<Media>() {
            @Override
            public void onSucceed(final List<Media> data, final OperationResult operationResult) {

                Log.d(TAG, "onSucceed: get media size: " + data.size());

                if (!hasCallStartUpload) {

                    hasCallStartUpload = true;

                    InjectUploadMediaUseCase.provideUploadMediaUseCase(containerActivity).startUploadMedia();

                }

                handleGetMediaSucceed(data, operationResult);

                mIsLoading = false;

            }

            @Override
            public void onFail(OperationResult operationResult) {

                loadingViewModel.showLoading.set(true);

                mIsLoading = false;

                if (mPhotoListListener != null)
                    mPhotoListListener.onNoPhotoItem(true);

            }
        }, this));

    }

    private void handleGetMediaSucceed(final List<Media> data, final OperationResult operationResult) {

        finishSwipeRefreshAnimation();

        initImageLoader();

        final NewPhotoListDataLoader loader = NewPhotoListDataLoader.getInstance();

        if (operationResult.getOperationResultType() == OperationResultType.MEDIA_DATA_CHANGED)
            loader.setNeedRefreshData(true);

        loader.retrieveData(new NewPhotoListDataLoader.OnPhotoListDataListener() {
            @Override
            public void onDataLoadFinished() {

                Log.d(TAG, "onDataLoadFinished: ");

                doAfterReloadData(loader);

                mIsLoaded = true;
            }
        }, data);

    }

    public boolean isLoaded() {

        return mIsLoaded;
    }


    private void doAfterReloadData(NewPhotoListDataLoader loader) {

        List<String> mPhotoDateGroups = loader.getPhotoDateGroups();
        mAdapterItemTotalCount = loader.getAdapterItemTotalCount();
        mMapKeyIsDateValueIsPhotoList = loader.getMapKeyIsDateValueIsPhotoList();
        mMapKeyIsPhotoPositionValueIsPhotoDate = loader.getMapKeyIsPhotoPositionValueIsPhotoDate();
        mMapKeyIsPhotoPositionValueIsPhoto = loader.getMapKeyIsPhotoPositionValueIsPhoto();
        medias = loader.getMedias();

        clearSelectedPhoto();

        loadingViewModel.showLoading.set(false);

        if (mPhotoDateGroups.size() == 0) {

            noContentViewModel.showNoContent.set(true);

            newPhotoListViewModel.showContent.set(false);

            if (mPhotoListListener != null)
                mPhotoListListener.onNoPhotoItem(true);

        } else {

            noContentViewModel.showNoContent.set(false);

            newPhotoListViewModel.showContent.set(true);

            //modify by liang.wu: add handler for crash:Cannot call this method while RecyclerView is computing a layout or scrolling
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mPhotoRecycleAdapter.notifyDataSetChanged();
                }
            });

            if (mPhotoListListener != null)
                mPhotoListListener.onNoPhotoItem(false);

            if (!mPreLoadPhoto) {
                mPreLoadPhoto = true;
                loadSmallThumbnail(medias);
            }

        }

    }

    private void loadSmallThumbnail(final List<Media> medias) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                String url;

                List<Media> preLoadMediaMiniThumbs = new ArrayList<>(medias);
                Media media;
                Iterator<Media> iterator = preLoadMediaMiniThumbs.iterator();
                while (iterator.hasNext()) {
                    media = iterator.next();
                    if (media.isLocal())
                        iterator.remove();
                }

                Log.i(TAG, "pre load media size: " + preLoadMediaMiniThumbs.size());

                for (int i = 0; i < preLoadMediaMiniThumbs.size(); i++) {

                    if (mCancelPreLoadPhoto)
                        break;

                    media = preLoadMediaMiniThumbs.get(i);

                    if (media instanceof Video)
                        continue;

                    HttpRequest httpRequest = media.getImageSmallThumbUrl(containerActivity);

                    url = httpRequest.getUrl();

                    ArrayMap<String, String> header = new ArrayMap<>();
                    header.put(httpRequest.getHeaderKey(), httpRequest.getHeaderValue());

                    mImageLoader.setHeaders(header);

                    mImageLoader.preLoadMediaSmallThumb(url, mItemWidth, mItemWidth);

                }

            }
        }).start();

    }

    private void cancelPreLoadMediaMiniThumb() {
        mCancelPreLoadPhoto = true;

        mImageLoader.cancelAllPreLoadMedia();
    }

    private void setupFastJumper() {
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

        mFastJumper.addListener(new FastJumper.Listener() {

            @Override
            public void onStateChange(int state) {
                super.onStateChange(state);

                if (state == FastJumper.STATE_DRAGGING) {
                    mIsFling = true;
                } else if (state == FastJumper.STATE_GONE) {
                    if (mIsFling) {

                        mIsFling = false;

                        mPhotoRecycleAdapter.notifyDataSetChanged();

                    }

//                    if (mIsScrollUp && !mSelectMode) {
//
//                        for (IPhotoListListener listener : mPhotoListListeners) {
//                            listener.onPhotoListScrollUp();
//                        }
//
//                    }

                }
            }

        });

    }

    private void setupGridLayoutManager() {

        SpannableCallback.ScrollCalculator mLinearScrollCalculator = new LinearScrollCalculator(mRecyclerView) {

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

        calcPhotoItemWidth();

        GridLayoutManager glm = new GridLayoutManager(containerActivity, mSpanCount);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mPhotoRecycleAdapter.getSpanSize(position);
            }
        });

        glm.getSpanSizeLookup().setSpanIndexCacheEnabled(true);

        mLayoutManager = glm;
        ((GridLayoutManager) mLayoutManager).setSpanCount(mSpanCount);

        mScrollCalculator = mLinearScrollCalculator;
    }

    private void setupRecyclerView() {
        mFastJumper.attachToRecyclerView(null);
        setupGridLayoutManager();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mPhotoRecycleAdapter);
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

    public List<Media> getSelectedMedias() {

        List<Media> selectedMedias = new ArrayList<>();

        for (List<Media> mediaList : mMapKeyIsDateValueIsPhotoList.values()) {
            for (Media media : mediaList) {
                if (media.isSelected()) {

                    String mediaUUID = media.getUuid();
                    if (mediaUUID.isEmpty()) {
                        mediaUUID = Util.calcSHA256OfFile(media.getOriginalPhotoPath());
                        media.setUuid(mediaUUID);
                    }

                    selectedMedias.add(media);
                }
            }
        }

        return selectedMedias;
    }

    public void clearSelectedPhoto() {

        if (mMapKeyIsPhotoPositionValueIsPhoto == null || mMapKeyIsPhotoPositionValueIsPhoto.size() == 0)
            return;

        Media media;

        for (int i = 0; i < mMapKeyIsPhotoPositionValueIsPhoto.size(); i++) {

            media = mMapKeyIsPhotoPositionValueIsPhoto.get(mMapKeyIsPhotoPositionValueIsPhoto.keyAt(i));

            if (media != null)
                media.setSelected(false);
        }

    }

    private void calcSelectedPhoto() {

        int selectCount = 0;

        for (List<Media> mediaList : mMapKeyIsDateValueIsPhotoList.values()) {
            for (Media media : mediaList) {
                if (media.isSelected())
                    selectCount++;
            }
        }

        mSelectCount = selectCount;
    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

        if (reenterState != null) {

            int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
            int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);
            String currentMediaKey = reenterState.getString(Util.CURRENT_MEDIA_KEY);

            if (initialPhotoPosition != currentPhotoPosition) {

                Media media;
                Media currentMedia = null;

                int size = mMapKeyIsPhotoPositionValueIsPhoto.size();

                for (int i = 0; i < size; i++) {
                    media = mMapKeyIsPhotoPositionValueIsPhoto.valueAt(i);
                    if (media.getKey().equals(currentMediaKey))
                        currentMedia = media;
                }

                if (currentMedia == null) return;

                View newSharedElement = mRecyclerView.findViewWithTag(currentMedia.getImageThumbUrl(containerActivity).getUrl());

                if (newSharedElement == null)
                    newSharedElement = mRecyclerView.findViewWithTag(currentMedia.getImageSmallThumbUrl(containerActivity).getUrl());

                names.clear();
                sharedElements.clear();

                String sharedElementName = currentMedia.getKey();

                names.add(sharedElementName);
                sharedElements.put(sharedElementName, newSharedElement);
            }

        }
        reenterState = null;

    }

    @Override
    public void onDestroy() {

        cancelPreLoadMediaMiniThumb();

        containerActivity = null;

        mediaDataSourceRepository.unregisterCalcDigestCallback(calcMediaDigestCallback);
    }

    @Override
    public boolean isActive() {
        return containerActivity != null;
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

        reenterState = new Bundle(data.getExtras());
        int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
        int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);
        String currentMediaKey = reenterState.getString(Util.CURRENT_MEDIA_KEY);

        if (initialPhotoPosition != currentPhotoPosition) {

            int scrollToPosition = 0;

            Media media;

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

    public void refreshVideo(Video video) {

        int index = mMapKeyIsPhotoPositionValueIsPhoto.indexOfValue(video);
        int key = mMapKeyIsPhotoPositionValueIsPhoto.keyAt(index);

        mPhotoRecycleAdapter.notifyItemChanged(key);

    }

    private class PhotoRecycleAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private static final int VIEW_TYPE_HEAD = 0x1000;
        private static final int VIEW_TYPE_CONTENT = 0x1001;

        private static final int VIEW_TYPE_VIDEO = 0x1002;

        private int mSubHeaderHeight = containerActivity.getResources().getDimensionPixelSize(R.dimen.photo_title_height);

        PhotoRecycleAdapter() {
            setHasStableIds(true);
        }

        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            if (holder instanceof PhotoGroupHolder) {
                PhotoGroupHolder groupHolder = (PhotoGroupHolder) holder;
                groupHolder.refreshView(position);
            } else if (holder instanceof PhotoHolder) {
                PhotoHolder photoHolder = (PhotoHolder) holder;
                photoHolder.refreshView(position);
            } else if (holder instanceof VideoViewHolder) {
                VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
                videoViewHolder.refreshView(position);
            }

        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_HEAD: {

                    NewPhotoTitleItemBinding binding = NewPhotoTitleItemBinding.inflate(LayoutInflater.from(containerActivity), parent, false);

                    return new PhotoGroupHolder(binding);
                }
                case VIEW_TYPE_CONTENT: {

                    NewPhotoGridlayoutItemBinding binding = NewPhotoGridlayoutItemBinding.inflate(LayoutInflater.from(containerActivity), parent, false);

                    return new PhotoHolder(binding);
                }
                case VIEW_TYPE_VIDEO: {


                    VideoItemBinding binding = VideoItemBinding.inflate(LayoutInflater.from(containerActivity), parent, false);

                    return new VideoViewHolder(binding);

                }
            }

            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return mAdapterItemTotalCount;
        }

        @Override
        public int getItemViewType(int position) {
            if (mMapKeyIsPhotoPositionValueIsPhotoDate.containsKey(position))
                return VIEW_TYPE_HEAD;
            else {

                Media media = mMapKeyIsPhotoPositionValueIsPhoto.get(position);

                if (media instanceof Video)
                    return VIEW_TYPE_VIDEO;
                else
                    return VIEW_TYPE_CONTENT;

            }

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

                Media media = mMapKeyIsPhotoPositionValueIsPhoto.get(position);

                if (media == null) {
                    title = Util.DEFAULT_DATE;
                } else
                    title = mMapKeyIsPhotoPositionValueIsPhoto.get(position).getDate();

            }

            if (title.contains(Util.DEFAULT_DATE)) {
                return containerActivity.getString(R.string.unknown_time);
            } else {

                return title;

//                String[] titleSplit = title.split("-");
//                return titleSplit[0] + "年" + titleSplit[1] + "月";

            }

        }
    }

    public class NewPhotoGroupViewModel {

        private String photoTitle;

        public final ObservableBoolean photoTitleSelect = new ObservableBoolean(false);

        private boolean showSpacingSecondLayout = false;

        private String date;

        public String getPhotoTitle() {
            return photoTitle;
        }

        public void setPhotoTitle(String photoTitle) {
            this.photoTitle = photoTitle;
        }

        public boolean isShowSpacingSecondLayout() {
            return showSpacingSecondLayout;
        }

        public void setShowSpacingSecondLayout(boolean showSpacingSecondLayout) {
            this.showSpacingSecondLayout = showSpacingSecondLayout;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public void photoTitleOnClick(NewPhotoGroupViewModel newPhotoGroupViewModel) {

            if (mSelectMode) {

                List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(date);

                boolean selected = newPhotoGroupViewModel.photoTitleSelect.get();

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

                    newPhotoGroupViewModel.photoTitleSelect.set(!selected);

                    for (Media media : mediaList)
                        media.setSelected(!selected);
                }

                mPhotoRecycleAdapter.notifyDataSetChanged();

                calcSelectedPhoto();

                onPhotoItemClick();
            }

        }

    }

    private void onPhotoItemClick() {

        if (mPhotoListListener != null)
            mPhotoListListener.onPhotoItemClick(mSelectCount);

    }


    private class PhotoGroupHolder extends BindingViewHolder {

        TextView mPhotoTitle;

        LinearLayout mPhotoTitleContainer;

        PhotoGroupHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            NewPhotoTitleItemBinding binding = (NewPhotoTitleItemBinding) viewDataBinding;

            mPhotoTitle = binding.photoGroupTv;

            mPhotoTitleContainer = binding.photoTitleContainer;

        }

        public void refreshView(int groupPosition) {

            String date = mMapKeyIsPhotoPositionValueIsPhotoDate.get(groupPosition);

            NewPhotoGroupViewModel newPhotoGroupViewModel = new NewPhotoGroupViewModel();

            newPhotoGroupViewModel.setDate(date);

            if (groupPosition == 0) {
                newPhotoGroupViewModel.setShowSpacingSecondLayout(true);
            } else {
                newPhotoGroupViewModel.setShowSpacingSecondLayout(false);
            }

            mPhotoTitle.setTypeface(mTypeface);

            if (date.equals(Util.DEFAULT_DATE)) {
                newPhotoGroupViewModel.setPhotoTitle(containerActivity.getString(R.string.unknown_time));
            } else {
                newPhotoGroupViewModel.setPhotoTitle(date);
            }

            if (mSelectMode) {

                if (!isPhotoTitleContainerHasTranslation()) {
                    if (mUseAnim) {
                        showPhotoTitleSelectImgAnim();
                    } else
                        showPhotoTitleSelectImg();
                }

                List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(date);
                int selectNum = 0;
                for (Media media : mediaList) {

                    if (alreadySelectedImageKeysFromChooseActivity != null && alreadySelectedImageKeysFromChooseActivity.contains(media.getKey()))
                        media.setSelected(true);

                    if (media.isSelected())
                        selectNum++;
                }
                if (selectNum == mediaList.size())
                    newPhotoGroupViewModel.photoTitleSelect.set(true);
                else
                    newPhotoGroupViewModel.photoTitleSelect.set(false);

            } else {

                if (isPhotoTitleContainerHasTranslation()) {

                    if (mUseAnim) {
                        dismissPhotoTitleSelectImgAnim();
                    } else
                        dismissPhotoTitleSelectImg();

                }

            }

            getViewDataBinding().setVariable(BR.newPhotoGroupViewModel, newPhotoGroupViewModel);
            getViewDataBinding().executePendingBindings();

        }

        private boolean isPhotoTitleContainerHasTranslation() {

            return mPhotoTitleContainer.getTranslationX() == 0;

        }

        private void showPhotoTitleSelectImgAnim() {

//            Animation animation = AnimationUtils.loadAnimation(containerActivity, R.anim.show_right_item_anim);
//
//            animation.setInterpolator(new LinearInterpolator());
//
//            animation.setAnimationListener(new BaseAnimationListener() {
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    super.onAnimationEnd(animation);
//
//                    showPhotoTitleSelectImg();
//
//                }
//            });
//
//            mPhotoTitleContainer.startAnimation(animation);

            showPhotoTitleSelectImgAnimator(false);

        }

        private void dismissPhotoTitleSelectImgAnim() {

//            Animation animation = AnimationUtils.loadAnimation(containerActivity, R.anim.dismiss_right_item_anim);
//
//            animation.setInterpolator(new LinearInterpolator());
//
//            animation.setAnimationListener(new BaseAnimationListener() {
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    super.onAnimationEnd(animation);
//
//                    dismissPhotoTitleSelectImg();
//
//                }
//            });
//
//            mPhotoTitleContainer.startAnimation(animation);

            dismissPhotoTitleSelectImgAnimator(false);
        }

        private void showPhotoTitleSelectImg() {
//            mPhotoTitleSelectImg.setVisibility(View.VISIBLE);
            showPhotoTitleSelectImgAnimator(true);
        }

        private void dismissPhotoTitleSelectImg() {
//            mPhotoTitleSelectImg.setVisibility(View.GONE);
            dismissPhotoTitleSelectImgAnimator(true);
        }


        private void showPhotoTitleSelectImgAnimator(boolean immediate) {

            int valueFrom = Util.dip2px(containerActivity, -32);
            int valueTo = 0;

            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mPhotoTitleContainer, "translationX", valueFrom, valueTo);

            startPhotoTitleContainerAnimator(objectAnimator, immediate);

//            startPhotoTitleContainerAnimator(R.animator.photo_title_select_img_translation, immediate);

        }

        private void dismissPhotoTitleSelectImgAnimator(boolean immediate) {

            int valueFrom = 0;
            int valueTo = Util.dip2px(containerActivity, -32);

            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mPhotoTitleContainer, "translationX", valueFrom, valueTo);

            startPhotoTitleContainerAnimator(objectAnimator, immediate);

//            startPhotoTitleContainerAnimator(R.animator.photo_title_select_img_translation_restore, immediate);
        }

        private void startPhotoTitleContainerAnimator(ObjectAnimator objectAnimator, boolean immediate) {

            if (immediate)
                objectAnimator.setDuration(0);
            else
                objectAnimator.setDuration(200);

            objectAnimator.start();

        }

    }

    private class PhotoHolder extends BindingViewHolder {

        NetworkImageView mPhotoIv;

        RelativeLayout mImageLayout;

        ImageView photoSelectImg;

        private NewPhotoGridlayoutItemBinding binding;

        PhotoHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            binding = (NewPhotoGridlayoutItemBinding) viewDataBinding;

            mPhotoIv = binding.photoIv;

            mImageLayout = binding.photoItemLayout;

            photoSelectImg = binding.photoSelectImg;

        }

        public void refreshView(int position) {

            final Media currentMedia = mMapKeyIsPhotoPositionValueIsPhoto.get(position);

            if (currentMedia == null) return;

            Log.d(TAG, "PhotoHolder refreshView: media key: " + currentMedia.getKey());

            if (alreadySelectedImageKeysFromChooseActivity != null && alreadySelectedImageKeysFromChooseActivity.contains(currentMedia.getKey()))
                currentMedia.setSelected(true);

            PhotoItemViewModel prePhotoItemViewModel = binding.getPhotoItemViewModel();

            final PhotoItemViewModel photoItemViewModel;

            if (prePhotoItemViewModel != null) {

                photoItemViewModel = prePhotoItemViewModel;

            } else {
                photoItemViewModel = new PhotoItemViewModel();
                photoItemViewModel.showPhotoSelectImg.set(currentMedia.isSelected());

            }

            photoItemViewModel.showGifCorner.set(MediaUtil.checkMediaIsGif(currentMedia));

            binding.setPhotoItemViewModel(photoItemViewModel);

            mImageLoader.setTag(position);

            HttpRequest httpRequest;

            if (!mIsFling) {

                httpRequest = currentMedia.getImageThumbUrl(containerActivity);

            } else {

                httpRequest = currentMedia.getImageSmallThumbUrl(containerActivity);

            }

            MediaUtil.setMediaImageUrl(currentMedia, mPhotoIv, httpRequest, mImageLoader);

            List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(currentMedia.getDate());

            int temporaryPosition = 0;

            if (mediaList == null) {

                Log.d(TAG, "refreshView: media list is null,currentMedia getDate:" + currentMedia.getDate());

            } else {

                temporaryPosition = getMediaPosition(mediaList, currentMedia);

            }

            setPhotoItemMargin(temporaryPosition, mImageLayout);

            final int mediaInListPosition = temporaryPosition;

            setMediaSelectImg(mPhotoIv, currentMedia, photoItemViewModel.showPhotoSelectImg);

//            getViewDataBinding().setVariable(BR.showPhotoSelectImg, showPhotoSelectImg);
//            getViewDataBinding().executePendingBindings();

            mImageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectMode) {

                        handleMediaOnClickWhenSelectMode(currentMedia, mPhotoIv, photoItemViewModel.showPhotoSelectImg);

                    } else {

                        if (mEnteringPhotoSlider)
                            return;

                        int initialPhotoPosition = getInitialPhotoPosition(currentMedia);

                        Intent intent = new Intent();
                        intent.putExtra(Util.INITIAL_PHOTO_POSITION, initialPhotoPosition);
                        intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, false);
                        intent.setClass(containerActivity, PhotoSliderActivity.class);

                        PhotoSliderActivity.startPhotoSliderActivity(mPhotoListListener.getToolbar(), containerActivity, medias, intent, mediaInListPosition, mSpanCount, mPhotoIv, currentMedia);

                        mEnteringPhotoSlider = true;

                    }

                    Log.d(TAG, "image key:" + currentMedia.getKey());
                }
            });

            mImageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    return handleMediaOnLongClick(currentMedia, mPhotoIv, photoItemViewModel.showPhotoSelectImg);

                }
            });

        }

    }

    private int getInitialPhotoPosition(Media currentMedia) {
        int initialPhotoPosition;

        int size = medias.size();

        for (initialPhotoPosition = 0; initialPhotoPosition < size; initialPhotoPosition++) {

            Media media = medias.get(initialPhotoPosition);

            if (media.getKey().equals(currentMedia.getKey()))
                break;

        }

        Log.d(TAG, "start photo slider activity initial photo position: " + initialPhotoPosition);
        return initialPhotoPosition;
    }

    private class VideoViewHolder extends BindingViewHolder {

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

        void refreshView(final int position) {

            durationTv.setTypeface(mTypeface);

            final Video video = (Video) mMapKeyIsPhotoPositionValueIsPhoto.get(position);

            if (alreadySelectedImageKeysFromChooseActivity != null && alreadySelectedImageKeysFromChooseActivity.contains(video.getKey()))
                video.setSelected(true);

            ObservableBoolean preShowPhotoSelectImg = binding.getShowPhotoSelectImg();

            final ObservableBoolean showPhotoSelectImg;

            if (preShowPhotoSelectImg != null) {

                showPhotoSelectImg = preShowPhotoSelectImg;
            } else {
                showPhotoSelectImg = new ObservableBoolean(video.isSelected());

                binding.setShowPhotoSelectImg(showPhotoSelectImg);
            }

            binding.setVideo(video);

            setMediaSelectImg(networkImageView, video, showPhotoSelectImg);

            List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(video.getDate());

            int temporaryPosition = 0;

            if (mediaList == null) {

                Log.d(TAG, "refreshView: media list is null,currentVideo getDate:" + video.getDate());

            } else {

                temporaryPosition = getMediaPosition(mediaList, video);

            }

            final int mediaInListPosition = temporaryPosition;

            setPhotoItemMargin(mediaInListPosition, viewGroup);

            if (video.isLocal() && video.getThumb().isEmpty() && video.getMiniThumbPath().isEmpty())
                return;

            mImageLoader.setTag(position);

            HttpRequest httpRequest;

            if (!mIsFling) {

                httpRequest = video.getImageThumbUrl(containerActivity);

            } else {

                httpRequest = video.getImageSmallThumbUrl(containerActivity);

            }

            MediaUtil.setMediaImageUrl(video, networkImageView, httpRequest, mImageLoader);

            viewGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mSelectMode) {

                        handleMediaOnClickWhenSelectMode(video, networkImageView, showPhotoSelectImg);

                    } else {

//                        PlayVideoActivity.startPlayVideoActivity(containerActivity, video);

                        int initialPhotoPosition = getInitialPhotoPosition(video);

                        Intent intent = new Intent();
                        intent.putExtra(Util.INITIAL_PHOTO_POSITION, initialPhotoPosition);
                        intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, false);
                        intent.setClass(containerActivity, PhotoSliderActivity.class);

                        PhotoSliderActivity.startPhotoSliderActivity(containerActivity, medias, intent);

                    }

                }
            });


            viewGroup.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return handleMediaOnLongClick(video, networkImageView, showPhotoSelectImg);
                }
            });

        }

    }


    private void handleMediaOnClickWhenSelectMode(Media currentMedia, View view, ObservableBoolean showPhotoSelectImg) {
        if (alreadySelectedImageKeysFromChooseActivity != null && alreadySelectedImageKeysFromChooseActivity.contains(currentMedia.getKey())) {
            Toast.makeText(containerActivity, containerActivity.getString(R.string.already_select_media), Toast.LENGTH_SHORT).show();
            return;
        }

        calcSelectedPhoto();

        boolean selected = currentMedia.isSelected();

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

        currentMedia.setSelected(selected);

        mPhotoRecycleAdapter.notifyDataSetChanged();

        calcSelectedPhoto();

        onPhotoItemClick();

    }

    private boolean handleMediaOnLongClick(Media media, View view, ObservableBoolean showPhotoSelectImg) {
        if (mSelectMode)
            return true;

        if (mPhotoListListener != null)
            mPhotoListListener.onPhotoItemLongClick();

        media.setSelected(true);

        mSelectCount = 1;

        scalePhoto(view, false);

        showPhotoSelectImg.set(true);

        return true;
    }

    private void setMediaSelectImg(View view, Media currentMedia, ObservableBoolean showPhotoSelectImg) {

        if (mSelectMode) {
            boolean selected = currentMedia.isSelected();
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

            currentMedia.setSelected(false);

            if (view.getScaleX() != 1) {
                restorePhoto(view, true);

                showPhotoSelectImg.set(false);
//                    photoSelectImg.setVisibility(View.INVISIBLE);
            }

        }
    }

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


    private void setPhotoItemMargin(int mediaInListPosition, ViewGroup viewGroup) {

        int normalMargin = Util.dip2px(containerActivity, 2.5f);

        int height = mItemWidth;

        if ((mediaInListPosition + 1) % mSpanCount == 0) {

            Util.setMarginAndHeight(viewGroup, height, normalMargin, normalMargin, normalMargin, 0);

        } else {

            Util.setMarginAndHeight(viewGroup, height, normalMargin, normalMargin, 0, 0);

        }

    }

    private int getMediaPosition(List<Media> mediaList, Media media) {

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

    private class NewPhotoListScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            //fix bug#313,update scroll offset when scroll
            mJumperCallback.invalidate();
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            Log.d(TAG, "onScrollStateChanged: state: " + newState);

            if (newState == RecyclerView.SCROLL_STATE_SETTLING) {

                mIsFling = true;

            } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                if (mIsFling) {

                    mIsFling = false;

                    mPhotoRecycleAdapter.notifyDataSetChanged();

//                    for (IPhotoListListener listener : mPhotoListListeners) {
//                        listener.onPhotoListScrollFinished();
//                    }

                }

//                if (mIsScrollUp) {
//
//                    for (IPhotoListListener listener : mPhotoListListeners) {
//                        listener.onPhotoListScrollUp();
//                    }
//
//                }

            }
        }

//        @Override
//        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//            super.onScrolled(recyclerView, dx, dy);
//
//            if (mSelectMode)
//                return;
//
//            if (dy > 0) {
//
//                if (mIsScrollUp)
//                    mIsScrollUp = false;
//
//                for (IPhotoListListener listener : mPhotoListListeners) {
//                    listener.onPhotoListScrollDown();
//                }
//
//            } else if (dy < 0) {
//
//                if (!mIsScrollUp)
//                    mIsScrollUp = true;
//
//                if (mFastJumper.getState() != FastJumper.STATE_GONE) {
//                    return;
//                }
//
//                for (IPhotoListListener listener : mPhotoListListeners) {
//                    listener.onPhotoListScrollUp();
//                }
//
//            }
//
//        }
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
                    NewPhotoListDataLoader.getInstance().calcPhotoPositionNumber();
                    ((GridLayoutManager) mLayoutManager).setSpanCount(mSpanCount);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mPhotoRecycleAdapter.notifyItemRangeChanged(0, mPhotoRecycleAdapter.getItemCount());
                }

                Log.i(TAG, "pinch more");

            } else if (mOldSpan > newSpan + Util.dip2px(containerActivity, 2)) {

                if (mSpanCount < mSpanMaxCount) {
                    mSpanCount++;
                    calcPhotoItemWidth();
                    NewPhotoListDataLoader.getInstance().calcPhotoPositionNumber();
                    ((GridLayoutManager) mLayoutManager).setSpanCount(mSpanCount);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mPhotoRecycleAdapter.notifyItemRangeChanged(0, mPhotoRecycleAdapter.getItemCount());
                }

                Log.i(TAG, "pinch less");
            }

        }
    }

}
