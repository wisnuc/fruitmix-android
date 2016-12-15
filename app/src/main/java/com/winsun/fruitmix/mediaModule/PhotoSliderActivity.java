package com.winsun.fruitmix.mediaModule;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.IImageLoadListener;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.component.GifTouchNetworkImageView;
import com.winsun.fruitmix.gif.GifLoader;
import com.winsun.fruitmix.gif.GifLruCache;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.util.CustomTransitionListener;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoSliderActivity extends AppCompatActivity implements IImageLoadListener {

    public static final String TAG = PhotoSliderActivity.class.getSimpleName();

    @BindView(R.id.date)
    TextView lbDate;
    @BindView(R.id.back)
    ImageView ivBack;
    @BindView(R.id.comment)
    LinearLayout ivComment;
    @BindView(R.id.chooseHeader)
    Toolbar rlChooseHeader;
    @BindView(R.id.panelFooter)
    RelativeLayout rlPanelFooter;
    @BindView(R.id.return_resize)
    ImageView mReturnResize;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;

    private List<Media> mediaList;
    private int initialPhotoPosition = 0;
    private int currentPhotoPosition = 0;

    private List<Media> mediaAlreadyLoadedList;

    private Map<String, Media> mediaMap;

    private boolean sInEdit;

    private ImageLoader mImageLoader;

    private GifLoader mGifLoader;

    private Context mContext;

    private boolean willReturn = false;

    private boolean transitionMediaNeedShowThumb = true;

    private boolean needTransition = true;

    private SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            if (willReturn) {
                if (initialPhotoPosition != currentPhotoPosition) {

                    names.clear();
                    sharedElements.clear();

                    Media media = mediaList.get(currentPhotoPosition);

                    String imageUUID = media.getUuid();
                    names.add(imageUUID);

                    String imageTag;

                    boolean isThumb = media.isLoaded();
                    imageTag = getImageUrl(isThumb, media);

                    sharedElements.put(imageUUID, mViewPager.findViewWithTag(imageTag));

                    Log.i(TAG, "onMapSharedElements: media uuid:" + imageUUID + " imageTag:" + imageTag);
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_slider);

        mContext = this;

        ButterKnife.bind(this);

        needTransition = getIntent().getBooleanExtra(Util.KEY_NEED_TRANSITION, true);

        Log.i(TAG, "onCreate: needTransition:" + needTransition);

        if (needTransition) {
            ActivityCompat.postponeEnterTransition(this);
            setEnterSharedElementCallback(sharedElementCallback);
        }

        initImageLoaderAndGifLoader();

        initialPhotoPosition = getIntent().getIntExtra(Util.INITIAL_PHOTO_POSITION, 0);

        boolean mShowCommentBtn = getIntent().getBooleanExtra(Util.KEY_SHOW_COMMENT_BTN, false);

        mediaList = LocalCache.photoSliderList;

        mediaMap = LocalCache.photoSliderMap;

        mediaAlreadyLoadedList = new ArrayList<>();

        transitionMediaNeedShowThumb = getIntent().getBooleanExtra(Util.KEY_TRANSITION_PHOTO_NEED_SHOW_THUMB, true);

        refreshReturnResizeVisibility();

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });

        initCommentBtn(mShowCommentBtn);

        sInEdit = true;

        initViewPager();

        setPosition(initialPhotoPosition);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;
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
                        intent.putExtra(Util.IMAGE_UUID, imageUUID);

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
        RequestQueue mRequestQueue = RequestQueueInstance.getInstance(this).getRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);

        mGifLoader = new GifLoader(mRequestQueue, GifLruCache.instance());
        mGifLoader.setHeaders(headers);
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

        intent.putExtra(Util.CURRENT_MEDIA_UUID, mediaList.get(currentPhotoPosition).getUuid());
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
            Log.d(TAG, "image:" + mediaList.get(position));

            String title = mediaList.get(position).getTime();
            if (title == null || title.contains("1916-01-01")) {
                lbDate.setText(getString(R.string.unknown_time_text));
            } else {
                lbDate.setText(title);
            }
        }

    }

    @Override
    public void onImageLoadFinish(String url, View view) {

        if (url == null)
            return;

        Media media = mediaMap.get(url);

        if (media.isLocal()) {

            handleLocalMediaLoaded(media);

        } else {

            handleRemoteMediaLoaded(url, view, media);

        }


//        for (int i = 0; i < mediaList.size(); i++) {
//            final Media media = mediaList.get(i);
//
//            final String imageUrl = getImageUrl(isImageThumb(url), media);
//
//            if (url.equals(imageUrl)) {
//
//                if (media.isLocal()) {
//
//                    if (isCurrentViewPage(i) && needTransition) {
//                        ActivityCompat.startPostponedEnterTransition(this);
//                    }
//
//                    if (!media.isLoaded()) {
//                        media.setLoaded(true);
//                    }
//
//                    if (isCurrentViewPage(i) && mDialog != null && mDialog.isShowing())
//                        mDialog.dismiss();
//
//                } else {
//
//                    if (isImageThumb(url)) {
//
//                        if (isCurrentViewPage(i) && needTransition) {
//                            ActivityCompat.startPostponedEnterTransition(this);
//
//                            startLoadCurrentImageAfterTransition(media);
//                        } else {
//                            String imageUUID = media.getUuid();
//
//                            startLoadingOriginalPhoto(imageUUID);
//                        }
//
//                    } else {
//
//                        if (!transitionMediaNeedShowThumb && needTransition) {
//                            ActivityCompat.startPostponedEnterTransition(this);
//                            transitionMediaNeedShowThumb = true;
//                        } else {
//                            dismissCurrentImageThumb(media);
//
//                            view.setVisibility(View.VISIBLE);
//                        }
//
//                        if (!media.isLoaded()) {
//                            media.setLoaded(true);
//                        }
//
//                        if (isCurrentViewPage(i) && mDialog != null && mDialog.isShowing())
//                            mDialog.dismiss();
//                    }
//
//                }
//
//            }
//
//        }

    }

    private void handleRemoteMediaLoaded(String url, View view, Media media) {
        if (isImageThumb(url)) {

            handleThumbLoaded(view, media);

        } else {

            handleOriginalMediaLoaded(view, media);

        }
    }

    private void handleOriginalMediaLoaded(View view, Media media) {
        if (!transitionMediaNeedShowThumb && needTransition) {
            ActivityCompat.startPostponedEnterTransition(this);
            transitionMediaNeedShowThumb = true;
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

    public boolean isImageThumb(String imageUrl) {
        return imageUrl.contains("thumb");
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
        }
    }

    private void dismissCurrentImageThumb(Media media) {
        String thumbImageUrl = media.getImageThumbUrl(mContext);
        View view = mViewPager.findViewWithTag(thumbImageUrl);

        Log.i(TAG, "dismissCurrentImageThumb: view visibility:" + view.getVisibility());

        view.setVisibility(View.INVISIBLE);
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

        mImageLoader.setShouldCache(true);

        mainPic.setOrientationNumber(media.getOrientationNumber());

        mainPic.setTag(remoteUrl);

        if (media.getType().equalsIgnoreCase("gif")) {
            mainPic.setGifUrl(remoteUrl, mGifLoader);
        } else {
            mainPic.setImageUrl(remoteUrl, mImageLoader);
        }

    }

    private class MyAdapter extends PagerAdapter {

        @Override
        public CharSequence getPageTitle(int position) {
            return "选X项" + position;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            View view;

            view = LayoutInflater.from(mContext).inflate(R.layout.photo_slider_cell, null);

            GifTouchNetworkImageView mainPic = (GifTouchNetworkImageView) view.findViewById(R.id.mainPic);

            if (mediaList.size() > position && position > -1) {

                Media media = mediaList.get(position);

                Log.i(TAG, "instantiateItem: orientationNumber:" + media.getOrientationNumber());

                setDefaultMainPicAndMainPicScreenHeight(mainPic, media);

                mainPic.registerImageLoadListener(PhotoSliderActivity.this);

                mainPic.setDefaultImageResId(R.drawable.placeholder_photo);

                mImageLoader.setShouldCache(!media.isLocal());

                if (transitionMediaNeedShowThumb && !media.isLocal()) {

                    if (position == initialPhotoPosition)
                        ViewCompat.setTransitionName(mainPic, media.getUuid());

                    String thumbImageUrl = media.getImageThumbUrl(mContext);
                    mainPic.setTag(thumbImageUrl);

                    mainPic.setImageUrl(thumbImageUrl, mImageLoader);

                } else {

                    if (position == initialPhotoPosition)
                        ViewCompat.setTransitionName(mainPic, media.getUuid());

                    mainPic.setOrientationNumber(media.getOrientationNumber());

                    String originalImageUrl = media.getImageOriginalUrl(mContext);
                    mainPic.setTag(originalImageUrl);

                    if (originalImageUrl.endsWith(".gif")) {
                        mainPic.setGifUrl(originalImageUrl, mGifLoader);
                    } else {
                        mainPic.setImageUrl(originalImageUrl, mImageLoader);
                    }

                }

                mainPic.setOnTouchListener(new CustomTouchListener());

            }

            container.addView(view);

            Log.i(TAG, "inistatiate position : " + position);

            return view;

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
                    if (lastY - y > 200 && !view.isZoomed()) finishActivity();
                    else if (!view.isZoomed()) {
                        view.setTranslationY(0);
                        if (Math.abs(lastY - y) + Math.abs(lastX - x) < 10) {
                            convertEditState();
                        }
                    }
                }
            }
        }

        private void setDefaultMainPicAndMainPicScreenHeight(GifTouchNetworkImageView mainPic, Media media) {

            int mediaWidth = Integer.parseInt(media.getWidth());
            int mediaHeight = Integer.parseInt(media.getHeight());
            int actualWidth = 0;
            int actualHeight = 0;
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mainPic.getLayoutParams();

            if (mediaWidthLargerThanHeight(media, mediaWidth, mediaHeight)) {
                actualWidth = Util.calcScreenWidth(PhotoSliderActivity.this);
                actualHeight = mediaHeight * actualWidth / mediaWidth;
            } else if (mediaHeightLargerThanWidth(media, mediaWidth, mediaHeight)) {
                actualHeight = Util.dip2px(mContext, 600);
                actualWidth = mediaWidth * actualHeight / mediaHeight;
            } else if (mediaWidthEqualsHeight(mediaWidth, mediaHeight)) {

                actualWidth = actualHeight = Util.calcScreenWidth(PhotoSliderActivity.this);
            }

            layoutParams.width = actualWidth;
            layoutParams.height = actualHeight;

            mainPic.setLayoutParams(layoutParams);
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

        private void convertEditState() {
            sInEdit = !sInEdit;
            if (sInEdit) {
                rlChooseHeader.setVisibility(View.VISIBLE);
                rlPanelFooter.setVisibility(View.VISIBLE);
            } else {
                rlChooseHeader.setVisibility(View.GONE);
                rlPanelFooter.setVisibility(View.GONE);
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            GifTouchNetworkImageView mainPic = (GifTouchNetworkImageView) ((View) object).findViewById(R.id.mainPic);
            mainPic.unregisterImageLoadListener();

            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            if (mediaList.size() == 0) {
                return 0;
            } else {
                return mediaList.size();
            }
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

}
