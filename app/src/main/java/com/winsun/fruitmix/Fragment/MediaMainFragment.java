package com.winsun.fruitmix.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.anim.BaseAnimationListener;
import com.winsun.fruitmix.component.NavPageBar;
import com.winsun.fruitmix.eventbus.MediaShareCommentOperationEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.fileModule.interfaces.OnFileInteractionListener;
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
import com.winsun.fruitmix.mediaModule.model.NewPhotoListDataLoader;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.model.OperationResultType;
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
 * {@link OnFileInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MediaMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MediaMainFragment extends Fragment implements OnMediaFragmentInteractionListener, View.OnClickListener, IPhotoListListener, NavPageBar.OnTabChangedListener {

    public static final String TAG = MediaMainFragment.class.getSimpleName();

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
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
    @BindView(R.id.bottom_navigation_view)
    BottomNavigationView bottomNavigationView;

    private List<Page> pageList;
    private AlbumList albumList;
    private NewPhotoList photoList;
    private MediaShareList shareList;

    private Animator mAnimator;

    private boolean sMenuUnfolding = false;

    private Context mContext;

    private ProgressDialog mDialog;

    private static final int PAGE_SHARE = 0;
    private static final int PAGE_PHOTO = 1;
    private static final int PAGE_ALBUM = 2;

    private boolean sInChooseMode = false;

    private boolean mRemoteMediaLoaded = false;

    private boolean mRemoteMediaShareLoaded = false;

    private boolean onResume = false;

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

        initPageList();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.nav_pager_main, container, false);

        ButterKnife.bind(this, view);

        toolbar.setTitle("");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.switchDrawerOpenState();
            }
        });

        initNavigationView();

        initViewPager();

        viewPager.setCurrentItem(PAGE_PHOTO);

        ivBtAlbum.setOnClickListener(this);
        ivBtShare.setOnClickListener(this);
        fab.setOnClickListener(this);
        lbRight.setOnClickListener(this);

        photoList.addPhotoListListener(this);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isHidden()) return;

        if (!onResume) {
            onResume = true;
        } else {
            FNAS.retrieveLocalMediaInCamera();
        }

    }

    @Override
    public void onStop() {

        EventBus.getDefault().unregister(this);

        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        photoList.removePhotoListListener(this);

        mContext = null;
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

            viewPager.setCurrentItem(PAGE_ALBUM);
            onDidAppear(PAGE_ALBUM);
            pageList.get(PAGE_ALBUM).onDidAppear();
            pageList.get(PAGE_SHARE).onDidAppear();
        } else if (requestCode == Util.KEY_CREATE_SHARE_REQUEST_CODE && resultCode == RESULT_OK) {

            viewPager.setCurrentItem(PAGE_SHARE);
            onDidAppear(PAGE_SHARE);
            pageList.get(PAGE_SHARE).onDidAppear();
        }
    }

    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        if (viewPager.getCurrentItem() == PAGE_PHOTO) {
            ((NewPhotoList) pageList.get(viewPager.getCurrentItem())).onMapSharedElements(names, sharedElements);
        } else if (viewPager.getCurrentItem() == PAGE_SHARE) {
            ((MediaShareList) pageList.get(viewPager.getCurrentItem())).onMapSharedElements(names, sharedElements);
        }
    }

    private void initNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.share:
                        viewPager.setCurrentItem(PAGE_SHARE);
                        break;
                    case R.id.photo:
                        viewPager.setCurrentItem(PAGE_PHOTO);
                        break;
                    case R.id.album:
                        viewPager.setCurrentItem(PAGE_ALBUM);
                        break;
                }

                return true;
            }
        });

        resetBottomNavigationItemCheckState();

    }

    private void resetBottomNavigationItemCheckState() {
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    private void initPageList() {
        shareList = new MediaShareList(getActivity(), this);
        photoList = new NewPhotoList(getActivity());
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

                resetBottomNavigationItemCheckState();
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }
        });
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void handleStickyOperationEvent(OperationEvent operationEvent) {

        Log.i(TAG, "handleStickyOperationEvent: action:" + operationEvent.getAction());

        String action = operationEvent.getAction();

        if (action.equals(Util.REMOTE_MEDIA_RETRIEVED)) {
            Log.i(TAG, "remote media loaded");

            mRemoteMediaLoaded = true;

            NewPhotoListDataLoader.INSTANCE.setNeedRefreshData(true);
            Util.needRefreshPhotoSliderList = true;
            photoList.refreshView();

            FNAS.retrieveRemoteMediaShare(mContext, true);

        } else if (action.equals(Util.REMOTE_MEDIA_SHARE_RETRIEVED)) {
            Log.i(TAG, "remote share loaded");

            mRemoteMediaShareLoaded = true;

            albumList.refreshView();
            shareList.refreshView();

            ButlerService.startTimingRetrieveMediaShare();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        OperationResultType result;

        Log.i(TAG, "handleOperationEvent: action:" + action);

        switch (action) {
            case Util.REMOTE_SHARE_CREATED:
                handleRemoteShareCreated(operationEvent);
                break;
            case Util.REMOTE_SHARE_MODIFIED:
            case Util.REMOTE_SHARE_DELETED:
                handleRemoteShareModifiedDeleted(operationEvent);
                break;
            case Util.REMOTE_COMMENT_CREATED:
                handleRemoteCommentCreated(operationEvent);
                break;
            case Util.LOCAL_COMMENT_DELETED:
                handleLocalCommentDeleted(operationEvent);
                break;
            case Util.LOCAL_MEDIA_COMMENT_RETRIEVED:
                Log.i(TAG, "local media comment loaded");
                ((MediaShareList) pageList.get(PAGE_SHARE)).refreshLocalComment();

                doCreateRemoteMediaCommentInLocalMediaCommentMapFunction();
                break;
            case Util.REMOTE_MEDIA_COMMENT_RETRIEVED:

                Log.i(TAG, "remote media comment loaded ");

                ((MediaShareList) pageList.get(PAGE_SHARE)).refreshRemoteComment();

                break;

            case Util.NEW_LOCAL_MEDIA_IN_CAMERA_RETRIEVED:

                result = operationEvent.getOperationResult().getOperationResultType();

                if (result == OperationResultType.SUCCEED) {

                    Log.i(TAG, "handleOperationEvent: new local media in camera retrieved succeed");

                    NewPhotoListDataLoader.INSTANCE.setNeedRefreshData(true);
                    Util.needRefreshPhotoSliderList = true;
                    photoList.refreshView();
                }

                break;
            case Util.LOCAL_MEDIA_RETRIEVED:

                result = operationEvent.getOperationResult().getOperationResultType();

                if (result == OperationResultType.SUCCEED) {

                    Log.i(TAG, "handleOperationEvent: local media in db retrieved succeed");

                    NewPhotoListDataLoader.INSTANCE.setNeedRefreshData(true);
                    Util.needRefreshPhotoSliderList = true;
                    photoList.refreshView();
                }

                break;

        }

    }

    private void doCreateRemoteMediaCommentInLocalMediaCommentMapFunction() {

        for (Map.Entry<String, List<Comment>> entry : LocalCache.LocalMediaCommentMapKeyIsImageUUID.entrySet()) {

            for (Comment comment : entry.getValue()) {
                FNAS.createRemoteMediaComment(mContext, entry.getKey(), comment);
            }

        }
    }

    private void handleLocalCommentDeleted(OperationEvent operationEvent) {
        Log.i(TAG, "local comment changed");

        OperationResultType result = operationEvent.getOperationResult().getOperationResultType();

        if (result == OperationResultType.SUCCEED) {
            shareList.refreshView();
        }
    }

    private void handleRemoteCommentCreated(OperationEvent operationEvent) {

        OperationResultType result = operationEvent.getOperationResult().getOperationResultType();

        if (result == OperationResultType.SUCCEED) {
            Log.i(TAG, "remote comment created");

            Comment comment = ((MediaShareCommentOperationEvent) operationEvent).getComment();
            String imageUUID = ((MediaShareCommentOperationEvent) operationEvent).getImageUUID();
            FNAS.deleteLocalMediaComment(mContext, imageUUID, comment);
        }
    }

    private void handleRemoteShareModifiedDeleted(OperationEvent operationEvent) {
        dismissDialog();

        OperationResult operationResult = operationEvent.getOperationResult();

        OperationResultType operationResultType = operationResult.getOperationResultType();

        switch (operationResultType) {
            case SUCCEED:
                Toast.makeText(mContext, operationResult.getResultMessage(mContext), Toast.LENGTH_SHORT).show();
                albumList.refreshView();
                shareList.refreshView();
                break;
            default:
                Toast.makeText(mContext, operationResult.getResultMessage(mContext), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRemoteShareCreated(OperationEvent operationEvent) {
        Log.i(TAG, "remote share created");

        OperationResult operationResult = operationEvent.getOperationResult();

        dismissDialog();

        if (operationResult.getOperationResultType() == OperationResultType.SUCCEED) {
            viewPager.setCurrentItem(PAGE_SHARE);
            onDidAppear(PAGE_SHARE);
            pageList.get(PAGE_SHARE).onDidAppear();
        }

        Toast.makeText(mContext, operationResult.getResultMessage(mContext), Toast.LENGTH_SHORT).show();

    }

    public void retrieveRemoteMediaComment(String mediaUUID) {

        FNAS.retrieveRemoteMediaCommentMap(mContext, mediaUUID);
    }

    public void setSelectCountText(String text) {
        title.setText(text);
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

    private void showChooseHeader() {
        fab.setVisibility(View.VISIBLE);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sInChooseMode) {
                    hideChooseHeader();
                    showBottomNavAnim();
                } else {
                    mListener.onBackPress();
                }

            }
        });
        lbRight.setVisibility(View.GONE);

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) viewPager.getLayoutParams();
        lp.bottomMargin = 0;
        viewPager.setLayoutParams(lp);
        sInChooseMode = true;
        photoList.setSelectMode(sInChooseMode);
        setSelectCountText(getString(R.string.choose_photo));

        mListener.lockDrawer();
    }

    private void hideChooseHeader() {
        if (sMenuUnfolding) {
            sMenuUnfolding = false;
            collapseFab();
        }
        fab.setVisibility(View.GONE);
        toolbar.setNavigationIcon(R.drawable.menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.switchDrawerOpenState();
            }
        });
        lbRight.setVisibility(View.VISIBLE);

        sInChooseMode = false;
        photoList.setSelectMode(sInChooseMode);
        setSelectCountText(getString(R.string.photo));

        mListener.unlockDrawer();
    }

    private void showBottomNavAnim() {

        bottomNavigationView.setVisibility(View.VISIBLE);

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.show_bottom_item_anim);
        animation.setAnimationListener(new BaseAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);

                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) viewPager.getLayoutParams();
                lp.bottomMargin = Util.dip2px(getActivity(), 56.0f);
                //if(LocalCache.ScreenWidth==540) lp.bottomMargin=76;
                //else if(LocalCache.ScreenWidth==1080) lp.bottomMargin=140;
                viewPager.setLayoutParams(lp);
            }
        });


        bottomNavigationView.startAnimation(animation);
    }

    private void showBottomNav() {
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    private void dismissBottomNavAnim() {

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.dismiss_bottom_item_anim);
        animation.setAnimationListener(new BaseAnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);

                bottomNavigationView.setVisibility(View.GONE);
            }
        });

        bottomNavigationView.startAnimation(animation);
    }


    private void dismissBottomNav() {
        bottomNavigationView.setVisibility(View.GONE);
    }


    @Override
    public void onTabChanged(int tabNum) {

        if (tabNum == NavPageBar.TAB_ALBUM) {
            showTips();
        }
    }

    public boolean handleBackPressedOrNot() {
        return sInChooseMode;
    }

    public void handleBackPressed() {

        hideChooseHeader();
        showBottomNavAnim();

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
    public void onPhotoItemLongClick() {
        showChooseHeader();

        dismissBottomNavAnim();
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
        List<String> selectMediaKeys;
        switch (v.getId()) {
            case R.id.bt_album:
                selectMediaKeys = photoList.getSelectedImageKeys();
                if (showNothingSelectToast(selectMediaKeys)) return;

                photoList.createAlbum(selectMediaKeys);
                hideChooseHeader();
                showBottomNavAnim();
                break;
            case R.id.bt_share:
                selectMediaKeys = photoList.getSelectedImageKeys();
                if (showNothingSelectToast(selectMediaKeys)) return;

                mDialog = ProgressDialog.show(mContext, null, getString(R.string.operating_title), true, false);

                doCreateShareFunction(selectMediaKeys);
                hideChooseHeader();
                showBottomNavAnim();
                break;
            case R.id.fab:
                refreshFabState();
                break;
            case R.id.right:
                showChooseHeader();
                dismissBottomNavAnim();
                break;
            default:
        }
    }

    private boolean showNothingSelectToast(List<String> selectUUIDs) {
        if (selectUUIDs.size() == 0) {
            Toast.makeText(mContext, getString(R.string.select_nothing), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void doCreateShareFunction(List<String> selectMediaKeys) {

        FNAS.createRemoteMediaShare(mContext, createMediaShare(selectMediaKeys));
    }

    private MediaShare createMediaShare(List<String> selectMediaKeys) {

        MediaShare mediaShare = new MediaShare();
        mediaShare.setUuid(Util.createLocalUUid());

        List<MediaShareContent> mediaShareContents = new ArrayList<>();
        for (String digest : selectMediaKeys) {
            MediaShareContent mediaShareContent = new MediaShareContent();
            mediaShareContent.setKey(digest);
            mediaShareContent.setAuthor(FNAS.userUUID);
            mediaShareContent.setTime(String.valueOf(System.currentTimeMillis()));
            mediaShareContents.add(mediaShareContent);
        }

        mediaShare.initMediaShareContents(mediaShareContents);

        mediaShare.setCoverImageKey(selectMediaKeys.get(0));
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
        mAnimator.setTarget(ivBtShare);
        mAnimator.start();

        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                ivBtAlbum.setVisibility(View.GONE);
                ivBtShare.setVisibility(View.GONE);

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
            mDialog = ProgressDialog.show(mContext, null, getString(R.string.operating_title), true, false);

            String requestData = mediaShare.createToggleShareStateRequestData();

            mediaShare.sendModifyMediaShareRequest(mContext, requestData);
        } else {
            Toast.makeText(mContext, getString(R.string.no_operate_media_share_permission), Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void deleteMediaShare(MediaShare mediaShare) {
        if (mediaShare.checkPermissionToOperate()) {
            mDialog = ProgressDialog.show(mContext, null, getString(R.string.operating_title), true, false);

            mediaShare.sendDeleteMediaShareRequest(mContext);
        } else {
            Toast.makeText(mContext, getString(R.string.no_operate_media_share_permission), Toast.LENGTH_SHORT).show();
        }

    }

    private void onDidAppear(int position) {
        hideChooseHeader();
        showBottomNav();

        switch (position) {
            case PAGE_SHARE:
                title.setText(getString(R.string.share_text));
                fab.setVisibility(View.GONE);
                ivBtAlbum.setVisibility(View.GONE);
                ivBtShare.setVisibility(View.GONE);
                lbRight.setVisibility(View.GONE);
                break;
            case PAGE_PHOTO:
                title.setText(getString(R.string.photo));
                lbRight.setVisibility(View.VISIBLE);
                fab.setVisibility(View.GONE);
                break;
            case PAGE_ALBUM:
                title.setText(getString(R.string.album));
                fab.setVisibility(View.GONE);
                ivBtAlbum.setVisibility(View.GONE);
                ivBtShare.setVisibility(View.GONE);
                lbRight.setVisibility(View.GONE);
                break;
            default:
        }


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
