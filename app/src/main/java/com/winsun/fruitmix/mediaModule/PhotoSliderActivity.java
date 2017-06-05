package com.winsun.fruitmix.mediaModule;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.util.Pair;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.IImageLoadListener;
import com.android.volley.toolbox.NetworkImageView;
import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.component.GifTouchImageView;
import com.winsun.fruitmix.component.GifTouchNetworkImageView;
import com.winsun.fruitmix.dialog.DialogFactory;
import com.winsun.fruitmix.dialog.PhotoOperationAlertDialogFactory;
import com.winsun.fruitmix.dialog.ShareMenuBottomDialogFactory;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveMediaOriginalPhotoRequestEvent;
import com.winsun.fruitmix.gif.GifLoader;
import com.winsun.fruitmix.mediaModule.fragment.NewPhotoList;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.ImageGifLoaderInstance;
import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.anim.CustomTransitionListener;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoSliderActivity extends BaseActivity implements IImageLoadListener {

    public static final String TAG = "PhotoSliderActivity";

    @BindView(R.id.mask_layout)
    View mMaskLayout;
    @BindView(R.id.ic_cloud_off)
    ImageView mCloudOff;
    @BindView(R.id.title)
    TextView mTitleTextView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.comment_layout)
    LinearLayout ivComment;
    @BindView(R.id.comment)
    ImageView commentImg;
    @BindView(R.id.panelFooter)
    RelativeLayout rlPanelFooter;
    @BindView(R.id.return_resize)
    ImageView mReturnResize;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;
    @BindView(R.id.share)
    ImageButton mShareBtn;

    private MyAdapter myAdapter;

    private static List<Media> mediaList;

    private int initialPhotoPosition = 0;
    private int currentPhotoPosition = 0;

    private List<Media> mediaAlreadyLoadedList;

    private boolean sInEdit = true;

    private boolean mIsFullScreen = false;

    private ImageLoader mImageLoader;

    private GifLoader mGifLoader;

    private Context mContext;

    private boolean willReturn = false;

    private boolean transitionMediaNeedShowThumb = true;

    private boolean needTransition = true;

    private ProgressDialog mDialog;

    private SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            if (willReturn) {
                if (initialPhotoPosition != currentPhotoPosition) {

                    names.clear();
                    sharedElements.clear();

                    Media media = mediaList.get(currentPhotoPosition);

                    String imageKey = media.getKey();
                    names.add(imageKey);

                    String imageTag;

                    boolean isThumb = media.isLoaded();
                    imageTag = getImageUrl(isThumb, media);

                    sharedElements.put(imageKey, mViewPager.findViewWithTag(imageTag));

                    Log.d(TAG, "onMapSharedElements: media key:" + imageKey + " imageTag:" + imageTag);
                }
            }

        }
    };

    public static void startPhotoSliderActivity(Activity activity, List<Media> transitionMedias, Intent intent, int motionPosition, int spanCount, NetworkImageView transitionView, Media currentMedia) {

        setMediaList(transitionMedias);

        if (transitionView.isLoaded()) {

            Util.setMotion(motionPosition, spanCount);

            ViewCompat.setTransitionName(transitionView, currentMedia.getKey());

            Pair mediaPair = new Pair<>((View) transitionView, currentMedia.getKey());

            Pair[] pairs = Util.createSafeTransitionPairs(activity, true, mediaPair);

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_slider);

        mContext = this;

        ButterKnife.bind(this);

        needTransition = getIntent().getBooleanExtra(Util.KEY_NEED_TRANSITION, false);

        Log.d(TAG, "onCreate: needTransition:" + needTransition);

        if (needTransition) {
            ActivityCompat.postponeEnterTransition(this);
            setEnterSharedElementCallback(sharedElementCallback);
        }

        initImageLoaderAndGifLoader();

        initialPhotoPosition = getIntent().getIntExtra(Util.INITIAL_PHOTO_POSITION, 0);

        boolean mShowCommentBtn = getIntent().getBooleanExtra(Util.KEY_SHOW_COMMENT_BTN, false);

        mediaAlreadyLoadedList = new ArrayList<>();

        transitionMediaNeedShowThumb = getIntent().getBooleanExtra(Util.KEY_TRANSITION_PHOTO_NEED_SHOW_THUMB, true);

        refreshReturnResizeVisibility();

        Util.showSystemUI(getWindow().getDecorView());

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });

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
                    // TODO: The system bars are visible. Make any desired
                    // adjustments to your UI, such as showing the action bar or0
                    // other navigational controls.

                    if (!sInEdit) {
                        convertEditState();
                    }

                } else {
                    // TODO: The system bars are NOT visible. Make any desired
                    // adjustments to your UI, such as hiding the action bar or
                    // other navigational controls.

                    if (sInEdit) {
                        convertEditState();
                    }
                }
            }
        });

