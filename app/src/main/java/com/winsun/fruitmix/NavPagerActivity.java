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
import android.os.AsyncTask;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.ActivityNavPagerBinding;
import com.winsun.fruitmix.databinding.LeftDrawerHeadBinding;
import com.winsun.fruitmix.equipment.data.InjectEquipment;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.file.view.FileDownloadActivity;
import com.winsun.fruitmix.fragment.MediaMainFragment;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.interfaces.OnMainFragmentInteractionListener;
import com.winsun.fruitmix.invitation.ConfirmInviteUserActivity;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.logout.InjectLogoutUseCase;
import com.winsun.fruitmix.logout.LogoutUseCase;
import com.winsun.fruitmix.mainpage.MainPagePresenter;
import com.winsun.fruitmix.mainpage.MainPagePresenterImpl;
import com.winsun.fruitmix.mainpage.MainPageView;
import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.equipment.data.EquipmentSearchManager;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.upload.media.CheckMediaIsUploadStrategy;
import com.winsun.fruitmix.upload.media.InjectUploadMediaUseCase;
import com.winsun.fruitmix.upload.media.UploadMediaCountChangeListener;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.user.manage.UserManageActivity;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class NavPagerActivity extends BaseActivity
        implements OnMainFragmentInteractionListener, MainPageView {

    public static final String TAG = NavPagerActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;

    private RecyclerView mNavigationMenuRecyclerView;

    @Override
    public void gotoUserManageActivity() {
        Util.startActivity(mContext, UserManageActivity.class);
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
    public void gotoFileDownloadActivity() {
        Util.startActivity(mContext, FileDownloadActivity.class);
    }

    @Override
    public void gotoConfirmInviteUserActivity() {
        Util.startActivity(mContext, ConfirmInviteUserActivity.class);
    }

    @Override
    public void loggedInUserItemOnClick(LoggedInUser loggedInUser) {

        showProgressDialog(String.format(getString(R.string.operating_title), getString(R.string.change_user)));

        startDiscovery(loggedInUser);

        mCustomHandler.sendEmptyMessageDelayed(DISCOVERY_TIMEOUT_MESSAGE, DISCOVERY_TIMEOUT_TIME);
    }

    @Override
    public void logout() {
        handleLogoutOnClick();
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

        public final ObservableField<String> uploadCountText = new ObservableField<>();

        public final ObservableInt headerArrowResID = new ObservableInt();

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

    private UploadMediaCountChangeListener uploadMediaCountChangeListener;

    private UploadMediaUseCase uploadMediaUseCase;

    private ActivityNavPagerBinding binding;

    private MediaDataSourceRepository mediaDataSourceRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_nav_pager);

        mDrawerLayout = binding.drawerLayout;

        mNavigationMenuRecyclerView = binding.navigationMenuRecyclerView;

        navPagerViewModel = new NavPagerViewModel();

        loginUseCase = InjectLoginUseCase.provideLoginUseCase(mContext);

        logoutUseCase = InjectLogoutUseCase.provideLogoutUseCase(mContext);

        loggedInUserDataSource = InjectLoggedInUser.provideLoggedInUserRepository(mContext);

        systemSettingDataSource = InjectSystemSettingDataSource.provideSystemSettingDataSource(this);

        mediaDataSourceRepository = InjectMedia.provideMediaDataSourceRepository(this);

        mainPagePresenter = new MainPagePresenterImpl(mContext, systemSettingDataSource, loggedInUserDataSource, navPagerViewModel, this);

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

        refreshUserInNavigationView();

        uploadMediaCountChangeListener = new UploadMediaCountChangeListener() {

            @Override
            public void onStartGetUploadMediaCount() {

                navPagerViewModel.showLoadingUploadProgress.set(true);

                navPagerViewModel.showConnectServerFailed.set(false);
                navPagerViewModel.showUploadProgress.set(false);

            }

            @Override
            public void onUploadMediaCountChanged(int uploadedMediaCount, int totalCount) {

                navPagerViewModel.showLoadingUploadProgress.set(false);
                navPagerViewModel.showConnectServerFailed.set(false);

                navPagerViewModel.showUploadProgress.set(true);

                mAlreadyUploadMediaCount = uploadedMediaCount;
                mTotalLocalMediaCount = totalCount;

                handleUploadMediaCount();

                binding.setViewModel(navPagerViewModel);

            }

            @Override
            public void onGetFolderFail(int httpErrorCode) {

                navPagerViewModel.showLoadingUploadProgress.set(false);
                navPagerViewModel.showConnectServerFailed.set(true);

                navPagerViewModel.showUploadProgress.set(false);

                if (httpErrorCode != -1)
                    showCustomErrorCode(Util.CUSTOM_ERROR_CODE_HEAD + Util.CUSTOM_ERROR_CODE_GET_FOLDER + httpErrorCode);

                binding.setViewModel(navPagerViewModel);
            }

            @Override
            public void onCreateFolderFail(int httpErrorCode) {

                navPagerViewModel.showLoadingUploadProgress.set(false);
                navPagerViewModel.showConnectServerFailed.set(true);

                navPagerViewModel.showUploadProgress.set(false);

                if (httpErrorCode != -1)
                    showCustomErrorCode(Util.CUSTOM_ERROR_CODE_HEAD + Util.CUSTOM_ERROR_CODE_CREATE_FOLDER + httpErrorCode);

                binding.setViewModel(navPagerViewModel);
            }

            @Override
            public void onUploadMediaFail(int httpErrorCode) {

                if (httpErrorCode != -1)
                    showCustomErrorCode(Util.CUSTOM_ERROR_CODE_HEAD + Util.CUSTOM_ERROR_CODE_UPLOAD_MEDIA + httpErrorCode);

                binding.setViewModel(navPagerViewModel);
            }

            @Override
            public void onGetUploadMediaCountFail(int httpErrorCode) {

                navPagerViewModel.showLoadingUploadProgress.set(false);
                navPagerViewModel.showConnectServerFailed.set(true);

                navPagerViewModel.showUploadProgress.set(false);

                if (httpErrorCode != -1)
                    showCustomErrorCode(Util.CUSTOM_ERROR_CODE_HEAD + Util.CUSTOM_ERROR_CODE_GET_UPLOADED_MEDIA + httpErrorCode);

                binding.setViewModel(navPagerViewModel);
            }
        };

        uploadMediaUseCase = InjectUploadMediaUseCase.provideUploadMediaUseCase(this);

    }

    private void calcAlreadyUploadMediaCount(final List<Media> medias) {

        if (medias == null) {

            Log.w(TAG, "calc already upload medias", new NullPointerException());

            return;

        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                int alreadyUploadMediaCount = 0;
                int totalUploadMediaCount = 0;

                /*for (Media media : medias) {

                    if (checkMediaIsUploadStrategy.isMediaUploaded(media))
                        alreadyUploadMediaCount++;

                    totalUploadMediaCount++;
                }*/

                alreadyUploadMediaCount = uploadMediaUseCase.getAlreadyUploadedMediaCount();
                totalUploadMediaCount = medias.size();

                mAlreadyUploadMediaCount = alreadyUploadMediaCount;
                mTotalLocalMediaCount = totalUploadMediaCount;

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                handleUploadMediaCount();

            }
        }.execute();

    }

    private void handleUploadMediaCount() {
        if (mAlreadyUploadMediaCount == mTotalLocalMediaCount) {

            navPagerViewModel.uploadMediaPercentText.set(getString(R.string.already_upload_finished));

            navPagerViewModel.uploadPercentProgress.set(100);

        } else {

            float percent = ((float) mAlreadyUploadMediaCount * 100) / ((float) mTotalLocalMediaCount);

            int roundPercent = Math.round(percent);

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

        if (mEquipmentSearchManager == null) {
            mEquipmentSearchManager = InjectEquipment.provideEquipmentSearchManager(mContext);
        }

        mEquipmentSearchManager.startDiscovery(new EquipmentSearchManager.IEquipmentDiscoveryListener() {
            @Override
            public void call(Equipment equipment) {
                handleFoundEquipment(equipment, loggedInUser);
            }
        });

    }

    private void handleFoundEquipment(Equipment createdEquipment, final LoggedInUser loggedInUser) {
        Log.d(TAG, "search equipment: loggedinuser equipment name: " + loggedInUser.getEquipmentName() + " createEquipment equipment name: " + createdEquipment.getEquipmentName());

        if (!loggedInUser.getEquipmentName().equals(createdEquipment.getEquipmentName()))
            return;

        FNAS.handleLogout();

        dismissDialog();

        stopDiscovery();

        mCustomHandler.removeMessages(DISCOVERY_TIMEOUT_MESSAGE);

        loginUseCase.loginWithLoggedInUser(loggedInUser, new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                refreshUserInNavigationView();

                navPagerViewModel.showLoadingUploadProgress.set(true);

                uploadMediaUseCase.startUploadMedia();

                if (data)
                    handleLoginWithLoggedInUserSucceed();
                else
                    handleLoginWithLoggedInUserFail();

            }

            @Override
            public void onFail(OperationResult result) {

                Log.d(TAG, "onFail: login with logged in user failed");

            }
        });

    }

    private void handleLoginWithLoggedInUserSucceed() {
        Toast.makeText(mContext, String.format(getString(R.string.success), getString(R.string.change_user)), Toast.LENGTH_SHORT).show();

        handleFinishLoginWithLoggedInUser();
    }

    private void handleLoginWithLoggedInUserFail() {
        Toast.makeText(mContext, getString(R.string.photo_auto_upload_already_close), Toast.LENGTH_SHORT).show();

        handleFinishLoginWithLoggedInUser();
    }

    private void handleFinishLoginWithLoggedInUser() {
        mediaMainFragment.refreshAllViews();

        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void stopDiscovery() {
        mEquipmentSearchManager.stopDiscovery();
    }

    private void showNeedAutoUploadDialog() {
        new AlertDialog.Builder(mContext).setMessage(getString(R.string.need_auto_upload)).setPositiveButton(getString(R.string.backup), new DialogInterface.OnClickListener() {
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

    @Override
    protected void onResume() {
        super.onResume();

        if (currentPage == PAGE_MEDIA) {
            mediaMainFragment.show();
        }

        calcAlreadyUploadMediaCount(mediaDataSourceRepository.getLocalMedia());

        uploadMediaUseCase.registerUploadMediaCountChangeListener(uploadMediaCountChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (currentPage == PAGE_MEDIA) {
            mediaMainFragment.hide();
        }

        uploadMediaUseCase.unregisterUploadMediaCountChangeListener(uploadMediaCountChangeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        InjectHttp.provideImageGifLoaderIntance().getImageLoader(mContext).cancelAllPreLoadMedia();

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
        }

    }

    private void refreshUserInNavigationView() {

        LoggedInUser loggedInUser = loggedInUserDataSource.getLoggedInUserByUserUUID(systemSettingDataSource.getCurrentLoginUserUUID());

        if (loggedInUser == null)
            return;

        User user = loggedInUser.getUser();

        String userName = user.getUserName();

        navPagerViewModel.userNameText.set(userName);

        navPagerViewModel.userAvatarText.set(user.getDefaultAvatar());
        navPagerViewModel.userAvatarBackgroundResID.set(user.getDefaultAvatarBgColorResourceId());

        mainPagePresenter.refreshUserInNavigationView(mContext, user);

        mainPagePresenter.notifyAdapterDataSetChanged();

    }

    private void checkShowAutoUploadDialog() {
        if (systemSettingDataSource.getShowAutoUploadDialog()) {
            showNeedAutoUploadDialog();
        }
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

                mainPagePresenter.refreshNavigationLoggedInUsers();
                mainPagePresenter.notifyAdapterDataSetChanged();

            } else if (resultCode == RESULT_LOGOUT) {
                handleLogoutOnClick();
            }

        } else {

            if (currentPage == PAGE_MEDIA)
                mediaMainFragment.onActivityResult(requestCode, resultCode, intent);

        }

    }

    private void handleLogoutOnClick() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                showProgressDialog(String.format(getString(R.string.operating_title), getString(R.string.logout)));

            }

            @Override
            protected Void doInBackground(Void... params) {

                FNAS.handleLogout();

//                LocalCache.clearToken(mContext);
//
//                FileDownloadManager.getInstance().clearFileDownloadItems();

                logoutUseCase.logout();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                dismissDialog();

                EquipmentSearchActivity.gotoEquipmentActivity((Activity) mContext, true);

            }

        }.execute();
    }

}
