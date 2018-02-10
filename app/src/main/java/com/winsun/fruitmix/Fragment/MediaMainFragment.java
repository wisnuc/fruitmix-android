package com.winsun.fruitmix.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.anim.SharpCurveInterpolator;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.contact.ContactListActivity;
import com.winsun.fruitmix.databinding.NavPagerMainBinding;
import com.winsun.fruitmix.dialog.ShareMenuBottomDialogFactory;
import com.winsun.fruitmix.eventbus.MqttMessageEvent;
import com.winsun.fruitmix.eventbus.TaskStateChangedEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveVideoThumbnailEvent;
import com.winsun.fruitmix.file.view.fragment.FileFragment;
import com.winsun.fruitmix.file.view.interfaces.FileListSelectModeListener;
import com.winsun.fruitmix.file.view.interfaces.HandleFileListOperateCallback;
import com.winsun.fruitmix.group.presenter.GroupContentPresenter;
import com.winsun.fruitmix.group.setting.GroupSettingActivity;
import com.winsun.fruitmix.group.view.GroupContentActivity;
import com.winsun.fruitmix.group.view.GroupListPage;
import com.winsun.fruitmix.inbox.view.InboxListPage;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.interfaces.OnMainFragmentInteractionListener;
import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.fragment.NewPhotoList;
import com.winsun.fruitmix.interfaces.Page;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.anim.AnimatorBuilder;
import com.winsun.fruitmix.anim.CustomTransitionListener;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.mqtt.MqttUseCase;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.RevealToolbarViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.winsun.fruitmix.group.view.GroupListPage.CREATE_GROUP_REQUEST_CODE;
import static com.winsun.fruitmix.group.view.GroupListPage.GROUP_CONTENT_REQUEST_CODE;

public class MediaMainFragment extends Fragment implements View.OnClickListener, IPhotoListListener, HandleFileListOperateCallback, FileListSelectModeListener {

    public static final String TAG = "MediaMainFragment";

    private Toolbar toolbar;

    private Toolbar revealToolbar;

    private FloatingActionButton fab;

    private ViewPager viewPager;

    private ImageView systemShareBtn;

    private FloatingActionButton downloadFileBtn;

    private ImageView mAlbumBalloon;

    private BottomNavigationView bottomNavigationView;

    private List<Page> pageList;
    private FileFragment fileFragment;
    private NewPhotoList photoList;
    private GroupListPage groupListPage;

    private InboxListPage mInboxListPage;

    private boolean sMenuUnfolding = false;

    private Context mContext;

    private ProgressDialog mDialog;

    //    private static final int PAGE_INBOX = 0;
    private static final int PAGE_GROUP = 0;
    private static final int PAGE_PHOTO = 1;
    private static final int PAGE_FILE = 2;

    private boolean sInChooseMode = false;

    private boolean onResume = false;

    private OnMainFragmentInteractionListener mListener;

    private boolean mPhotoListRefresh = false;

    private IShowHideFragmentListener mCurrentFragment;

    private boolean mIsResume = false;

    private List<Media> mSelectMedias;

    private List<String> mSelectMediaOriginalPhotoPaths;

    private ToolbarViewModel toolbarViewModel;

    private RevealToolbarViewModel revealToolbarViewModel;

    private ToolbarViewModel.ToolbarNavigationOnClickListener defaultListener;

    private MediaDataSourceRepository mediaDataSourceRepository;

    private User mCurrentUser;

    private SystemSettingDataSource mSystemSettingDataSource;

    private boolean noMediaSelect = true;
    private boolean noFileSelect = true;

    public MediaMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MediaMainFragment.
     */
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

        mediaDataSourceRepository = InjectMedia.provideMediaDataSourceRepository(mContext);

        mSystemSettingDataSource = InjectSystemSettingDataSource.provideSystemSettingDataSource(mContext);

        String currentLoginUserUUID = mSystemSettingDataSource.getCurrentLoginUserUUID();

