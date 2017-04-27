package com.winsun.fruitmix.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.StateListAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.anim.BaseAnimationListener;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.dialog.ShareMenuBottomDialogFactory;
import com.winsun.fruitmix.eventbus.MediaShareCommentOperationEvent;
import com.winsun.fruitmix.eventbus.MediaShareOperationEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveMediaOriginalPhotoRequestEvent;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.interfaces.OnMainFragmentInteractionListener;
import com.winsun.fruitmix.mediaModule.fragment.AlbumList;
import com.winsun.fruitmix.mediaModule.fragment.MediaShareList;
import com.winsun.fruitmix.mediaModule.fragment.NewPhotoList;
import com.winsun.fruitmix.mediaModule.interfaces.OnMediaFragmentInteractionListener;
import com.winsun.fruitmix.mediaModule.interfaces.Page;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.model.operationResult.OperationNoChanged;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.CustomTransitionListener;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class MediaMainFragment extends Fragment implements OnMediaFragmentInteractionListener, View.OnClickListener, IPhotoListListener {

    public static final String TAG = "MediaMainFragment";

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

    private boolean onResume = false;

    private OnMainFragmentInteractionListener mListener;

    private boolean mPhotoListRefresh = false;
    private boolean mShareAlbumListRefresh = false;

    private IShowHideFragmentListener mCurrentFragment;

    private boolean mIsResume = false;

    private List<Media> mSelectMedias;

    private List<String> mSelectMediaOriginalPhotoPaths;

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

        Log.d(TAG, "onCreate: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.nav_pager_main, container, false);

        ButterKnife.bind(this, view);

        toolbar.setNavigationIcon(R.drawable.menu_black);

        toolbar.setTitle("");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.switchDrawerOpenState();
            }
        });

        lbRight.setVisibility(View.VISIBLE);

        initNavigationView();

        initViewPager();

        viewPager.setCurrentItem(PAGE_PHOTO);

        ivBtAlbum.setOnClickListener(this);
        ivBtShare.setOnClickListener(this);
        fab.setOnClickListener(this);
        lbRight.setOnClickListener(this);

        photoList.addPhotoListListener(this);
        shareList.addPhotoListListener(this);
        albumList.addPhotoListListener(this);

        Log.d(TAG, "onCreateView: ");

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        Log.d(TAG, "onStart: ");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: isHidden: " + isHidden());

        if (isHidden()) return;

        if (!onResume) {
            onResume = true;
        } else {
            FNAS.retrieveLocalMediaInCamera();
        }

        if (Util.isRemoteMediaLoaded() && Util.isLocalMediaInCameraLoaded() && Util.isLocalMediaInDBLoaded() && !mPhotoListRefresh) {
            photoList.refreshView();

            mPhotoListRefresh = true;
        }

        if (!mShareAlbumListRefresh && Util.isRemoteMediaShareLoaded()) {
            shareList.refreshView();
            albumList.refreshView();

            mShareAlbumListRefresh = true;
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause: ");

    }

    @Override
    public void onStop() {

        EventBus.getDefault().unregister(this);

        super.onStop();

        Log.d(TAG, "onStop: ");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mDialog = null;

        photoList.cancelPreLoadMediaMiniThumb();

        photoList.removePhotoListListener(this);
        shareList.removePhotoListListener(this);
        albumList.removePhotoListListener(this);

        mContext = null;

        Log.d(TAG, "onDestroy: ");
    }

    private void setCurrentItem(IShowHideFragmentListener currentItem) {
        if (mCurrentFragment != null)
            mCurrentFragment.hide();

        mCurrentFragment = currentItem;

        if (mIsResume)
            mCurrentFragment.show();

    }

    public void show() {

        Log.d(TAG, "show: mIsResume: " + mIsResume);

        if (!mIsResume) {
            mIsResume = true;

            Log.d(TAG, "show: onPageStart");

            MobclickAgent.onPageStart(TAG);
            MobclickAgent.onResume(mContext);

//            if (mCurrentFragment == null)
//                mCurrentFragment = photoList;
//
//            mCurrentFragment.show();
        }

    }

    public void hide() {

        Log.d(TAG, "hide: mIsResume: " + mIsResume);

        if (mIsResume) {
            mIsResume = false;

            Log.d(TAG, "hide: onPageEnd");

            MobclickAgent.onPageEnd(TAG);
            MobclickAgent.onPause(mContext);

//            mCurrentFragment.hide();
        }

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

        dismissToolbarBottomBarWhenSharedElementTransition();

        if (viewPager.getCurrentItem() == PAGE_PHOTO) {
            ((NewPhotoList) pageList.get(viewPager.getCurrentItem())).onActivityReenter(resultCode, data);
        } else if (viewPager.getCurrentItem() == PAGE_SHARE) {
            ((MediaShareList) pageList.get(viewPager.getCurrentItem())).onActivityReenter(resultCode, data);
        }
    }

    private void dismissToolbarBottomBarWhenSharedElementTransition() {
        if (Util.checkRunningOnLollipopOrHigher()) {
            getActivity().getWindow().getSharedElementEnterTransition().addListener(new CustomTransitionListener() {

                @Override
                public void onTransitionStart(Transition transition) {
                    super.onTransitionStart(transition);

                    toolbar.setVisibility(View.INVISIBLE);
                    bottomNavigationView.setVisibility(View.INVISIBLE);

                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);

                    toolbar.setVisibility(View.VISIBLE);
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            });

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

                String eventId = "";

                switch (item.getItemId()) {
                    case R.id.share:

                        eventId = Util.SWITCH_MEDIA_SHARE_MODULE_UMENG_EVENT_ID;

                        viewPager.setCurrentItem(PAGE_SHARE);
                        break;
                    case R.id.photo:

                        eventId = Util.SWITCH_MEDIA_MODULE_UMENG_EVENT_ID;

                        viewPager.setCurrentItem(PAGE_PHOTO);
                        break;
                    case R.id.album:

                        eventId = Util.SWITCH_ALBUM_MODULE_UMENG_EVENT_ID;

                        viewPager.setCurrentItem(PAGE_ALBUM);
                        break;
                }

                MobclickAgent.onEvent(getActivity(), eventId);

                return true;
            }
        });

        resetBottomNavigationItemCheckState();

    }

    private void resetBottomNavigationItemCheckState() {

        int size = bottomNavigationView.getMenu().size();

        for (int i = 0; i < size; i++) {
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

    public void refreshUser() {

        shareList.refreshView();
        albumList.refreshView();

    }

    public void refreshAllViews() {
        shareList.refreshView();
        albumList.refreshView();
        photoList.refreshView();
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

                if (position == PAGE_ALBUM && mPhotoListRefresh) {
                    albumList.showPhoto();
                } else if (position == PAGE_SHARE && mPhotoListRefresh) {
                    shareList.showPhoto();
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        Log.i(TAG, "handleOperationEvent: action:" + action);

        switch (action) {
            case Util.REMOTE_MEDIA_SHARE_CREATED:
                if (handleRemoteShareOperated(operationEvent)) {
                    handleRemoteShareCreated(operationEvent);
                }
                break;
            case Util.REMOTE_MEDIA_SHARE_MODIFIED:
                if (handleRemoteShareOperated(operationEvent)) {
                    handleRemoteShareModified(operationEvent);
                }
                break;
            case Util.REMOTE_MEDIA_SHARE_DELETED:
                if (handleRemoteShareOperated(operationEvent)) {
                    handleRemoteShareDeleted(operationEvent);
                }
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

                Log.i(TAG, "handleOperationEvent: new local media in camera retrieved succeed");

                setPhotoListRefresh();

                photoList.refreshView();

                break;
            case Util.CALC_NEW_LOCAL_MEDIA_DIGEST_FINISHED:

                Log.i(TAG, "handleOperationEvent: calc new local media digest finished");

                if (operationEvent.getOperationResult().getOperationResultType() != OperationResultType.NO_CHANGED) {
                    setPhotoListRefresh();
                }

                if (!photoList.isLoaded()) {
                    photoList.refreshView();
                }

                break;
            case Util.LOCAL_MEDIA_RETRIEVED:

                Log.i(TAG, "handleOperationEvent: local media in db retrieved succeed");

                setPhotoListRefresh();

                photoList.refreshView();

                break;

            case Util.REMOTE_MEDIA_RETRIEVED:

                Log.i(TAG, "remote media loaded");

                setPhotoListRefresh();

                photoList.refreshView();

                break;

            case Util.REMOTE_MEDIA_SHARE_RETRIEVED:

                Log.i(TAG, "remote share loaded");

                setShareAlbumListRefresh();

                albumList.refreshView();
                shareList.refreshView();

                break;

            case Util.SHARED_PHOTO_THUMB_RETRIEVED:

                if (mDialog == null || !mDialog.isShowing()) {
                    return;
                }

                dismissDialog();

                String path;
                for (Media media : mSelectMedias) {
                    path = media.getOriginalPhotoPath();

                    if (!path.isEmpty())
                        mSelectMediaOriginalPhotoPaths.add(path);
                }

                if (mSelectMediaOriginalPhotoPaths.isEmpty()) {
                    Toast.makeText(mContext, getString(R.string.download_original_photo_fail), Toast.LENGTH_SHORT).show();
                } else {
                    Util.sendShare(getContext(), mSelectMediaOriginalPhotoPaths);
                }

                break;

        }

    }

    public void setPhotoListRefresh() {
        if (Util.isLocalMediaInCameraLoaded() && Util.isLocalMediaInDBLoaded() && Util.isRemoteMediaLoaded())
            mPhotoListRefresh = true;
    }

    public void setShareAlbumListRefresh() {
        if (Util.isRemoteMediaShareLoaded())
            mShareAlbumListRefresh = true;
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

    private void handleRemoteShareModified(OperationEvent operationEvent) {

        MediaShare mediaShare = ((MediaShareOperationEvent) operationEvent).getMediaShare();
        if (mediaShare.getViewersListSize() == 0) {
            MobclickAgent.onEvent(mContext, Util.ALBUM_SWITCH_UN_SHARE_STATE_UMENG_EVENT_ID);
        } else {
            MobclickAgent.onEvent(mContext, Util.ALBUM_SWITCH_SHARE_STATE_UMENG_EVENT_ID);
        }
        albumList.refreshView();
        shareList.refreshView();
    }

    private void handleRemoteShareDeleted(OperationEvent operationEvent) {

        Log.d(TAG, "handleRemoteShareDeleted: ");

        MobclickAgent.onEvent(mContext, Util.DELETE_ALBUM_UMENG_EVENT_ID);

        albumList.refreshView();
        shareList.refreshView();

    }

    private void handleRemoteShareCreated(OperationEvent operationEvent) {
        MobclickAgent.onEvent(mContext, Util.CREATE_MEDIA_SHARE_UMENG_EVENT_ID);

        viewPager.setCurrentItem(PAGE_SHARE);
        onDidAppear(PAGE_SHARE);
        pageList.get(PAGE_SHARE).onDidAppear();
    }

    private boolean handleRemoteShareOperated(OperationEvent operationEvent) {
        OperationResult operationResult = operationEvent.getOperationResult();

        dismissDialog();

        Toast.makeText(mContext, operationResult.getResultMessage(mContext), Toast.LENGTH_SHORT).show();

        return operationResult.getOperationResultType() == OperationResultType.SUCCEED;

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
        toolbar.setNavigationIcon(R.drawable.ic_back_black);
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
        toolbar.setNavigationIcon(R.drawable.menu_black);
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

        if (selectedItemCount == 0) {
            handleBackPressed();
        } else {
            setSelectCountText(String.format(getString(R.string.select_count), selectedItemCount));
        }

    }

    private void dismissDialog() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    public void onPhotoItemLongClick() {
        showChooseHeader();

        dismissBottomNavAnim();

        setSelectCountText(String.format(getString(R.string.select_count), 1));
    }

    @Override
    public void onNoPhotoItem(boolean noPhotoItem) {

        Log.d(TAG, "onNoPhotoItem:" + noPhotoItem);

        int currentItem = viewPager.getCurrentItem();

        if (noPhotoItem && currentItem == PAGE_PHOTO) {
            lbRight.setVisibility(View.GONE);
        } else if (!noPhotoItem && currentItem == PAGE_PHOTO) {
            lbRight.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPhotoListScrollDown() {

        if (toolbar.getVisibility() == View.GONE)
            return;

        ViewCompat.setElevation(toolbar, Util.dip2px(mContext, 6f));

        toolbar.setVisibility(View.GONE);

    }

    @Override
    public void onPhotoListScrollUp() {

        if (toolbar.getVisibility() == View.VISIBLE)
            return;

        ViewCompat.setElevation(toolbar, Util.dip2px(mContext, 6f));

        toolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPhotoListScrollFinished() {

        ViewCompat.setElevation(toolbar, Util.dip2px(mContext, 2f));

    }

    @Override
    public void onClick(View v) {
        List<String> selectMediaUUIDs;
        switch (v.getId()) {
            case R.id.bt_share:

                if (!Util.getNetworkState(mContext)) {
                    Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    return;
                }

                mSelectMedias = photoList.getSelectedMedias();

                AbstractCommand shareInAppCommand = new AbstractCommand() {
                    @Override
                    public void execute() {
                        handleShareInApp();

                        hideChooseHeader();
                        showBottomNavAnim();
                    }

                    @Override
                    public void unExecute() {
                    }
                };

                AbstractCommand shareToOtherAppCommand = new AbstractCommand() {
                    @Override
                    public void execute() {
                        handleShareToOtherApp();

                        hideChooseHeader();
                        showBottomNavAnim();
                    }

                    @Override
                    public void unExecute() {
                    }
                };

                new ShareMenuBottomDialogFactory(shareInAppCommand, shareToOtherAppCommand).createDialog(mContext).show();

                break;

            case R.id.bt_album:
                selectMediaUUIDs = photoList.getSelectedImageUUIDs();

                photoList.createAlbum(selectMediaUUIDs);
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

    private void handleShareToOtherApp() {

        mSelectMediaOriginalPhotoPaths = new ArrayList<>(mSelectMedias.size());

        Iterator<Media> iterator = mSelectMedias.iterator();
        while (iterator.hasNext()) {
            Media media = iterator.next();
            String originalPhotoPath = media.getOriginalPhotoPath();

            if (originalPhotoPath.length() != 0) {
                mSelectMediaOriginalPhotoPaths.add(originalPhotoPath);
                iterator.remove();
            }
        }

        if (mSelectMedias.size() == 0) {
            Util.sendShare(getContext(), mSelectMediaOriginalPhotoPaths);
        } else {

            mDialog = ProgressDialog.show(mContext, null, String.format(getString(R.string.operating_title), getString(R.string.create_share)), true, true);
            mDialog.setCanceledOnTouchOutside(false);

            EventBus.getDefault().post(new RetrieveMediaOriginalPhotoRequestEvent(OperationType.GET, OperationTargetType.MEDIA_ORIGINAL_PHOTO, mSelectMedias));
        }
    }

    private void handleShareInApp() {
        List<String> selectMediaUUIDs = new ArrayList<>(mSelectMedias.size());

        for (Media media : mSelectMedias) {
            selectMediaUUIDs.add(media.getUuid());
        }

        mDialog = ProgressDialog.show(mContext, null, String.format(getString(R.string.operating_title), getString(R.string.create_share)), true, false);

        photoList.createShare(selectMediaUUIDs);

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
    public void modifyMediaShare(MediaShare mediaShare) {

        if (Util.getNetworkState(mContext)) {

            if (mediaShare.checkPermissionToOperate()) {

                String operation;
                if (mediaShare.getViewersListSize() == 0) {
                    operation = String.format(getString(R.string.operating_title), getString(R.string.set_public));
                } else {
                    operation = String.format(getString(R.string.operating_title), getString(R.string.set_private));
                }

                mDialog = ProgressDialog.show(mContext, null, operation, true, false);

                String requestData = mediaShare.createToggleShareStateRequestData();

                FNAS.modifyRemoteMediaShare(mContext, mediaShare, requestData);
            } else {
                Toast.makeText(mContext, getString(R.string.no_operate_media_share_permission), Toast.LENGTH_SHORT).show();

            }

        } else {
            Toast.makeText(mContext, mContext.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void deleteMediaShare(MediaShare mediaShare) {

        if (Util.getNetworkState(mContext)) {

            if (mediaShare.checkPermissionToOperate()) {
                mDialog = ProgressDialog.show(mContext, null, String.format(getString(R.string.operating_title), getString(R.string.delete_text)), true, false);

                FNAS.deleteRemoteMediaShare(mContext, mediaShare);
            } else {
                Toast.makeText(mContext, getString(R.string.no_operate_media_share_permission), Toast.LENGTH_SHORT).show();

            }

        } else {
            Toast.makeText(mContext, mContext.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
        }

    }

    private void onDidAppear(int position) {

        if (sInChooseMode) {
            hideChooseHeader();
            showBottomNav();
        }

        if (toolbar.getVisibility() != View.VISIBLE) {
            toolbar.setVisibility(View.VISIBLE);
        }

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
