package com.winsun.fruitmix;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
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
import android.view.animation.BounceInterpolator;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.winsun.fruitmix.Fragment.AlbumList;
import com.winsun.fruitmix.Fragment.NewPhotoList;
import com.winsun.fruitmix.Fragment.PhotoList;
import com.winsun.fruitmix.Fragment.ShareList;
import com.winsun.fruitmix.component.NavPageBar;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NavPagerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NavPageBar.OnTabChangedListener, View.OnClickListener, IPhotoListListener {

    public static final String TAG = NavPagerActivity.class.getSimpleName();

    public Toolbar toolbar;
    public RelativeLayout chooseHeader;
    public TabLayout tabLayout;
    public ImageView fab;
    public TextView lbRight;
    ViewPager viewPager;
    ImageView ivBack;
    TextView ivSelectCount;

    public LinearLayout llBtMenu;
    public ImageView ivBtAlbum, ivBtShare;

    public List<Page> pageList;
    AlbumList albumList;
    NewPhotoList photoList;
    ShareList shareList;

    public boolean sInChooseMode = false;

    //add by liang.wu
    private ImageView mAlbumBalloon;
    private TextView mUserNameTextView;
    private TextView mUserAvatar;

    NavPageBar mNavPageBar;

    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;

    private LocalBroadcastManager mManager;
    private CustomBroadReceiver mReceiver;

    private Animator mAnimator;

    private boolean sMenuUnfolding = false;

    private Context mContext;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_pager);

        mContext = this;

        mManager = LocalBroadcastManager.getInstance(this);
        mReceiver = new CustomBroadReceiver();
        IntentFilter intentFilter = new IntentFilter(Util.LOCAL_SHARE_CHANGED);
        intentFilter.addAction(Util.LOCAL_COMMENT_CHANGED);
        intentFilter.addAction(Util.LOCAL_PHOTO_UPLOAD_STATE_CHANGED);
        mManager.registerReceiver(mReceiver, intentFilter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        chooseHeader = (RelativeLayout) findViewById(R.id.chooseHeader);
        ivBack = (ImageView) findViewById(R.id.back);
        ivSelectCount = (TextView) findViewById(R.id.select_count_tv);

        llBtMenu = (LinearLayout) findViewById(R.id.btmenu);
        ivBtAlbum = (ImageView) findViewById(R.id.bt_album);
        ivBtShare = (ImageView) findViewById(R.id.bt_share);
        lbRight = (TextView) findViewById(R.id.right);

        //setSupportActionBar(toolbar);

        fab = (ImageView) findViewById(R.id.fab);
        /*
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        //drawer.openDrawer(GravityCompat.START);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        String userName = getIntent().getStringExtra(Util.EQUIPMENT_CHILD_NAME);
        mUserNameTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.user_name_textview);
        mUserNameTextView.setText(userName);

        mUserAvatar = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.avatar);
        StringBuilder stringBuilder = new StringBuilder();
        String[] splitStrings = userName.split(" ");
        for (String splitString : splitStrings) {
            stringBuilder.append(splitString.substring(0, 1).toUpperCase());
        }
        mUserAvatar.setText(stringBuilder.toString());
        int color = new Random().nextInt(3);
        switch (color) {
            case 0:
                mUserAvatar.setBackgroundResource(R.drawable.user_portrait_bg_blue);
                break;
            case 1:
                mUserAvatar.setBackgroundResource(R.drawable.user_portrait_bg_green);
                break;
            case 2:
                mUserAvatar.setBackgroundResource(R.drawable.user_portrait_bg_yellow);
                break;
        }


        shareList = new ShareList(this);
        photoList = new NewPhotoList(this);
        albumList = new AlbumList(this);
        pageList = new ArrayList<Page>();
        pageList.add(shareList);
        pageList.add(photoList);
        pageList.add(albumList);

        final MyAdapter myAdapter = new MyAdapter(this);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(myAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                onDidAppear(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mNavPageBar = new NavPageBar(tabLayout, viewPager);

        viewPager.setCurrentItem(1);
        onDidAppear(1);

        ivBtAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoList.createAlbum();
                hideChooseHeader();
            }
        });

        ivBtShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoList.createShare();
            }
        });

        fab.setOnClickListener(this);
        lbRight.setOnClickListener(this);

        photoList.addPhotoListListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mNavPageBar.registerOnTabChangedListener(this);
/*
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    FNAS.LoadDocuments();
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();

                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {

                pageList.get(viewPager.getCurrentItem()).refreshView();

            }
        }.execute();*/

    }

    @Override
    protected void onPause() {
        super.onPause();

        mNavPageBar.unregisterOnTabChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mManager.unregisterReceiver(mReceiver);

        photoList.removePhotoListListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (sMenuUnfolding) {
                    sMenuUnfolding = false;
                    collapseFab();

                } else {
                    sMenuUnfolding = true;
                    extendFab();
                }
                break;
            case R.id.right:
                /*                if (!Util.getNetworkState(containerActivity)) {
                    Toast.makeText(containerActivity, containerActivity.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    return;
                }*/

                showChooseHeader();
                break;
            default:
        }
    }

    private void collapseFab() {

        mAnimator = AnimatorInflater.loadAnimator(this, R.animator.fab_restore);
        mAnimator.setTarget(fab);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(this, R.animator.album_btn_restore);
        mAnimator.setTarget(ivBtAlbum);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(this, R.animator.share_btn_restore);
        mAnimator.setInterpolator(new BounceInterpolator());
        mAnimator.setTarget(ivBtShare);
        mAnimator.start();

        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                ivBtAlbum.setVisibility(View.GONE);
                ivBtShare.setVisibility(View.GONE);

                Log.i(TAG, "share getTop:" + ivBtShare.getTop());
                Log.i(TAG, "share getTranslationY" + ivBtShare.getTranslationY());
                Log.i(TAG, "share getY:" + ivBtShare.getY());
            }
        });

    }

    private void extendFab() {

        ivBtAlbum.setVisibility(View.VISIBLE);
        ivBtShare.setVisibility(View.VISIBLE);

        mAnimator = AnimatorInflater.loadAnimator(this, R.animator.fab_remote);
        mAnimator.setTarget(fab);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(this, R.animator.album_btn_translation);
        mAnimator.setTarget(ivBtAlbum);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(this, R.animator.share_btn_translation);
        mAnimator.setInterpolator(new BounceInterpolator());
        mAnimator.setTarget(ivBtShare);
        mAnimator.start();

    }

    @Override
    public void onPhotoItemClick(int selectedItemCount) {
        if (sMenuUnfolding) {
            sMenuUnfolding = false;
            collapseFab();
        }

        setSelectCountText(String.format(getString(R.string.select_count), selectedItemCount));
    }

    @Override
    public void onPhotoItemLongClick(int selectedItemCount) {
        showChooseHeader();
    }

    private class CustomBroadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Util.LOCAL_SHARE_CHANGED)) {

                Log.i(TAG, "local share changed");

                albumList.refreshView();
            } else if (intent.getAction().equals(Util.LOCAL_COMMENT_CHANGED)) {

                Log.i(TAG, "local changed");

                shareList.refreshView();
            } else if (intent.getAction().equals(Util.LOCAL_PHOTO_UPLOAD_STATE_CHANGED)) {
                Log.i(TAG, "local photo upload state changed");

                photoList.refreshView();
            }
        }
    }

    @Override
    public void onNoPhotoItem(boolean noPhotoItem) {
        if (noPhotoItem) {
            lbRight.setVisibility(View.GONE);
        } else {
            lbRight.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (sInChooseMode) {
            hideChooseHeader();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == 200) {
            hideChooseHeader();
            viewPager.setCurrentItem(2);
            onDidAppear(2);
        } else if (resultCode == 201) {
            hideChooseHeader();
            viewPager.setCurrentItem(0);

            onDidAppear(0);
        }
    }

    private void onDidAppear(int position) {
        switch (position) {
            case 0://share
                toolbar.setTitle(getString(R.string.share_text));
                fab.setVisibility(View.GONE);
                ivBtAlbum.setVisibility(View.GONE);
                ivBtShare.setVisibility(View.GONE);
                lbRight.setVisibility(View.GONE);
                break;
            case 1://photo
                toolbar.setTitle(getString(R.string.photo_text));
                lbRight.setVisibility(View.VISIBLE);
                break;
            case 2://album
                toolbar.setTitle(getString(R.string.album_text));
                fab.setVisibility(View.GONE);
                ivBtAlbum.setVisibility(View.GONE);
                ivBtShare.setVisibility(View.GONE);
                lbRight.setVisibility(View.GONE);
                break;
            default:
        }
        pageList.get(position).onDidAppear();

    }

    public void setSelectCountText(String text) {
        ivSelectCount.setText(text);
    }

    public void showTips() {
        //add by liang.wu
        if (getShowAlbumTipsValue()) {
            setShowAlbumTipsValue(false);

            mAlbumBalloon = (ImageView) findViewById(R.id.album_balloon);
            if (mAlbumBalloon != null) {
                mAlbumBalloon.setVisibility(View.VISIBLE);
                mAlbumBalloon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlbumBalloon.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    //add by liang.wu
    private boolean getShowAlbumTipsValue() {
        SharedPreferences sp;
        sp = getSharedPreferences("fruitMix", Context.MODE_PRIVATE);
        return sp.getBoolean(Util.SHOW_ALBUM_TIPS, true);
    }

    //add by liang.wu
    private void setShowAlbumTipsValue(boolean value) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = getSharedPreferences("fruitMix", Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putBoolean(Util.SHOW_ALBUM_TIPS, value);
        editor.apply();
    }

    @Override
    public void onTabChanged(int tabNum) {

        if (tabNum == NavPageBar.TAB_ALBUM) {
            showTips();
        }
    }

    public void showChooseHeader() {
        chooseHeader.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.GONE);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) viewPager.getLayoutParams();
        lp.bottomMargin = 0;
        viewPager.setLayoutParams(lp);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        sInChooseMode = true;
        photoList.setSelectMode(sInChooseMode);
        setSelectCountText(getString(R.string.choose_photo));

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void hideChooseHeader() {
        if (sMenuUnfolding) {
            sMenuUnfolding = false;
            collapseFab();
        }
        fab.setVisibility(View.GONE);
        ivBtAlbum.setVisibility(View.GONE);
        ivBtShare.setVisibility(View.GONE);
        chooseHeader.setVisibility(View.GONE);
        tabLayout.setVisibility(View.VISIBLE);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) viewPager.getLayoutParams();
        lp.bottomMargin = Util.Dp2Px(48.0f);
        //if(LocalCache.ScreenWidth==540) lp.bottomMargin=76;
        //else if(LocalCache.ScreenWidth==1080) lp.bottomMargin=140;
        viewPager.setLayoutParams(lp);
        sInChooseMode = false;
        photoList.setSelectMode(sInChooseMode);
        setSelectCountText(getString(R.string.photo_text));

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

/*        if (id == R.id.person_info) {
            Intent intent = new Intent(this, PersonInfoActivity.class);
            startActivity(intent);
        } else if (id == R.id.cloud) {

        } else if (id == R.id.user_manage) {
            Intent intent = new Intent(this, UserManageActivity.class);
            startActivity(intent);
        } else if (id == R.id.setting) {

            Intent intent = new Intent(this, EquipmentSearchActivity.class);
            startActivity(intent);

        } else if (id == R.id.help) {

//            Intent intent = new Intent(this,GalleryTestActivity.class);
//            startActivity(intent);

        } else */
        if (id == R.id.logout) {

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    mDialog = ProgressDialog.show(mContext, mContext.getString(R.string.operating_title), getString(R.string.loading_message), true, false);

                }

                @Override
                protected Void doInBackground(Void... params) {

                    LocalCache.clearGatewayUuidPasswordToken(mContext);
                    LocalCache.CleanAll();
                    FNAS.restoreLocalPhotoUploadState();

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);

                    mDialog.dismiss();

                    Intent intent = new Intent(NavPagerActivity.this, EquipmentSearchActivity.class);
                    startActivity(intent);
                    finish();

                }

            }.execute();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private class MyAdapter extends PagerAdapter {
        private NavPagerActivity activity;

        public MyAdapter(NavPagerActivity activity) {
            this.activity = activity;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "选X项" + position;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view = pageList.get(position).getView();

            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);

        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }

    public interface Page {
        public abstract void onDidAppear();

        public abstract View getView();

        void refreshView();
    }
}
