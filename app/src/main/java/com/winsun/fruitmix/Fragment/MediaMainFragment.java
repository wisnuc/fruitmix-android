package com.winsun.fruitmix.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.component.NavPageBar;
import com.winsun.fruitmix.eventbus.MediaShareCommentOperationEvent;
import com.winsun.fruitmix.eventbus.MediaShareOperationEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.fileModule.interfaces.OnFileFragmentInteractionListener;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.interfaces.OnMainFragmentInteractionListener;
import com.winsun.fruitmix.mediaModule.fragment.AlbumList;
import com.winsun.fruitmix.mediaModule.fragment.MediaShareList;
import com.winsun.fruitmix.mediaModule.fragment.NewPhotoList;
import com.winsun.fruitmix.mediaModule.interfaces.OnMediaFragmentInteractionListener;
import com.winsun.fruitmix.mediaModule.interfaces.Page;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFileFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MediaMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MediaMainFragment extends Fragment implements OnMediaFragmentInteractionListener, View.OnClickListener, IPhotoListListener, NavPageBar.OnTabChangedListener {

    public static final String TAG = MediaMainFragment.class.getSimpleName();

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

    private List<Page> pageList;
    private AlbumList albumList;
    private NewPhotoList photoList;
    private MediaShareList shareList;
    private NavPageBar mNavPageBar;

    private LocalBroadcastManager mManager;

    private Animator mAnimator;

    private boolean sMenuUnfolding = false;

    private Context mContext;

    private ProgressDialog mDialog;

    private static final int PAGE_SHARE = 0;
    private static final int PAGE_PHOTO = 1;
    private static final int PAGE_ALBUM = 2;

    private boolean sInChooseMode = false;

    private boolean mLocalMediaLoaded = false;
    private boolean mRemoteMediaLoaded = false;

    private boolean mRemoteMediaShareLoaded = false;
    private boolean mLocalMediaShareLoaded = false;

    private boolean onCreate = false;

    private OnMainFragmentInteractionListener mListener;


    public MediaMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MediaMainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MediaMainFragment newInstance() {
        MediaMainFragment fragment = new MediaMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        mManager = LocalBroadcastManager.getInstance(mContext);


        initPageList();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.nav_pager_main, container, false);

        ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.switchDrawerOpenState();
            }
        });

        initViewPager();

        mNavPageBar = new NavPageBar(tabLayout, viewPager);
        viewPager.setCurrentItem(PAGE_PHOTO);

        ivBtAlbum.setOnClickListener(this);
        ivBtShare.setOnClickListener(this);
        fab.setOnClickListener(this);
        lbRight.setOnClickListener(this);

        photoList.addPhotoListListener(this);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isHidden()) return;

        mNavPageBar.registerOnTabChangedListener(this);

        EventBus.getDefault().register(this);

        if (viewPager.getCurrentItem() == PAGE_PHOTO && mLocalMediaLoaded & mRemoteMediaLoaded) {
            retrieveLocalMediaInCamera();
        }

        if (!onCreate) {
            FNAS.retrieveLocalMediaMap(mContext);
//            FNAS.retrieveLocalMediaCommentMap(mContext);

            onCreate = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isHidden()) return;

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mNavPageBar.unregisterOnTabChangedListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        photoList.removePhotoListListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMainFragmentInteractionListener) {
            mListener = (OnMainFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onActivityReenter(int resultCode, Intent data) {
        if (viewPager.getCurrentItem() == PAGE_PHOTO) {
            ((NewPhotoList) pageList.get(viewPager.getCurrentItem())).onActivityReenter(resultCode, data);
        } else if (viewPager.getCurrentItem() == PAGE_SHARE) {
            ((MediaShareList) pageList.get(viewPager.getCurrentItem())).onActivityReenter(resultCode, data);
        }
    }

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

    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        if (viewPager.getCurrentItem() == PAGE_PHOTO) {
            ((NewPhotoList) pageList.get(viewPager.getCurrentItem())).onMapSharedElements(names, sharedElements);
        } else if (viewPager.getCurrentItem() == PAGE_SHARE) {
            ((MediaShareList) pageList.get(viewPager.getCurrentItem())).onMapSharedElements(names, sharedElements);
        }
    }

    private void initPageList() {
        shareList = new MediaShareList(getActivity(), this);
        photoList = new NewPhotoList(getActivity(), this);
        albumList = new AlbumList(getActivity(), this);
        pageList = new ArrayList<Page>();
        pageList.add(shareList);
        pageList.add(photoList);
        pageList.add(albumList);
    }

    private void initViewPager() {
        MyAdapter myAdapter = new MyAdapter();
        viewPager.setAdapter(myAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                onDidAppear(position);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        if (action.equals(Util.LOCAL_SHARE_CREATED)) {

            handleLocalShareCreated(operationEvent);

        } else if (action.equals(Util.REMOTE_SHARE_CREATED)) {

            handleRemoteShareCreated(operationEvent);

        } else if (action.equals(Util.LOCAL_SHARE_DELETED) || action.equals(Util.REMOTE_SHARE_MODIFIED) || action.equals(Util.REMOTE_SHARE_DELETED) || action.equals(Util.LOCAL_SHARE_MODIFIED)) {

            handleLocalShareModifiedDeletedOrRemoteShareModifiedDeleted(operationEvent);

        } else if (action.equals(Util.REMOTE_COMMENT_CREATED)) {

            handleRemoteCommentCreated(operationEvent);

        } else if (action.equals(Util.LOCAL_COMMENT_DELETED)) {

            handleLocalCommentDeleted(operationEvent);

        } else if (action.equals(Util.LOCAL_PHOTO_UPLOAD_STATE_CHANGED)) {

            handleLocalPhotoUploadStateChanged();

        } else if (action.equals(Util.REMOTE_MEDIA_RETRIEVED)) {
            Log.i(TAG, "remote media loaded");

            mRemoteMediaLoaded = true;

            pageList.get(PAGE_PHOTO).refreshView();

            retrieveLocalMediaInCamera();

            FNAS.retrieveRemoteMediaShare(mContext);

        } else if (action.equals(Util.LOCAL_MEDIA_RETRIEVED)) {

            Log.i(TAG, "local media loaded");

            mLocalMediaLoaded = true;

            FNAS.retrieveRemoteMediaMap(mContext);

        } else if (action.equals(Util.NEW_LOCAL_MEDIA_IN_CAMERA_RETRIEVED)) {

            handleNewLocalMediaInCameraRetrieved(operationEvent);

        } else if (action.equals(Util.REMOTE_MEDIA_SHARE_RETRIEVED)) {
            Log.i(TAG, "remote share loaded");

            mRemoteMediaShareLoaded = true;

            pageList.get(PAGE_ALBUM).refreshView();
            pageList.get(PAGE_SHARE).refreshView();

            if (!mLocalMediaShareLoaded)
                FNAS.retrieveLocalMediaShare(mContext);

        } else if (action.equals(Util.LOCAL_MEDIA_SHARE_RETRIEVED)) {
            Log.i(TAG, "local share loaded");

            mLocalMediaShareLoaded = true;

            doCreateMediaShareInLocalMediaShareMapFunction();
        } else if (action.equals(Util.LOCAL_MEDIA_COMMENT_RETRIEVED)) {
            Log.i(TAG, "local media comment loaded");
            ((MediaShareList) pageList.get(PAGE_SHARE)).refreshLocalComment();

            doCreateRemoteMediaCommentInLocalMediaCommentMapFunction();
        } else if (action.equals(Util.REMOTE_MEDIA_COMMENT_RETRIEVED)) {

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

    private void handleNewLocalMediaInCameraRetrieved(OperationEvent operationEvent) {
        OperationResult result = operationEvent.getOperationResult();

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

    private void handleLocalCommentDeleted(OperationEvent operationEvent) {
        Log.i(TAG, "local comment changed");

        OperationResult result = operationEvent.getOperationResult();

        if (result == OperationResult.SUCCEED) {
            pageList.get(PAGE_SHARE).refreshView();
        }
    }

    private void handleRemoteCommentCreated(OperationEvent operationEvent) {

        OperationResult result = operationEvent.getOperationResult();

        if (result == OperationResult.SUCCEED) {
            Log.i(TAG, "remote comment created");

            Comment comment = ((MediaShareCommentOperationEvent) operationEvent).getComment();
            String imageUUID = ((MediaShareCommentOperationEvent) operationEvent).getImageUUID();
            Intent operationIntent = new Intent(Util.OPERATION);
            operationIntent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.DELETE.name());
            operationIntent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIA_COMMENT.name());
            operationIntent.putExtra(Util.OPERATION_COMMENT, comment);
            operationIntent.putExtra(Util.OPERATION_IMAGE_UUID, imageUUID);
            mManager.sendBroadcast(operationIntent);
        }
    }

    private void handleLocalShareModifiedDeletedOrRemoteShareModifiedDeleted(OperationEvent operationEvent) {
        dismissDialog();

        OperationResult operationResult = operationEvent.getOperationResult();

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

    private void handleRemoteShareCreated(OperationEvent operationEvent) {
        Log.i(TAG, "remote share created");

        OperationResult operationResult = operationEvent.getOperationResult();

        switch (operationResult) {
            case SUCCEED:

                MediaShare mediaShare = ((MediaShareOperationEvent) operationEvent).getMediaShare();
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

    private void handleLocalShareCreated(OperationEvent operationEvent) {

        OperationResult operationResult = operationEvent.getOperationResult();

        switch (operationResult) {
            case SUCCEED:

                if (Util.getNetworkState(mContext)) {
                    MediaShare mediaShare = ((MediaShareOperationEvent) operationEvent).getMediaShare();
                    Intent operationIntent = new Intent(Util.OPERATION);
                    operationIntent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.CREATE.name());
                    operationIntent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIASHARE.name());
                    operationIntent.putExtra(Util.OPERATION_MEDIASHARE, mediaShare);
                    mManager.sendBroadcast(operationIntent);
                }

                onActivityResult(Util.KEY_CREATE_SHARE_REQUEST_CODE, RESULT_OK, null);
                break;
            case FAIL:

                dismissDialog();

                Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();

                break;
        }
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
        sp = getActivity().getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(Util.SHOW_ALBUM_TIPS, true);
    }

    private void setShowAlbumTipsValue(boolean value) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = getActivity().getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putBoolean(Util.SHOW_ALBUM_TIPS, value);
        editor.apply();
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
                if (sInChooseMode) {
                    hideChooseHeader();
                } else {
                    mListener.onBackPress();
                }

            }
        });
        sInChooseMode = true;
        photoList.setSelectMode(sInChooseMode);
        setSelectCountText(getString(R.string.choose_photo));

        mListener.lockDrawer();
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
                mListener.switchDrawerOpenState();
            }
        });
        lbRight.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.VISIBLE);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) viewPager.getLayoutParams();
        lp.bottomMargin = Util.dip2px(getActivity(), 48.0f);
        //if(LocalCache.ScreenWidth==540) lp.bottomMargin=76;
        //else if(LocalCache.ScreenWidth==1080) lp.bottomMargin=140;
        viewPager.setLayoutParams(lp);
        sInChooseMode = false;
        photoList.setSelectMode(sInChooseMode);
        setSelectCountText(getString(R.string.photo_text));

        mListener.unlockDrawer();
    }

    @Override
    public void onTabChanged(int tabNum) {

        if (tabNum == NavPageBar.TAB_ALBUM) {
            showTips();
        }
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

    private void doCreateShareFunction(List<String> selectUUIDs) {

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.CREATE.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIASHARE.name());

        intent.putExtra(Util.OPERATION_MEDIASHARE, createMediaShare(selectUUIDs));
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

        mAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.fab_restore);
        mAnimator.setTarget(fab);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.album_btn_restore);
        mAnimator.setTarget(ivBtAlbum);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.share_btn_restore);
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

        mAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.fab_remote);
        mAnimator.setTarget(fab);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.album_btn_translation);
        mAnimator.setTarget(ivBtAlbum);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.share_btn_translation);
        mAnimator.setInterpolator(new BounceInterpolator());
        mAnimator.setTarget(ivBtShare);
        mAnimator.start();

    }

    @Override
    public boolean isRemoteMediaShareLoaded() {
        return mRemoteMediaShareLoaded;
    }

    @Override
    public boolean isRemoteMediaLoaded() {
        return mRemoteMediaLoaded;
    }

    @Override
    public void modifyMediaShare(MediaShare mediaShare) {

        if (mediaShare.checkPermissionToOperate()) {
            mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);

            String requestData = mediaShare.createToggleShareStateRequestData();

            mediaShare.sendModifyMediaShareRequest(mContext, requestData);
        } else {
            Toast.makeText(mContext, getString(R.string.no_edit_photo_permission), Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void deleteMediaShare(MediaShare mediaShare) {
        if (mediaShare.checkPermissionToOperate()) {
            mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);

            mediaShare.sendDeleteMediaShareRequest(mContext);
        } else {
            Toast.makeText(mContext, getString(R.string.no_edit_photo_permission), Toast.LENGTH_SHORT).show();
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


}
