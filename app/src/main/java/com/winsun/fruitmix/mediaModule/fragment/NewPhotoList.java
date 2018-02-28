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

import com.android.volley.toolbox.IImageLoadListener;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.databinding.NewPhotoGridlayoutItemBinding;
import com.winsun.fruitmix.databinding.NewPhotoLayoutBinding;
import com.winsun.fruitmix.databinding.NewPhotoTitleItemBinding;
import com.winsun.fruitmix.databinding.VideoItemBinding;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.media.CalcMediaDigestStrategy;
import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.PhotoSliderActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.interfaces.Page;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.NewMediaListDataLoader;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.mediaModule.model.NewPhotoListViewModel;
import com.winsun.fruitmix.mediaModule.model.MediaListConverter;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.mediaModule.viewmodel.MediaViewModel;
import com.winsun.fruitmix.mediaModule.viewmodel.PhotoItemViewModel;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationMediaDataChanged;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.upload.media.CheckMediaIsUploadStrategy;
import com.winsun.fruitmix.upload.media.InjectUploadMediaUseCase;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BindingViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    private Map<String, List<MediaViewModel>> mMapKeyIsDate;

    private Map<Integer, String> mMapKeyIsPhotoPositionValueIsPhotoDate;

    private SparseArray<MediaViewModel> mMapKeyIsPhotoPosition;

    private List<MediaViewModel> mMediaViewModels;

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

    private MediaDataSourceRepository mMediaDataSourceRepository;

    private boolean hasCallStartUpload = false;

    private CalcMediaDigestStrategy.CalcMediaDigestCallback calcMediaDigestCallback;

    private CheckMediaIsUploadStrategy mCheckMediaIsUploadStrategy;

    private ThreadManager mThreadManager;

    private boolean selectForCreateComment = false;

    private int mSelectLocalMediaCount;

    private boolean mEnableSwipeRefreshLayout;

    private MediaListConverter mMediaListConverter;

    private GroupRequestParam mGroupRequestParam;

    private HttpRequestFactory mHttpRequestFactory;

    public NewPhotoList(Activity activity,IPhotoListListener photoListListener) {

        mMediaDataSourceRepository = InjectMedia.provideMediaDataSourceRepository(containerActivity);

        mMediaListConverter = NewMediaListDataLoader.getInstance();

        initField(activity);

        initView();

        mEnableSwipeRefreshLayout = true;

        initSwipeRefreshLayout();

        setupFastJumper(true);

        setPhotoListListener(photoListListener);

    }

    public NewPhotoList(Activity activity,IPhotoListListener photoListListener, boolean showFastJumper, boolean enableSwipeRefreshLayout,
                        MediaDataSourceRepository mediaDataSourceRepository, MediaListConverter mediaListConverter,
                        GroupRequestParam groupRequestParam) {

        mMediaDataSourceRepository = mediaDataSourceRepository;

        mMediaListConverter = mediaListConverter;

        mGroupRequestParam = groupRequestParam;

        initField(activity);

        initView();

        mEnableSwipeRefreshLayout = enableSwipeRefreshLayout;

        initSwipeRefreshLayout();

        setupFastJumper(showFastJumper);

        setPhotoListListener(photoListListener);

    }

    private void initField(Activity activity) {

        containerActivity = activity;

        mMediaViewModels = Collections.emptyList();

        mMapKeyIsDate = Collections.emptyMap();

        mMapKeyIsPhotoPositionValueIsPhotoDate = Collections.emptyMap();

        mMapKeyIsPhotoPosition = new SparseArray<>();

        mPinchScaleDetector = new ScaleGestureDetector(containerActivity, new PinchScaleListener());

        mTypeface = Typeface.createFromAsset(containerActivity.getAssets(), "fonts/Roboto-Medium.ttf");

        calcMediaDigestCallback = new CalcMediaDigestStrategy.CalcMediaDigestCallback() {
            @Override
            public void handleFinished() {

                refreshViewForce();

            }

            @Override
            public void handleNothing() {

            }
        };

        mMediaDataSourceRepository.registerCalcDigestCallback(calcMediaDigestCallback);

        mCheckMediaIsUploadStrategy = CheckMediaIsUploadStrategy.getInstance();

        mThreadManager = ThreadManagerImpl.getInstance();

        mHttpRequestFactory = InjectHttp.provideHttpRequestFactory(containerActivity);

    }

    private void initView() {

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

        mRecyclerView = binding.photoRecyclerview;

        calcScreenWidth();

        calcPhotoItemWidth();

        mRecyclerView.addOnScrollListener(new NewPhotoListScrollListener());

        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mPinchScaleDetector.onTouchEvent(event);

                return false;
            }
        });

        mPhotoRecycleAdapter = new PhotoRecycleAdapter();

        setupRecyclerView();

    }

    private void initSwipeRefreshLayout() {

        if (!mEnableSwipeRefreshLayout) {

            mSwipeRefreshLayout.setEnabled(false);

            return;
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refreshStationMediaForce();

            }
        });
    }

    private void finishSwipeRefreshAnimation() {

        if (!mEnableSwipeRefreshLayout)
            return;

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

    public void setSelectForCreateComment(boolean selectForCreateComment) {
        this.selectForCreateComment = selectForCreateComment;
    }

    @Override
    public boolean canEnterSelectMode() {
        return mMediaViewModels.size() > 0;
    }

    public void setSelectMode(boolean selectMode) {

        mSelectMode = selectMode;

        if (mSelectMode) {
            clearSelectedPhoto();

            if (mEnableSwipeRefreshLayout)
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

        mMediaDataSourceRepository.getStationMediaForceRefresh(new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<Media>() {
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

        mMediaDataSourceRepository.getStationMediaForceRefresh(new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<Media>() {
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

            if (mMediaViewModels.size() < totalLocalMediaCount) {

                mMediaDataSourceRepository.getLocalMedia(new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<Media>() {
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

        if (mSelectMode && !(loadingViewModel.showLoading.get()))
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

        if (loadingViewModel.showLoading.get() && mPhotoListListener != null)
            mPhotoListListener.onNoPhotoItem(true);

        mMediaDataSourceRepository.getMedia(new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<Media>() {
            @Override
            public void onSucceed(final List<Media> data, final OperationResult operationResult) {

                Log.d(TAG, "onSucceed: get media size: " + data.size());

                if (!hasCallStartUpload && mGroupRequestParam == null) {

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

        boolean dataChanged = operationResult.getOperationResultType() == OperationResultType.MEDIA_DATA_CHANGED;

        if (dataChanged)
            mMediaListConverter.setNeedRefreshData(true);

        if (!mIsLoaded || dataChanged) {

            mMediaListConverter.convertData(new NewMediaListDataLoader.OnPhotoListDataListener() {
                @Override
                public void onDataLoadFinished() {

                    Log.d(TAG, "onDataLoadFinished: ");

                    doAfterReloadData(mMediaListConverter);

                }
            }, data);

        }

    }

    public boolean isLoaded() {

        return mIsLoaded;

    }

    private void doAfterReloadData(MediaListConverter loader) {

        mAdapterItemTotalCount = loader.getAdapterItemTotalCount();

        //fix crash:java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid item position 10(offset:10).state:611 at android.support.v7.widget.RecyclerView$Recycler.getViewForPosition(RecyclerView.java:5202)
        mMapKeyIsDate = new HashMap<>(loader.getMapKeyIsDateList());
        mMapKeyIsPhotoPositionValueIsPhotoDate = new HashMap<>(loader.getMapKeyIsPhotoPositionValueIsPhotoDate());
        mMapKeyIsPhotoPosition = loader.getMapKeyIsPhotoPosition();
        mMediaViewModels = new ArrayList<>(loader.getMediaViewModels());

        clearSelectedPhoto();

        loadingViewModel.showLoading.set(false);

        if (mMediaViewModels.size() == 0) {

            noContentViewModel.showNoContent.set(true);

            newPhotoListViewModel.showContent.set(false);

            if (mPhotoListListener != null)
                mPhotoListListener.onNoPhotoItem(true);

        } else {

            noContentViewModel.showNoContent.set(false);

            newPhotoListViewModel.showContent.set(true);

            //modify by liang.wu: add handler for crash:Cannot call this method while RecyclerView is computing a layout or scrolling

            if (!mRecyclerView.isComputingLayout()) {

                try {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            mPhotoRecycleAdapter.notifyDataSetChanged();

                            mIsLoaded = true;
                        }
                    });

                } catch (Exception e) {

                    e.printStackTrace();
                }

            }

            if (mPhotoListListener != null)
                mPhotoListListener.onNoPhotoItem(false);

            if (!mPreLoadPhoto) {
                mPreLoadPhoto = true;

                mThreadManager.runOnCacheThread(new Runnable() {
                    @Override
                    public void run() {

                        Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);

                        loadSmallThumbnail(mMediaViewModels);
                    }
                });

            }

        }

    }

    private void loadSmallThumbnail(final List<MediaViewModel> mediaViewModels) {

        String url;

        List<MediaViewModel> preLoadMediaMiniThumbs = new ArrayList<>(mediaViewModels);
        MediaViewModel mediaViewModel;
        Iterator<MediaViewModel> iterator = preLoadMediaMiniThumbs.iterator();
        while (iterator.hasNext()) {
            mediaViewModel = iterator.next();
            if (mediaViewModel.getMedia().isLocal())
                iterator.remove();
        }

        Log.i(TAG, "pre load mediaViewModel size: " + preLoadMediaMiniThumbs.size());

        int preLoadMediaMiniThumbSize = preLoadMediaMiniThumbs.size() > 100 ? 100 : preLoadMediaMiniThumbs.size();

        for (int i = 0; i < preLoadMediaMiniThumbSize; i++) {

            if (mCancelPreLoadPhoto)
                break;

            mediaViewModel = preLoadMediaMiniThumbs.get(i);

            Media media = mediaViewModel.getMedia();

            if (media instanceof Video)
                continue;

            HttpRequest httpRequest = media.getImageSmallThumbUrl(mHttpRequestFactory);

            url = httpRequest.getUrl();

            ArrayMap<String, String> header = new ArrayMap<>();
            header.put(httpRequest.getHeaderKey(), httpRequest.getHeaderValue());

            mImageLoader.setHeaders(header);

            mImageLoader.preLoadMediaSmallThumb(url, mItemWidth, mItemWidth);

        }

    }

    private void cancelPreLoadMediaMiniThumb() {
        mCancelPreLoadPhoto = true;

        if (mImageLoader != null)
            mImageLoader.cancelAllPreLoadMedia();

    }

    private void initFastJumper() {
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

        mScrollCalculator = new LinearScrollCalculator(mRecyclerView) {

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

        mJumperCallback.setScrollCalculator(mScrollCalculator);

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

    private void initGridLayoutManager() {

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

    }

    private void setupRecyclerView() {

        initGridLayoutManager();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mPhotoRecycleAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    private void setupFastJumper(boolean needSetUpFastJumper) {

        if (needSetUpFastJumper) {

            initFastJumper();

            mFastJumper.attachToRecyclerView(mRecyclerView);
            mFastJumper.invalidate();

        }

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

    private void clearSelectedPhoto() {

        if (mMapKeyIsPhotoPosition == null || mMapKeyIsPhotoPosition.size() == 0)
            return;

        MediaViewModel mediaViewModel;

        for (int i = 0; i < mMapKeyIsPhotoPosition.size(); i++) {

            mediaViewModel = mMapKeyIsPhotoPosition.get(mMapKeyIsPhotoPosition.keyAt(i));

            if (mediaViewModel != null)
                mediaViewModel.setSelected(false);
        }

    }

    private void calcSelectedPhoto() {

        int selectCount = 0;

        int selectLocalMediaCount = 0;

        for (MediaViewModel mediaViewModel : mMediaViewModels) {
            if (mediaViewModel.isSelected()) {

                selectCount++;

                if (mediaViewModel.getMedia().isLocal())
                    selectLocalMediaCount++;

            }

        }


        mSelectCount = selectCount;

        mSelectLocalMediaCount = selectLocalMediaCount;

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

                int size = mMapKeyIsPhotoPosition.size();

                for (int i = 0; i < size; i++) {
                    media = mMapKeyIsPhotoPosition.valueAt(i).getMedia();
                    if (media.getKey().equals(currentMediaKey))
                        currentMedia = media;
                }

                if (currentMedia == null) return;

                View newSharedElement = mRecyclerView.findViewWithTag(createMediaThumbHttpRequest(currentMedia).getUrl());

                if (newSharedElement == null)
                    newSharedElement = mRecyclerView.findViewWithTag(createMediaSmallThumbHttpRequest(currentMedia).getUrl());

                if (newSharedElement == null)
                    return;

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

        mMediaDataSourceRepository.unregisterCalcDigestCallback(calcMediaDigestCallback);
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

            int size = mMapKeyIsPhotoPosition.size();

            for (int i = 0; i < size; i++) {
                media = mMapKeyIsPhotoPosition.valueAt(i).getMedia();
                if (media.getKey().equals(currentMediaKey))
                    scrollToPosition = mMapKeyIsPhotoPosition.keyAt(i);
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

        int index = -1;

        for (int i = 0; i < mMapKeyIsPhotoPosition.size(); i++) {

            MediaViewModel mediaViewModel = mMapKeyIsPhotoPosition.valueAt(i);

            if (mediaViewModel.getMedia().equals(video)) {

                index = i;
                break;

            }

        }

        if (index == -1)
            return;

        int key = mMapKeyIsPhotoPosition.keyAt(index);

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

                MediaViewModel mediaViewModel = mMapKeyIsPhotoPosition.get(position);

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

                MediaViewModel mediaViewModel = mMapKeyIsPhotoPosition.get(position);

                if (mediaViewModel == null) {
                    title = Util.DEFAULT_DATE;
                } else {

                    Media media = mediaViewModel.getMedia();

                    if (media == null)
                        title = Util.DEFAULT_DATE;
                    else
                        title = media.getDateWithoutHourMinSec();

                }

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

                List<MediaViewModel> mediaViewModels = mMapKeyIsDate.get(date);

                boolean selected = newPhotoGroupViewModel.photoTitleSelect.get();

                int unSelectNumInList = 0;

                int unSelectLocalMediaNum = 0;

                for (MediaViewModel mediaViewModel : mediaViewModels) {
                    if (!mediaViewModel.isSelected()) {

                        unSelectNumInList++;

                        if (mediaViewModel.getMedia().isLocal())
                            unSelectLocalMediaNum++;

                    }

                }

                calcSelectedPhoto();

                if (!selected && selectForCreateComment) {

                    if (unSelectLocalMediaNum + mSelectLocalMediaCount > 1) {

                        Toast.makeText(containerActivity, containerActivity.getString(R.string.only_support_upload_for_one_local_media), Toast.LENGTH_SHORT).show();

                    }

                    int newSelectLocalItemCount = mSelectLocalMediaCount;
                    int newSelectLocalItemTotalCount = 1;

                    for (MediaViewModel mediaViewModel : mediaViewModels) {

                        if (!mediaViewModel.isSelected()) {

                            if (mediaViewModel.getMedia().isLocal()) {

                                if (newSelectLocalItemCount < newSelectLocalItemTotalCount) {

                                    mediaViewModel.setSelected(true);

                                    newSelectLocalItemCount++;

                                }

                            } else
                                mediaViewModel.setSelected(true);

                        }

                    }

                } else if (!selected && unSelectNumInList + mSelectCount > Util.MAX_PHOTO_SIZE) {
                    Toast.makeText(containerActivity, containerActivity.getString(R.string.max_select_photo), Toast.LENGTH_SHORT).show();

                    int newSelectItemCount = 0;
                    int newSelectItemTotalCount = Util.MAX_PHOTO_SIZE - mSelectCount;
                    if (newSelectItemTotalCount != 0) {

                        for (MediaViewModel mediaViewModel : mediaViewModels) {

                            if (!mediaViewModel.isSelected()) {
                                mediaViewModel.setSelected(true);
                                newSelectItemCount++;
                                if (newSelectItemCount == newSelectItemTotalCount)
                                    break;
                            }

                        }
                    }

                } else {

                    newPhotoGroupViewModel.photoTitleSelect.set(!selected);

                    for (MediaViewModel mediaViewModel : mediaViewModels)
                        mediaViewModel.setSelected(!selected);
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

                List<MediaViewModel> mediaViewModels = mMapKeyIsDate.get(date);
                int selectNum = 0;
                for (MediaViewModel mediaViewModel : mediaViewModels) {

                    if (alreadySelectedImageKeysFromChooseActivity != null && alreadySelectedImageKeysFromChooseActivity.contains(mediaViewModel.getMedia().getKey()))
                        mediaViewModel.setSelected(true);

                    if (mediaViewModel.isSelected())
                        selectNum++;
                }
                if (selectNum == mediaViewModels.size())
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

    private class BaseMediaHolder extends BindingViewHolder {

        public BaseMediaHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        void checkMediaSelected(MediaViewModel mediaViewModel) {

            if (alreadySelectedImageKeysFromChooseActivity != null && alreadySelectedImageKeysFromChooseActivity.contains(mediaViewModel.getMedia().getKey()))
                mediaViewModel.setSelected(true);

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

            if (currentMedia.isLocal()) {

                if (mCheckMediaIsUploadStrategy.isMediaUploaded(currentMedia)) {
                    photoItemViewModel.showCloudOff.set(false);
                } else {
                    photoItemViewModel.showCloudOff.set(true);
                }

            } else
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

        public void refreshView(int position) {

            final MediaViewModel mediaViewModel = mMapKeyIsPhotoPosition.get(position);

            if (mediaViewModel == null) return;

            currentMedia = mediaViewModel.getMedia();

            Log.d(TAG, "PhotoHolder refreshView: media key: " + currentMedia.getKey());

            checkMediaSelected(mediaViewModel);

            PhotoItemViewModel prePhotoItemViewModel = binding.getPhotoItemViewModel();

            final PhotoItemViewModel photoItemViewModel = getPhotoItemViewModel(prePhotoItemViewModel);

            refreshPhotoSelectImg(mediaViewModel, photoItemViewModel);

            binding.setPhotoItemViewModel(photoItemViewModel);

            binding.executePendingBindings();

            mImageLoader.setTag(position);

            HttpRequest httpRequest;

            if (!mIsFling) {

                httpRequest = createMediaThumbHttpRequest(currentMedia);

            } else {

                httpRequest = createMediaSmallThumbHttpRequest(currentMedia);

            }

            mPhotoIv.registerImageLoadListener(new IImageLoadListener() {
                @Override
                public void onImageLoadFinish(String url, View view) {

                    refreshGIFAndCloudOffIcon(photoItemViewModel, currentMedia);

                }

                @Override
                public void onImageLoadFail(String url, View view) {

                }
            });

            MediaUtil.setMediaImageUrl(currentMedia, mPhotoIv, httpRequest, mImageLoader);

            List<MediaViewModel> mediaViewModels = mMapKeyIsDate.get(currentMedia.getDateWithoutHourMinSec());

            int temporaryPosition = 0;

            if (mediaViewModels == null) {

                Log.d(TAG, "refreshView: media list is null,currentMedia getDateWithoutHourMinSec:" + currentMedia.getDateWithoutHourMinSec());

                mediaViewModels = mMediaViewModels;

            }

            temporaryPosition = getMediaPosition(mediaViewModels, currentMedia);

            setPhotoItemMargin(temporaryPosition, mImageLayout);

            final int mediaInListPosition = temporaryPosition;

            setMediaSelectImg(mPhotoIv, mediaViewModel, photoItemViewModel.showPhotoSelectImg);

//            getViewDataBinding().setVariable(BR.showPhotoSelectImg, showPhotoSelectImg);
//            getViewDataBinding().executePendingBindings();

            mImageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectMode) {

                        handleMediaOnClickWhenSelectMode(mediaViewModel, mPhotoIv, photoItemViewModel.showPhotoSelectImg);

                    } else {

                        startPhotoSliderActivity(currentMedia, mPhotoIv, mediaInListPosition);

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

    private int getInitialPhotoPosition(Media currentMedia) {
        int initialPhotoPosition;

        int size = mMediaViewModels.size();

        for (initialPhotoPosition = 0; initialPhotoPosition < size; initialPhotoPosition++) {

            Media media = mMediaViewModels.get(initialPhotoPosition).getMedia();

            if (media.getKey().equals(currentMedia.getKey()))
                break;

        }

        Log.d(TAG, "start photo slider activity initial photo position: " + initialPhotoPosition);
        return initialPhotoPosition;
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

        public void refreshView(final int position) {

            durationTv.setTypeface(mTypeface);

            final MediaViewModel mediaViewModel = mMapKeyIsPhotoPosition.get(position);

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

            List<MediaViewModel> mediaViewModels = mMapKeyIsDate.get(video.getDateWithoutHourMinSec());

            int temporaryPosition = 0;

            if (mediaViewModels == null) {

                Log.d(TAG, "refreshView: media list is null,currentVideo getDateWithoutHourMinSec:" + video.getDateWithoutHourMinSec());

                mediaViewModels = mMediaViewModels;
            }

            temporaryPosition = getMediaPosition(mediaViewModels, video);

            final int mediaInListPosition = temporaryPosition;

            setPhotoItemMargin(mediaInListPosition, viewGroup);

            if (video.isLocal() && video.getThumb().isEmpty() && video.getMiniThumbPath().isEmpty())
                return;

            mImageLoader.setTag(position);

            HttpRequest httpRequest;

            if (!mIsFling) {

                httpRequest = createMediaThumbHttpRequest(video);

            } else {

                httpRequest = createMediaSmallThumbHttpRequest(video);

            }

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

                        startPhotoSliderActivity(video, networkImageView);

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


    private void handleMediaOnClickWhenSelectMode(MediaViewModel mediaViewModel, View view, ObservableBoolean showPhotoSelectImg) {
        if (alreadySelectedImageKeysFromChooseActivity != null && alreadySelectedImageKeysFromChooseActivity.contains(mediaViewModel.getMedia().getKey())) {
            Toast.makeText(containerActivity, containerActivity.getString(R.string.already_select_media), Toast.LENGTH_SHORT).show();
            return;
        }

        calcSelectedPhoto();

        boolean selected = mediaViewModel.isSelected();

        if (!selected && selectForCreateComment && mSelectLocalMediaCount + 1 > 1) {

            Toast.makeText(containerActivity, containerActivity.getString(R.string.only_support_upload_for_one_local_media), Toast.LENGTH_SHORT).show();

            return;

        } else if (!selected && mSelectCount >= Util.MAX_PHOTO_SIZE) {

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

        mPhotoRecycleAdapter.notifyDataSetChanged();

        calcSelectedPhoto();

        onPhotoItemClick();

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

    private int getMediaPosition(List<MediaViewModel> mediaViewModels, Media media) {

        int position = 0;
        int size = mediaViewModels.size();

        for (int i = 0; i < size; i++) {
            Media media1 = mediaViewModels.get(i).getMedia();

            if (media.getKey().equals(media1.getKey())) {
                position = i;
                break;
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
                    mMediaListConverter.calcPhotoPositionNumber();
                    ((GridLayoutManager) mLayoutManager).setSpanCount(mSpanCount);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mPhotoRecycleAdapter.notifyItemRangeChanged(0, mPhotoRecycleAdapter.getItemCount());
                }

                Log.i(TAG, "pinch more");

            } else if (mOldSpan > newSpan + Util.dip2px(containerActivity, 2)) {

                if (mSpanCount < mSpanMaxCount) {
                    mSpanCount++;
                    calcPhotoItemWidth();
                    mMediaListConverter.calcPhotoPositionNumber();
                    ((GridLayoutManager) mLayoutManager).setSpanCount(mSpanCount);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mPhotoRecycleAdapter.notifyItemRangeChanged(0, mPhotoRecycleAdapter.getItemCount());
                }

                Log.i(TAG, "pinch less");
            }

        }
    }

    private HttpRequest createMediaThumbHttpRequest(Media media) {

        if (mGroupRequestParam != null)
            return media.getImageThumbUrl(mHttpRequestFactory, mGroupRequestParam);
        else
            return media.getImageThumbUrl(mHttpRequestFactory);

    }

    private HttpRequest createMediaSmallThumbHttpRequest(Media media) {

        if (mGroupRequestParam != null)
            return media.getImageSmallThumbUrl(mHttpRequestFactory, mGroupRequestParam);
        else
            return media.getImageSmallThumbUrl(mHttpRequestFactory);

    }

    private void startPhotoSliderActivity(Media media, NetworkImageView networkImageView, int mediaInListPosition) {

        if (mGroupRequestParam != null) {

            PhotoSliderActivity.startPhotoSliderActivity(mPhotoListListener.getToolbar(), containerActivity, mMediaViewModels,
                    mGroupRequestParam.getGroupUUID(), mGroupRequestParam.getStationID(), mSpanCount, networkImageView, media);


        } else {

            if (mEnteringPhotoSlider)
                return;

            int initialPhotoPosition = getInitialPhotoPosition(media);

            PhotoSliderActivity.startPhotoSliderActivity(mPhotoListListener.getToolbar(), containerActivity, mMediaViewModels,
                    initialPhotoPosition, mediaInListPosition, mSpanCount, networkImageView, media);

            mEnteringPhotoSlider = true;

        }

    }

    private void startPhotoSliderActivity(Video video, NetworkImageView networkImageView) {

        if (mGroupRequestParam != null) {

            PhotoSliderActivity.startPhotoSliderActivity(mPhotoListListener.getToolbar(), containerActivity, mMediaViewModels,
                    mGroupRequestParam.getGroupUUID(), mGroupRequestParam.getStationID(), mSpanCount, networkImageView, video);


        } else {

            int initialPhotoPosition = getInitialPhotoPosition(video);

            Intent intent = new Intent();
            intent.putExtra(Util.INITIAL_PHOTO_POSITION, initialPhotoPosition);
            intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, false);
            intent.setClass(containerActivity, PhotoSliderActivity.class);

            PhotoSliderActivity.startPhotoSliderActivity(containerActivity, mMediaViewModels, initialPhotoPosition);

        }

    }


}
