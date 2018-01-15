package com.winsun.fruitmix;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.component.UserAvatar;
import com.winsun.fruitmix.databinding.ActivityNavPagerBinding;
import com.winsun.fruitmix.equipment.manage.EquipmentManageActivity;
import com.winsun.fruitmix.equipment.manage.ShutDownEquipmentActivity;
import com.winsun.fruitmix.equipment.search.data.InjectEquipment;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.file.view.FileDownloadActivity;
import com.winsun.fruitmix.fragment.MediaMainFragment;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.interfaces.OnMainFragmentInteractionListener;
import com.winsun.fruitmix.invitation.ConfirmInviteUserActivity;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.logged.in.user.LoggedInWeChatUser;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.logout.InjectLogoutUseCase;
import com.winsun.fruitmix.logout.LogoutUseCase;
import com.winsun.fruitmix.mainpage.MainPagePresenter;
import com.winsun.fruitmix.mainpage.MainPagePresenterImpl;
import com.winsun.fruitmix.mainpage.MainPageView;
import com.winsun.fruitmix.equipment.search.data.Equipment;
import com.winsun.fruitmix.equipment.search.data.EquipmentSearchManager;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.torrent.view.TorrentDownloadManageActivity;
import com.winsun.fruitmix.upload.media.InjectUploadMediaUseCase;
import com.winsun.fruitmix.upload.media.UploadMediaCountChangeListener;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.upload.media.uploadMediaState.UploadMediaState;
import com.winsun.fruitmix.usecase.InjectGetAllBindingLocalUserUseCase;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.user.manage.UserManageActivity;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.winsun.fruitmix.upload.media.uploadMediaState.UploadMediaState.CREATE_FOLDER_FAIL;
import static com.winsun.fruitmix.upload.media.uploadMediaState.UploadMediaState.GET_FOLDER_FAIL;
import static com.winsun.fruitmix.upload.media.uploadMediaState.UploadMediaState.GET_MEDIA_COUNT_FAIL;
import static com.winsun.fruitmix.upload.media.uploadMediaState.UploadMediaState.START_GET_UPLOAD_MEDIA_COUNT;
import static com.winsun.fruitmix.upload.media.uploadMediaState.UploadMediaState.UPLOAD_MEDIA_COUNT_CHANGED;
import static com.winsun.fruitmix.upload.media.uploadMediaState.UploadMediaState.UPLOAD_MEDIA_FAIL;