        UserDataRepository userDataRepository = InjectUser.provideRepository(mContext);

        mCurrentUser = userDataRepository.getUserByUUID(currentLoginUserUUID);

        Log.d(TAG, "onCreate: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        NavPagerMainBinding binding = NavPagerMainBinding.inflate(inflater, container, false);

        toolbar = binding.toolbarLayout.toolbar;

        revealToolbar = binding.revealToolbarLayout.revealToolbar;

        fab = binding.fab;

        viewPager = binding.viewPager;

        systemShareBtn = binding.systemShare;

        downloadFileBtn = binding.downloadFileBtn;

        mAlbumBalloon = binding.albumBalloon;

        bottomNavigationView = binding.bottomNavigationView;

        initToolbar(binding);

        initRevealToolbar(binding);

        initNavigationView();

        initPageList();

        initViewPager();

        viewPager.setCurrentItem(PAGE_PHOTO);
        onPageSelect(PAGE_PHOTO);

        systemShareBtn.setOnClickListener(this);
        downloadFileBtn.setOnClickListener(this);
        fab.setOnClickListener(this);

        photoList.setPhotoListListener(this);

        Log.d(TAG, "onCreateView: ");

        return binding.getRoot();
    }

    private void initRevealToolbar(NavPagerMainBinding binding) {
        revealToolbarViewModel = new RevealToolbarViewModel();

        revealToolbarViewModel.selectCountTitleText.set(getString(R.string.choose_text));

        revealToolbarViewModel.rightTextVisibility.set(View.INVISIBLE);

        revealToolbarViewModel.setRevealToolbarNavigationOnClickListener(new RevealToolbarViewModel.RevealToolbarNavigationOnClickListener() {
            @Override
            public void onClick() {
                if (sInChooseMode) {

                    quitSelectMode();

                } else {
                    mListener.onBackPress();
                }
            }
        });

        binding.setRevealToolbarViewModel(revealToolbarViewModel);
    }

    private void initToolbar(NavPagerMainBinding binding) {
        toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.navigationIconResId.set(R.drawable.menu_black);

        toolbarViewModel.titleText.set("");
        toolbarViewModel.titleTextColorResID.set(ContextCompat.getColor(mContext, R.color.eighty_seven_percent_black));

        defaultListener = new ToolbarViewModel.ToolbarNavigationOnClickListener() {
            @Override
            public void onClick() {
                mListener.switchDrawerOpenState();
            }
        };

        toolbarViewModel.setToolbarNavigationOnClickListener(defaultListener);

        toolbarViewModel.setToolbarSelectBtnOnClickListener(new ToolbarViewModel.ToolbarSelectBtnOnClickListener() {
            @Override
            public void onClick() {

                if (viewPager.getCurrentItem() == PAGE_PHOTO) {

                    if (!photoList.canEnterSelectMode()) {
                        return;
                    }

                } else if (viewPager.getCurrentItem() == PAGE_FILE) {

                    if (!fileFragment.canEnterSelectMode()) {
                        return;
                    }

                }

                enterSelectMode();

            }
        });

        toolbarViewModel.showSelect.set(true);

        binding.setToolbarViewModel(toolbarViewModel);

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

        photoList.refreshView();

        groupListPage.onResume();

//        if (Util.isRemoteMediaLoaded() && Util.isLocalMediaInCameraLoaded() && Util.isLocalMediaInDBLoaded() && !mPhotoListRefresh) {
//            pageList.get(PAGE_PHOTO).refreshDownloadItemView();
//
//            mPhotoListRefresh = true;
//        }

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

        for (Page page : pageList)
            page.onDestroy();

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

        pageList.get(viewPager.getCurrentItem()).onActivityReenter(resultCode, data);

    }

