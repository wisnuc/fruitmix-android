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
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.databinding.NewPhotoGridlayoutItemBinding;
import com.winsun.fruitmix.databinding.NewPhotoLayoutBinding;
import com.winsun.fruitmix.databinding.NewPhotoTitleItemBinding;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
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
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationMediaDataChanged;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.upload.media.InjectUploadMediaUseCase;
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
public class NewPhotoList implements Page, IShowHideFragmentListener {

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

        mediaDataSourceRepository.getStationMediaForceRefresh(new BaseLoadDataCallbackImpl<Media>() {
            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                Log.d(TAG, "onSucceed: refresh station media force");

                handleGetMediaSucceed(data, new OperationMediaDataChanged());
            }

            @Override
            public void onFail(OperationResult operationResult) {
                super.onFail(operationResult);

                finishSwipeRefreshAnimation();

            }
        });

    }

    @Override
    public void refreshViewForce() {

        mediaDataSourceRepository.getStationMediaForceRefresh(new BaseLoadDataCallback<Media>() {
            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {

                Log.d(TAG, "onSucceed: refresh view force");

                handleGetMediaSucceed(data, new OperationMediaDataChanged());

            }

            @Override
            public void onFail(OperationResult operationResult) {

                Log.d(TAG, "onFail: refresh view force");

            }
        });

    }

    //prevent load media from system db return 0,but UploadMediaUseCase load local media return correct count,then refresh view
    public void onUploadMediaCountChanged(int totalLocalMediaCount) {

        if (mIsLoaded) {

            if (medias.size() < totalLocalMediaCount) {

                mediaDataSourceRepository.getLocalMedia(new BaseLoadDataCallback<Media>() {
                    @Override
                    public void onSucceed(List<Media> data, OperationResult operationResult) {

                        handleGetMediaSucceed(data, new OperationMediaDataChanged());

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                    }
                });

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

        mediaDataSourceRepository.getMedia(new BaseLoadDataCallback<Media>() {
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

            }
        });

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

    public void cancelPreLoadMediaMiniThumb() {
        mCancelPreLoadPhoto = true;
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

    private class PhotoRecycleAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private static final int VIEW_TYPE_HEAD = 0x1000;
        private static final int VIEW_TYPE_CONTENT = 0x1001;

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

                Media media = mMapKeyIsPhotoPositionValueIsPhoto.get(position);

                if (media == null) {
                    title = Util.DEFAULT_DATE;
                } else
                    title = mMapKeyIsPhotoPositionValueIsPhoto.get(position).getDate();

            }

            if (title.contains(Util.DEFAULT_DATE)) {
                return containerActivity.getString(R.string.unknown_time);
            } else {
                String[] titleSplit = title.split("-");
                return titleSplit[0] + "年" + titleSplit[1] + "月";

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

            ObservableBoolean preShowPhotoSelectImg = binding.getShowPhotoSelectImg();

            final ObservableBoolean showPhotoSelectImg;

            if (preShowPhotoSelectImg != null) {

                showPhotoSelectImg = preShowPhotoSelectImg;
            } else {
                showPhotoSelectImg = new ObservableBoolean(currentMedia.isSelected());

                binding.setShowPhotoSelectImg(showPhotoSelectImg);
            }

            mImageLoader.setTag(position);

            String imageUrl;

            mImageLoader.setShouldCache(!currentMedia.isLocal());

            if (currentMedia.isLocal())
                mPhotoIv.setOrientationNumber(currentMedia.getOrientationNumber());

            mPhotoIv.setBackgroundResource(R.drawable.default_place_holder);

//            mPhotoIv.setBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));

            mPhotoIv.setDefaultImageResId(R.drawable.default_place_holder);

//            mPhotoIv.setDefaultBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));

            HttpRequest httpRequest;

            if (!mIsFling) {

                httpRequest = currentMedia.getImageThumbUrl(containerActivity);

                imageUrl = httpRequest.getUrl();

            } else {

                httpRequest = currentMedia.getImageSmallThumbUrl(containerActivity);

                imageUrl = httpRequest.getUrl();

            }

            ArrayMap<String, String> header = new ArrayMap<>();
            header.put(httpRequest.getHeaderKey(), httpRequest.getHeaderValue());

            mImageLoader.setHeaders(header);

            mPhotoIv.setTag(imageUrl);

            mPhotoIv.setImageUrl(imageUrl, mImageLoader);

            List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(currentMedia.getDate());

            int temporaryPosition = 0;

            if (mediaList == null) {

                Log.d(TAG, "refreshView: media list is null,currentMedia getDate:" + currentMedia.getDate());

                Log.d(TAG, "refreshView: media list is null,map key is date,key:" + mMapKeyIsDateValueIsPhotoList.keySet());

            } else {

                temporaryPosition = getPosition(mediaList, currentMedia);

            }

            final int mediaInListPosition = temporaryPosition;

            setPhotoItemMargin(mediaInListPosition);

            if (mSelectMode) {
                boolean selected = currentMedia.isSelected();
                if (selected && mPhotoIv.getScaleX() == 1) {
                    scalePhoto(true);

                    showPhotoSelectImg.set(true);
//                    photoSelectImg.setVisibility(View.VISIBLE);

                } else if (!selected && mPhotoIv.getScaleX() != 1) {

                    restorePhoto(true);

                    showPhotoSelectImg.set(false);
//                    photoSelectImg.setVisibility(View.INVISIBLE);

                }
            } else {

                currentMedia.setSelected(false);

                if (mPhotoIv.getScaleX() != 1) {
                    restorePhoto(true);

                    showPhotoSelectImg.set(false);
//                    photoSelectImg.setVisibility(View.INVISIBLE);
                }

            }

//            getViewDataBinding().setVariable(BR.showPhotoSelectImg, showPhotoSelectImg);
//            getViewDataBinding().executePendingBindings();

            mImageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectMode) {

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
                            scalePhoto(false);

                            showPhotoSelectImg.set(true);

                        } else {
                            restorePhoto(false);

                            showPhotoSelectImg.set(false);

                        }

                        currentMedia.setSelected(selected);

                        mPhotoRecycleAdapter.notifyDataSetChanged();

                        calcSelectedPhoto();

                        onPhotoItemClick();

                    } else {

                        if (mEnteringPhotoSlider)
                            return;

                        int initialPhotoPosition;

                        int size = medias.size();

                        for (initialPhotoPosition = 0; initialPhotoPosition < size; initialPhotoPosition++) {

                            Media media = medias.get(initialPhotoPosition);

                            if (media.getKey().equals(currentMedia.getKey()))
                                break;

                        }

                        Log.d(TAG, "start photo slider activity initial photo position: " + initialPhotoPosition);

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

                    if (mSelectMode)
                        return true;

                    if (mPhotoListListener != null)
                        mPhotoListListener.onPhotoItemLongClick();

                    currentMedia.setSelected(true);

                    mSelectCount = 1;

                    scalePhoto(false);

                    showPhotoSelectImg.set(true);

                    return true;
                }
            });

        }

        private void setPhotoItemMargin(int mediaInListPosition) {

            int normalMargin = Util.dip2px(containerActivity, 2.5f);

            if ((mediaInListPosition + 1) % mSpanCount == 0) {

                Util.setMarginAndHeight(mImageLayout, mItemWidth, normalMargin, normalMargin, normalMargin, 0);

            } else {

                Util.setMarginAndHeight(mImageLayout, mItemWidth, normalMargin, normalMargin, 0, 0);

            }

        }

        private void scalePhoto(boolean immediate) {

            if (scaleAnimator != null)
                scaleAnimator.cancel();

            scaleAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.photo_scale);
            scaleAnimator.setTarget(mPhotoIv);

            if (immediate) {
                scaleAnimator.setDuration(0);
            }

            scaleAnimator.start();
        }

        private void restorePhoto(boolean immediate) {

            if (scaleAnimator != null)
                scaleAnimator.cancel();

            scaleAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.photo_restore);
            scaleAnimator.setTarget(mPhotoIv);

            if (immediate) {
                scaleAnimator.setDuration(0);
            }

            scaleAnimator.start();

        }

        private void setPhotoIvLayoutParams(int margin) {

            Util.setMargin(mPhotoIv, margin, margin, margin, margin);

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
