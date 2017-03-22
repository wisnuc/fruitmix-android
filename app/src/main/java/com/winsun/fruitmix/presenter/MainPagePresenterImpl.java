package com.winsun.fruitmix.presenter;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.github.druk.rxdnssd.BonjourService;
import com.github.druk.rxdnssd.RxDnssd;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.business.DataRepository;
import com.winsun.fruitmix.business.callback.EquipmentDiscoveryCallback;
import com.winsun.fruitmix.business.callback.OperationCallback;
import com.winsun.fruitmix.business.callback.UserOperationCallback;
import com.winsun.fruitmix.contract.FileMainFragmentContract;
import com.winsun.fruitmix.contract.MainPageContract;
import com.winsun.fruitmix.contract.MediaMainFragmentContract;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.navigationItem.NavigationItemType;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.FileUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/9.
 */

public class MainPagePresenterImpl implements MainPageContract.MainPagePresenter {

    public static final String TAG = MainPagePresenterImpl.class.getSimpleName();

    private MainPageContract.MainPageView mView;
    private DataRepository mRepository;

    private MediaMainFragmentContract.MediaMainFragmentPresenter mMediaMainFragmentPresenter;
    private FileMainFragmentContract.FileMainFragmentPresenter mFileMainFragmentPresenter;

    private static final int PAGE_FILE = 1;
    private static final int PAGE_MEDIA = 0;

    private int currentPage = PAGE_MEDIA;

    private static final int TIME_INTERNAL = 2 * 1000;
    private long backPressedTimeMillis = 0;

    private List<NavigationItemType> mNavigationItemMenu;
    private List<NavigationItemType> mNavigationItemLoggedInUser;

    private List<NavigationItemType> mCurrentNavigationItems;

    private boolean mNavigationHeaderArrowExpanded = false;

    public static final int NAVIGATION_ITEM_TYPE_MENU = 0;
    public static final int NAVIGATION_ITEM_TYPE_LOGGED_IN_USER = 1;
    public static final int NAVIGATION_ITEM_TYPE_DIVIDER = 2;
    public static final int NAVIGATION_ITEM_TYPE_ACCOUNT_MANAGE = 3;

    public static final int DISCOVERY_TIMEOUT_MESSAGE = 0x1001;

    public static final int DISCOVERY_TIMEOUT_TIME = 10 * 1000;

    private CustomHandler mCustomHandler;

    private static class CustomHandler extends Handler {

        WeakReference<MainPagePresenterImpl> weakReference = null;

