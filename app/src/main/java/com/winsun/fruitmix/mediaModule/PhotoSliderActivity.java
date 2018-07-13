package com.winsun.fruitmix.mediaModule;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.util.Pair;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.IImageLoadListener;
import com.android.volley.toolbox.NetworkImageView;
import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.base.data.BaseDataOperator;
import com.winsun.fruitmix.base.data.InjectBaseDataOperator;
import com.winsun.fruitmix.base.data.SCloudTokenContainer;
import com.winsun.fruitmix.base.data.retry.RefreshTokenRetryStrategy;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.component.GifTouchNetworkImageView;
import com.winsun.fruitmix.component.PinchImageView;
import com.winsun.fruitmix.component.fab.menu.SelectedMediasListener;
import com.winsun.fruitmix.databinding.ActivityPhotoSliderBinding;
import com.winsun.fruitmix.dialog.DialogFactory;
import com.winsun.fruitmix.dialog.PhotoOperationAlertDialogFactory;
import com.winsun.fruitmix.gif.GifLoader;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.anim.CustomTransitionListener;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.mediaModule.viewmodel.MediaViewModel;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.manager.InjectSCloudTokenManager;
import com.winsun.fruitmix.token.manager.TokenManager;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.util.ToastUtil;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.video.PlayVideoFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoSliderActivity extends BaseActivity implements IImageLoadListener, SCloudTokenContainer,
        SelectedMediasListener {

    public static final String TAG = "PhotoSliderActivity";

    public static final String KEY_SHOW_COMMENT_BTN = "key_show_comment_btn";
    public static final String KEY_GROUP_UUID = "key_group_uuid";

    private LinearLayout ivComment;

    private ImageView commentImg;

    private ViewPager mViewPager;

    private ImageButton mShareBtn;

    @Override
    public void setSCloudToken(String sCloudToken) {
        mSCloudToken = sCloudToken;
    }

    public class PhotoSliderViewModel {

        public final ObservableBoolean showMaskLayout = new ObservableBoolean(true);

        public final ObservableBoolean showCloudOff = new ObservableBoolean(true);

        public final ObservableField<String> titleText = new ObservableField<>();

        public final ObservableBoolean showReturnResize = new ObservableBoolean(false);

        public final ObservableBoolean showToolbar = new ObservableBoolean(true);

        public final ObservableInt returnResizeResId = new ObservableInt(R.drawable.return_resize);

        public final ObservableInt toolbarIconResId = new ObservableInt(R.drawable.ic_back);

        public final ObservableBoolean showPanelFooter = new ObservableBoolean(true);

        public void dismissShowReturnResize() {
            showReturnResize.set(false);
        }

    }

    private PhotoSliderViewModel photoSliderViewModel;

    private static List<MediaViewModel> mediaViewModels;

    private int initialPhotoPosition = 0;
    private int currentPhotoPosition = 0;

    private String groupUUID;

    private String stationID;

    private List<MediaViewModel> mediaViewModelsAlreadyLoaded;

    private boolean sInEdit = true;

    private boolean mIsFullScreen = false;

    private ImageLoader mImageLoader;

    private GifLoader mGifLoader;

    private Context mContext;

    private boolean willReturn = false;

    private boolean transitionMediaNeedShowThumb = true;

    private boolean needTransition = true;

    private boolean hasStartPostPoneEnterTransition = false;

    private SystemSettingDataSource systemSettingDataSource;

    private HttpRequestFactory mHttpRequestFactory;

    private String mSCloudToken;

    private BaseDataOperator mBaseDataOperator;

    private SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            if (willReturn) {

                Media media = mediaViewModels.get(currentPhotoPosition).getMedia();

                if (media instanceof Video)
                    return;

                String imageTag;

                imageTag = getMediaThumbHttpRequest(media, "").getUrl();

                PinchImageView view = (PinchImageView) mViewPager.findViewWithTag(imageTag);

                if (view == null) {

                    imageTag = getMediaOriginalHttpRequest(media, "").getUrl();

                    view = (PinchImageView) mViewPager.findViewWithTag(imageTag);
                }

                if (view == null) {

                    imageTag = getLargeImageHttpRequest(media, "").getUrl();

                    view = (PinchImageView) mViewPager.findViewWithTag(imageTag);

                }

                if (view == null)
                    return;

                view.setDoMatrixOnDraw(false);

                view.setScaleTypeByUser(ImageView.ScaleType.CENTER_CROP);

                if (initialPhotoPosition != currentPhotoPosition) {

                    names.clear();
                    sharedElements.clear();

                    String imageKey = media.getKey();
                    names.add(imageKey);

                    sharedElements.put(imageKey, view);

                    Log.d(TAG, "onMapSharedElements: media key:" + imageKey + " imageTag:" + imageTag);
                }
            }

        }
    };

    public static void startPhotoSliderActivity(View toolbar, Activity activity, List<MediaViewModel> transitionMediasViewModels,
                                                String groupUUID, String stationID, int spanCount, NetworkImageView transitionView, Media currentMedia) {

        int initialPhotoPosition = getMediaPosition(transitionMediasViewModels, currentMedia);

        PhotoSliderActivity.startPhotoSliderActivity(toolbar, activity, transitionMediasViewModels,
                groupUUID, stationID, initialPhotoPosition, initialPhotoPosition, spanCount, transitionView, currentMedia);

    }

    public static void startPhotoSliderActivityWithMedias(View toolbar, Activity activity, List<Media> transitionMedias,
                                                          String groupUUID, String stationID, int spanCount, NetworkImageView transitionView, Media currentMedia) {

        int initialPhotoPosition = getMediaPositionInMedias(transitionMedias, currentMedia);

        List<MediaViewModel> mediaViewModels = new ArrayList<>(transitionMedias.size());

        for (Media media1 : transitionMedias) {
            mediaViewModels.add(new MediaViewModel(media1));
        }

        PhotoSliderActivity.startPhotoSliderActivity(toolbar, activity, mediaViewModels,
                groupUUID, stationID, initialPhotoPosition, initialPhotoPosition, spanCount, transitionView, currentMedia);

    }

    private static int getMediaPositionInMedias(List<Media> medias, Media media) {

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


    private static int getMediaPosition(List<MediaViewModel> medias, Media media) {

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


    private static void startPhotoSliderActivity(View toolbar, Activity activity, List<MediaViewModel> transitionMediaViewModels,
                                                 String groupUUID, String stationID, int initialPhotoPosition,
                                                 int motionPosition, int spanCount, NetworkImageView transitionView, Media currentMedia) {

        setMediaViewModels(transitionMediaViewModels);

        Intent intent = new Intent();
        intent.putExtra(KEY_GROUP_UUID, groupUUID);
        intent.putExtra(KEY_STATION_ID, stationID);

        startPhotoSliderActivity(toolbar, activity, intent, initialPhotoPosition, motionPosition, spanCount, transitionView, currentMedia);

    }

    public static void startPhotoSliderActivity(View toolbar, Activity activity, List<MediaViewModel> transitionMediaViewModels,
                                                int initialPhotoPosition,
                                                int motionPosition, int spanCount, NetworkImageView transitionView, Media currentMedia) {

        setMediaViewModels(transitionMediaViewModels);

        Intent intent = new Intent();

        startPhotoSliderActivity(toolbar, activity, intent, initialPhotoPosition, motionPosition, spanCount, transitionView, currentMedia);

    }

    private static void startPhotoSliderActivity(View toolbar, Activity activity, Intent intent,
                                                 int initialPhotoPosition,
                                                 int motionPosition, int spanCount, NetworkImageView transitionView, Media currentMedia) {

        intent.putExtra(Util.INITIAL_PHOTO_POSITION, initialPhotoPosition);
        intent.putExtra(KEY_SHOW_COMMENT_BTN, false);
        intent.setClass(activity, PhotoSliderActivity.class);

        if (transitionView == null || transitionView.isLoaded()) {

            Util.setMotion(motionPosition, spanCount);

            ViewCompat.setTransitionName(transitionView, currentMedia.getKey());

            Pair mediaPair = new Pair<>((View) transitionView, currentMedia.getKey());

            Pair<View, String>[] pairs = Util.createSafeTransitionPairs(toolbar, activity,  mediaPair);

            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(activity, pairs);

//                              ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, mPhotoIv, currentMedia.getKey());

            intent.putExtra(Util.KEY_NEED_TRANSITION, true);

            activity.startActivity(intent, options.toBundle());

        } else {

            intent.putExtra(Util.KEY_NEED_TRANSITION, false);

            activity.startActivity(intent);

        }

    }


    public static void startPhotoSliderActivity(Activity activity, List<MediaViewModel> transitionMediaViewModels, int initialPhotoPosition) {

        setMediaViewModels(transitionMediaViewModels);

        Intent intent = new Intent();
        intent.putExtra(Util.INITIAL_PHOTO_POSITION, initialPhotoPosition);
        intent.putExtra(KEY_SHOW_COMMENT_BTN, false);

        intent.putExtra(Util.KEY_NEED_TRANSITION, false);

        intent.setClass(activity, PhotoSliderActivity.class);

        activity.startActivity(intent);

    }

    private Map<Integer, PlayVideoFragment> playVideoFragments;
    private int currentItem;

    public static final String KEY_STATION_ID = "key_station_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        playVideoFragments = new HashMap<>();

        mContext = this;

        systemSettingDataSource = InjectSystemSettingDataSource.provideSystemSettingDataSource(mContext);

        mHttpRequestFactory = InjectHttp.provideHttpRequestFactory(mContext);

        ActivityPhotoSliderBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_photo_slider);

        ivComment = binding.commentLayout;

        commentImg = binding.comment;

        mViewPager = binding.viewPager;

        mShareBtn = binding.share;

        photoSliderViewModel = new PhotoSliderViewModel();

        binding.setPhotoSliderViewModel(photoSliderViewModel);

        binding.setBaseView(this);

        needTransition = getIntent().getBooleanExtra(Util.KEY_NEED_TRANSITION, false);

        Log.d(TAG, "onCreate: needTransition:" + needTransition);

        if (needTransition) {

            Log.d(TAG, "onCreate: postpone enter transition");

            ActivityCompat.postponeEnterTransition(this);
            setEnterSharedElementCallback(sharedElementCallback);
        }

        initImageLoaderAndGifLoader();

        initialPhotoPosition = getIntent().getIntExtra(Util.INITIAL_PHOTO_POSITION, 0);

        boolean mShowCommentBtn = getIntent().getBooleanExtra(KEY_SHOW_COMMENT_BTN, false);

        groupUUID = getIntent().getStringExtra(KEY_GROUP_UUID);

        stationID = getIntent().getStringExtra(KEY_STATION_ID);

        mediaViewModelsAlreadyLoaded = new ArrayList<>();

        transitionMediaNeedShowThumb = getIntent().getBooleanExtra(Util.KEY_TRANSITION_PHOTO_NEED_SHOW_THUMB, true);

        refreshReturnResizeVisibility();

        Util.showSystemUI(getWindow().getDecorView());

        initCommentBtn(mShowCommentBtn);

        initViewPager();

        setPosition(initialPhotoPosition);

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                // Note that system bars will only be "visible" if none of the
                // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {

                    // adjustments to your UI, such as showing the action bar or0
                    // other navigational controls.

                    if (!sInEdit) {
                        convertEditState();
                    }

                } else {

                    // adjustments to your UI, such as hiding the action bar or
                    // other navigational controls.

                    if (sInEdit) {
                        convertEditState();
                    }
                }
            }
        });

        setSupportActionBar(binding.toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);