public class NavPagerActivity extends BaseActivity
        implements OnMainFragmentInteractionListener, MainPageView, UploadMediaCountChangeListener {

    public static final String TAG = NavPagerActivity.class.getSimpleName();

    public static final int REQUEST_EQUIPMENT_MANAGE = 0x1011;

    UserAvatar userAvatar;

    DrawerLayout mDrawerLayout;

    RecyclerView mNavigationMenuRecyclerView;

    @Override
    public void gotoUserManageActivity() {
        Util.startActivity(mContext, UserManageActivity.class);
    }

    @Override
    public void gotoEquipmentManageActivity() {

        startActivityForResult(new Intent(mContext, EquipmentManageActivity.class), REQUEST_EQUIPMENT_MANAGE);

    }

    @Override
    public void gotoSettingActivity() {
        Util.startActivity(mContext, SettingActivity.class);
    }

    @Override
    public void gotoAccountManageActivity() {
        startActivityForResult(new Intent(mContext, AccountManageActivity.class), START_ACCOUNT_MANAGE);
    }

    @Override
    public void gotoTorrentDownloadManageActivity() {
        Util.startActivity(mContext, TorrentDownloadManageActivity.class);
    }

    @Override
    public void gotoFileDownloadActivity() {
        Util.startActivity(mContext, FileDownloadActivity.class);
    }

    @Override
    public void gotoConfirmInviteUserActivity() {
        Util.startActivity(mContext, ConfirmInviteUserActivity.class);
    }

    @Override
    public void loggedInUserItemOnClick(LoggedInUser loggedInUser) {

        showProgressDialog(getString(R.string.change_user, loggedInUser.getUser().getUserName()));

        if (loggedInUser instanceof LoggedInWeChatUser) {

            LoggedInWeChatUser loggedInWeChatUser = (LoggedInWeChatUser) loggedInUser;

            loginUseCase.loginWithOtherWeChatUserBindingLocalUser(loggedInWeChatUser, new BaseOperateDataCallback<Boolean>() {
                @Override
                public void onSucceed(Boolean data, OperationResult result) {

                    dismissDialog();

                    handleChangeUserSucceed(data);

                }

                @Override
                public void onFail(OperationResult result) {

                    dismissDialog();

                    Toast.makeText(mContext, result.getResultMessage(mContext), Toast.LENGTH_SHORT).show();

                }
            });

        } else {

            startDiscovery(loggedInUser);

            mCustomHandler.sendEmptyMessageDelayed(DISCOVERY_TIMEOUT_MESSAGE, DISCOVERY_TIMEOUT_TIME);

        }

    }

    @Override
    public void quitApp() {
        finishApp();
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void setCurrentPage(int page) {
        currentPage = page;
    }

    @Override
    public void showMediaHideFile() {
        fragmentManager.beginTransaction().show(mediaMainFragment).commit();

        mediaMainFragment.show();
    }


    @Override
    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public class NavPagerViewModel {

        public final ObservableField<String> versionNameText = new ObservableField<>();

        public final ObservableField<String> userAvatarText = new ObservableField<>();

        public final ObservableInt userAvatarBackgroundResID = new ObservableInt();

        public final ObservableField<String> userNameText = new ObservableField<>();

        public final ObservableBoolean equipmentNameVisibility = new ObservableBoolean();

        public final ObservableField<String> equipmentNameText = new ObservableField<>();

        public final ObservableField<String> uploadMediaPercentText = new ObservableField<>();

        public final ObservableInt uploadPercentProgress = new ObservableInt();

        public final ObservableBoolean uploadPercentProgressVisibility = new ObservableBoolean(true);

        public final ObservableField<String> uploadCountText = new ObservableField<>();

        public final ObservableBoolean uploadCountTextVisibility = new ObservableBoolean(true);

        public final ObservableInt headerArrowResID = new ObservableInt();

        public final ObservableInt headerArrowStr = new ObservableInt(R.string.main_menu);

        public final ObservableBoolean showUploadProgress = new ObservableBoolean(false);

        public final ObservableBoolean showConnectServerFailed = new ObservableBoolean(false);

        public final ObservableBoolean showLoadingUploadProgress = new ObservableBoolean(true);

    }

    private Context mContext;

    private MediaMainFragment mediaMainFragment;

    private FragmentManager fragmentManager;

    private EquipmentSearchManager mEquipmentSearchManager;

    public static final int PAGE_FILE = 1;
    public static final int PAGE_MEDIA = 0;

    private int currentPage = 0;

    public static final int START_ACCOUNT_MANAGE = 0x1001;

    public static final int START_PERSON_INFO = 0x1002;

    public static final int RESULT_REFRESH_LOGGED_IN_USER = 0x1002;
    public static final int RESULT_FINISH_ACTIVITY = 0x1003;
    public static final int RESULT_LOGOUT = 0x1004;

    private SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            if (currentPage == PAGE_MEDIA)
                mediaMainFragment.onMapSharedElements(names, sharedElements);
        }
    };

    public static final int DISCOVERY_TIMEOUT_MESSAGE = 0x1001;

    public static final int DISCOVERY_TIMEOUT_TIME = 15 * 1000;

    private CustomHandler mCustomHandler;

    private static class CustomHandler extends Handler {

        WeakReference<NavPagerActivity> weakReference = null;

        CustomHandler(NavPagerActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISCOVERY_TIMEOUT_MESSAGE:
                    weakReference.get().stopDiscovery();

                    weakReference.get().dismissDialog();

                    Toast.makeText(weakReference.get(), weakReference.get().getString(R.string.search_equipment_failed), Toast.LENGTH_SHORT).show();

                    weakReference.get().mDrawerLayout.closeDrawer(GravityCompat.START);

                    break;
                default:
            }
        }
    }

    private int mAlreadyUploadMediaCount = -1;
    private int mTotalLocalMediaCount = 0;

    private NavPagerViewModel navPagerViewModel;

    private MainPagePresenter mainPagePresenter;

    private SystemSettingDataSource systemSettingDataSource;

    private LoginUseCase loginUseCase;

    private LogoutUseCase logoutUseCase;

    private LoggedInUserDataSource loggedInUserDataSource;

    private UserDataRepository userDataRepository;

    private UploadMediaCountChangeListener uploadMediaCountChangeListener;

    private UploadMediaUseCase uploadMediaUseCase;

    private ActivityNavPagerBinding binding;

    private ThreadManager threadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_nav_pager);

        threadManager = ThreadManagerImpl.getInstance();

        mDrawerLayout = binding.drawerLayout;

        mNavigationMenuRecyclerView = binding.navigationMenuRecyclerView;

        userAvatar = binding.leftDrawerHeadLayout.avatar;

        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(mContext, PersonInfoActivity.class), START_PERSON_INFO);
            }
        });

        navPagerViewModel = new NavPagerViewModel();

        userDataRepository = InjectUser.provideRepository(mContext);

        loginUseCase = InjectLoginUseCase.provideLoginUseCase(mContext);

        loginUseCase.setAlreadyLogin(true);

        logoutUseCase = InjectLogoutUseCase.provideLogoutUseCase(mContext);

        loggedInUserDataSource = InjectLoggedInUser.provideLoggedInUserRepository(mContext);

        systemSettingDataSource = InjectSystemSettingDataSource.provideSystemSettingDataSource(this);

        mainPagePresenter = new MainPagePresenterImpl(mContext, systemSettingDataSource, loggedInUserDataSource,
                navPagerViewModel, this, InjectGetAllBindingLocalUserUseCase.provideInstance(this), InjectEquipment.provideEquipmentDataSource(this));

        binding.setViewModel(navPagerViewModel);

        binding.setPresenter(mainPagePresenter);

        setExitSharedElementCallback(sharedElementCallback);

        initNavigationMenuRecyclerView();

        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

