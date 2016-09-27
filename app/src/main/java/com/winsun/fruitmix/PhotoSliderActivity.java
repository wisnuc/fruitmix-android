package com.winsun.fruitmix;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.IImageLoadListener;
import com.winsun.fruitmix.component.TouchNetworkImageView;
import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.util.CustomTransitionListener;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PhotoSliderActivity extends AppCompatActivity implements IImageLoadListener {

    public static final String TAG = PhotoSliderActivity.class.getSimpleName();

    private List<Media> mediaList;
    private int initialPhotoPosition;
    private int currentPhotoPosition;

    TextView lbDate;
    ImageView ivBack, ivComment;
    Toolbar rlChooseHeader;
    RelativeLayout rlPanelFooter;

    private ImageView mReturnResize;
    private MyAdapter myAdapter;
    private ViewPager mViewPager;

    boolean sInEdit;

    private ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;

    private Context mContext;

    private ProgressDialog mDialog;
    private static final String LOAD_FINISH = "load_finish";

    private boolean mShowCommentBtn = false;

    private boolean willReturn = false;

    private boolean transitionMediaNeedShowThumb = true;

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
                    imageTag = generateImageUrl(isThumb, media);

                    sharedElements.put(imageUUID, mViewPager.findViewWithTag(imageTag));

                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.postponeEnterTransition(this);

        setContentView(R.layout.activity_photo_slider);

        setEnterSharedElementCallback(sharedElementCallback);

        mContext = this;

        mRequestQueue = RequestQueueInstance.REQUEST_QUEUE_INSTANCE.getmRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);

        initialPhotoPosition = getIntent().getIntExtra(Util.INITIAL_PHOTO_POSITION, 0);

        mShowCommentBtn = getIntent().getBooleanExtra(Util.KEY_SHOW_COMMENT_BTN, false);

        mediaList = getIntent().getParcelableArrayListExtra(Util.KEY_MEDIA_LIST);

        transitionMediaNeedShowThumb = getIntent().getBooleanExtra(Util.KEY_TRANSITION_PHOTO_NEED_SHOW_THUMB, true);

        if (getShowPhotoReturnTipsValue()) {
            setShowPhotoReturnTipsValue(false);

            mReturnResize = (ImageView) findViewById(R.id.return_resize1);
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

        rlChooseHeader = (Toolbar) findViewById(R.id.chooseHeader);
        rlPanelFooter = (RelativeLayout) findViewById(R.id.panelFooter);
        lbDate = (TextView) findViewById(R.id.date);
        ivBack = (ImageView) findViewById(R.id.back);
        ivComment = (ImageView) findViewById(R.id.comment);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });

        if (mShowCommentBtn) {
            ivComment.setVisibility(View.VISIBLE);
            ivComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mediaList.size() > currentPhotoPosition) {

                        String imageUUID = mediaList.get(currentPhotoPosition).getUuid();

                        Intent intent = new Intent();
                        intent.setClass(PhotoSliderActivity.this, MediaShareCommentActivity.class);
                        intent.putExtra(Util.IMAGE_UUID, imageUUID);

                        intent.putExtra(Util.INITIAL_PHOTO_POSITION, currentPhotoPosition);

                        View view = mViewPager.getChildAt(currentPhotoPosition).findViewById(R.id.mainPic);

                        ViewCompat.setTransitionName(view, imageUUID);

                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(PhotoSliderActivity.this, view, imageUUID);

                        startActivity(intent, options.toBundle());
                    }
                }
            });
        } else {
            ivComment.setVisibility(View.GONE);
        }

        sInEdit = true;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, getString(R.string.replace_action), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        myAdapter = new MyAdapter(this);
        mViewPager.setAdapter(myAdapter);
        mViewPager.setCurrentItem(initialPhotoPosition, false);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected:" + position);
                setPosition(position);

                Media media = mediaList.get(position);

                if (!media.isLoaded() && mViewPager.getCurrentItem() == position) {
                    if (mDialog == null || !mDialog.isShowing()) {
                        mDialog = ProgressDialog.show(mContext, mContext.getString(R.string.loading_title), mContext.getString(R.string.loading_message), true, true, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        });
                    }
                }
            }

        });

        setPosition(initialPhotoPosition);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity();
    }

    private void finishActivity() {
        if (Util.checkRunningOnLollipopOrHigher())
            finishAfterTransition();
        else
            finish();
    }

    @Override
    public void finishAfterTransition() {

        willReturn = true;
        Intent intent = new Intent();
        intent.putExtra(Util.INITIAL_PHOTO_POSITION, initialPhotoPosition);
        intent.putExtra(Util.CURRENT_PHOTO_POSITION, currentPhotoPosition);
        intent.putExtra(Util.CURRENT_PHOTO_DATE, lbDate.getText().toString());
        intent.putExtra(Util.CURRENT_MEDIASHARE_TIME, getIntent().getStringExtra(Util.CURRENT_MEDIASHARE_TIME));
        setResult(RESULT_OK, intent);

        super.finishAfterTransition();
    }

    //add by liang.wu
    private boolean getShowPhotoReturnTipsValue() {
        SharedPreferences sp;
        sp = getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(Util.SHOW_PHOTO_RETURN_TIPS, true);
    }

    //add by liang.wu
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

        for (int i = 0; i < mediaList.size(); i++) {
            final Media media = mediaList.get(i);

            final String imageUrl = generateImageUrl(isImageThumb(url), media);

            if (url.equals(imageUrl)) {

                if (media.isLocal()) {

                    if (isCurrentViewPage(i)) {
                        ActivityCompat.startPostponedEnterTransition(this);
                    }

                } else {

                    if (isImageThumb(url)) {

                        if (isCurrentViewPage(i)) {
                            ActivityCompat.startPostponedEnterTransition(this);

                            startLoadCurrentImageAfterTransition(media);
                        } else {
                            final String imageUUID = media.getUuid();

                            startLoadingOriginalPhoto(imageUUID);
                        }

                    } else {

                        if (!transitionMediaNeedShowThumb) {
                            ActivityCompat.startPostponedEnterTransition(this);
                            transitionMediaNeedShowThumb = true;
                        }else {
                            dismissCurrentImageThumb(media);
                        }

                        if (!media.isLoaded()) {
                            media.setLoaded(true);
                        }

                        if (isCurrentViewPage(i) && mDialog != null && mDialog.isShowing())
                            mDialog.dismiss();
                    }

                }

            }

        }

    }

    public boolean isCurrentViewPage(int viewPosition) {
        return mViewPager.getCurrentItem() == viewPosition;
    }

    public boolean isImageThumb(String imageUrl) {
        return imageUrl.contains("thumb");
    }

    private void startLoadCurrentImageAfterTransition(final Media media) {
        if (Util.checkRunningOnLollipopOrHigher()) {
            getWindow().getSharedElementEnterTransition().addListener(new CustomTransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);

                    startLoadingOriginalPhoto(media.getUuid());

                    showDialog();
                }
            });
        }
    }

    private void dismissCurrentImageThumb(Media media) {
        String thumbImageUrl = generateThumbImageUrl(media);
        final TouchNetworkImageView defaultMainPic = (TouchNetworkImageView) mViewPager.findViewWithTag(thumbImageUrl);
        defaultMainPic.setVisibility(View.INVISIBLE);
    }

    private String generateThumbImageUrl(Media media) {

        int width = Integer.parseInt(media.getWidth());
        int height = Integer.parseInt(media.getHeight());

        int[] result = Util.formatPhotoWidthHeight(width, height);

        return String.format(getString(R.string.thumb_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + media.getUuid(), String.valueOf(result[0]), String.valueOf(result[1]));

    }

    private String generateImageUrl(boolean isThumb, Media media) {
        String currentUrl;
        if (media.isLocal()) {
            currentUrl = String.valueOf(media.getThumb());
        } else {

            if (isThumb) {

                currentUrl = generateThumbImageUrl(media);

            } else {
                currentUrl = String.format(getString(R.string.original_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + media.getUuid());
            }

        }

        return currentUrl;
    }

    private void showDialog() {

        if (mDialog == null || !mDialog.isShowing()) {
            mDialog = ProgressDialog.show(mContext, mContext.getString(R.string.loading_title), mContext.getString(R.string.loading_message), true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finishActivity();
                }
            });
        }
    }

    private void startLoadingOriginalPhoto(final String imageUUID) {

        final String remoteUrl = String.format(getString(R.string.original_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + imageUUID);

        final TouchNetworkImageView mainPic = (TouchNetworkImageView) mViewPager.findViewWithTag(remoteUrl);

        mainPic.setVisibility(View.VISIBLE);

        ViewCompat.setTransitionName(mainPic, imageUUID);

        mImageLoader.setShouldCache(true);

        mainPic.setImageUrl(remoteUrl, mImageLoader);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.left_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class MyAdapter extends PagerAdapter {
        private PhotoSliderActivity activity;

        public MyAdapter(PhotoSliderActivity activity) {
            this.activity = activity;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "选X项" + position;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            View view;

            view = LayoutInflater.from(activity).inflate(R.layout.photo_slider_cell, null);

            TouchNetworkImageView defaultMainPic = (TouchNetworkImageView) view.findViewById(R.id.default_main_pic);

            final TouchNetworkImageView mainPic = (TouchNetworkImageView) view.findViewById(R.id.mainPic);

            if (mediaList.size() > position && position > -1) {

                Media media = mediaList.get(position);

                int screenHeight = Util.calcScreenWidth(PhotoSliderActivity.this);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) defaultMainPic.getLayoutParams();
                layoutParams.height = screenHeight;
                defaultMainPic.setLayoutParams(layoutParams);

                if (media.isLocal()) {

                    defaultMainPic.setVisibility(View.INVISIBLE);
                    mainPic.setVisibility(View.VISIBLE);
                    ViewCompat.setTransitionName(mainPic, media.getUuid());

                    mainPic.registerImageLoadListener(PhotoSliderActivity.this);

                    String localImageUrl = media.getThumb();

                    mImageLoader.setShouldCache(false);
                    mainPic.setTag(localImageUrl);
                    mainPic.setImageUrl(localImageUrl, mImageLoader);

                } else {

                    mainPic.registerImageLoadListener(PhotoSliderActivity.this);
                    String remoteUrl = String.format(getString(R.string.original_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + media.getUuid());
                    mainPic.setTag(remoteUrl);

                    if (transitionMediaNeedShowThumb) {
                        defaultMainPic.setVisibility(View.VISIBLE);
                        mainPic.setVisibility(View.INVISIBLE);
                        ViewCompat.setTransitionName(defaultMainPic, media.getUuid());

                        defaultMainPic.registerImageLoadListener(PhotoSliderActivity.this);

                        int width = Integer.parseInt(media.getWidth());
                        int height = Integer.parseInt(media.getHeight());

                        int[] result = Util.formatPhotoWidthHeight(width, height);

                        String thumbImageUrl = String.format(getString(R.string.thumb_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + media.getUuid(), String.valueOf(result[0]), String.valueOf(result[1]));

                        mImageLoader.setShouldCache(true);
                        defaultMainPic.setTag(thumbImageUrl);
                        defaultMainPic.setImageUrl(thumbImageUrl, mImageLoader);

                    } else {

                        mainPic.setVisibility(View.VISIBLE);
                        defaultMainPic.setVisibility(View.INVISIBLE);

                        ViewCompat.setTransitionName(mainPic,media.getUuid());
                        mImageLoader.setShouldCache(true);
                        mainPic.setImageUrl(remoteUrl,mImageLoader);
                    }

                }

                mainPic.setOnTouchListener(new View.OnTouchListener() {

                    float x, y, lastX, lastY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        //Log.d("winsun", "aa "+event.getAction());
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            x = event.getRawX();
                            y = event.getRawY();
                            lastX = x;
                            lastY = y;
                        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            lastX = event.getRawX();
                            lastY = event.getRawY();
                            if (!mainPic.isZoomed() && lastY > y && lastX - x <= 0) {
                                mainPic.setTranslationY(lastY - y);
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (lastY - y > 200 && !mainPic.isZoomed()) finishActivity();
                            else if (!mainPic.isZoomed()) {
                                mainPic.setTranslationY(0);
                                if (Math.abs(lastY - y) + Math.abs(lastX - x) < 10) {
                                    sInEdit = !sInEdit;
                                    if (sInEdit) {
                                        rlChooseHeader.setVisibility(View.VISIBLE);
                                        rlPanelFooter.setVisibility(View.VISIBLE);
                                    } else {
                                        rlChooseHeader.setVisibility(View.GONE);
                                        rlPanelFooter.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }
                        return false;
                    }
                });

            }

            container.addView(view);

            Log.i(TAG, "inistatiate position : " + position);

            return view;


        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            TouchNetworkImageView defaultMainPic = (TouchNetworkImageView) ((View) object).findViewById(R.id.default_main_pic);
            defaultMainPic.unregisterImageLoadListener();

            TouchNetworkImageView mainPic = (TouchNetworkImageView) ((View) object).findViewById(R.id.mainPic);
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