//        initShareBtn();

//        registerForContextMenu(mViewPager);

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

        mToolbar.setNavigationIcon(R.drawable.ic_back);
        commentImg.setImageResource(R.drawable.comment);
        mReturnResize.setImageResource(R.drawable.return_resize);

//        Media media = mediaList.get(currentPhotoPosition);
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

        NewPhotoList.mEnteringPhotoSlider = false;
    }

    public static void setMediaList(List<Media> mediaList) {
        PhotoSliderActivity.mediaList = new ArrayList<>(mediaList);
    }

    private void initViewPager() {
        myAdapter = new MyAdapter();

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

                    if (mediaList.size() > currentPhotoPosition) {

                        Toast.makeText(mContext, mContext.getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();

/*                        String imageUUID = mediaList.get(currentPhotoPosition).getUuid();

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
        if (!Util.getNetworkState(mContext)) {
            Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            return;
        }

        final Media media = mediaList.get(currentPhotoPosition);

        String mediaUUID = media.getUuid();
        if (mediaUUID.isEmpty()) {
            mediaUUID = Util.CalcSHA256OfFile(media.getOriginalPhotoPath());
            media.setUuid(mediaUUID);
        }

        AbstractCommand shareInAppCommand = new AbstractCommand() {
            @Override
            public void execute() {

                mDialog = ProgressDialog.show(mContext, null, String.format(getString(R.string.operating_title), getString(R.string.create_share)), true, false);

                FNAS.createRemoteMediaShare(mContext, Util.createMediaShare(false, true, false, "", "", Collections.singletonList(media.getUuid())));

            }

            @Override
            public void unExecute() {
            }
        };

        AbstractCommand shareToOtherAppCommand = new AbstractCommand() {
            @Override
            public void execute() {

                String originalPhotoPath = media.getOriginalPhotoPath();

                if (originalPhotoPath.length() != 0) {

                    Util.sendShare(mContext, Collections.singletonList(originalPhotoPath));

                } else {
                    mDialog = ProgressDialog.show(mContext, null, String.format(getString(R.string.operating_title), getString(R.string.create_share)), true, true);
                    mDialog.setCanceledOnTouchOutside(false);

                    EventBus.getDefault().post(new RetrieveMediaOriginalPhotoRequestEvent(OperationType.GET, OperationTargetType.MEDIA_ORIGINAL_PHOTO, Collections.singletonList(media)));
                }

            }

            @Override
            public void unExecute() {
            }
        };

        new ShareMenuBottomDialogFactory(shareInAppCommand, shareToOtherAppCommand).createDialog(mContext).show();
    }

    @Override
    public void handleOperationEvent(OperationEvent operationEvent) {

        super.handleOperationEvent(operationEvent);

        Log.i(TAG, "handleOperationEvent: action:" + action);

        switch (action) {
            case Util.REMOTE_MEDIA_SHARE_CREATED:

                dismissDialog();

                Toast.makeText(mContext, operationEvent.getOperationResult().getResultMessage(mContext), Toast.LENGTH_SHORT).show();

                break;
            case Util.SHARED_PHOTO_THUMB_RETRIEVED:

                if (mDialog == null || !mDialog.isShowing()) {
                    return;
                }

                dismissDialog();

                String originalPhotoPath = mediaList.get(currentPhotoPosition).getOriginalPhotoPath();

                if (originalPhotoPath.isEmpty()) {
                    Toast.makeText(mContext, getString(R.string.download_original_photo_fail), Toast.LENGTH_SHORT).show();
                } else {
                    Util.sendShare(mContext, Collections.singletonList(originalPhotoPath));
                }

                break;
        }

    }

    private void dismissDialog() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    private void refreshReturnResizeVisibility() {
        if (getShowPhotoReturnTipsValue()) {
            setShowPhotoReturnTipsValue(false);

            mReturnResize = (ImageView) findViewById(R.id.return_resize);
            if (mReturnResize != null) {
                mReturnResize.setVisibility(View.VISIBLE);
                mReturnResize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mReturnResize.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    private void initImageLoaderAndGifLoader() {
        ImageGifLoaderInstance imageGifLoaderInstance = ImageGifLoaderInstance.INSTANCE;
        mImageLoader = imageGifLoaderInstance.getImageLoader(mContext);
        mGifLoader = imageGifLoaderInstance.getGifLoader(mContext);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity();
    }

    private void finishActivity() {

        resetMediaLoadedState();

        if (needTransition) {
            supportFinishAfterTransition();
        } else
            finish();
    }

    private void resetMediaLoadedState() {
        for (Media media : mediaAlreadyLoadedList) {
            media.setLoaded(false);
        }
    }

    @Override
    public void finishAfterTransition() {

        willReturn = true;
        Intent intent = new Intent();
        intent.putExtra(Util.INITIAL_PHOTO_POSITION, initialPhotoPosition);
        intent.putExtra(Util.CURRENT_PHOTO_POSITION, currentPhotoPosition);

        if (mediaList != null) {
            Media media = mediaList.get(currentPhotoPosition);

            if (media != null)
                intent.putExtra(Util.CURRENT_MEDIA_KEY, mediaList.get(currentPhotoPosition).getKey());

        }

        intent.putExtra(Util.CURRENT_MEDIASHARE_TIME, getIntent().getStringExtra(Util.CURRENT_MEDIASHARE_TIME));
        setResult(RESULT_OK, intent);

        super.finishAfterTransition();
    }

    private boolean getShowPhotoReturnTipsValue() {
        SharedPreferences sp;
        sp = getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(Util.SHOW_PHOTO_RETURN_TIPS, true);
    }

    private void setShowPhotoReturnTipsValue(boolean value) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putBoolean(Util.SHOW_PHOTO_RETURN_TIPS, value);
        editor.apply();
    }

    public void setPosition(int position) {
        currentPhotoPosition = position;

        if (mediaList.size() > position && position > -1) {

            Media media = mediaList.get(position);

            String title = media.getTime();
            if (title == null || title.contains(Util.DEFAULT_DATE)) {
                mTitleTextView.setText(getString(R.string.unknown_time));
            } else {
                mTitleTextView.setText(title);
            }

            if (LocalCache.DeviceID != null && media.getUploadedDeviceIDs().contains(LocalCache.DeviceID)) {
                mCloudOff.setVisibility(View.INVISIBLE);
            } else {
                mCloudOff.setVisibility(View.VISIBLE);
            }

        }

    }

    @Override
    public void onImageLoadFinish(String url, View view) {

        if (url == null)
            return;

        Media media = ((GifTouchNetworkImageView) view).getCurrentMedia();

        handleMediaLoaded(url, view, media);

    }

    private void handleMediaLoaded(String url, View view, Media media) {
        if (isImageThumb(url, media)) {

            handleThumbLoaded(view, media);

        } else {

            handleOriginalMediaLoaded(view, media);

        }
    }

    private void handleOriginalMediaLoaded(View view, Media media) {
        if (!transitionMediaNeedShowThumb && needTransition) {
            ActivityCompat.startPostponedEnterTransition(this);
            transitionMediaNeedShowThumb = true;
        } else if (media.isLocal() && media.getThumb().isEmpty()) {
            ActivityCompat.startPostponedEnterTransition(this);
        }

        if (!media.isLoaded()) {
            media.setLoaded(true);

            mediaAlreadyLoadedList.add(media);
        }

    }

    private void handleThumbLoaded(View view, Media media) {
        if (isCurrentViewPage(initialPhotoPosition) && needTransition) {
            ActivityCompat.startPostponedEnterTransition(this);

            startLoadCurrentImageAfterTransition(view, media);

        } else {

            startLoadingOriginalPhoto(view, media);
        }

    }

    private void handleLocalMediaLoaded(Media media) {
        if (isCurrentViewPage(initialPhotoPosition) && needTransition) {
            ActivityCompat.startPostponedEnterTransition(this);
        }

        if (!media.isLoaded()) {
            media.setLoaded(true);

            mediaAlreadyLoadedList.add(media);
        }
    }

    public boolean isCurrentViewPage(int viewPosition) {
        return mViewPager.getCurrentItem() == viewPosition;
    }

    public boolean isImageThumb(String imageUrl, Media media) {

        if (media.isLocal()) {

            return imageUrl.contains(FileUtil.getFolderPathForLocalPhotoThumbnailFolderName200());

        } else {
            return imageUrl.contains("thumb");
        }

    }

    private void startLoadCurrentImageAfterTransition(final View view, final Media media) {
        if (Util.checkRunningOnLollipopOrHigher()) {
            getWindow().getSharedElementEnterTransition().addListener(new CustomTransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);

                    startLoadingOriginalPhoto(view, media);

                }
            });

        } else {
            startLoadingOriginalPhoto(view, media);
        }
    }

    private String getImageUrl(boolean isThumb, Media media) {
        String currentUrl;

        if (isThumb) {
            currentUrl = media.getImageThumbUrl(mContext);
        } else {
            currentUrl = media.getImageOriginalUrl(mContext);
        }

        return currentUrl;
    }

    private void startLoadingOriginalPhoto(View view, Media media) {

        String remoteUrl = media.getImageOriginalUrl(mContext);

        GifTouchNetworkImageView mainPic = (GifTouchNetworkImageView) view;

        mainPic.setOrientationNumber(media.getOrientationNumber());

        mainPic.setTag(remoteUrl);

        if (media.getType().equalsIgnoreCase("gif")) {
            mainPic.setGifUrl(remoteUrl, mGifLoader);
        } else {
            mainPic.setImageUrl(remoteUrl, mImageLoader);
        }

    }

    private class MyAdapter extends PagerAdapter {

        private List<Media> medias;

        public MyAdapter() {

            //TODO: check mediaList is null logic

            if (mediaList != null) {
                medias = new ArrayList<>(mediaList);
            } else {
                medias = new ArrayList<>();
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "选X项" + position;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            View view;

            view = LayoutInflater.from(mContext).inflate(R.layout.photo_slider_cell, null);

            GifTouchNetworkImageView mainPic = (GifTouchNetworkImageView) view.findViewById(R.id.mainPic);

            if (medias.size() > position && position > -1) {

                Media media = medias.get(position);

                Log.d(TAG, "instantiateItem: orientationNumber:" + media.getOrientationNumber());

                mainPic.registerImageLoadListener(PhotoSliderActivity.this);

                mainPic.setDefaultImageResId(R.drawable.new_placeholder);
//                mainPic.setDefaultBackgroundColor(ContextCompat.getColor(mContext,R.color.default_imageview_color));

                mImageLoader.setShouldCache(!media.isLocal());

                mainPic.setCurrentMedia(media);

                if (transitionMediaNeedShowThumb && !media.isLocal()) {

                    if (position == initialPhotoPosition)
                        ViewCompat.setTransitionName(mainPic, media.getKey());

                    String thumbImageUrl = media.getImageThumbUrl(mContext);
                    mainPic.setTag(thumbImageUrl);

                    mainPic.setImageUrl(thumbImageUrl, mImageLoader);

                } else {

                    if (position == initialPhotoPosition)
                        ViewCompat.setTransitionName(mainPic, media.getKey());

                    mainPic.setOrientationNumber(media.getOrientationNumber());

                    String imageThumbUrl = media.getImageThumbUrl(mContext);
                    mainPic.setTag(imageThumbUrl);

                    if (imageThumbUrl.endsWith(".gif")) {
                        mainPic.setGifUrl(imageThumbUrl, mGifLoader);
                    } else {
                        mainPic.setImageUrl(imageThumbUrl, mImageLoader);
                    }

                }

                mainPic.setOnTouchListener(new CustomTouchListener());
                mainPic.setOnDoubleTapListener(new CustomTapListener(mainPic));

                mainPic.setUserScaleGestureListener(new CustomScaleListener(mainPic));

                mainPic.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        handleOnLongClick();

                        return false;
                    }
                });

            }

            container.addView(view);

            Log.d(TAG, "inistatiate position : " + position);

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

                if (mToolbar.getVisibility() == View.VISIBLE) {
                    mToolbar.setVisibility(View.INVISIBLE);
                    mMaskLayout.setVisibility(View.INVISIBLE);
                }
                if (rlPanelFooter.getVisibility() == View.VISIBLE) {
                    rlPanelFooter.setVisibility(View.INVISIBLE);
                }

                return super.onScaleBegin(detector);
            }

        }

        private class CustomTapListener implements GestureDetector.OnDoubleTapListener {

            private GifTouchNetworkImageView mView;

            CustomTapListener(GifTouchNetworkImageView view) {
                mView = view;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                convertEditState();
                toggleFullScreenState(getWindow().getDecorView());

                return true;
            }

            /**
             * Notified when a double-tap occurs.
             *
             * @param e The down motion event of the first tap of the double-tap.
             * @return true if the event is consumed, else false
             */
            @Override
            public boolean onDoubleTap(MotionEvent e) {

                if (mToolbar.getVisibility() == View.VISIBLE) {
                    mToolbar.setVisibility(View.INVISIBLE);
                    mMaskLayout.setVisibility(View.INVISIBLE);
                }
                if (rlPanelFooter.getVisibility() == View.VISIBLE) {
                    rlPanelFooter.setVisibility(View.INVISIBLE);
                }

                return false;
            }

            /**
             * Notified when an event within a double-tap gesture occurs, including
             * the down, move, and up events.
             *
             * @param e The motion event that occurred during the double-tap gesture.
             * @return true if the event is consumed, else false
             */
            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        }

        private class CustomTouchListener implements View.OnTouchListener {

            float x, y, lastX, lastY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handleTouchEvent(event, (GifTouchNetworkImageView) v);
                return false;
            }

            private void handleTouchEvent(MotionEvent event, GifTouchNetworkImageView view) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    x = event.getRawX();
                    y = event.getRawY();
                    lastX = x;
                    lastY = y;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    lastX = event.getRawX();
                    lastY = event.getRawY();

                    //Log.i(TAG, "handleTouchEvent: action move lastX" + lastX + " lastY:" + lastY + " y:" + y + " x:" + x);

                    if (!view.isZoomed() && lastY > y) {
                        view.setTranslationY(lastY - y);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    if (lastY - y > Util.dip2px(mContext, 30)) {

                        if (!view.isZoomed())
                            finishActivity();
                    } else {

                        if (!view.isZoomed())
                            view.setTranslationY(0);
                    }

                }
            }
        }

        private void setCloudOffPosition(View view, Media media, boolean isLandScape) {

            int mediaWidth = Integer.parseInt(media.getWidth());
            int mediaHeight = Integer.parseInt(media.getHeight());
            int actualWidth;
            int actualHeight;
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();

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

                layoutParams.setMargins(0, marginTop, marginRight, 0);

            } else if (mediaWidth - mediaHeight < screenWidth - screenHeight) {

                layoutParams.setMargins(0, systemUIHeight, marginRight, 0);

            }

            view.setLayoutParams(layoutParams);
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

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            GifTouchNetworkImageView mainPic = (GifTouchNetworkImageView) ((View) object).findViewById(R.id.mainPic);
            mainPic.unregisterImageLoadListener();

            container.removeView((View) object);
        }

        @Override
        public int getCount() {

            if (medias == null || medias.size() == 0) {
                return 0;
            } else {
                return medias.size();
            }
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    private void convertEditState() {

        sInEdit = !sInEdit;
        if (sInEdit) {
            mToolbar.setVisibility(View.VISIBLE);
            mMaskLayout.setVisibility(View.VISIBLE);
            rlPanelFooter.setVisibility(View.VISIBLE);
        } else {
            mToolbar.setVisibility(View.INVISIBLE);
            mMaskLayout.setVisibility(View.INVISIBLE);
            rlPanelFooter.setVisibility(View.INVISIBLE);
        }
    }

    private void toggleFullScreenState(View view) {

        mIsFullScreen = !mIsFullScreen;
        if (mIsFullScreen) {
            Util.hideSystemUI(view);
        } else {
            Util.showSystemUI(view);
        }
    }
}
