package com.winsun.fruitmix;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
import com.winsun.fruitmix.component.TouchNetworkImageView;
import com.winsun.fruitmix.interfaces.IImageLoadListener;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PhotoSliderActivity extends AppCompatActivity implements IImageLoadListener {

    public static final String TAG = PhotoSliderActivity.class.getSimpleName();

    public List<Map<String, Object>> imgList;
    int pos;

    TextView lbDate;
    ImageView ivBack, ivComment;
    RelativeLayout rlChooseHeader;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_slider);

        mContext = this;

        mRequestQueue = RequestQueueInstance.REQUEST_QUEUE_INSTANCE.getmRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);

        pos = getIntent().getIntExtra("pos", 0);

        mShowCommentBtn = getIntent().getBooleanExtra(Util.KEY_SHOW_COMMENT_BTN, false);

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

        rlChooseHeader = (RelativeLayout) findViewById(R.id.chooseHeader);
        rlPanelFooter = (RelativeLayout) findViewById(R.id.panelFooter);
        lbDate = (TextView) findViewById(R.id.date);
        ivBack = (ImageView) findViewById(R.id.back);
        ivComment = (ImageView) findViewById(R.id.comment);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (mShowCommentBtn) {
            ivComment.setVisibility(View.VISIBLE);
            ivComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (imgList.size() > pos) {
                        Intent intent = new Intent();
                        intent.setClass(PhotoSliderActivity.this, MediaShareCommentActivity.class);
                        intent.putExtra("imageUUID", "" + imgList.get(pos).get("uuid"));
                        startActivity(intent);
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

        imgList = (List<Map<String, Object>>) LocalCache.TransActivityContainer.get("imgSliderList");

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        myAdapter = new MyAdapter(this);
        mViewPager.setAdapter(myAdapter);
        mViewPager.setCurrentItem(pos, false);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected:" + position);
                setPosition(position);

                Map<String, Object> map = imgList.get(position);

                if (!map.containsKey(LOAD_FINISH) && mViewPager.getCurrentItem() == position) {
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

        setPosition(pos);
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
        pos = position;

        if (imgList.size() > position && position > -1) {
            Log.d(TAG, "image:" + imgList.get(position));

            String title = (String) imgList.get(position).get("mtime");
            if (title == null || title.contains("1916-01-01")) {
                lbDate.setText(getString(R.string.unknown_time_text));
            } else {
                lbDate.setText((String) imgList.get(position).get("mtime"));
            }
        }

    }

    @Override
    public void onImageLoadFinish(String url) {

        if (url == null)
            return;

        for (int i = 0; i < imgList.size(); i++) {
            Map<String, Object> map = imgList.get(i);
            String currentUrl;
            if (map.get("cacheType").equals("local")) {
                currentUrl = String.valueOf(map.get("thumb"));
            } else {
                currentUrl = String.format(getString(R.string.original_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + map.get("resHash"));
            }
            if (currentUrl.equals(url)) {

                if (!map.containsKey(LOAD_FINISH)) {
                    map.put(LOAD_FINISH, "true");
                }

                if (mViewPager.getCurrentItem() == i && mDialog != null && mDialog.isShowing())
                    mDialog.dismiss();
            }
        }

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

            final TouchNetworkImageView networkImageView = (TouchNetworkImageView) view.findViewById(R.id.mainPic);
            ImageView defaultMainImg = (ImageView) view.findViewById(R.id.default_main_pic);

            networkImageView.registerImageLoadListener(PhotoSliderActivity.this);


            if (imgList.size() > position && position > -1) {

                networkImageView.setVisibility(View.VISIBLE);
                defaultMainImg.setVisibility(View.GONE);

                Map<String, Object> map = imgList.get(position);

                if (!map.containsKey(LOAD_FINISH) && mViewPager.getCurrentItem() == position) {
                    if (mDialog == null || !mDialog.isShowing()) {
                        mDialog = ProgressDialog.show(mContext, mContext.getString(R.string.loading_title), mContext.getString(R.string.loading_message), true, true, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        });
                    }
                }

                if (map.get("cacheType").equals("local")) {
                    String url = String.valueOf(map.get("thumb"));

                    mImageLoader.setShouldCache(false);
                    networkImageView.setTag(url);
                    networkImageView.setDefaultImageResId(R.drawable.placeholder_photo);
                    networkImageView.setImageUrl(url, mImageLoader);
                } else {

                    String url = String.format(getString(R.string.original_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + map.get("resHash"));

                    mImageLoader.setShouldCache(true);
                    networkImageView.setTag(url);
                    networkImageView.setDefaultImageResId(R.drawable.placeholder_photo);
                    networkImageView.setImageUrl(url, mImageLoader);

                }

                networkImageView.setOnTouchListener(new View.OnTouchListener() {

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
                            if (!networkImageView.isZoomed()) {
                                networkImageView.setTranslationY(lastY - y);
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (lastY - y > 200 && !networkImageView.isZoomed()) finish();
                            else if (!networkImageView.isZoomed()) {
                                networkImageView.setTranslationY(0);
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

            } else {
                networkImageView.setVisibility(View.GONE);
                defaultMainImg.setVisibility(View.VISIBLE);
            }

            container.addView(view);

            Log.i(TAG, "inistatiate position : " + position);

            return view;


        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            TouchNetworkImageView touchNetworkImageView = (TouchNetworkImageView) ((View) object).findViewById(R.id.mainPic);
            touchNetworkImageView.unregisterImageLoadListener();

            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            if (imgList.size() == 0) {
                return 1;
            } else {
                return imgList.size();
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