    private void dismissToolbarBottomBarWhenSharedElementTransition() {
        if (Util.checkRunningOnLollipopOrHigher()) {

            FragmentActivity activity = getActivity();

            if (activity == null)
                return;

            activity.getWindow().getSharedElementEnterTransition().addListener(new CustomTransitionListener() {

                @Override
                public void onTransitionStart(Transition transition) {
                    super.onTransitionStart(transition);

                    toolbarViewModel.showToolbar.set(false);

                    bottomNavigationView.setVisibility(View.INVISIBLE);

                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);

                    toolbarViewModel.showToolbar.set(true);

                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if ((requestCode == Util.KEY_CHOOSE_PHOTO_REQUEST_CODE) && resultCode == RESULT_OK) {

            viewPager.setCurrentItem(PAGE_FILE);
            onDidAppear(PAGE_FILE);
            pageList.get(PAGE_FILE).refreshView();
//            pageList.get(PAGE_GROUP).refreshDownloadItemView();
        } else if (requestCode == CREATE_GROUP_REQUEST_CODE && resultCode == RESULT_OK) {

            Log.d(TAG, "onActivityResult: create group,so refresh group");

//            groupListPage.refreshView();

        } else if (requestCode == GROUP_CONTENT_REQUEST_CODE && resultCode == GroupSettingActivity.RESULT_MODIFY_GROUP_INFO) {

            Log.d(TAG, "onActivityResult: modify group info in setting,so refresh group ");

//            groupListPage.refreshView();
        } else if (requestCode == GROUP_CONTENT_REQUEST_CODE && resultCode == GroupSettingActivity.RESULT_DELETE_OR_QUIT_GROUP) {

            Log.d(TAG, "onActivityResult: delete or quit group in setting,so refresh group ");

            groupListPage.refreshView();

        }


    }

    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

        pageList.get(viewPager.getCurrentItem()).onMapSharedElements(names, sharedElements);

    }

    private void initNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                String eventId = "";

                switch (item.getItemId()) {
/*                    case R.id.inbox:

                        eventId = Util.SWITCH_INBOX_MODULE_UMENG_EVENT_ID;

                        viewPager.setCurrentItem(PAGE_INBOX);

                        break;*/
                    case R.id.group:

                        eventId = Util.SWITCH_GROUP_MODULE_UMENG_EVENT_ID;

                        viewPager.setCurrentItem(PAGE_GROUP);

//                       ViewPagerTranslation.INSTANCE.animatePagerTransition(false, viewPager, 200, viewPager.getCurrentItem() - PAGE_GROUP);

                        break;
                    case R.id.photo:

                        eventId = Util.SWITCH_MEDIA_MODULE_UMENG_EVENT_ID;

                        viewPager.setCurrentItem(PAGE_PHOTO);

//                        boolean forward = viewPager.getCurrentItem() < PAGE_PHOTO;
//
//                        int pageCount = forward ? PAGE_PHOTO - viewPager.getCurrentItem() : viewPager.getCurrentItem() - PAGE_PHOTO;
//
//                        ViewPagerTranslation.INSTANCE.animatePagerTransition(forward, viewPager, 200, pageCount);

                        break;
                    case R.id.file:

                        eventId = Util.SWITCH_FILE_MODULE_UMENG_EVENT_ID;

                        viewPager.setCurrentItem(PAGE_FILE);

//                        ViewPagerTranslation.INSTANCE.animatePagerTransition(true, viewPager, 200, PAGE_FILE - viewPager.getCurrentItem());

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

//        mInboxListPage = new InboxListPage((BaseActivity) getActivity());

        groupListPage = new GroupListPage(getActivity());
        photoList = new NewPhotoList(getActivity());
        fileFragment = new FileFragment(getActivity(), this, this);

        pageList = new ArrayList<>();

//        pageList.add(mInboxListPage);
        pageList.add(groupListPage);
        pageList.add(photoList);
        pageList.add(fileFragment);

    }

    public void refreshUser() {

//        groupListPage.refreshDownloadItemView();

    }

    public void onUploadMediaCountChanged(int totalLocalMediaCount) {

        photoList.onUploadMediaCountChanged(totalLocalMediaCount);

    }

    public void refreshAllViewsForce() {

        groupListPage.refreshViewForce();

        photoList.refreshViewForce();
        fileFragment.refreshViewForce();

//        mInboxListPage.refreshViewForce();

    }

    private void initViewPager() {
        MyAdapter myAdapter = new MyAdapter();
        viewPager.setAdapter(myAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                onPageSelect(position);

            }
        });
    }

