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
import com.winsun.fruitmix.file.FileMainActivity;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.MediaShareContent;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavPagerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NavPageBar.OnTabChangedListener, View.OnClickListener, IPhotoListListener {

    public static final String TAG = NavPagerActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.right)
    TextView lbRight;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.btmenu)
    LinearLayout llBtMenu;
    @BindView(R.id.bt_album)
    ImageView ivBtAlbum;
    @BindView(R.id.bt_share)
    ImageView ivBtShare;
    @BindView(R.id.album_balloon)
    ImageView mAlbumBalloon;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private List<Page> pageList;
    private AlbumList albumList;
    private NewPhotoList photoList;
    private MediaShareList shareList;
    private NavPageBar mNavPageBar;

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

    private boolean sInChooseMode = false;

    private boolean mLocalMediaLoaded = false;
    private boolean mRemoteMediaLoaded = false;

    private boolean onCreate = false;

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

        ButterKnife.bind(this);

        initFilterForReceiver();

        instance = ExecutorServiceInstance.SINGLE_INSTANCE;
        instance.startFixedThreadPool();

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchDrawerOpenState();
            }
        });

/*        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();*/

        initNavigationView();

        initPageList();

        initViewPager();

        mNavPageBar = new NavPageBar(tabLayout, viewPager);
        viewPager.setCurrentItem(PAGE_PHOTO);

        ivBtAlbum.setOnClickListener(this);
        ivBtShare.setOnClickListener(this);
        fab.setOnClickListener(this);
        lbRight.setOnClickListener(this);

        photoList.addPhotoListListener(this);

    }

    private void switchDrawerOpenState() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void initNavigationView() {
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        User user = LocalCache.RemoteUserMapKeyIsUUID.get(FNAS.userUUID);

        String userName = user.getUserName();
        TextView mUserNameTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.user_name_textview);
        mUserNameTextView.setText(userName);

        TextView mUserAvatar = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.avatar);
        mUserAvatar.setText(user.getDefaultAvatar());
        mUserAvatar.setBackgroundResource(user.getDefaultAvatarBgColorResourceId());

    }

    private void initViewPager() {
        MyAdapter myAdapter = new MyAdapter();
        viewPager.setAdapter(myAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mLocalMediaLoaded)
                    onDidAppear(position);
            }
        });
    }

    private void initPageList() {
        shareList = new MediaShareList(this);
        photoList = new NewPhotoList(this);
        albumList = new AlbumList(this);
        pageList = new ArrayList<Page>();
        pageList.add(shareList);
        pageList.add(photoList);
        pageList.add(albumList);
    }

    private void initFilterForReceiver() {
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
    }

    private void retrieveLocalMediaInCamera() {
        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.GET.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIA_IN_CAMERA.name());

        mManager.sendBroadcast(intent);
    }

    public void retrieveRemoteMediaComment(String mediaUUID) {

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.GET.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIA_COMMENT.name());
        intent.putExtra(Util.OPERATION_IMAGE_UUID, mediaUUID);
        mManager.sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mNavPageBar.registerOnTabChangedListener(this);

        if (viewPager.getCurrentItem() == PAGE_PHOTO && mLocalMediaLoaded & mRemoteMediaLoaded) {
            retrieveLocalMediaInCamera();
        }

        mManager.registerReceiver(mReceiver, intentFilter);

        if (!onCreate) {
            FNAS.retrieveLocalMediaMap(mContext);
//            FNAS.retrieveLocalMediaCommentMap(mContext);
            FNAS.retrieveLocalMediaShare(mContext);
            FNAS.retrieveRemoteMediaShare(mContext);

            onCreate = true;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        mManager.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mNavPageBar.unregisterOnTabChangedListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        photoList.removePhotoListListener(this);

        instance.shutdownFixedThreadPool();


    }

    public void doCreateShareFunction(List<String> selectUUIDs) {

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.CREATE.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIASHARE.name());

        intent.putExtra(Util.OPERATION_MEDIASHARE, createMediaShare(selectUUIDs));
        mManager.sendBroadcast(intent);
    }

    public void modifyMediaShare(MediaShare mediaShare, String requestData) {
        if (!checkPermissionToOperate(mediaShare)) {
            Toast.makeText(mContext, getString(R.string.no_edit_photo_permission), Toast.LENGTH_SHORT).show();

            return;
        }

        mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.MODIFY.name());
        if (Util.getNetworkState(mContext)) {
            intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIASHARE.name());
        } else {
            intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIASHARE.name());
        }
        intent.putExtra(Util.OPERATION_MEDIASHARE, mediaShare);
        intent.putExtra(Util.KEY_MODIFY_REMOTE_MEDIASHARE_REQUEST_DATA, requestData);
        mManager.sendBroadcast(intent);
    }

    private boolean checkPermissionToOperate(MediaShare mediaShare) {
        return mediaShare.checkMaintainersListContainCurrentUserUUID() || mediaShare.getCreatorUUID().equals(FNAS.userUUID);
    }

    public void deleteMediaShare(MediaShare mediaShare) {
        if (!checkPermissionToOperate(mediaShare)) {
            Toast.makeText(mContext, getString(R.string.no_edit_photo_permission), Toast.LENGTH_SHORT).show();

            return;
        }

        mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.DELETE.name());
        if (Util.getNetworkState(mContext)) {
            intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIASHARE.name());
        } else {
            intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIASHARE.name());
        }
        intent.putExtra(Util.OPERATION_MEDIASHARE, mediaShare);
        mManager.sendBroadcast(intent);
    }


    private MediaShare createMediaShare(List<String> selectUUIDs) {

        MediaShare mediaShare = new MediaShare();
        mediaShare.setUuid(Util.createLocalUUid());

        List<MediaShareContent> mediaShareContents = new ArrayList<>();
        for (String digest : selectUUIDs) {
            MediaShareContent mediaShareContent = new MediaShareContent();
            mediaShareContent.setDigest(digest);
            mediaShareContent.setAuthor(FNAS.userUUID);
            mediaShareContent.setTime(String.valueOf(System.currentTimeMillis()));
            mediaShareContents.add(mediaShareContent);
        }

        mediaShare.initMediaShareContents(mediaShareContents);

        mediaShare.setCoverImageDigest(selectUUIDs.get(0));
        mediaShare.setTitle("");
        mediaShare.setDesc("");
        for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
            mediaShare.addViewer(userUUID);
        }
        mediaShare.addMaintainer(FNAS.userUUID);
        mediaShare.setCreatorUUID(FNAS.userUUID);
        mediaShare.setTime(String.valueOf(System.currentTimeMillis()));
        mediaShare.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(mediaShare.getTime()))));
        mediaShare.setArchived(false);
        mediaShare.setAlbum(false);
        mediaShare.setLocal(true);

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
        List<String> selectUUIDs;
        switch (v.getId()) {
            case R.id.bt_album:
                selectUUIDs = photoList.getSelectedImageUUIDs();
                if (selectUUIDs.size() == 0) {
                    Toast.makeText(mContext, getString(R.string.select_nothing), Toast.LENGTH_SHORT).show();
                    return;
                }

                photoList.createAlbum(selectUUIDs);
                hideChooseHeader();
                break;
            case R.id.bt_share:
                selectUUIDs = photoList.getSelectedImageUUIDs();
                if (selectUUIDs.size() == 0) {
                    Toast.makeText(mContext, getString(R.string.select_nothing), Toast.LENGTH_SHORT).show();
                    return;
                }

                mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);

                doCreateShareFunction(selectUUIDs);
                break;
            case R.id.fab:
                refreshFabState();
                break;
            case R.id.right:
                showChooseHeader();
                break;
            default:
        }
    }

    private void refreshFabState() {
        if (sMenuUnfolding) {
            sMenuUnfolding = false;
            collapseFab();
        } else {
            sMenuUnfolding = true;
            extendFab();
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

                handleLocalShareCreated(intent);

            } else if (intent.getAction().equals(Util.REMOTE_SHARE_CREATED)) {

                handleRemoteShareCreated(intent);

            } else if (intent.getAction().equals(Util.LOCAL_SHARE_DELETED) || intent.getAction().equals(Util.REMOTE_SHARE_MODIFIED) || intent.getAction().equals(Util.REMOTE_SHARE_DELETED) || intent.getAction().equals(Util.LOCAL_SHARE_MODIFIED)) {

                handleLocalShareModifiedDeletedOrRemoteShareModifiedDeleted(intent);

            } else if (intent.getAction().equals(Util.REMOTE_COMMENT_CREATED)) {

                handleRemoteCommentCreated(intent);

            } else if (intent.getAction().equals(Util.LOCAL_COMMENT_DELETED)) {

                handleLocalCommentDeleted(intent);

            } else if (intent.getAction().equals(Util.LOCAL_PHOTO_UPLOAD_STATE_CHANGED)) {

                handleLocalPhotoUploadStateChanged();

            } else if (intent.getAction().equals(Util.REMOTE_MEDIA_RETRIEVED)) {
                Log.i(TAG, "remote media loaded");

                pageList.get(PAGE_PHOTO).refreshView();

                mRemoteMediaLoaded = true;

                retrieveLocalMediaInCamera();

            } else if (intent.getAction().equals(Util.LOCAL_MEDIA_RETRIEVED)) {

                Log.i(TAG, "local media loaded");

                mLocalMediaLoaded = true;

                FNAS.retrieveRemoteMediaMap(mContext);

            } else if (intent.getAction().equals(Util.NEW_LOCAL_MEDIA_IN_CAMERA_RETRIEVED)) {

                handleNewLocalMediaInCaremaRetrieved(intent);

            } else if (intent.getAction().equals(Util.REMOTE_MEDIA_SHARE_RETRIEVED)) {
                Log.i(TAG, "remote share loaded");
                pageList.get(PAGE_ALBUM).refreshView();
                pageList.get(PAGE_SHARE).refreshView();

            } else if (intent.getAction().equals(Util.LOCAL_MEDIA_SHARE_RETRIEVED)) {
                Log.i(TAG, "local share loaded");
                pageList.get(PAGE_ALBUM).refreshView();
                pageList.get(PAGE_SHARE).refreshView();

                doCreateMediaShareInLocalMediaShareMapFunction();
            } else if (intent.getAction().equals(Util.LOCAL_MEDIA_COMMENT_RETRIEVED)) {
                Log.i(TAG, "local media comment loaded");
                ((MediaShareList) pageList.get(PAGE_SHARE)).refreshLocalComment();

                doCreateRemoteMediaCommentInLocalMediaCommentMapFunction();
            } else if (intent.getAction().equals(Util.REMOTE_MEDIA_COMMENT_RETRIEVED)) {

                Log.i(TAG, "remote media comment loaded ");

                ((MediaShareList) pageList.get(PAGE_SHARE)).refreshRemoteComment();

            }
        }

        private void doCreateRemoteMediaCommentInLocalMediaCommentMapFunction() {


            for (Map.Entry<String, List<Comment>> entry : LocalCache.LocalMediaCommentMapKeyIsImageUUID.entrySet()) {

                for (Comment comment : entry.getValue()) {
                    Intent operationResult = new Intent(Util.OPERATION);
                    operationResult.putExtra(Util.OPERATION_TYPE_NAME, OperationType.CREATE.name());
                    operationResult.removeExtra(Util.OPERATION_TARGET_TYPE_NAME);
                    operationResult.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIA_COMMENT.name());
                    operationResult.putExtra(Util.OPERATION_COMMENT, comment);
                    operationResult.putExtra(Util.OPERATION_IMAGE_UUID, entry.getKey());
                    mManager.sendBroadcast(operationResult);
                }

            }
        }

        private void doCreateMediaShareInLocalMediaShareMapFunction() {

            for (MediaShare mediaShare : LocalCache.LocalMediaShareMapKeyIsUUID.values()) {

                Intent operationIntent = new Intent(Util.OPERATION);
                operationIntent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.CREATE.name());
                operationIntent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIASHARE.name());
                operationIntent.putExtra(Util.OPERATION_MEDIASHARE, mediaShare);
                mManager.sendBroadcast(operationIntent);

            }
        }

        private void handleNewLocalMediaInCaremaRetrieved(Intent intent) {
            OperationResult result = OperationResult.valueOf(intent.getStringExtra(Util.OPERATION_RESULT_NAME));

            if (result == OperationResult.SUCCEED) {

                Log.i(TAG, "new local media in camera loaded");

                pageList.get(PAGE_PHOTO).refreshView();
            }

            FNAS.startUploadAllLocalPhoto(mContext);
        }

        private void handleLocalPhotoUploadStateChanged() {
            Log.i(TAG, "local photo upload state changed");

            Intent operationIntent = new Intent(Util.OPERATION);
            operationIntent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.GET.name());
            operationIntent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIA.name());
            mManager.sendBroadcast(operationIntent);
        }

        private void handleLocalCommentDeleted(Intent intent) {
            Log.i(TAG, "local comment changed");

            if (intent.getStringExtra(Util.OPERATION_RESULT_NAME).equals(OperationResult.SUCCEED.name())) {
                pageList.get(PAGE_SHARE).refreshView();
            }
        }

        private void handleRemoteCommentCreated(Intent intent) {
            if (intent.getStringExtra(Util.OPERATION_RESULT_NAME).equals(OperationResult.SUCCEED.name())) {
                Log.i(TAG, "remote comment created");

                Comment comment = intent.getParcelableExtra(Util.OPERATION_COMMENT);
                String imageUUID = intent.getStringExtra(Util.OPERATION_IMAGE_UUID);
                Intent operationIntent = new Intent(Util.OPERATION);
                operationIntent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.DELETE.name());
                operationIntent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIA_COMMENT.name());
                operationIntent.putExtra(Util.OPERATION_COMMENT, comment);
                operationIntent.putExtra(Util.OPERATION_IMAGE_UUID, imageUUID);
                mManager.sendBroadcast(operationIntent);
            }
        }

        private void handleLocalShareModifiedDeletedOrRemoteShareModifiedDeleted(Intent intent) {
            dismissDialog();

            String result = intent.getStringExtra(Util.OPERATION_RESULT_NAME);

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
        }

        private void handleRemoteShareCreated(Intent intent) {
            Log.i(TAG, "remote share created");

            String result = intent.getStringExtra(Util.OPERATION_RESULT_NAME);

            OperationResult operationResult = OperationResult.valueOf(result);

            switch (operationResult) {
                case SUCCEED:

                    MediaShare mediaShare = intent.getParcelableExtra(Util.OPERATION_MEDIASHARE);
                    Intent operationIntent = new Intent(Util.OPERATION);
                    operationIntent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.DELETE.name());
                    operationIntent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIASHARE.name());
                    operationIntent.putExtra(Util.OPERATION_MEDIASHARE, mediaShare);
                    mManager.sendBroadcast(operationIntent);

                    break;
                case FAIL:

                    dismissDialog();

                    Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();

                    break;
            }
        }

        private void handleLocalShareCreated(Intent intent) {
            String result = intent.getStringExtra(Util.OPERATION_RESULT_NAME);

            OperationResult operationResult = OperationResult.valueOf(result);

            switch (operationResult) {
                case SUCCEED:

                    if (Util.getNetworkState(mContext)) {
                        MediaShare mediaShare = intent.getParcelableExtra(Util.OPERATION_MEDIASHARE);
                        Intent operationIntent = new Intent(Util.OPERATION);
                        operationIntent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.CREATE.name());
                        operationIntent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIASHARE.name());
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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchDrawerOpenState();
            }
        });
        lbRight.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.VISIBLE);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) viewPager.getLayoutParams();
        lp.bottomMargin = Util.dip2px(this, 48.0f);
        //if(LocalCache.ScreenWidth==540) lp.bottomMargin=76;
        //else if(LocalCache.ScreenWidth==1080) lp.bottomMargin=140;
        viewPager.setLayoutParams(lp);
        sInChooseMode = false;
        photoList.setSelectMode(sInChooseMode);
        setSelectCountText(getString(R.string.photo_text));

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
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
                    FNAS.restoreLocalPhotoUploadState(mContext);

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

        } else if (id == R.id.file) {
            Intent intent = new Intent(mContext, FileMainActivity.class);
            startActivity(intent);
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
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