//        initShareBtn();

//        registerForContextMenu(mViewPager);

        TokenManager tokenManager = InjectSCloudTokenManager.provideInstance(this);

        mBaseDataOperator = InjectBaseDataOperator.provideInstance(this,
                tokenManager, this, new RefreshTokenRetryStrategy(tokenManager));

    }

    @Override
    public List<Media> getSelectedMedias() {

        Media media = mediaViewModels.get(currentPhotoPosition).getMedia();

        String mediaUUID = media.getUuid();
        if (mediaUUID.isEmpty()) {
            mediaUUID = Util.calcSHA256OfFile(media.getOriginalPhotoPath());
            media.setUuid(mediaUUID);
        }

        return Collections.singletonList(media);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MobclickAgent.onPageStart(TAG);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        MobclickAgent.onPageEnd(TAG);
        MobclickAgent.onPause(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        photoSliderViewModel.toolbarIconResId.set(R.drawable.ic_back);
        photoSliderViewModel.returnResizeResId.set(R.drawable.return_resize);

        commentImg.setImageResource(R.drawable.comment);

//        Media media = mediaViewModels.get(currentPhotoPosition);
//
//        LinearLayout cloudOff = (LinearLayout) mViewPager.findViewWithTag(media.getKey() + currentPhotoPosition);
//
//        if (cloudOff.getVisibility() == View.VISIBLE) {
//
//            boolean isLandScape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
//
//            myAdapter.setCloudOffPosition(cloudOff, media, isLandScape);
//
//        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && mIsFullScreen) {
            Util.hideSystemUI(getWindow().getDecorView());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;

        for (PlayVideoFragment playVideoFragment : playVideoFragments.values()) {
            playVideoFragment.stopPlayVideo();
        }

        playVideoFragments.clear();

    }

    @Override
    public void finishView() {
        finishActivity();
    }

    public static void setMediaViewModels(List<MediaViewModel> mediaViewModels) {

        PhotoSliderActivity.mediaViewModels = new ArrayList<>(mediaViewModels);

    }

    private void initViewPager() {
        MyAdapter myAdapter = new MyAdapter();

        mViewPager.setAdapter(myAdapter);
        mViewPager.setCurrentItem(initialPhotoPosition);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected:" + position);
                setPosition(position);

                MobclickAgent.onEvent(mContext, Util.SWITCH_ORIGINAL_MEDIA_UMENG_EVENT_ID);
            }

        });

    }

    private void initCommentBtn(boolean mShowCommentBtn) {
        if (mShowCommentBtn) {
            ivComment.setVisibility(View.VISIBLE);
            ivComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mediaViewModels.size() > currentPhotoPosition) {

                        ToastUtil.showToast(mContext, mContext.getString(R.string.coming_soon));

/*                        String imageUUID = mediaViewModels.get(currentPhotoPosition).getUuid();

                        Intent intent = new Intent();
                        intent.setClass(PhotoSliderActivity.this, MediaShareCommentActivity.class);
                        intent.putExtra(Util.IMAGE_KEY, imageUUID);

                        intent.putExtra(Util.INITIAL_PHOTO_POSITION, currentPhotoPosition);

                        View view = mViewPager.getChildAt(currentPhotoPosition).findViewById(R.id.mainPic);

                        ViewCompat.setTransitionName(view, imageUUID);

                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(PhotoSliderActivity.this, view, imageUUID);

                        startActivity(intent, options.toBundle());*/
                    }
                }
            });
        } else {
            ivComment.setVisibility(View.GONE);
        }
    }

    private void initShareBtn() {

        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showCreateShareBottomDialog();
            }
        });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.menu_photo_slider, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.share:
                showCreateShareBottomDialog();
                break;
        }

        return super.onContextItemSelected(item);
    }

    private void showCreateShareBottomDialog() {

    }

    private void refreshReturnResizeVisibility() {
        if (systemSettingDataSource.getShowPhotoReturnTipsValue()) {
            systemSettingDataSource.setShowPhotoReturnTipsValue(false);

            photoSliderViewModel.showReturnResize.set(true);

        }
    }

    private void initImageLoaderAndGifLoader() {
        ImageGifLoaderInstance imageGifLoaderInstance = InjectHttp.provideImageGifLoaderInstance(mContext);
        mImageLoader = imageGifLoaderInstance.getImageLoader(mContext);
        mImageLoader.setPriority(Request.Priority.HIGH);
        mGifLoader = imageGifLoaderInstance.getGifLoader(mContext);
        mGifLoader.setPriority(Request.Priority.HIGH);
    }

    @Override
    public void onBackPressed() {

        finishActivity();
    }

    private void finishActivity() {

        resetMediaLoadedState();

        if (needTransition && groupUUID == null) {
            supportFinishAfterTransition();
        } else
            finish();
    }

    private void resetMediaLoadedState() {
        for (MediaViewModel mediaViewModel : mediaViewModelsAlreadyLoaded) {
            mediaViewModel.setLoaded(false);
        }
    }

    @Override
    public void finishAfterTransition() {

        willReturn = true;
        Intent intent = new Intent();
        intent.putExtra(Util.INITIAL_PHOTO_POSITION, initialPhotoPosition);
        intent.putExtra(Util.CURRENT_PHOTO_POSITION, currentPhotoPosition);

        if (mediaViewModels != null) {
            Media media = mediaViewModels.get(currentPhotoPosition).getMedia();

            if (media != null)
                intent.putExtra(Util.CURRENT_MEDIA_KEY, media.getKey());

        }

        intent.putExtra(Util.CURRENT_MEDIASHARE_TIME, getIntent().getStringExtra(Util.CURRENT_MEDIASHARE_TIME));
        setResult(RESULT_OK, intent);

        super.finishAfterTransition();
    }

    public void setPosition(int position) {

        currentPhotoPosition = position;

        if (mediaViewModels.size() > position && position > -1) {

            Media media = mediaViewModels.get(position).getMedia();

            String title = media.getFormattedTime();
            if (title == null || title.contains(Util.DEFAULT_DATE)) {

                photoSliderViewModel.titleText.set(getString(R.string.unknown_time));

            } else {

                photoSliderViewModel.titleText.set(title);
            }

            if (media.isLocal()) {

                //TODO:add logic check media is uploaded to toggle showCloudOff

                photoSliderViewModel.showCloudOff.set(false);

            } else
                photoSliderViewModel.showCloudOff.set(false);

        }

        int lastItem = currentItem;
        currentItem = position;

        if (playVideoFragments.containsKey(lastItem)) {

            PlayVideoFragment playVideoFragment = playVideoFragments.get(lastItem);

            stopPlayVideo(playVideoFragment);

        }

        if (playVideoFragments.containsKey(currentItem)) {

            PlayVideoFragment currentPlayVideoFragment = playVideoFragments.get(currentItem);

            currentPlayVideoFragment.startPlayVideo((Video) mediaViewModels.get(currentItem).getMedia(), mContext);

        }

    }

    private void stopPlayVideo(PlayVideoFragment playVideoFragment) {
        playVideoFragment.stopPlayVideo();
    }

    private void toggleFullScreenState() {
        convertEditState();
        toggleSystemUIHideOrNot(getWindow().getDecorView());
    }

    @Override
    public void onImageLoadFinish(String url, View view) {

        if (url == null)
            return;

        MediaViewModel mediaViewModel = ((GifTouchNetworkImageView) view).getCurrentMediaViewModel();

        handleMediaLoaded(url, view, mediaViewModel);

    }

    @Override
    public void onImageLoadFail(String url, View view) {

        if (url == null)
            return;

        MediaViewModel mediaViewModel = ((GifTouchNetworkImageView) view).getCurrentMediaViewModel();

        if (isImageThumb(url, mediaViewModel.getMedia())) {
            ((GifTouchNetworkImageView) view).setDefaultColor();
        }

    }

    private void handleMediaLoaded(String url, View view, MediaViewModel mediaViewModel) {
        if (isImageThumb(url, mediaViewModel.getMedia())) {

            handleThumbLoaded(view, mediaViewModel.getMedia());

        } else {

            handleOriginalMediaLoaded(view, mediaViewModel);

        }
    }

    private void handleOriginalMediaLoaded(View view, MediaViewModel mediaViewModel) {

        Media media = mediaViewModel.getMedia();

        if (!transitionMediaNeedShowThumb && needTransition) {

            Log.d(TAG, "handleOriginalMediaLoaded: start postponed enter transition");

            scheduleStartPostponedTransition(view);
            transitionMediaNeedShowThumb = true;
        } else if (media.isLocal() && media.getThumb().isEmpty()) {
            scheduleStartPostponedTransition(view);
        }

        if (!mediaViewModel.isLoaded()) {
            mediaViewModel.setLoaded(true);

            mediaViewModelsAlreadyLoaded.add(mediaViewModel);
        }

    }

    private void handleThumbLoaded(View view, Media media) {
        if (!hasStartPostPoneEnterTransition && needTransition) {

            hasStartPostPoneEnterTransition = true;

            Log.d(TAG, "handleThumbLoaded: start postponed enter transition");

            scheduleStartPostponedTransition(view);

            startLoadCurrentImageAfterTransition(view, media);

        } else {

            startLoadingOriginalPhotoOrLargePhoto(view, media);
        }

    }

    private void scheduleStartPostponedTransition(final View view) {

        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                ActivityCompat.startPostponedEnterTransition(PhotoSliderActivity.this);
                return false;
            }
        });

    }

    public boolean isImageThumb(String imageUrl, Media media) {

        if (media.isLocal()) {

            return imageUrl.contains(FileUtil.getLocalPhotoThumbnailFolderPath());

        } else {
            return imageUrl.contains("width=200");
        }

    }

    private void startLoadCurrentImageAfterTransition(final View view, final Media media) {
        if (Util.checkRunningOnLollipopOrHigher()) {

            getWindow().getSharedElementEnterTransition().addListener(new CustomTransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);

                    startLoadingOriginalPhotoOrLargePhoto(view, media);

                }
            });

        } else {
            startLoadingOriginalPhotoOrLargePhoto(view, media);
        }
    }

    private void startLoadingOriginalPhotoOrLargePhoto(View view, final Media media) {

        if (media instanceof Video)
            return;

        final GifTouchNetworkImageView mainPic = (GifTouchNetworkImageView) view;

        final boolean isGif = MediaUtil.checkMediaIsGif(media);

        mBaseDataOperator.preConditionCheck(true, new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                handleGetSCloudTokenAfterLoadThumb(media, mainPic, isGif);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                handleGetSCloudTokenAfterLoadThumb(media, mainPic, isGif);
            }
        });


    }

    private void handleGetSCloudTokenAfterLoadThumb(Media media, GifTouchNetworkImageView mainPic, boolean isGif) {

        HttpRequest httpRequest;
        String remoteUrl;

        if (systemSettingDataSource.getLoginWithWechatCodeOrNot() && !isGif && !media.isLocal()) {

            httpRequest = getLargeImageHttpRequest(media, mSCloudToken);

            remoteUrl = httpRequest.getUrl();

        } else {

            httpRequest = getMediaOriginalHttpRequest(media, mSCloudToken);

            remoteUrl = httpRequest.getUrl();

            mainPic.setOrientationNumber(media.getOrientationNumber());
        }

        mainPic.setTag(remoteUrl);

        if (isGif) {

            mGifLoader.setHeaders(httpRequest.getHeaders());

            mainPic.setGifUrl(remoteUrl, mGifLoader);
        } else {

            mImageLoader.setHeaders(httpRequest.getHeaders());

            mainPic.setImageUrl(remoteUrl, mImageLoader);
        }
    }

    private HttpRequest getLargeImageHttpRequest(Media media, String sCloudToken) {
        HttpRequest httpRequest;
        DisplayMetrics displayMetrics = Util.getDisplayMetrics(this);

        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        String mediaWidthStr = media.getWidth();
        String mediaHeightStr = media.getHeight();

        int mediaWidth = screenWidth;
        int mediaHeight = screenHeight;

        if (Util.isNumeric(mediaWidthStr)) {
            mediaWidth = Integer.parseInt(media.getWidth());
        }

        if (Util.isNumeric(mediaHeightStr)) {
            mediaHeight = Integer.parseInt(media.getHeight());
        }

        if (screenWidth / screenHeight > mediaWidth / mediaHeight) {

            httpRequest = getMediaThumbHttpRequest(media, -1, screenHeight, sCloudToken);

        } else {

            httpRequest = getMediaThumbHttpRequest(media, screenWidth, -1, sCloudToken);

        }


        return httpRequest;
    }

    private HttpRequest getMediaThumbHttpRequest(Media media, int width, int height, String sCloudToken) {
        HttpRequest httpRequest;
        if (groupUUID != null)
            httpRequest = media.getImageThumbUrl(mHttpRequestFactory, width, height, new GroupRequestParam(groupUUID, stationID), sCloudToken);
        else
            httpRequest = media.getImageThumbUrl(mHttpRequestFactory);
        return httpRequest;
    }


    private HttpRequest getMediaThumbHttpRequest(Media media, String sCloudToken) {
        HttpRequest httpRequest;
        if (groupUUID != null)
            httpRequest = media.getImageThumbUrl(mHttpRequestFactory, new GroupRequestParam(groupUUID, stationID), sCloudToken);
        else
            httpRequest = media.getImageThumbUrl(mHttpRequestFactory);
        return httpRequest;
    }

    private HttpRequest getMediaOriginalHttpRequest(Media media, String sCloudToken) {
        HttpRequest httpRequest;
        if (groupUUID != null)
            httpRequest = media.getImageOriginalUrl(mHttpRequestFactory, new GroupRequestParam(groupUUID, stationID), sCloudToken);
        else
            httpRequest = media.getImageOriginalUrl(mHttpRequestFactory);
        return httpRequest;
    }

    private class MyAdapter extends PagerAdapter {

        private List<MediaViewModel> mMediaViewModels;

        public MyAdapter() {

            if (mediaViewModels != null) {
                mMediaViewModels = new ArrayList<>(mediaViewModels);
            } else {
                mMediaViewModels = new ArrayList<>();
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "" + position;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            View view;

            MediaViewModel mediaViewModel = mMediaViewModels.get(position);

            final Media media = mediaViewModel.getMedia();

//                setMainPicScreenWidthHeight(mainPic, media);

            if (media instanceof Video) {

                final PlayVideoFragment playVideoFragment = new PlayVideoFragment(mContext);

                view = playVideoFragment.getView();

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playVideo((Video) media, playVideoFragment);
                    }
                });

                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        handleOnLongClick();

                        return false;
                    }
                });

                view.setOnTouchListener(new CustomTouchListener());

                playVideoFragments.put(position, playVideoFragment);

                if (position == initialPhotoPosition) {
                    playVideoFragment.startPlayVideo((Video) media, mContext);
                }

            } else
                view = getViewForMedia(position, mediaViewModel);

            container.addView(view);

            Log.d(TAG, "inistatiate position : " + position);

            return view;

        }

        @NonNull
        private View getViewForMedia(final int position, MediaViewModel mediaViewModel) {
            View view;
            view = LayoutInflater.from(mContext).inflate(R.layout.photo_slider_cell, null);

            final GifTouchNetworkImageView mainPic = (GifTouchNetworkImageView) view.findViewById(R.id.mainPic);

            final Media media = mediaViewModel.getMedia();

            Log.d(TAG, "instantiateItem: orientationNumber:" + media.getOrientationNumber());

            mainPic.registerImageLoadListener(PhotoSliderActivity.this);

            mainPic.setDefaultImageResId(R.drawable.new_placeholder);
//                mainPic.setDefaultBackgroundColor(ContextCompat.getColor(mContext,R.color.default_imageview_color));

            mImageLoader.setShouldCache(!media.isLocal());

            mainPic.setCurrentMediaViewModel(mediaViewModel);

            mBaseDataOperator.preConditionCheck(true, new BaseOperateCallback() {
                @Override
                public void onSucceed() {

                    handleGetSCloudTokenWhenInitItem(position, mainPic, media);

                }

                @Override
                public void onFail(OperationResult operationResult) {

                    handleGetSCloudTokenWhenInitItem(position, mainPic, media);

                }
            });

            mainPic.setUserTouchListener(new CustomTouchListener());
            mainPic.setUserDoubleTapListener(new CustomTapListener(mainPic));

            mainPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFullScreenState();
                }
            });

            mainPic.setUserScaleGestureListener(new CustomScaleListener(mainPic));

            mainPic.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    handleOnLongClick();

                    return false;
                }
            });
            return view;
        }

        private void handleOnLongClick() {
            AbstractCommand abstractCommand = new AbstractCommand() {
                @Override
                public void execute() {
                    showCreateShareBottomDialog();
                }

                @Override
                public void unExecute() {
                }
            };

            List<AbstractCommand> commands = Collections.singletonList(abstractCommand);

            DialogFactory dialogFactory = new PhotoOperationAlertDialogFactory(Collections.singletonList(getString(R.string.share_verb)), commands);

            dialogFactory.createDialog(mContext).show();
        }

        private class CustomScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

            private GifTouchNetworkImageView mView;

            CustomScaleListener(GifTouchNetworkImageView view) {
                this.mView = view;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {

                handleToolbarFooter();

                return super.onScaleBegin(detector);
            }

        }

        private void handleToolbarFooter() {
            if (photoSliderViewModel.showToolbar.get()) {
                photoSliderViewModel.showToolbar.set(false);

                photoSliderViewModel.showMaskLayout.set(false);
            }

            if (photoSliderViewModel.showPanelFooter.get()) {
                photoSliderViewModel.showPanelFooter.set(false);
            }
        }

        private class CustomTapListener implements PinchImageView.UserDoubleTapListener {

            private GifTouchNetworkImageView mView;

            CustomTapListener(GifTouchNetworkImageView view) {
                mView = view;
            }

            /**
             * Notified when a double-tap occurs.
             *
             * @param e The down motion event of the first tap of the double-tap.
             * @return true if the event is consumed, else false
             */
            @Override
            public boolean onDoubleTap(MotionEvent e) {

                handleToolbarFooter();

                return false;
            }

        }

        private class CustomTouchListener implements View.OnTouchListener {

            float x, y, lastX, lastY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handleTouchEvent(event, v);
                return false;
            }

            private void handleTouchEvent(MotionEvent event, View view) {

                int action = event.getAction() & MotionEvent.ACTION_MASK;

                if (view instanceof GifTouchNetworkImageView) {

                    GifTouchNetworkImageView gifTouchNetworkImageView = (GifTouchNetworkImageView) view;

                    Log.d(TAG, "handleTouchEvent: isEnlargeState: " + gifTouchNetworkImageView.isEnlargeState());

                    if (gifTouchNetworkImageView.isEnlargeState())
                        return;

                }

                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {

                    Log.d(TAG, "handleTouchEvent: action up lastX" + lastX + " lastY:" + lastY + " y:" + y + " x:" + x);

                    if (lastY - y > Util.dip2px(mContext, 60)) {

                        finishActivity();

                    } else {

                        view.setTranslationY(0);

                    }

                } else if (action == MotionEvent.ACTION_DOWN) {

                    Log.d(TAG, "handleTouchEvent: action down lastX" + lastX + " lastY:" + lastY + " y:" + y + " x:" + x);

                    x = event.getRawX();
                    y = event.getRawY();
                    lastX = x;
                    lastY = y;

                } else if (action == MotionEvent.ACTION_MOVE) {

                    lastX = event.getRawX();
                    lastY = event.getRawY();

                    Log.d(TAG, "handleTouchEvent: action move lastX" + lastX + " lastY:" + lastY + " y:" + y + " x:" + x);

                    if (lastY > y) {
                        view.setTranslationY(lastY - y);
                    }

                }
            }
        }

        private void setCloudOffPosition(View view, Media media, boolean isLandScape) {

            int mediaWidth = Integer.parseInt(media.getWidth());
            int mediaHeight = Integer.parseInt(media.getHeight());
            int actualWidth;
            int actualHeight;

            int systemUIHeight = Util.dip2px(mContext, 24);

            int navigationBarHeight = Util.dip2px(mContext, 48);

            int screenWidth = Util.calcScreenWidth(PhotoSliderActivity.this);
            int screenHeight = Util.calcScreenHeight(PhotoSliderActivity.this);

            Log.d(TAG, "setCloudOffPosition: mediaWidth: " + mediaWidth + " mediaHeight: " + mediaHeight + " screenWidth: " + screenWidth + " screenHeight: " + screenHeight);

            int marginRight = isLandScape ? navigationBarHeight : 0;

            if (mediaWidth - mediaHeight >= screenWidth - screenHeight) {
                actualWidth = Util.calcScreenWidth(PhotoSliderActivity.this);
                actualHeight = mediaHeight * actualWidth / mediaWidth;

                int marginTop = (screenHeight - actualHeight) / 2 + systemUIHeight;

                Util.setMargin(view, 0, marginTop, marginRight, 0);

            } else if (mediaWidth - mediaHeight < screenWidth - screenHeight) {

                Util.setMargin(view, 0, systemUIHeight, marginRight, 0);

            }

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            GifTouchNetworkImageView mainPic = (GifTouchNetworkImageView) ((View) object).findViewById(R.id.mainPic);

            if (mainPic != null)
                mainPic.unregisterImageLoadListener();

            container.removeView((View) object);

        }

        @Override
        public int getCount() {

            if (mMediaViewModels == null || mMediaViewModels.size() == 0) {
                return 0;
            } else {
                return mMediaViewModels.size();
            }
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    private void handleGetSCloudTokenWhenInitItem(int position, GifTouchNetworkImageView mainPic, Media media) {
        HttpRequest httpRequest = getMediaThumbHttpRequest(media, mSCloudToken);

        if (transitionMediaNeedShowThumb && !media.isLocal()) {

            if (position == initialPhotoPosition)
                ViewCompat.setTransitionName(mainPic, media.getKey());

            String thumbImageUrl = httpRequest.getUrl();

            mImageLoader.setHeaders(httpRequest.getHeaders());

            mainPic.setTag(thumbImageUrl);

            mainPic.setImageUrl(thumbImageUrl, mImageLoader);

        } else {

            if (position == initialPhotoPosition)
                ViewCompat.setTransitionName(mainPic, media.getKey());

            mainPic.setOrientationNumber(media.getOrientationNumber());

            String imageThumbUrl = httpRequest.getUrl();

            mainPic.setTag(imageThumbUrl);

            if (imageThumbUrl.endsWith(".gif")) {

                mGifLoader.setHeaders(httpRequest.getHeaders());

                mainPic.setGifUrl(imageThumbUrl, mGifLoader);
            } else {

                mImageLoader.setHeaders(httpRequest.getHeaders());

                mainPic.setImageUrl(imageThumbUrl, mImageLoader);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_photo_slider, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.share:
                showCreateShareBottomDialog();
                break;

        }

        return true;
    }

    private void setMainPicScreenWidthHeight(View mainPic, Media media) {

        int mediaWidth = Integer.parseInt(media.getWidth());
        int mediaHeight = Integer.parseInt(media.getHeight());

        Log.d(TAG, "setMainPicScreenWidthHeight: media width: " + mediaWidth + " media height: " + mediaHeight);

        int actualWidth = 0;
        int actualHeight = 0;

        if (mediaWidthLargerThanHeight(media, mediaWidth, mediaHeight)) {
            actualWidth = Util.calcScreenWidth(PhotoSliderActivity.this);
            actualHeight = mediaHeight * actualWidth / mediaWidth;
        } else if (mediaHeightLargerThanWidth(media, mediaWidth, mediaHeight)) {
            actualHeight = Util.calcScreenHeight(PhotoSliderActivity.this);
            actualWidth = mediaWidth * actualHeight / mediaHeight;
        } else if (mediaWidthEqualsHeight(mediaWidth, mediaHeight)) {

            actualWidth = actualHeight = Util.calcScreenWidth(PhotoSliderActivity.this);
        }

        Util.setWidthAndHeight(mainPic, actualWidth, actualHeight);

    }

    private boolean mediaWidthEqualsHeight(int mediaWidth, int mediaHeight) {
        return mediaWidth == mediaHeight;
    }

    private boolean mediaHeightLargerThanWidth(Media media, int mediaWidth, int mediaHeight) {
        return (mediaWidth < mediaHeight && media.getOrientationNumber() <= 4) || (mediaWidth > mediaHeight && media.getOrientationNumber() > 4);
    }

    private boolean mediaWidthLargerThanHeight(Media media, int mediaWidth, int mediaHeight) {
        return (mediaWidth > mediaHeight && media.getOrientationNumber() <= 4) || (mediaWidth < mediaHeight && media.getOrientationNumber() > 4);
    }

    private void playVideo(Video media, PlayVideoFragment playVideoFragment) {
        playVideoFragment.startPlayVideo(media, mContext);

        //hide system status bar may fail cause of media controller
        //toggleFullScreenState();
    }

    private void convertEditState() {

        sInEdit = !sInEdit;
        if (sInEdit) {
            photoSliderViewModel.showToolbar.set(true);
            photoSliderViewModel.showMaskLayout.set(true);
            photoSliderViewModel.showPanelFooter.set(true);
        } else {
            photoSliderViewModel.showToolbar.set(false);
            photoSliderViewModel.showMaskLayout.set(false);
            photoSliderViewModel.showPanelFooter.set(false);
        }
    }

    private void toggleSystemUIHideOrNot(View view) {

        mIsFullScreen = !mIsFullScreen;
        if (mIsFullScreen) {
            Util.hideSystemUI(view);
        } else {
            Util.showSystemUI(view);
        }
    }
}