//                final BitmapDrawable drawable = (BitmapDrawable) ContextCompat.getDrawable(mContext, R.drawable.navigation_header_bg);
//
//                leftDrawerHeadLayout.setBackground(drawable);
//
//                new AnimateColorMatrixUtil().startAnimation(drawable, 2000);

                if (systemSettingDataSource.getLoginWithWechatCodeOrNot())
                    binding.leftDrawerHeadLayout.cloud.setVisibility(View.VISIBLE);
                else
                    binding.leftDrawerHeadLayout.cloud.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                mainPagePresenter.switchToNavigationItemMenu();
            }
        });

        mainPagePresenter.switchToNavigationItemMenu();

        navPagerViewModel.versionNameText.set(String.format(getString(R.string.android_version_name), Util.getVersionName(mContext)));

        mediaMainFragment = MediaMainFragment.newInstance();

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.frame_layout, mediaMainFragment).commit();

        currentPage = PAGE_MEDIA;

        mCustomHandler = new CustomHandler(this);

        checkShowAutoUploadDialog();

        uploadMediaUseCase = InjectUploadMediaUseCase.provideUploadMediaUseCase(this);

        uploadMediaUseCase.registerUploadMediaCountChangeListener(this);
//        ButlerService.registerUploadMediaCountChangeListener(this);

        userAvatar.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {

                userAvatar.getViewTreeObserver().removeOnPreDrawListener(this);

                refreshUserInNavigationView();

                return true;

            }
        });

    }

    @Override
    public void onStartGetUploadMediaCount() {

        handleUploadStateChangeToStartGetUploadMediaCount();

    }

    @Override
    public void onUploadMediaCountChanged(int uploadedMediaCount, int totalCount) {

        handleUploadStateChangeToUploadMediaCountChanged(uploadedMediaCount, totalCount);

//        mediaMainFragment.onUploadMediaCountChanged(totalCount);

    }

    @Override
    public void onGetFolderFail(int httpErrorCode) {

        handleUploadStateChangeToGetFolderFail(httpErrorCode);
    }

    @Override
    public void onCreateFolderFail(int httpErrorCode) {

        handleUploadStateChangeToCreateFolderFail(httpErrorCode);
    }

    @Override
    public void onUploadMediaFail(int httpErrorCode) {

        handleUploadStateChangeToUploadMediaFail(httpErrorCode);
    }

    @Override
    public void onGetUploadMediaCountFail(int httpErrorCode) {

        handleUploadStateChangeToGetUploadMediaCountFail(httpErrorCode);
    }

    private void handleUploadStateChangeToGetUploadMediaCountFail(int httpErrorCode) {
        navPagerViewModel.showLoadingUploadProgress.set(false);
        navPagerViewModel.showConnectServerFailed.set(true);

        navPagerViewModel.showUploadProgress.set(false);

//        if (httpErrorCode != -1)
//            showCustomErrorCode(Util.CUSTOM_ERROR_CODE_HEAD + Util.CUSTOM_ERROR_CODE_GET_UPLOADED_MEDIA + httpErrorCode);

        binding.setViewModel(navPagerViewModel);

        binding.executePendingBindings();
    }

    private void handleUploadStateChangeToUploadMediaFail(int httpErrorCode) {
//        if (httpErrorCode != -1)
//            showCustomErrorCode(Util.CUSTOM_ERROR_CODE_HEAD + Util.CUSTOM_ERROR_CODE_UPLOAD_MEDIA + httpErrorCode);

        binding.setViewModel(navPagerViewModel);

        binding.executePendingBindings();
    }

    private void handleUploadStateChangeToCreateFolderFail(int httpErrorCode) {
        navPagerViewModel.showLoadingUploadProgress.set(false);
        navPagerViewModel.showConnectServerFailed.set(true);

        navPagerViewModel.showUploadProgress.set(false);

//        if (httpErrorCode != -1)
//            showCustomErrorCode(Util.CUSTOM_ERROR_CODE_HEAD + Util.CUSTOM_ERROR_CODE_CREATE_FOLDER + httpErrorCode);

        binding.setViewModel(navPagerViewModel);

        binding.executePendingBindings();
    }

    private void handleUploadStateChangeToGetFolderFail(int httpErrorCode) {
        navPagerViewModel.showLoadingUploadProgress.set(false);
        navPagerViewModel.showConnectServerFailed.set(true);

        navPagerViewModel.showUploadProgress.set(false);

//        if (httpErrorCode != -1)
//            showCustomErrorCode(Util.CUSTOM_ERROR_CODE_HEAD + Util.CUSTOM_ERROR_CODE_GET_FOLDER + httpErrorCode);

        binding.setViewModel(navPagerViewModel);

        binding.executePendingBindings();
    }

    private void handleUploadStateChangeToUploadMediaCountChanged(int uploadedMediaCount, int totalCount) {
        navPagerViewModel.showLoadingUploadProgress.set(false);
        navPagerViewModel.showConnectServerFailed.set(false);

        navPagerViewModel.showUploadProgress.set(true);

        mAlreadyUploadMediaCount = uploadedMediaCount;
        mTotalLocalMediaCount = totalCount;

        handleUploadMediaCount();

        binding.setViewModel(navPagerViewModel);

        binding.executePendingBindings();
    }

    private void handleUploadStateChangeToStartGetUploadMediaCount() {
        navPagerViewModel.showLoadingUploadProgress.set(true);

        navPagerViewModel.showConnectServerFailed.set(false);
        navPagerViewModel.showUploadProgress.set(false);

        binding.setViewModel(navPagerViewModel);

        binding.executePendingBindings();
    }

    private void handleUploadMediaCount() {

        if (mTotalLocalMediaCount == 0) {

            navPagerViewModel.uploadMediaPercentText.set(getString(R.string.no_photo));

            navPagerViewModel.uploadPercentProgressVisibility.set(false);

            navPagerViewModel.uploadCountTextVisibility.set(false);

            return;

        } else {

            navPagerViewModel.uploadPercentProgressVisibility.set(true);

            navPagerViewModel.uploadCountTextVisibility.set(true);

        }

        if (mAlreadyUploadMediaCount == mTotalLocalMediaCount) {

            navPagerViewModel.uploadMediaPercentText.set(getString(R.string.already_upload_finished));

            navPagerViewModel.uploadPercentProgress.set(100);

        } else {

            float percent = ((float) mAlreadyUploadMediaCount * 100) / ((float) mTotalLocalMediaCount);

            int roundPercent = (int) percent;

            String percentText = roundPercent + "%";

            navPagerViewModel.uploadMediaPercentText.set(String.format(getString(R.string.already_upload_media_percent_text), percentText));

            navPagerViewModel.uploadPercentProgress.set(roundPercent);

        }

        navPagerViewModel.uploadCountText.set(mAlreadyUploadMediaCount + "/" + mTotalLocalMediaCount);
    }


    private void initNavigationMenuRecyclerView() {

        mNavigationMenuRecyclerView.setAdapter(mainPagePresenter.getNavigationItemAdapter());

        mNavigationMenuRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mNavigationMenuRecyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    private void startDiscovery(final LoggedInUser loggedInUser) {

        mEquipmentSearchManager = InjectEquipment.provideEquipmentSearchManager(mContext);

        mEquipmentSearchManager.startDiscovery(new EquipmentSearchManager.IEquipmentDiscoveryListener() {
            @Override
            public void call(Equipment equipment) {
                handleFoundEquipment(equipment, loggedInUser);
            }
        });

    }

    private void handleFoundEquipment(final Equipment createdEquipment, final LoggedInUser loggedInUser) {
        Log.d(TAG, "search equipment_blue: loggedinuser equipment name: " + loggedInUser.getEquipmentName() + " createEquipment equipment name: " + createdEquipment.getEquipmentName());

        loggedInUserDataSource.checkFoundedEquipment(createdEquipment, loggedInUser, new BaseOperateDataCallbackImpl<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {
                super.onSucceed(data, result);

                loggedInUser.setGateway(createdEquipment.getHosts().get(0));

                handleFoundEquipmentSucceed(loggedInUser);
            }
        });

    }

    private void handleFoundEquipmentSucceed(LoggedInUser loggedInUser) {
        dismissDialog();

        stopDiscovery();

        mCustomHandler.removeMessages(DISCOVERY_TIMEOUT_MESSAGE);

        loginUseCase.loginWithLoggedInUser(loggedInUser, new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                handleChangeUserSucceed(data);

            }

            @Override
            public void onFail(OperationResult result) {

                Log.d(TAG, "onFail: login with logged in user failed");

                Toast.makeText(mContext, result.getResultMessage(mContext), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void handleChangeUserSucceed(Boolean data) {
        refreshUserInNavigationView();

        navPagerViewModel.showLoadingUploadProgress.set(true);

        uploadMediaUseCase.startUploadMedia();

        if (systemSettingDataSource.getAutoUploadOrNot())
            Toast.makeText(mContext, String.format(getString(R.string.success), getString(R.string.login)), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(mContext, getString(R.string.photo_auto_upload_already_close), Toast.LENGTH_SHORT).show();

        handleFinishLoginWithLoggedInUser();

    }

    private void handleFinishLoginWithLoggedInUser() {
        mediaMainFragment.refreshAllViewsForce();

        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void stopDiscovery() {
        mEquipmentSearchManager.stopDiscovery();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentPage == PAGE_MEDIA) {
            mediaMainFragment.show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (currentPage == PAGE_MEDIA) {
            mediaMainFragment.hide();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        ButlerService.unregisterUploadMediaCountChangeListener(this);
        uploadMediaUseCase.unregisterUploadMediaCountChangeListener(this);

        mainPagePresenter.onDestroy();

        mContext = null;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void switchDrawerOpenState() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void lockDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void unlockDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void handleStickyOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        if (action.equals(Util.SET_CURRENT_LOGIN_USER_AFTER_LOGIN)) {

            Log.i(TAG, "handleOperationEvent: refreshUser");

            EventBus.getDefault().removeStickyEvent(operationEvent);

            checkShowAutoUploadDialog();

            refreshUserInNavigationView();

            if (operationEvent.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

                if (currentPage == PAGE_MEDIA) {
                    mediaMainFragment.refreshUser();
                }

            }
        } else if (action.equals(Util.LOGIN_STATE_CHANGED)) {

            Log.d(TAG, "handleStickyOperationEvent: login state changed");

            EventBus.getDefault().removeStickyEvent(operationEvent);

            refreshUserInNavigationView();

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleUploadMediaStateChanged(UploadMediaState uploadMediaState) {

        EventBus.getDefault().removeStickyEvent(uploadMediaState);

        switch (uploadMediaState.getType()) {

            case START_GET_UPLOAD_MEDIA_COUNT:
                handleUploadStateChangeToStartGetUploadMediaCount();
                break;
            case GET_FOLDER_FAIL:
                handleUploadStateChangeToGetFolderFail(uploadMediaState.getErrorCode());
                break;
            case CREATE_FOLDER_FAIL:
                handleUploadStateChangeToCreateFolderFail(uploadMediaState.getErrorCode());
                break;
            case GET_MEDIA_COUNT_FAIL:
                handleUploadStateChangeToGetUploadMediaCountFail(uploadMediaState.getErrorCode());
                break;
            case UPLOAD_MEDIA_FAIL:
                handleUploadStateChangeToUploadMediaFail(uploadMediaState.getErrorCode());

                break;
            case UPLOAD_MEDIA_COUNT_CHANGED:
                handleUploadStateChangeToUploadMediaCountChanged(uploadMediaUseCase.getAlreadyUploadedMediaCount(), uploadMediaUseCase.getLocalMedias().size());

                break;
            default:
                Log.d(TAG, "handleUploadMediaStateChanged: enter default upload state,something wrong");

        }

    }


    private void refreshUserInNavigationView() {

        User user = userDataRepository.getUserByUUID(systemSettingDataSource.getCurrentLoginUserUUID());

        Log.d(TAG, "refreshUserInNavigationView: user is null:" + (user == null ? "true" : "false"));

        refreshUserName(user);

        userAvatar.setUnregisterListenerWhenDetachFromWindow(false);

        userAvatar.setUser(user, InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this));
//        navPagerViewModel.userAvatarText.set(user.getDefaultAvatar());
//        navPagerViewModel.userAvatarBackgroundResID.set(user.getDefaultAvatarBgColorResourceId());

        mainPagePresenter.refreshUserInNavigationView(mContext, user);

        mainPagePresenter.notifyAdapterDataSetChanged();

    }

    private void refreshUserName(User user) {
        String userAssociatedWeChatUserName = user.getAssociatedWeChatUserName();

        String userName;

        if (userAssociatedWeChatUserName.isEmpty())
            userName = user.getUserName();
        else
            userName = userAssociatedWeChatUserName;

        navPagerViewModel.userNameText.set(userName);
    }

    private void checkShowAutoUploadDialog() {
        if (systemSettingDataSource.getShowAutoUploadDialog()) {
            showNeedAutoUploadDialog();
        }
    }

    private void showNeedAutoUploadDialog() {
        new AlertDialog.Builder(mContext).setMessage(getString(R.string.need_auto_upload, getString(R.string.wisnuc_server))).setPositiveButton(getString(R.string.backup), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                systemSettingDataSource.setShowAutoUploadDialog(false);

                systemSettingDataSource.setCurrentUploadUserUUID(systemSettingDataSource.getCurrentLoginUserUUID());

                systemSettingDataSource.setAutoUploadOrNot(true);

                EventBus.getDefault().post(new RequestEvent(OperationType.START_UPLOAD, null));

            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                systemSettingDataSource.setShowAutoUploadDialog(false);

                systemSettingDataSource.setAutoUploadOrNot(false);

                EventBus.getDefault().post(new RequestEvent(OperationType.STOP_UPLOAD, null));

            }
        }).setCancelable(false).create().show();
    }

    private void showExplanation(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.finish_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(NavPagerActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                    }
                });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.i(TAG, "onRequestPermissionsResult: " + (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ? "true" : "false"));

        mediaMainFragment.requestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (currentPage == PAGE_MEDIA)
            mediaMainFragment.onActivityReenter(resultCode, data);

    }

    @Override
    public void onBackPress() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {

        if (currentPage == PAGE_MEDIA) {

            if (mediaMainFragment.handleBackPressedOrNot()) {
                mediaMainFragment.handleBackPressed();
            } else if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                finishApp();
            }

        } else {
            finishApp();
        }
    }

    private void finishApp() {

        ButlerService.stopButlerService(mContext);

        super.onBackPressed();

/*        if (System.currentTimeMillis() - backPressedTimeMillis < TIME_INTERNAL) {

            ButlerService.stopButlerService(mContext);

            super.onBackPressed();
        } else {

            Toast.makeText(mContext, getString(R.string.android_finishAppToast), Toast.LENGTH_SHORT).show();
        }

        backPressedTimeMillis = System.currentTimeMillis();*/

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == START_ACCOUNT_MANAGE) {

            Log.i(TAG, "onActivityResult: requestCode == START_ACCOUNT_MANAGE");

            Log.i(TAG, "onActivityResult: resultCode" + resultCode);

            if (resultCode == RESULT_FINISH_ACTIVITY) {
                finish();
            } else if (resultCode == RESULT_REFRESH_LOGGED_IN_USER) {

                mainPagePresenter.refreshNavigationLoggedInUsers(this);
                mainPagePresenter.notifyAdapterDataSetChanged();

            } else if (resultCode == RESULT_LOGOUT) {
                handleLogoutOnClick();
            }

        } else if (requestCode == START_PERSON_INFO) {

            if (resultCode == RESULT_OK) {

                refreshUserInNavigationView();

            } else if (resultCode == RESULT_FINISH_ACTIVITY) {
                finish();
            }

        } else if (requestCode == REQUEST_EQUIPMENT_MANAGE && resultCode == EquipmentManageActivity.RESULT_SHUTDOWN_EQUIPMENT) {

            Log.d(TAG, "onActivityResult: logout when reboot equipment and enter maintenance");

            handleLogoutOnClick();

        } else {

            if (currentPage == PAGE_MEDIA)
                mediaMainFragment.onActivityResult(requestCode, resultCode, intent);

        }

    }

    private void handleLogoutOnClick() {

        showProgressDialog(String.format(getString(R.string.operating_title), getString(R.string.logout)));

        Future<Boolean> future = threadManager.runOnCacheThread(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {

                logoutUseCase.logout();

                //TODO:check put setAlreadyLogin here will cause every time call logout need call this method
                loginUseCase.setAlreadyLogin(false);

                return true;
            }
        });

        try {
            Boolean result = future.get();

            if (mContext == null)
                return;

            dismissDialog();

            EquipmentSearchActivity.gotoEquipmentActivity((Activity) mContext, true);

        } catch (InterruptedException e) {
            e.printStackTrace();

            dismissDialog();

            showToast(getString(R.string.fail, getString(R.string.logout)));


        } catch (ExecutionException e) {
            e.printStackTrace();

            dismissDialog();

            showToast(getString(R.string.fail, getString(R.string.logout)));

        }

    }

}
