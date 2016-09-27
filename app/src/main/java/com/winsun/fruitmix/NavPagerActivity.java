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
import android.support.design.widget.TabLayout;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.Fragment.AlbumList;
import com.winsun.fruitmix.Fragment.NewPhotoList;
import com.winsun.fruitmix.Fragment.MediaShareList;
import com.winsun.fruitmix.component.NavPageBar;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NavPagerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NavPageBar.OnTabChangedListener, View.OnClickListener, IPhotoListListener {

    public static final String TAG = NavPagerActivity.class.getSimpleName();

    public Toolbar toolbar;
    //    public RelativeLayout chooseHeader;
    public TabLayout tabLayout;
    public FloatingActionButton fab;
    public TextView lbRight;
    ViewPager viewPager;
//    ImageView ivBack;
//    TextView ivSelectCount;

    public LinearLayout llBtMenu;
    public ImageView ivBtAlbum, ivBtShare;

    public List<Page> pageList;
    AlbumList albumList;
    NewPhotoList photoList;
    MediaShareList shareList;

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
    private IntentFilter intentFilter;

    private Animator mAnimator;

    private boolean sMenuUnfolding = false;

    private Context mContext;

    private ProgressDialog mDialog;

    private static final int PAGE_SHARE = 0;
    private static final int PAGE_PHOTO = 1;
    private static final int PAGE_ALBUM = 2;

    private ExecutorServiceInstance instance;

    private boolean mLocalMediaLoaded = false;

    private SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            if (viewPager.getCurrentItem() == PAGE_PHOTO) {
                ((NewPhotoList) pageList.get(viewPager.getCurrentItem())).onMapSharedElements(names, sharedElements);
            } else if (viewPager.getCurrentItem() == PAGE_SHARE) {
                ((MediaShareList) pageList.get(viewPager.getCurrentItem())).onMapSharedElements(names, sharedElements);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_pager);

        mContext = this;

        setExitSharedElementCallback(sharedElementCallback);

        mManager = LocalBroadcastManager.getInstance(this);
        mReceiver = new CustomBroadReceiver();
        intentFilter = new IntentFilter(Util.REMOTE_SHARE_CREATED);
        intentFilter.addAction(Util.REMOTE_SHARE_MODIFIED);
        intentFilter.addAction(Util.LOCAL_SHARE_MODIFIED);
        intentFilter.addAction(Util.LOCAL_SHARE_DELETED);
        intentFilter.addAction(Util.REMOTE_SHARE_DELETED);
        intentFilter.addAction(Util.REMOTE_COMMENT_CREATED);
        intentFilter.addAction(Util.LOCAL_SHARE_CREATED);
        intentFilter.addAction(Util.LOCAL_PHOTO_UPLOAD_STATE_CHANGED);
        intentFilter.addAction(Util.REMOTE_MEDIA_RETRIEVED);
        intentFilter.addAction(Util.REMOTE_MEDIA_SHARE_RETRIEVED);
        intentFilter.addAction(Util.LOCAL_MEDIA_RETRIEVED);
        intentFilter.addAction(Util.LOCAL_MEDIA_SHARE_RETRIEVED);
        intentFilter.addAction(Util.NEW_LOCAL_MEDIA_IN_CAMERA_RETRIEVED);
        intentFilter.addAction(Util.LOCAL_MEDIA_COMMENT_RETRIEVED);
        intentFilter.addAction(Util.REMOTE_MEDIA_COMMENT_RETRIEVED);

        instance = ExecutorServiceInstance.SINGLE_INSTANCE;
        instance.startFixedThreadPool();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        chooseHeader = (RelativeLayout) findViewById(R.id.chooseHeader);
//        ivBack = (ImageView) findViewById(R.id.back);
//        ivSelectCount = (TextView) findViewById(R.id.select_count_tv);

        setSupportActionBar(toolbar);

        llBtMenu = (LinearLayout) findViewById(R.id.btmenu);
        ivBtAlbum = (ImageView) findViewById(R.id.bt_album);
        ivBtShare = (ImageView) findViewById(R.id.bt_share);
        lbRight = (TextView) findViewById(R.id.right);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
/*        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();*/
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


        shareList = new MediaShareList(this);
        photoList = new NewPhotoList(this);
        albumList = new AlbumList(this);
        pageList = new ArrayList<Page>();
        pageList.add(shareList);
        pageList.add(photoList);
        pageList.add(albumList);

        final MyAdapter myAdapter = new MyAdapter();
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(myAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (mLocalMediaLoaded)
                    onDidAppear(position);
            }

        });

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mNavPageBar = new NavPageBar(tabLayout, viewPager);

        viewPager.setCurrentItem(PAGE_PHOTO);

        ivBtAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> selectUUIDs = photoList.getSelectedImageUUIDs();
                if (selectUUIDs.size() == 0) {
                    Toast.makeText(mContext, getString(R.string.select_nothing), Toast.LENGTH_SHORT).show();
                    return;
                }

                photoList.createAlbum(selectUUIDs);
                hideChooseHeader();
            }
        });

        ivBtShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> selectUUIDs = photoList.getSelectedImageUUIDs();
                if (selectUUIDs.size() == 0) {
                    Toast.makeText(mContext, getString(R.string.select_nothing), Toast.LENGTH_SHORT).show();
                    return;
                }

                mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);

                createShare(selectUUIDs);

            }
        });

        fab.setOnClickListener(this);
        lbRight.setOnClickListener(this);

        photoList.addPhotoListListener(this);

        FNAS.retrieveMediaMap(mContext);
        FNAS.retrieveLocalMediaCommentMap(mContext);
    }

    private void retrieveLocalMediaInCamera() {
        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE, OperationType.GET.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.LOCAL_MEDIA_IN_CAMERA.name());

        mManager.sendBroadcast(intent);
    }

    public void retrieveRemoteMediaComment(String mediaUUID) {

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE, OperationType.GET.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.REMOTE_MEDIA_COMMENT.name());
        intent.putExtra(Util.OPERATION_IMAGE_UUID, mediaUUID);
        mManager.sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mNavPageBar.registerOnTabChangedListener(this);

        mManager.registerReceiver(mReceiver, intentFilter);

        if (viewPager.getCurrentItem() == PAGE_PHOTO && mLocalMediaLoaded) {
            retrieveLocalMediaInCamera();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        mNavPageBar.unregisterOnTabChangedListener(this);

        mManager.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        photoList.removePhotoListListener(this);

        instance.shutdownFixedThreadPool();
    }

    public void createShare(List<String> selectUUIDs) {

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE, OperationType.CREATE.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.LOCAL_MEDIASHARE.name());

        intent.putExtra(Util.OPERATION_MEDIASHARE, generateMediaShare(selectUUIDs));
        mManager.sendBroadcast(intent);
    }

    public void modifyMediaShare(MediaShare mediaShare) {
        if (!mediaShare.getMaintainer().contains(FNAS.userUUID)) {
            Toast.makeText(mContext, getString(R.string.no_edit_photo_permission), Toast.LENGTH_SHORT).show();

            return;
        }

        mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE, OperationType.MODIFY.name());
        if (Util.getNetworkState(mContext)) {
            intent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.REMOTE_MEDIASHARE.name());
        } else {
            intent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.LOCAL_MEDIASHARE.name());
        }
        intent.putExtra(Util.OPERATION_MEDIASHARE, mediaShare);
        mManager.sendBroadcast(intent);
    }

    public void deleteMediaShare(MediaShare mediaShare) {
        if (!mediaShare.getMaintainer().contains(FNAS.userUUID)) {
            Toast.makeText(mContext, getString(R.string.no_edit_photo_permission), Toast.LENGTH_SHORT).show();

            return;
        }

        mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE, OperationType.DELETE.name());
        if (Util.getNetworkState(mContext)) {
            intent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.REMOTE_MEDIASHARE.name());
        } else {
            intent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.LOCAL_MEDIASHARE.name());
        }
        intent.putExtra(Util.OPERATION_MEDIASHARE, mediaShare);
        mManager.sendBroadcast(intent);
    }


    private MediaShare generateMediaShare(List<String> selectUUIDs) {

        MediaShare mediaShare = new MediaShare();
        mediaShare.setUuid(Util.createLocalUUid());
        mediaShare.setImageDigests(selectUUIDs);
        mediaShare.setCoverImageDigest(selectUUIDs.get(0));
        mediaShare.setTitle("");
        mediaShare.setDesc("");
        mediaShare.setViewer(new ArrayList<>(LocalCache.RemoteUserMapKeyIsUUID.keySet()));
        mediaShare.setMaintainer(Collections.singletonList(FNAS.userUUID));
        mediaShare.setCreatorUUID(FNAS.userUUID);
        mediaShare.setTime(String.valueOf(System.currentTimeMillis()));
        mediaShare.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(mediaShare.getTime()))));
        mediaShare.setArchived(false);
        mediaShare.setAlbum(false);
        mediaShare.setLocked(true);

        return mediaShare;

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (viewPager.getCurrentItem() == PAGE_PHOTO) {
            ((NewPhotoList) pageList.get(viewPager.getCurrentItem())).onActivityReenter(resultCode, data);
        } else if (viewPager.getCurrentItem() == PAGE_SHARE) {
            ((MediaShareList) pageList.get(viewPager.getCurrentItem())).onActivityReenter(resultCode, data);
        }

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

    private void dismissDialog() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    public void onPhotoItemLongClick(int selectedItemCount) {
        showChooseHeader();
    }

    private class CustomBroadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Util.LOCAL_SHARE_CREATED)) {

                String result = intent.getStringExtra(Util.OPERATION_RESULT);

                OperationResult operationResult = OperationResult.valueOf(result);

                switch (operationResult) {
                    case SUCCEED:

                        if (Util.getNetworkState(mContext)) {
                            MediaShare mediaShare = intent.getParcelableExtra(Util.OPERATION_MEDIASHARE);
                            Intent operationIntent = new Intent(Util.OPERATION);
                            operationIntent.putExtra(Util.OPERATION_TYPE, OperationType.CREATE.name());
                            operationIntent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.REMOTE_MEDIASHARE.name());
                            operationIntent.putExtra(Util.OPERATION_MEDIASHARE, mediaShare);
                            mManager.sendBroadcast(operationIntent);
                        }

                        onActivityResult(Util.KEY_CREATE_SHARE_REQUEST_CODE, Activity.RESULT_OK, null);
                        break;
                    case FAIL:

                        dismissDialog();

                        Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();

                        break;
                }

            } else if (intent.getAction().equals(Util.REMOTE_SHARE_CREATED)) {

                Log.i(TAG, "remote share created");

                String result = intent.getStringExtra(Util.OPERATION_RESULT);

                OperationResult operationResult = OperationResult.valueOf(result);

                switch (operationResult) {
                    case SUCCEED:

                        String localMediaShareUUID = intent.getStringExtra(Util.OPERATION_LOCAL_MEDIASHARE_UUID);
                        boolean localMediaShareLocked = intent.getBooleanExtra(Util.OPERATION_LOCAL_MEDIASHARE_LOCKED,false);
                        Intent operationIntent = new Intent(Util.OPERATION);
                        operationIntent.putExtra(Util.OPERATION_TYPE, OperationType.DELETE.name());
                        operationIntent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.LOCAL_MEDIASHARE.name());
                        operationIntent.putExtra(Util.OPERATION_LOCAL_MEDIASHARE_UUID, localMediaShareUUID);
                        operationIntent.putExtra(Util.OPERATION_LOCAL_MEDIASHARE_LOCKED,localMediaShareLocked);
                        mManager.sendBroadcast(operationIntent);

                        break;
                    case FAIL:

                        dismissDialog();

                        Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();

                        break;
                }


            } else if (intent.getAction().equals(Util.LOCAL_SHARE_DELETED) || intent.getAction().equals(Util.REMOTE_SHARE_MODIFIED) || intent.getAction().equals(Util.REMOTE_SHARE_DELETED) || intent.getAction().equals(Util.LOCAL_SHARE_MODIFIED)) {

                dismissDialog();

                String result = intent.getStringExtra(Util.OPERATION_RESULT);

                OperationResult operationResult = OperationResult.valueOf(result);

                switch (operationResult) {
                    case SUCCEED:
                        Toast.makeText(mContext, getString(R.string.operation_success), Toast.LENGTH_SHORT).show();

                        pageList.get(PAGE_ALBUM).refreshView();
                        pageList.get(PAGE_SHARE).refreshView();

                        break;
                    case LOCAL_MEDIASHARE_UPLOADING:
                        Toast.makeText(mContext, getString(R.string.share_uploading), Toast.LENGTH_SHORT).show();

                        break;
                    case NO_NETWORK:
                        Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_SHORT).show();

                        break;
                    case FAIL:
                        Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();

                        break;
                }

            } else if (intent.getAction().equals(Util.REMOTE_COMMENT_CREATED) && intent.getStringExtra(Util.OPERATION_RESULT).equals(OperationResult.SUCCEED.name())) {

                Log.i(TAG, "remote comment created");

                Comment comment = intent.getParcelableExtra(Util.OPERATION_COMMENT);
                String imageUUID = intent.getStringExtra(Util.OPERATION_IMAGE_UUID);
                Intent operationIntent = new Intent(Util.OPERATION);
                operationIntent.putExtra(Util.OPERATION_TYPE, OperationType.DELETE.name());
                operationIntent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.LOCAL_MEDIA_COMMENT.name());
                operationIntent.putExtra(Util.OPERATION_COMMENT, comment);
                operationIntent.putExtra(Util.OPERATION_IMAGE_UUID, imageUUID);
                mManager.sendBroadcast(operationIntent);

            } else if (intent.getAction().equals(Util.LOCAL_COMMENT_DELETED)) {

                Log.i(TAG, "local comment changed");

                if (intent.getStringExtra(Util.OPERATION_RESULT).equals(OperationResult.SUCCEED.name())) {
                    pageList.get(PAGE_SHARE).refreshView();
                }

            } else if (intent.getAction().equals(Util.LOCAL_PHOTO_UPLOAD_STATE_CHANGED)) {
                Log.i(TAG, "local photo upload state changed");

                Intent operationIntent = new Intent(Util.OPERATION);
                operationIntent.putExtra(Util.OPERATION_TYPE, OperationType.GET.name());
                operationIntent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.REMOTE_MEDIA.name());
                mManager.sendBroadcast(operationIntent);

            } else if (intent.getAction().equals(Util.REMOTE_MEDIA_RETRIEVED)) {
                Log.i(TAG, "remote media loaded");

                pageList.get(PAGE_PHOTO).refreshView();

                FNAS.retrieveShareMap(mContext);


            } else if (intent.getAction().equals(Util.LOCAL_MEDIA_RETRIEVED)) {

                Log.i(TAG, "local media loaded");

                mLocalMediaLoaded = true;
                retrieveLocalMediaInCamera();

            } else if (intent.getAction().equals(Util.NEW_LOCAL_MEDIA_IN_CAMERA_RETRIEVED)) {

                OperationResult result = OperationResult.valueOf(intent.getStringExtra(Util.OPERATION_RESULT));

                if (result == OperationResult.SUCCEED) {

                    Log.i(TAG, "new local media in camera loaded");

                    pageList.get(PAGE_PHOTO).refreshView();
                }

                FNAS.startUploadAllLocalPhoto(mContext);


            } else if (intent.getAction().equals(Util.REMOTE_MEDIA_SHARE_RETRIEVED)) {
                Log.i(TAG, "remote share loaded");
                pageList.get(PAGE_ALBUM).refreshView();
                pageList.get(PAGE_SHARE).refreshView();

            } else if (intent.getAction().equals(Util.LOCAL_MEDIA_SHARE_RETRIEVED)) {
                Log.i(TAG, "local share loaded");
                pageList.get(PAGE_ALBUM).refreshView();
                pageList.get(PAGE_SHARE).refreshView();

                Intent operationIntent = new Intent(Util.OPERATION);
                operationIntent.putExtra(Util.OPERATION_TYPE, OperationType.CREATE.name());
                operationIntent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.REMOTE_MEDIASHARE.name());
                for (MediaShare mediaShare : LocalCache.LocalMediaShareMapKeyIsUUID.values()) {

                    operationIntent.putExtra(Util.OPERATION_MEDIASHARE, mediaShare);
                    mManager.sendBroadcast(operationIntent);

                }
            } else if (intent.getAction().equals(Util.LOCAL_MEDIA_COMMENT_RETRIEVED)) {
                Log.i(TAG, "local media comment loaded");
                pageList.get(PAGE_SHARE).refreshView();

                Intent operationResult = new Intent(Util.OPERATION);
                operationResult.putExtra(Util.OPERATION_TYPE, OperationType.CREATE.name());
                operationResult.removeExtra(Util.OPERATION_TARGET_TYPE);
                operationResult.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.REMOTE_MEDIA_COMMENT.name());

                for (Map.Entry<String, Comment> entry : LocalCache.LocalMediaCommentMapKeyIsImageUUID.entrySet()) {

                    operationResult.putExtra(Util.OPERATION_COMMENT, entry.getValue());
                    operationResult.putExtra(Util.OPERATION_IMAGE_UUID, entry.getKey());
                    mManager.sendBroadcast(operationResult);
                }
            } else if (intent.getAction().equals(Util.REMOTE_MEDIA_COMMENT_RETRIEVED)) {

                Log.i(TAG, "remote media comment loaded ");

                pageList.get(PAGE_SHARE).refreshView();

            }
        }
    }

    @Override
    public void onNoPhotoItem(boolean noPhotoItem) {

        Log.i(TAG, "onNoPhotoItem:" + noPhotoItem);
        int currentItem = viewPager.getCurrentItem();

        if (noPhotoItem && currentItem == PAGE_PHOTO) {
            lbRight.setVisibility(View.GONE);
        } else if (!noPhotoItem && currentItem == PAGE_PHOTO) {
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

        if ((requestCode == Util.KEY_ALBUM_CONTENT_REQUEST_CODE || requestCode == Util.KEY_CREATE_ALBUM_REQUEST_CODE || requestCode == Util.KEY_CHOOSE_PHOTO_REQUEST_CODE) && resultCode == RESULT_OK) {
            hideChooseHeader();
            viewPager.setCurrentItem(PAGE_ALBUM);
            onDidAppear(PAGE_ALBUM);
        } else if (requestCode == Util.KEY_CREATE_SHARE_REQUEST_CODE && resultCode == RESULT_OK) {
            hideChooseHeader();
            viewPager.setCurrentItem(PAGE_SHARE);
            onDidAppear(PAGE_SHARE);
        }
    }

    private void onDidAppear(int position) {
        switch (position) {
            case PAGE_SHARE:
                toolbar.setTitle(getString(R.string.share_text));
                fab.setVisibility(View.GONE);
                ivBtAlbum.setVisibility(View.GONE);
                ivBtShare.setVisibility(View.GONE);
                lbRight.setVisibility(View.GONE);
                break;
            case PAGE_PHOTO:
                toolbar.setTitle(getString(R.string.photo_text));
                lbRight.setVisibility(View.VISIBLE);
                fab.setVisibility(View.GONE);
                break;
            case PAGE_ALBUM:
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
        toolbar.setTitle(text);
    }

    public void showTips() {
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

    private boolean getShowAlbumTipsValue() {
        SharedPreferences sp;
        sp = getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(Util.SHOW_ALBUM_TIPS, true);
    }

    private void setShowAlbumTipsValue(boolean value) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
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
        fab.setVisibility(View.VISIBLE);
        toolbar.setNavigationIcon(R.drawable.arrow_back);
        lbRight.setVisibility(View.GONE);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) viewPager.getLayoutParams();
        lp.bottomMargin = 0;
        viewPager.setLayoutParams(lp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
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
        toolbar.setNavigationIcon(R.drawable.menu);
        toolbar.setNavigationOnClickListener(null);
        lbRight.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.VISIBLE);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) viewPager.getLayoutParams();
        lp.bottomMargin = Util.dip2px(48.0f);
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

                    LocalCache.clearToken(mContext);
                    FNAS.restoreLocalPhotoUploadState();

                    instance.shutdownFixedThreadPoolNow();

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
        void onDidAppear();

        View getView();

        void refreshView();
    }
}
