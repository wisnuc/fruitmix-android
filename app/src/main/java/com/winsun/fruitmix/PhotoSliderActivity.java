package com.winsun.fruitmix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.winsun.fruitmix.Fragment.AlbumList;
import com.winsun.fruitmix.Fragment.PhotoList;
import com.winsun.fruitmix.Fragment.ShareList;
import com.winsun.fruitmix.component.BigLittleImageView;
import com.winsun.fruitmix.component.NavPageBar;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PhotoSliderActivity extends AppCompatActivity {

    public static final String TAG = PhotoSliderActivity.class.getSimpleName();

    public List<Map<String, Object>> imgList;
    int pos;

    TextView lbDate;
    ImageView ivBack, ivComment;
    RelativeLayout rlChooseHeader;
    RelativeLayout rlPanelFooter;

    //add by liang.wu
    private ImageView mReturnResize;
    private MyAdapter myAdapter;

    boolean sInEdit;
    //public List<Page> pageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_slider);

        pos = getIntent().getIntExtra("pos", 0);

        //add by liang.wu
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

        ivComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (imgList.size() > pos) {
                    Intent intent = new Intent();
                    intent.setClass(PhotoSliderActivity.this, ShareCommentActivity.class);
                    intent.putExtra("imageUUID", "" + imgList.get(pos).get("uuid"));
                    startActivity(intent);
                }
            }
        });
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

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        myAdapter = new MyAdapter(this);
        viewPager.setAdapter(myAdapter);
        viewPager.setCurrentItem(pos, false);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //LocalCache.LoadLocalBitmap((String) imgList.get(position).get("thumb"), (ImageView)imgList.get(position).get("view"));
                Log.d("winsun", position + "");
                setPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setPosition(pos);

    }

    //add by liang.wu
    private boolean getShowPhotoReturnTipsValue() {
        SharedPreferences sp;
        sp = getSharedPreferences("fruitMix", Context.MODE_PRIVATE);
        return sp.getBoolean(Util.SHOW_PHOTO_RETURN_TIPS, true);
    }

    //add by liang.wu
    private void setShowPhotoReturnTipsValue(boolean value) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = getSharedPreferences("fruitMix", Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putBoolean(Util.SHOW_PHOTO_RETURN_TIPS, false);
        editor.apply();
    }

    public void setPosition(int position) {
        pos = position;
        // ((BigLittleImageView) imgList.get(position).get("view")).loadBigPic();

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
        private long lastClickTime;


        public MyAdapter(PhotoSliderActivity activity) {
            this.activity = activity;
            lastClickTime = 0;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "选X项" + position;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            /*TextView tv = new TextView(activity);
            tv.setText("ViewPager" + position);
            tv.setTextSize(30.0f);
            tv.setGravity(Gravity.CENTER);

            container.addView(tv);

            return tv;
            */
            /*
            ImageView iv=new ImageView(activity);
            iv.setImageResource(R.drawable.yesshou);

            container.addView(iv);
            return iv;
            */
            View view;
            final BigLittleImageView iv;
            ImageView defaultMainImg;
            int w, h;

            view = LayoutInflater.from(activity).inflate(R.layout.photo_slider_cell, null);
            iv = (BigLittleImageView) view.findViewById(R.id.mainPic);
            defaultMainImg = (ImageView) view.findViewById(R.id.default_main_pic);

            Log.i(TAG, "instantiateItem");

            if (imgList.size() > position && position > -1) {

                iv.setVisibility(View.VISIBLE);
                defaultMainImg.setVisibility(View.GONE);

                activity.imgList.get(position).put("view", iv);

                w = Integer.parseInt((String) activity.imgList.get(position).get("width"));
                h = Integer.parseInt((String) activity.imgList.get(position).get("height"));
                if (w > h) {
                    w = w * 640 / h;
                    h = 640;
                } else if (h > w) {
                    h = h * 640 / w;
                    w = 640;
                }

                iv.setData(activity.imgList.get(position), w, h);

            /*
            if(pos==position) {
                iv.loadBigPic();
                lbDate.setText((String) activity.imgList.get(position).get("mtime"));
            }
            else iv.loadSmallPic();*/
                iv.loadSmallPic();
/*
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(iv.bigPic!=null) {
                        LocalCache.TransActivityContainer.put("bigPic", iv.bigPic);
                        Intent intent = new Intent();
                        intent.setClass(activity, ImageZoomActivity.class);
                        startActivity(intent);
                    }
                }
            });
*/

                iv.setOnTouchListener(new View.OnTouchListener() {

                    float x, y, lastX, lastY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        //Log.d("winsun", "aa "+event.getAction());
                        if (event.getAction() == 0) {
                            x = event.getRawX();
                            y = event.getRawY();
                            lastX = x;
                            lastY = y;
                        } else if (event.getAction() == 2) {
                            lastX = event.getRawX();
                            lastY = event.getRawY();
                            iv.setTranslationY(lastY - y);
                        } else if (event.getAction() == 1) {
                            if (lastY - y > 200) finish();
                            else {
                                iv.setTranslationY(0);
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
                        } else if (event.getAction() == 261) {
                            //LocalCache.TransActivityContainer.put("bigPic", iv.BigPic);
                            LocalCache.TransActivityContainer.put("bigPic", activity.imgList.get(position));
                            Intent intent = new Intent();
                            intent.setClass(activity, ImageZoomActivity.class);
                            startActivity(intent);
                        }
                        //return super.onTouch(v, event);
                        return false;
                    }
                });

                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("winsin", "clicked!");
                        if (new Date().getTime() - lastClickTime < 300) {
                            Log.d("winsin", "dblclicked!");
                            lastClickTime = 0;
                            LocalCache.TransActivityContainer.put("bigPic", activity.imgList.get(position));
                            Intent intent = new Intent();
                            intent.setClass(activity, ImageZoomActivity.class);
                            intent.putExtra("zoomIn", 1);
                            startActivity(intent);
                        } else lastClickTime = new Date().getTime();

                    }
                });
            } else {
                iv.setVisibility(View.GONE);
                defaultMainImg.setVisibility(View.VISIBLE);
            }

            container.addView(view);

            return view;


        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            BigLittleImageView iv = (BigLittleImageView) ((View) object).findViewById(R.id.mainPic);
            iv.recycleBigPic();
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