        CustomHandler(MainPagePresenterImpl mainPagePresenter) {
            weakReference = new WeakReference<>(mainPagePresenter);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISCOVERY_TIMEOUT_MESSAGE:
                    weakReference.get().stopDiscovery();

                    weakReference.get().dismissDialog();

                    weakReference.get().showOperateFailed();

                    weakReference.get().closeDrawer();

                    break;
                default:
            }
        }
    }

    public static final int START_ACCOUNT_MANAGE = 0x1001;

    static final int RESULT_REFRESH_LOGGED_IN_USER = 0x1002;
    static final int RESULT_FINISH_ACTIVITY = 0x1003;
    static final int RESULT_LOGOUT = 0x1004;

    private RxDnssd mRxDnssd;

    public abstract class NavigationMenuItem implements NavigationItemType {

        private int menuIconResID;
        private String menuText;

        @Override
        public int getType() {
            return NAVIGATION_ITEM_TYPE_MENU;
        }

        public NavigationMenuItem(int menuIconResID, String menuText) {
            this.menuIconResID = menuIconResID;
            this.menuText = menuText;
        }

        public void setMenuIconResID(int menuIconResID) {
            this.menuIconResID = menuIconResID;
        }

        public void setMenuText(String menuText) {
            this.menuText = menuText;
        }

        public int getMenuIconResID() {
            return menuIconResID;
        }

        public String getMenuText() {
            return menuText;
        }

        public abstract void onClick();
    }

    public class NavigationLoggerInUserItem implements NavigationItemType {

        private LoggedInUser loggedInUser;

        @Override
        public int getType() {
            return NAVIGATION_ITEM_TYPE_LOGGED_IN_USER;
        }

        public NavigationLoggerInUserItem(LoggedInUser loggedInUser) {
            this.loggedInUser = loggedInUser;
        }

        public LoggedInUser getLoggedInUser() {
            return loggedInUser;
        }

        public void onClick() {

            mView.showDialog();
            startDiscovery(loggedInUser);
            mCustomHandler.sendEmptyMessageDelayed(DISCOVERY_TIMEOUT_MESSAGE, DISCOVERY_TIMEOUT_TIME);

        }

    }

    private void startDiscovery(final LoggedInUser loggedInUser) {

        mRepository.startDiscovery(mRxDnssd, new EquipmentDiscoveryCallback() {
            @Override
            public void onEquipmentDiscovery(BonjourService bonjourService) {

                String serviceName = bonjourService.getServiceName();
                if (!serviceName.equals(loggedInUser.getEquipmentName())) return;

                if (bonjourService.getInet4Address() == null) return;

                mView.dismissDialog();
                stopDiscovery();

                mCustomHandler.removeMessages(DISCOVERY_TIMEOUT_MESSAGE);

                final String hostAddress = bonjourService.getInet4Address().getHostAddress();

                mRepository.logout(new OperationCallback() {
                    @Override
                    public void onOperationSucceed(OperationResult result) {

                        String gateway = "http://" + hostAddress;
                        User user = loggedInUser.getUser();

                        mRepository.insertGatewayToMemory(gateway);
                        mRepository.insertLoginUserUUIDToMemory(user.getUuid());
                        mRepository.insertTokenToMemory(loggedInUser.getToken());
                        mRepository.insertDeviceIDToMemory(loggedInUser.getDeviceID());

                        mRepository.insertDeviceIDToDB(loggedInUser.getDeviceID());
                        mRepository.insertTokenToDB(loggedInUser.getToken());
                        mRepository.insertLoginUserUUIDToDB(user.getUuid());
                        mRepository.insertGatewayToDB(gateway);

                        if (!mRepository.checkAutoUpload())
                            mView.showAutoUploadAlreadyClose();

                        closeDrawer();

                        mRepository.loadUsersInThread(null);
                        mRepository.loadMediasInThread(null);
                        mRepository.loadMediaSharesInThread(null);

                        // refresh slide menu
                        toggleUserManageNavigationItem(user);
                        mView.refreshUserInNavigationView(user);
                        refreshLoggedInUserNavigationItem();

                        mMediaMainFragmentPresenter.onCreate(null);
                        mMediaMainFragmentPresenter.onCreateView();

                        mFileMainFragmentPresenter.onResume();

                    }
                });


            }
        });

    }


    private void stopDiscovery() {
        mRepository.stopDiscovery();
    }

    private void dismissDialog() {
        mView.dismissDialog();
    }

    private void showOperateFailed() {
        mView.showOperateFailed();
    }

    public class NavigationDividerItem implements NavigationItemType {

        @Override
        public int getType() {
            return NAVIGATION_ITEM_TYPE_DIVIDER;
        }
    }

    public class NavigationAccountManageItem implements NavigationItemType {

        public int getItemTextResID() {
            return R.string.account_manage;
        }

        @Override
        public int getType() {
            return NAVIGATION_ITEM_TYPE_ACCOUNT_MANAGE;
        }

        public void onClick() {

            mView.gotoAccountManageActivity();
        }

    }

    private int mAlreadyUploadMediaCount = -1;
    private int mTotalLocalMediaCount = 0;

    public MainPagePresenterImpl(DataRepository repository, RxDnssd rxDnssd) {
        mRepository = repository;

        mRxDnssd = rxDnssd;

        mNavigationItemMenu = new ArrayList<>();
        mNavigationItemLoggedInUser = new ArrayList<>();

        mCustomHandler = new CustomHandler(this);

    }

    @Override
    public void initNavigationItemMenu() {

        mNavigationItemMenu.add(new NavigationMenuItem(R.drawable.ic_folder, mView.getString(R.string.my_file)) {
            @Override
            public void onClick() {
                onFileNavigationItemSelected();
            }
        });
        mNavigationItemMenu.add(new NavigationDividerItem());
        mNavigationItemMenu.add(new NavigationMenuItem(R.drawable.ic_settings, mView.getString(R.string.setting)) {
            @Override
            public void onClick() {
                mView.gotoSettingActivity();
            }
        });
        mNavigationItemMenu.add(new NavigationMenuItem(R.drawable.logout, mView.getString(R.string.logout)) {
            @Override
            public void onClick() {
                onLogoutNavigationItemSelected();
            }
        });

        mCurrentNavigationItems = mNavigationItemMenu;

        mView.setNavigationItemTypes(mCurrentNavigationItems);
    }

    @Override
    public void setMediaMainFragmentPresenter(MediaMainFragmentContract.MediaMainFragmentPresenter presenter) {
        mMediaMainFragmentPresenter = presenter;
    }

    @Override
    public void setFileMainFragmentPresenter(FileMainFragmentContract.FileMainFragmentPresenter presenter) {
        mFileMainFragmentPresenter = presenter;
    }

    private void onUserManageNavigationItemSelected() {
        mView.gotoUserManageActivity();
    }

    private void onLogoutNavigationItemSelected() {

        mView.showDialog();

        mRepository.logout(new OperationCallback() {
            @Override
            public void onOperationSucceed(OperationResult result) {

                if (mView == null)
                    return;

                mView.dismissDialog();

                mView.gotoEquipmentActivity();
                mView.finishActivity();
            }
        });

    }

    private void onFileNavigationItemSelected() {

        NavigationMenuItem item = (NavigationMenuItem) mNavigationItemMenu.get(0);

        if (currentPage == PAGE_MEDIA) {

            currentPage = PAGE_FILE;

            item.setMenuText(mView.getString(R.string.my_photo));
            item.setMenuIconResID(R.drawable.ic_photo_black);

            mView.hideMediaAndShowFileFragment();

            mRepository.pauseTimingRetrieveMediaShare();

            mFileMainFragmentPresenter.setHidden(false);
            mMediaMainFragmentPresenter.setHidden(true);

            mFileMainFragmentPresenter.onResume();

        } else {

            currentPage = PAGE_MEDIA;


            item.setMenuText(mView.getString(R.string.my_file));
            item.setMenuIconResID(R.drawable.ic_folder);

            mView.showMediaAndHideFileFragment();

            mRepository.resumeTimingRetrieveMediaShare();

            mMediaMainFragmentPresenter.setHidden(false);
            mFileMainFragmentPresenter.setHidden(true);
        }

    }

    @Override
    public void switchDrawerOpenState() {
        mView.switchDrawerOpenState();
    }

    @Override
    public void lockDrawer() {
        mView.lockDrawer();
    }

    @Override
    public void unlockDrawer() {
        mView.unlockDrawer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult: " + (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ? "true" : "false"));

        if (currentPage == PAGE_FILE)
            mFileMainFragmentPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

        if (currentPage == PAGE_MEDIA && mMediaMainFragmentPresenter.isResumed())
            mMediaMainFragmentPresenter.onActivityReenter(resultCode, data);
    }

    @Override
    public void closeDrawer() {
        mView.closeDrawer();
    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        if (currentPage == PAGE_MEDIA)
            mMediaMainFragmentPresenter.onMapSharedElements(names, sharedElements);
    }

    @Override
    public void attachView(MainPageContract.MainPageView view) {
        mView = view;
    }

    @Override
    public void detachView() {

        mView.dismissDialog();

        mRepository.stopUpload();

        mView = null;
    }

    @Override
    public void calcAlreadyUploadMediaCount() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                mAlreadyUploadMediaCount = mRepository.getAlreadyUploadMediaCount();
                mTotalLocalMediaCount = mRepository.getTotalMediaCount();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if (mAlreadyUploadMediaCount == mTotalLocalMediaCount) {
                    mView.setUploadMediaPercentText(mView.getString(R.string.already_upload_finished));
                } else {

                    float percent = mAlreadyUploadMediaCount * 100 / mTotalLocalMediaCount;

                    String percentText = (int) percent + "%";

                    mView.setUploadMediaPercentText(String.format(mView.getString(R.string.already_upload_media_percent_text), percentText));
                }

            }
        }.execute();

    }

    @Override
    public void loadCurrentUser() {

        mRepository.loadCurrentLoginUser(new UserOperationCallback.LoadCurrentUserCallback() {
            @Override
            public void onLoadSucceed(OperationResult operationResult, User user) {

                if (mView == null) return;

                Log.d(TAG, "onLoadSucceed: load current login user");

                toggleUserManageNavigationItem(user);

                mView.refreshUserInNavigationView(user);

                refreshLoggedInUserNavigationItem();
            }

            @Override
            public void onLoadFail(OperationResult operationResult) {

            }
        });

    }

    private void toggleUserManageNavigationItem(User user) {
        if (user.isAdmin()) {

            if (mNavigationItemMenu.get(1).getType() != NAVIGATION_ITEM_TYPE_MENU) {
                mNavigationItemMenu.add(1, new NavigationMenuItem(R.drawable.ic_person_add, mView.getString(R.string.user_manage)) {
                    @Override
                    public void onClick() {
                        onUserManageNavigationItemSelected();
                    }
                });
            }

        } else {

            if (mNavigationItemMenu.get(1).getType() == NAVIGATION_ITEM_TYPE_MENU)
                mNavigationItemMenu.remove(1);
        }
    }

    private void refreshLoggedInUserNavigationItem() {

        String userUUID = mRepository.loadCurrentLoginUserUUIDInMemory();

        List<LoggedInUser> loggedInUsers = mRepository.loadLoggedInUserInMemory();

        if (loggedInUsers == null) return;

        Iterator<LoggedInUser> iterator = loggedInUsers.iterator();
        while (iterator.hasNext()) {
            LoggedInUser loggedInUser = iterator.next();
            if (loggedInUser.getUser().getUuid().equals(userUUID)) {
                iterator.remove();
            }
        }

        int loggedInUserListSize = loggedInUsers.size();
        User user0;
        User user1;

        if (loggedInUserListSize > 0) {

            mNavigationItemLoggedInUser.clear();
            for (LoggedInUser loggedInUser : loggedInUsers) {
                mNavigationItemLoggedInUser.add(new NavigationLoggerInUserItem(loggedInUser));
            }
            mNavigationItemLoggedInUser.add(new NavigationAccountManageItem());

            user0 = loggedInUsers.get(0).getUser();

            mView.setLoggedInUser0AvatarVisibility(View.VISIBLE);
            mView.setLoggedInUser0AvatarText(user0.getDefaultAvatar());
            mView.setLoggedInUser0AvatarBackgroundResource(user0.getDefaultAvatarBgColorResourceId());
            mView.setLoggedInUser0AvatarOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((NavigationLoggerInUserItem) mNavigationItemLoggedInUser.get(0)).onClick();
                }
            });

            if (loggedInUserListSize > 1) {
                user1 = loggedInUsers.get(1).getUser();

                mView.setLoggedInUser1AvatarVisibility(View.VISIBLE);
                mView.setLoggedInUser1AvatarText(user1.getDefaultAvatar());
                mView.setLoggedInUser1AvatarBackgroundResource(user1.getDefaultAvatarBgColorResourceId());
                mView.setLoggedInUser1AvatarOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((NavigationLoggerInUserItem) mNavigationItemLoggedInUser.get(1)).onClick();
                    }
                });

            } else {

                mView.setLoggedInUser1AvatarVisibility(View.INVISIBLE);

            }

        } else {

            mView.setLoggedInUser0AvatarVisibility(View.INVISIBLE);
            mView.setLoggedInUser1AvatarVisibility(View.INVISIBLE);

            mNavigationItemLoggedInUser.clear();
            mNavigationItemLoggedInUser.add(new NavigationAccountManageItem());
        }

        mView.setNavigationHeaderArrowVisibility(View.VISIBLE);
        mView.setNavigationHeaderArrowOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNavigationHeaderArrowExpanded = !mNavigationHeaderArrowExpanded;
                refreshNavigationHeader();
            }
        });

    }

    private void refreshNavigationHeader() {
        if (mNavigationHeaderArrowExpanded) {

            mView.setNavigationHeaderArrowImageResource(R.drawable.navigation_header_arrow_down);

            if (mView.getLoggedInUser1AvatarVisibility() == View.VISIBLE) {
                mView.setLoggedInUser1AvatarVisibility(View.INVISIBLE);
            }
            if (mView.getLoggedInUser0AvatarVisibility() == View.VISIBLE) {
                mView.setLoggedInUser0AvatarVisibility(View.INVISIBLE);
            }

            mCurrentNavigationItems = mNavigationItemLoggedInUser;
            mView.setNavigationItemTypes(mCurrentNavigationItems);

        } else {

            mView.setNavigationHeaderArrowImageResource(R.drawable.navigation_header_arrow_up);

            int size = mNavigationItemLoggedInUser.size();

            if (mView.getLoggedInUser0AvatarVisibility() == View.INVISIBLE && size > 1) {
                mView.setLoggedInUser0AvatarVisibility(View.VISIBLE);
            }
            if (mView.getLoggedInUser1AvatarVisibility() == View.INVISIBLE && size > 2) {
                mView.setLoggedInUser1AvatarVisibility(View.VISIBLE);
            }

            mCurrentNavigationItems = mNavigationItemMenu;
            mView.setNavigationItemTypes(mCurrentNavigationItems);

        }
    }


    @Override
    public void handleBackEvent() {

        if (mView.isDrawerOpen()) {

            mView.closeDrawer();

        } else if (currentPage == PAGE_FILE) {
            if (mFileMainFragmentPresenter.handleBackPressedOrNot()) {
                mFileMainFragmentPresenter.handleBackEvent();
            } else {
                finishApp();
            }
        } else if (currentPage == PAGE_MEDIA) {

            if (mMediaMainFragmentPresenter.handleBackPressedOrNot()) {
                mMediaMainFragmentPresenter.handleBackEvent();
            } else {
                finishApp();
            }

        } else {
            finishApp();
        }
    }

    private void finishApp() {

        if (System.currentTimeMillis() - backPressedTimeMillis < TIME_INTERNAL) {

            mRepository.stopTimingRetrieveMediaShare();

            mView.finishActivity();
        } else {
            mView.showFinishAppToast();
        }

        backPressedTimeMillis = System.currentTimeMillis();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == START_ACCOUNT_MANAGE) {

            Log.i(TAG, "onActivityResult: requestCode == START_ACCOUNT_MANAGE");

            Log.i(TAG, "onActivityResult: resultCode" + resultCode);

            if (resultCode == RESULT_FINISH_ACTIVITY) {
                mView.finishActivity();
            } else if (resultCode == RESULT_REFRESH_LOGGED_IN_USER) {
                refreshLoggedInUserNavigationItem();
                mView.setNavigationItemTypes(mCurrentNavigationItems);
            } else if (resultCode == RESULT_LOGOUT) {
                onLogoutNavigationItemSelected();
            }

        } else {

            if (currentPage == PAGE_MEDIA)
                mMediaMainFragmentPresenter.handleOnActivityResult(requestCode, resultCode, data);

        }

    }
}