    private void onPageSelect(int position) {
        onDidAppear(position);

        resetBottomNavigationItemCheckState();
        bottomNavigationView.getMenu().getItem(position).setChecked(true);
    }

    public void requestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        fileFragment.requestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleEvent(TaskStateChangedEvent taskStateChangedEvent) {

        EventBus.getDefault().removeStickyEvent(taskStateChangedEvent);

        fileFragment.handleEvent(taskStateChangedEvent);

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void handleStickyOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        Log.i(TAG, "handleOperationEvent: action:" + action);

        if (action.equals(MqttUseCase.MQTT_MESSAGE)) {

            Log.d(TAG, "handleStickyOperationEvent: mqtt message retrieved");

            EventBus.getDefault().removeStickyEvent(operationEvent);

            groupListPage.handleMqttMessage((MqttMessageEvent) operationEvent);

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        Log.i(TAG, "handleOperationEvent: action:" + action);

        switch (action) {

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

            case Util.SHARED_PHOTO_THUMB_RETRIEVED:

                handleDownloadMedia();

                break;

            case Util.REMOTE_FILE_RETRIEVED:

                fileFragment.handleOperationEvent(operationEvent);

                break;

            case Util.LOCAL_VIDEO_THUMBNAIL_RETRIEVED:

                RetrieveVideoThumbnailEvent retrieveVideoThumbnailEvent = (RetrieveVideoThumbnailEvent) operationEvent;

                photoList.refreshVideo((Video) retrieveVideoThumbnailEvent.getMedia());

                break;
        }

    }

    private void handleDownloadMedia() {
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
            FileUtil.sendShareToOtherApp(getContext(), mSelectMediaOriginalPhotoPaths);
        }
    }

    public void setPhotoListRefresh() {
        if (Util.isLocalMediaInCameraLoaded() && Util.isLocalMediaInDBLoaded() && Util.isRemoteMediaLoaded())
            mPhotoListRefresh = true;
    }

    public void setSelectCountText(String text, int selectCount) {
//        title.setText(text);

        revealToolbarViewModel.selectCountTitleText.set(text);

        if (selectCount > 0 && fab.getVisibility() != View.VISIBLE)
            showFab();

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

        FragmentActivity activity = getActivity();

        if (activity == null)
            return false;

        sp = activity.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(Util.SHOW_ALBUM_TIPS, true);
    }

    private void setShowAlbumTipsValue(boolean value) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;

        FragmentActivity activity = getActivity();

        if (activity == null)
            return;

        sp = activity.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putBoolean(Util.SHOW_ALBUM_TIPS, value);
        editor.apply();
    }

    private void showChooseHeader() {

        showRevealToolbarAnim();

        mListener.lockDrawer();
    }

    private void setSelectMode(boolean selectMode) {
        sInChooseMode = selectMode;

        if (viewPager.getCurrentItem() == PAGE_PHOTO) {
            photoList.setSelectMode(sInChooseMode);

            setSelectCountText(getString(R.string.select_photo), 0);
        } else if (viewPager.getCurrentItem() == PAGE_FILE) {

            if (selectMode)
                fileFragment.enterSelectMode();
            else
                fileFragment.quitSelectMode();

            setSelectCountText(getString(R.string.select_file), 0);
        }

    }

    private void hideChooseHeader() {
        collapseFab();

//        fab.setVisibility(View.GONE);

        dismissFab();

        dismissRevealToolbarAnim();

/*        toolbar.setNavigationIcon(R.drawable.menu_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.switchDrawerOpenState();
            }
        });
        lbRight.setVisibility(View.VISIBLE);

        setSelectCountText(getString(R.string.photo));*/

        mListener.unlockDrawer();
    }

    private void collapseFab() {
        if (sMenuUnfolding) {
            sMenuUnfolding = false;
            collapseFabAnimation();
        }
    }

    private void showFab() {

        new AnimatorBuilder(getContext(), R.animator.fab_translation, fab).addAdapter(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                fab.setVisibility(View.VISIBLE);
            }
        }).setInterpolator(new LinearOutSlowInInterpolator()).startAnimator();

    }

    private void dismissFab() {

        new AnimatorBuilder(getContext(), R.animator.fab_translation_restore, fab).addAdapter(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                fab.setVisibility(View.GONE);
            }
        }).setInterpolator(SharpCurveInterpolator.getSharpCurveInterpolator()).startAnimator();

    }

    private void showRevealToolbarAnim() {

        toolbarViewModel.showToolbar.set(false);

        revealToolbarViewModel.showRevealToolbar.set(true);

        new AnimatorBuilder(getContext(), R.animator.reveal_toolbar_translation, revealToolbar).addAdapter(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                Util.setStatusBarColor(getActivity(), R.color.fab_bg_color);
            }
        }).setInterpolator(new LinearOutSlowInInterpolator()).startAnimator();

    }

    private void dismissRevealToolbarAnim() {

        new AnimatorBuilder(getContext(), R.animator.reveal_toolbar_translation_restore, revealToolbar).addAdapter(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                revealToolbarViewModel.showRevealToolbar.set(false);

                toolbarViewModel.showToolbar.set(true);

                Util.setStatusBarColor(getActivity(), R.color.colorPrimaryDark);
            }
        }).setInterpolator(SharpCurveInterpolator.getSharpCurveInterpolator()).startAnimator();

    }


    private void showBottomNavAnim() {

        final int marginValue = Util.dip2px(getContext(), 56f);

        new AnimatorBuilder(getContext(), R.animator.bottom_nav_translation_restore, bottomNavigationView)
                .addAdapter(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        Util.setBottomMargin(viewPager, marginValue);

                    }
                }).setInterpolator(new FastOutSlowInInterpolator()).startAnimator();

    }

    private void showBottomNav() {
        bottomNavigationView.setTranslationY(0);

        Util.setBottomMargin(viewPager, Util.dip2px(getContext(), 56f));

    }

    private void dismissBottomNavAnim() {

        Util.setBottomMargin(viewPager, 0);

        new AnimatorBuilder(getContext(), R.animator.bottom_nav_translation, bottomNavigationView)
                .setInterpolator(new FastOutSlowInInterpolator()).startAnimator();
    }

    private void dismissBottomNav() {
        bottomNavigationView.setTranslationY(168);
    }

    public boolean handleBackPressedOrNot() {

        int currentItem = viewPager.getCurrentItem();

        if (currentItem == PAGE_FILE) {
            return fileFragment.handleBackPressedOrNot();
        } else
            return currentItem == PAGE_PHOTO && sInChooseMode;

    }

    public void handleBackPressed() {

        int currentItem = viewPager.getCurrentItem();

        if (currentItem == PAGE_FILE) {

            if (sInChooseMode) {

                quitSelectMode();

            } else
                fileFragment.onBackPressed();

        } else if (currentItem == PAGE_PHOTO) {

            quitSelectMode();

        }

    }

    @Override
    public void onPhotoItemClick(int selectedItemCount) {
        collapseFab();

        if (selectedItemCount == 0) {
            handleBackPressed();
        } else {
            setSelectCountText(String.format(getString(R.string.select_count), selectedItemCount), selectedItemCount);
        }

    }

    private void dismissDialog() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    public void onPhotoItemLongClick() {

        enterSelectMode();

        setSelectCountText(String.format(getString(R.string.select_count), 1), 1);
    }

    @Override
    public void onNoPhotoItem(boolean noPhotoItem) {

        Log.d(TAG, "onNoPhotoItem:" + noPhotoItem);

        int currentItem = viewPager.getCurrentItem();

        Context context = getContext();
        if (context == null)
            return;

        if (noPhotoItem && currentItem == PAGE_PHOTO) {

//            toolbarViewModel.showSelect.set(false);

            toolbarViewModel.showSelect.set(true);

            toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(context, R.color.twenty_six_percent_black));

        } else if (!noPhotoItem && currentItem == PAGE_PHOTO) {

            toolbarViewModel.showSelect.set(true);

            toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(context, R.color.eighty_seven_percent_black));

        }

        noMediaSelect = noPhotoItem;

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
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.system_share:

                if (!Util.isNetworkConnected(mContext)) {
                    Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    return;
                }

                AbstractCommand shareInAppCommand = new AbstractCommand() {
                    @Override
                    public void execute() {
                    }

                    @Override
                    public void unExecute() {
                    }
                };

                AbstractCommand shareToOtherAppCommand = new AbstractCommand() {
                    @Override
                    public void execute() {
                        handleShareToOtherApp();

                        quitSelectMode();
                    }

                    @Override
                    public void unExecute() {
                    }
                };

                new ShareMenuBottomDialogFactory(shareInAppCommand, shareToOtherAppCommand).createDialog(mContext).show();

                break;

            case R.id.download_file_btn:

                fileFragment.downloadSelectItems();

                quitSelectMode();

                break;

            case R.id.fab:
                refreshFabState();
                break;

            default:
        }
    }

    private void handleShareToOtherApp() {

        if (viewPager.getCurrentItem() == PAGE_PHOTO)
            shareMediaToOtherApp();
        else if (viewPager.getCurrentItem() == PAGE_FILE)
            shareFileToOtherApp();

    }

    private void shareMediaToOtherApp() {
        mSelectMedias = new ArrayList<>(photoList.getSelectedMedias());

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
            FileUtil.sendShareToOtherApp(getContext(), mSelectMediaOriginalPhotoPaths);
        } else {

            mDialog = ProgressDialog.show(mContext, null, String.format(getString(R.string.operating_title), getString(R.string.download_original_photo)), true, true);
            mDialog.setCanceledOnTouchOutside(false);

            mediaDataSourceRepository.downloadMedia(mSelectMedias, new BaseOperateDataCallbackImpl<Boolean>() {
                @Override
                public void onSucceed(Boolean data, OperationResult result) {
                    super.onSucceed(data, result);

                    handleDownloadMedia();

                }
            });

//            EventBus.getDefault().post(new RetrieveMediaOriginalPhotoRequestEvent(OperationType.GET, OperationTargetType.MEDIA_ORIGINAL_PHOTO, mSelectMedias));

        }
    }

    private void shareFileToOtherApp() {

        fileFragment.shareSelectFilesToOtherApp();

    }


    private void refreshFabState() {
        if (sMenuUnfolding) {
            sMenuUnfolding = false;
            collapseFabAnimation();
        } else {
            sMenuUnfolding = true;
            extendFabAnimation();
        }
    }

    private void collapseFabAnimation() {

        new AnimatorBuilder(getContext(), R.animator.fab_remote_restore, fab).startAnimator();

        if (systemShareBtn.getVisibility() == View.VISIBLE) {
            new AnimatorBuilder(getContext(), R.animator.first_btn_above_fab_translation_restore, systemShareBtn).addAdapter(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    systemShareBtn.setVisibility(View.GONE);

                }
            }).startAnimator();

            if (downloadFileBtn.getVisibility() == View.VISIBLE) {

                new AnimatorBuilder(getContext(), R.animator.second_btn_above_fab_translation_restore, downloadFileBtn).addAdapter(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        downloadFileBtn.setVisibility(View.GONE);

                    }
                }).startAnimator();
            }
        } else {

            if (downloadFileBtn.getVisibility() == View.VISIBLE) {

                new AnimatorBuilder(getContext(), R.animator.first_btn_above_fab_translation_restore, downloadFileBtn).addAdapter(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        downloadFileBtn.setVisibility(View.GONE);

                    }
                }).startAnimator();
            }

        }


    }

    private void extendFabAnimation() {

        new AnimatorBuilder(getContext(), R.animator.fab_remote, fab).startAnimator();

        if (viewPager.getCurrentItem() == PAGE_PHOTO) {

            downloadFileBtn.setVisibility(View.GONE);

            systemShareBtn.setVisibility(View.VISIBLE);

            new AnimatorBuilder(getContext(), R.animator.first_btn_above_fab_translation, systemShareBtn).startAnimator();

        } else {

            systemShareBtn.setVisibility(View.GONE);

            downloadFileBtn.setVisibility(View.VISIBLE);

            if (systemShareBtn.getVisibility() == View.VISIBLE) {

                new AnimatorBuilder(getContext(), R.animator.first_btn_above_fab_translation, systemShareBtn).startAnimator();

                if (downloadFileBtn.getVisibility() == View.VISIBLE)
                    new AnimatorBuilder(getContext(), R.animator.second_btn_above_fab_translation, downloadFileBtn).startAnimator();

            } else {

                if (downloadFileBtn.getVisibility() == View.VISIBLE)
                    new AnimatorBuilder(getContext(), R.animator.first_btn_above_fab_translation, downloadFileBtn).startAnimator();

            }

        }

    }

    @Override
    public void handleFileListOperate(String currentFolderName) {

        collapseFab();

        if (fileFragment.handleBackPressedOrNot()) {

            toolbarViewModel.titleText.set(currentFolderName);
            toolbarViewModel.navigationIconResId.set(R.drawable.ic_back_black);
            toolbarViewModel.setToolbarNavigationOnClickListener(new ToolbarViewModel.ToolbarNavigationOnClickListener() {
                @Override
                public void onClick() {
                    fileFragment.onBackPressed();
                }
            });

            mListener.lockDrawer();

        } else {

            toolbarViewModel.titleText.set(getString(R.string.files));
            toolbarViewModel.navigationIconResId.set(R.drawable.menu_black);

            toolbarViewModel.setToolbarNavigationOnClickListener(defaultListener);

            mListener.unlockDrawer();

        }

    }

    @Override
    public void onFileItemLongClick() {

        enterSelectMode();

        setSelectCountText(String.format(getString(R.string.select_count), 1), 1);

    }

    private void enterSelectMode() {
        setSelectMode(true);
        showChooseHeader();
        dismissBottomNavAnim();
    }

    @Override
    public void onFileSelectItemClick(int selectItemCount) {

        collapseFab();

        if (selectItemCount == 0) {
            quitSelectMode();
        } else
            setSelectCountText(String.format(getString(R.string.select_count), selectItemCount), selectItemCount);

    }

    @Override
    public void onFileSelectOperationAvailable() {

        Log.d(TAG, "onFileSelectOperationAvailable: ");

        Context context = getContext();
        if (context == null)
            return;

        int currentItem = viewPager.getCurrentItem();

        if (currentItem == PAGE_FILE) {
            toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(getContext(), R.color.eighty_seven_percent_black));
        }

        noFileSelect = false;
    }

    @Override
    public void onFileSelectOperationUnavailable() {

        Log.d(TAG, "onFileSelectOperationUnavailable: ");

        Context context = getContext();
        if (context == null)
            return;

        if (viewPager.getCurrentItem() == PAGE_FILE)
            toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(getContext(), R.color.twenty_six_percent_black));

        noFileSelect = true;

    }

    private void quitSelectMode() {
        setSelectMode(false);
        hideChooseHeader();
        showBottomNavAnim();
    }

    private boolean groupOnResume = false;

    private void onDidAppear(int position) {

        if (sInChooseMode) {
            quitSelectMode();
        }

        if (!toolbarViewModel.showToolbar.get()) {
            toolbarViewModel.showToolbar.set(true);
        }

        switch (position) {
            case PAGE_GROUP:

                setCurrentItem(groupListPage);

                toolbarViewModel.titleText.set(getString(R.string.group));
                toolbarViewModel.navigationIconResId.set(R.drawable.menu_black);
                toolbarViewModel.setToolbarNavigationOnClickListener(defaultListener);

                fab.setVisibility(View.GONE);
                systemShareBtn.setVisibility(View.GONE);
                downloadFileBtn.setVisibility(View.GONE);

                toolbarViewModel.showSelect.set(false);

                if (mSystemSettingDataSource.getCurrentWAToken().length() != 0) {

                    toolbarViewModel.showMenu.set(true);

                    toolbarViewModel.menuResID.set(R.drawable.ic_add_black_24dp);
                    toolbarViewModel.setToolbarMenuBtnOnClickListener(new ToolbarViewModel.ToolbarMenuBtnOnClickListener() {
                        @Override
                        public void onClick() {

                            Activity activity = getActivity();
                            if (activity == null)
                                return;

                            ContactListActivity.startForCreateGroup(getActivity());

                        }
                    });

                } else {

                    toolbarViewModel.showMenu.set(false);

                }

                if (groupListPage != null && isResumed() && !groupOnResume) {

                    Log.d(TAG, "onDidAppear: groupOnResume " + groupOnResume);

                    groupListPage.refreshView();

                    groupOnResume = true;

                }

                mListener.unlockDrawer();

                break;
/*            case PAGE_INBOX:

                setCurrentItem(mInboxListPage);

                toolbarViewModel.titleText.set(getString(R.string.inbox));
                toolbarViewModel.navigationIconResId.set(R.drawable.menu_black);
                toolbarViewModel.setToolbarNavigationOnClickListener(defaultListener);

                fab.setVisibility(View.GONE);
                systemShareBtn.setVisibility(View.GONE);
                downloadFileBtn.setVisibility(View.GONE);

                toolbarViewModel.showSelect.set(false);
                toolbarViewModel.showMenu.set(false);

                mListener.unlockDrawer();

                mInboxListPage.refreshView();

                break;*/
            case PAGE_PHOTO:

                setCurrentItem(photoList);

                toolbarViewModel.titleText.set(getString(R.string.photo));
                toolbarViewModel.navigationIconResId.set(R.drawable.menu_black);
                toolbarViewModel.setToolbarNavigationOnClickListener(defaultListener);

                fab.setVisibility(View.GONE);
                systemShareBtn.setVisibility(View.GONE);
                downloadFileBtn.setVisibility(View.GONE);

                toolbarViewModel.showSelect.set(true);
                toolbarViewModel.showMenu.set(false);

                onNoPhotoItem(noMediaSelect);

                mListener.unlockDrawer();

                break;
            case PAGE_FILE:

                setCurrentItem(fileFragment);

                fab.setVisibility(View.GONE);
                systemShareBtn.setVisibility(View.GONE);
                downloadFileBtn.setVisibility(View.GONE);

                toolbarViewModel.showSelect.set(true);
                toolbarViewModel.showMenu.set(false);

                if (noFileSelect)
                    onFileSelectOperationUnavailable();
                else
                    onFileSelectOperationAvailable();

                if (fileFragment != null && isResumed()) {

                    String title = fileFragment.getCurrentFolderName();

                    handleFileListOperate(title);

                    fileFragment.refreshView();
                }

                break;
            default:
        }

    }

    private class MyAdapter extends PagerAdapter {

        @Override
        public CharSequence getPageTitle(int position) {
            return "" + position;
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
            return pageList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }


}
