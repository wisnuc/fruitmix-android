package com.winsun.fruitmix;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.druk.rxdnssd.BonjourService;
import com.github.druk.rxdnssd.RxDnssd;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.fragment.FileMainFragment;
import com.winsun.fruitmix.fragment.MediaMainFragment;
import com.winsun.fruitmix.interfaces.OnMainFragmentInteractionListener;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class NavPagerActivity extends AppCompatActivity
        implements OnMainFragmentInteractionListener {

    public static final String TAG = NavPagerActivity.class.getSimpleName();

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.frame_layout)
    FrameLayout mFrameLayout;
    @BindView(R.id.version_name)
    TextView versionName;
    @BindView(R.id.avatar)
    TextView mUserAvatar;
    @BindView(R.id.user_name_textview)
    TextView mUserNameTextView;
    @BindView(R.id.logged_in_user0_avatar)
    TextView mLoggedInUser0Avatar;
    @BindView(R.id.logged_in_user1_avatar)
    TextView mLoggedInUser1Avatar;
    @BindView(R.id.navigation_header_arrow)
    ImageView mNavigationHeaderArrow;
    @BindView(R.id.navigation_menu_recycler_view)
    RecyclerView mNavigationMenuRecyclerView;

    private Context mContext;

    private ProgressDialog mDialog;

    private MediaMainFragment mediaMainFragment;
    private FileMainFragment fileMainFragment;
    private FragmentManager fragmentManager;

    private NavigationItemAdapter mNavigationItemAdapter;

    private boolean mNavigationHeaderArrowExpanded = false;

    private RxDnssd mRxDnssd;
    private Subscription mSubscription;

    private static final String SERVICE_PORT = "_http._tcp";
    private static final String DEMAIN = "local.";

    public static final int PAGE_FILE = 1;
    public static final int PAGE_MEDIA = 0;

    private int currentPage = 0;

    public static final int START_ACCOUNT_MANAGE = 0x1001;

    public static final int RESULT_REFRESH_LOGGED_IN_USER = 0x1002;
    public static final int RESULT_FINISH_ACTIVITY = 0x1003;
    public static final int RESULT_LOGOUT = 0x1004;

    private static final int TIME_INTERNAL = 2 * 1000;
    private long backPressedTimeMillis = 0;

    private SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            if (currentPage == PAGE_MEDIA)
                mediaMainFragment.onMapSharedElements(names, sharedElements);
        }
    };

    private static final int NAVIGATION_ITEM_TYPE_MENU = 0;
    private static final int NAVIGATION_ITEM_TYPE_LOGGED_IN_USER = 1;
    private static final int NAVIGATION_ITEM_TYPE_DIVIDER = 2;
    public static final int NAIGATION_ITEM_TYPE_ACCOUNT_MANAGE = 3;


    private interface NavigationItemType {
        int getType();
    }

    private abstract class NavigationMenuItem implements NavigationItemType {

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

    private class NavigationLoggerInUserItem implements NavigationItemType {

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

            mDialog = ProgressDialog.show(mContext, null, getString(R.string.operating_title), true, false);
            startDiscovery(loggedInUser);
            mCustomHandler.sendEmptyMessageDelayed(DISCOVERY_TIMEOUT_MESSAGE, DISCOVERY_TIMEOUT_TIME);
        }

    }

    private class NavigationDividerItem implements NavigationItemType {

        @Override
        public int getType() {
            return NAVIGATION_ITEM_TYPE_DIVIDER;
        }
    }

    private class NavigationAccountManageItem implements NavigationItemType {

        int getItemTextResID() {
            return R.string.account_manage;
        }

        @Override
        public int getType() {
            return NAIGATION_ITEM_TYPE_ACCOUNT_MANAGE;
        }

        public void onClick() {

            startActivityForResult(new Intent(mContext, AccountManageActivity.class), START_ACCOUNT_MANAGE);
        }

    }


    private List<NavigationItemType> mNavigationItemMenu;
    private List<NavigationItemType> mNavigationItemLoggedInUser;

    public static final int DISCOVERY_TIMEOUT_MESSAGE = 0x1001;

    public static final int DISCOVERY_TIMEOUT_TIME = 10 * 1000;

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

                    weakReference.get().mDialog.dismiss();

                    Toast.makeText(weakReference.get(), "操作失败", Toast.LENGTH_SHORT).show();

                    weakReference.get().mDrawerLayout.closeDrawer(GravityCompat.START);

                    break;
                default:
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_pager);

        mContext = this;

        setExitSharedElementCallback(sharedElementCallback);

        ButterKnife.bind(NavPagerActivity.this);

/*        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();*/

        mNavigationItemMenu = new ArrayList<>();
        mNavigationItemLoggedInUser = new ArrayList<>();

        initNavigationMenuRecyclerView();

        initNavigationItemMenu();

        refreshUserInNavigationView();

        mNavigationItemAdapter.setNavigationItemTypes(mNavigationItemMenu);
        mNavigationItemAdapter.notifyDataSetChanged();

        versionName.setText(String.format(getString(R.string.android_version_name), Util.getVersionName(mContext)));

        mediaMainFragment = MediaMainFragment.newInstance();
        fileMainFragment = FileMainFragment.newInstance();

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.frame_layout, mediaMainFragment).add(R.id.frame_layout, fileMainFragment).hide(fileMainFragment).commit();

        currentPage = PAGE_MEDIA;

        mCustomHandler = new CustomHandler(this);

        Log.d(TAG, "onCreate: ");
    }

    private void initNavigationMenuRecyclerView() {
        mNavigationItemAdapter = new NavigationItemAdapter();
        mNavigationMenuRecyclerView.setAdapter(mNavigationItemAdapter);
        mNavigationMenuRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mNavigationMenuRecyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    private void initNavigationItemMenu() {

        mNavigationItemMenu.add(new NavigationMenuItem(R.drawable.ic_folder, getString(R.string.my_file)) {
            @Override
            public void onClick() {
                toggleFileOrMediaFragment();
            }
        });
        mNavigationItemMenu.add(new NavigationDividerItem());
        mNavigationItemMenu.add(new NavigationMenuItem(R.drawable.ic_settings, getString(R.string.setting)) {
            @Override
            public void onClick() {
                Intent intent = new Intent(NavPagerActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        mNavigationItemMenu.add(new NavigationMenuItem(R.drawable.logout, getString(R.string.logout)) {
            @Override
            public void onClick() {
                handleLogoutOnClick();
            }
        });
    }

    private void startDiscovery(final LoggedInUser loggedInUser) {

        if (mRxDnssd == null)
            mRxDnssd = CustomApplication.getRxDnssd(mContext);

        mSubscription = mRxDnssd.browse(SERVICE_PORT, DEMAIN)
                .compose(mRxDnssd.resolve())
                .compose(mRxDnssd.queryRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BonjourService>() {
                    @Override
                    public void call(BonjourService bonjourService) {

                        if (bonjourService.isLost()) return;

                        String serviceName = bonjourService.getServiceName();
                        if (!serviceName.equals(loggedInUser.getEquipmentName())) return;

                        if (bonjourService.getInet4Address() == null) return;

                        mDialog.dismiss();
                        stopDiscovery();

                        mCustomHandler.removeMessages(DISCOVERY_TIMEOUT_MESSAGE);

                        String hostAddress = bonjourService.getInet4Address().getHostAddress();

                        FNAS.Gateway = "http://" + hostAddress;
                        FNAS.userUUID = loggedInUser.getUser().getUuid();
                        FNAS.JWT = loggedInUser.getToken();
                        LocalCache.DeviceID = loggedInUser.getDeviceID();

                        LocalCache.saveToken(mContext, FNAS.JWT);
                        LocalCache.saveGateway(mContext, FNAS.Gateway);
                        LocalCache.saveUserUUID(mContext, FNAS.userUUID);
                        LocalCache.SetGlobalData(mContext, Util.DEVICE_ID_MAP_NAME, LocalCache.DeviceID);

                        EventBus.getDefault().post(new RequestEvent(OperationType.STOP_UPLOAD, null));

                        ButlerService.stopTimingRetrieveMediaShare();

                        FNAS.retrieveUser(mContext);

                        checkAutoUpload();

                        Util.setRemoteMediaShareLoaded(false);
                        Util.setRemoteMediaLoaded(false);
                        mediaMainFragment.refreshAllViews();

                        mDrawerLayout.closeDrawer(GravityCompat.START);
                    }
                });

    }

    private void checkAutoUpload() {
        for (LoggedInUser loggedInUser : LocalCache.LocalLoggedInUsers) {

            if (loggedInUser.getUser().getUuid().equals(FNAS.userUUID)) {
                if (!LocalCache.getCurrentUploadDeviceID(mContext).equals(LocalCache.DeviceID)) {
                    LocalCache.setAutoUploadOrNot(mContext, false);
                    Toast.makeText(mContext, getString(R.string.photo_auto_upload_already_close), Toast.LENGTH_SHORT).show();
                } else {
                    LocalCache.setAutoUploadOrNot(mContext, true);
                }
            }
        }
    }

    private void stopDiscovery() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ExecutorServiceInstance.SINGLE_INSTANCE.shutdownFixedThreadPoolNow();

        mContext = null;

        Log.d(TAG, "onDestroy: ");
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
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        if (action.equals(Util.REFRESH_VIEW_AFTER_DATA_RETRIEVED)) {

            Log.i(TAG, "handleOperationEvent: refreshUser");

            EventBus.getDefault().removeStickyEvent(operationEvent);

            refreshUserInNavigationView();
            mNavigationItemAdapter.notifyDataSetChanged();

            if (operationEvent.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

                if (currentPage == PAGE_MEDIA) {
                    mediaMainFragment.refreshUser();
                } else {
                    fileMainFragment.refreshUser();
                }

            }
        }

    }

    private void refreshUserInNavigationView() {

        User user;

        if (FNAS.userUUID != null && LocalCache.RemoteUserMapKeyIsUUID != null && LocalCache.RemoteUserMapKeyIsUUID.containsKey(FNAS.userUUID)) {
            user = LocalCache.RemoteUserMapKeyIsUUID.get(FNAS.userUUID);
        } else {
            user = LocalCache.getUser(mContext);
        }

        String userName = user.getUserName();

        mUserNameTextView.setText(userName);

        mUserAvatar.setText(user.getDefaultAvatar());
        mUserAvatar.setBackgroundResource(user.getDefaultAvatarBgColorResourceId());

        toggleUserManageNavigationItem(user);

        refreshLoggedInUserNavigationItem();

    }

    private void refreshLoggedInUserNavigationItem() {

        List<LoggedInUser> loggedInUsers = new ArrayList<>(LocalCache.LocalLoggedInUsers);

        Iterator<LoggedInUser> iterator = loggedInUsers.iterator();
        while (iterator.hasNext()) {
            LoggedInUser loggedInUser = iterator.next();
            if (loggedInUser.getUser().getUuid().equals(FNAS.userUUID)) {
                iterator.remove();
            }
        }

        int loggedInUserListSize = loggedInUsers.size();
        User user0;
        User user1;

        if (loggedInUserListSize > 0) {

            mNavigationHeaderArrow.setVisibility(View.VISIBLE);

            mNavigationHeaderArrowExpanded = false;
            refreshNavigationHeader();

            mNavigationHeaderArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNavigationHeaderArrowExpanded = !mNavigationHeaderArrowExpanded;
                    refreshNavigationHeader();
                }
            });

            mNavigationItemLoggedInUser.clear();
            for (LoggedInUser loggedInUser : loggedInUsers) {
                mNavigationItemLoggedInUser.add(new NavigationLoggerInUserItem(loggedInUser));
            }
            mNavigationItemLoggedInUser.add(new NavigationAccountManageItem());

            user0 = loggedInUsers.get(0).getUser();

            mLoggedInUser0Avatar.setVisibility(View.VISIBLE);
            mLoggedInUser0Avatar.setText(user0.getDefaultAvatar());
            mLoggedInUser0Avatar.setBackgroundResource(user0.getDefaultAvatarBgColorResourceId());
            mLoggedInUser0Avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((NavigationLoggerInUserItem) mNavigationItemLoggedInUser.get(0)).onClick();
                }
            });

            if (loggedInUserListSize > 1) {
                user1 = loggedInUsers.get(1).getUser();

                mLoggedInUser1Avatar.setVisibility(View.VISIBLE);
                mLoggedInUser1Avatar.setText(user1.getDefaultAvatar());
                mLoggedInUser1Avatar.setBackgroundResource(user1.getDefaultAvatarBgColorResourceId());

                mLoggedInUser1Avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ((NavigationLoggerInUserItem) mNavigationItemLoggedInUser.get(1)).onClick();

                    }
                });

            } else {
                mLoggedInUser1Avatar.setVisibility(View.GONE);
            }

        } else {
            mNavigationHeaderArrow.setVisibility(View.GONE);
            mLoggedInUser0Avatar.setVisibility(View.GONE);
            mLoggedInUser1Avatar.setVisibility(View.GONE);
        }
    }

    private void refreshNavigationHeader() {
        if (mNavigationHeaderArrowExpanded) {

            mNavigationHeaderArrow.setImageResource(R.drawable.navigation_header_arrow_down);

            if (mLoggedInUser1Avatar.getVisibility() == View.VISIBLE) {
                mLoggedInUser1Avatar.setVisibility(View.INVISIBLE);
            }
            if (mLoggedInUser0Avatar.getVisibility() == View.VISIBLE) {
                mLoggedInUser0Avatar.setVisibility(View.INVISIBLE);
            }

            mNavigationItemAdapter.setNavigationItemTypes(mNavigationItemLoggedInUser);
            mNavigationItemAdapter.notifyDataSetChanged();


        } else {

            mNavigationHeaderArrow.setImageResource(R.drawable.navigation_header_arrow_up);

            if (mLoggedInUser1Avatar.getVisibility() == View.INVISIBLE) {
                mLoggedInUser1Avatar.setVisibility(View.VISIBLE);
            }
            if (mLoggedInUser0Avatar.getVisibility() == View.INVISIBLE) {
                mLoggedInUser0Avatar.setVisibility(View.VISIBLE);
            }

            mNavigationItemAdapter.setNavigationItemTypes(mNavigationItemMenu);
            mNavigationItemAdapter.notifyDataSetChanged();

        }
    }

    private void toggleUserManageNavigationItem(User user) {
        if (user.isAdmin()) {

            if (mNavigationItemMenu.get(1).getType() != NAVIGATION_ITEM_TYPE_MENU) {
                mNavigationItemMenu.add(1, new NavigationMenuItem(R.drawable.ic_person_add, getString(R.string.user_manage)) {
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(NavPagerActivity.this, UserManageActivity.class);
                        startActivity(intent);
                    }
                });
            }

        } else {

            if (mNavigationItemMenu.get(1).getType() == NAVIGATION_ITEM_TYPE_MENU)
                mNavigationItemMenu.remove(1);
        }
    }

    private void showExplanation(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
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

        if (currentPage == PAGE_FILE)
            fileMainFragment.requestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (currentPage == PAGE_MEDIA && mediaMainFragment.isResumed())
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

        if (currentPage == PAGE_FILE) {
            if (fileMainFragment.handleBackPressedOrNot()) {
                fileMainFragment.handleBackPressed();
            } else if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                finishApp();
            }
        } else if (currentPage == PAGE_MEDIA) {

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

        if (System.currentTimeMillis() - backPressedTimeMillis < TIME_INTERNAL) {

            ButlerService.stopButlerService(mContext);

            super.onBackPressed();
        } else {

            Toast.makeText(mContext, getString(R.string.android_finishAppToast), Toast.LENGTH_SHORT).show();
        }

        backPressedTimeMillis = System.currentTimeMillis();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == START_ACCOUNT_MANAGE) {

            if (resultCode == RESULT_FINISH_ACTIVITY) {
                finish();
            } else if (resultCode == RESULT_REFRESH_LOGGED_IN_USER) {
                refreshLoggedInUserNavigationItem();
                mNavigationItemAdapter.notifyDataSetChanged();
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

                mDialog = ProgressDialog.show(mContext, null, getString(R.string.operating_title), true, false);

            }

            @Override
            protected Void doInBackground(Void... params) {

                LocalCache.clearToken(mContext);
                EventBus.getDefault().post(new RequestEvent(OperationType.STOP_UPLOAD, null));

                ButlerService.stopTimingRetrieveMediaShare();

                Util.setRemoteMediaLoaded(false);
                Util.setRemoteMediaShareLoaded(false);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mDialog.dismiss();

                Intent intent = new Intent(NavPagerActivity.this, EquipmentSearchActivity.class);
                intent.putExtra(Util.KEY_SHOULD_STOP_SERVICE, true);
                startActivity(intent);
                finish();

            }

        }.execute();
    }

    private void toggleFileOrMediaFragment() {

        NavigationMenuItem item = (NavigationMenuItem) mNavigationItemMenu.get(0);

        if (currentPage == PAGE_MEDIA) {

            currentPage = PAGE_FILE;

            item.setMenuText(getString(R.string.my_photo));
            item.setMenuIconResID(R.drawable.ic_photo_black);

            fragmentManager.beginTransaction().hide(mediaMainFragment).show(fileMainFragment).commit();

            ButlerService.stopTimingRetrieveMediaShare();

        } else {

            currentPage = PAGE_MEDIA;

            item.setMenuText(getString(R.string.my_file));
            item.setMenuIconResID(R.drawable.ic_folder);

            fragmentManager.beginTransaction().hide(fileMainFragment).show(mediaMainFragment).commit();

        }
        mNavigationItemAdapter.notifyItemChanged(0);

    }

    private class NavigationItemAdapter extends RecyclerView.Adapter<BaseNavigationViewHolder> {


        private List<NavigationItemType> mNavigationItemTypes;

        public void setNavigationItemTypes(List<NavigationItemType> navigationItemTypes) {
            mNavigationItemTypes = navigationItemTypes;
        }

        @Override
        public BaseNavigationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;

            switch (viewType) {
                case NAVIGATION_ITEM_TYPE_DIVIDER:

                    view = LayoutInflater.from(mContext).inflate(R.layout.navigation_divider_item, parent, false);

                    return new NavigationDividerViewHolder(view);

                case NAVIGATION_ITEM_TYPE_LOGGED_IN_USER:

                    view = LayoutInflater.from(mContext).inflate(R.layout.navigation_logged_in_user_item, parent, false);

                    return new NavigationLoggedInUserViewHolder(view);

                case NAVIGATION_ITEM_TYPE_MENU:
                    view = LayoutInflater.from(mContext).inflate(R.layout.navigation_menu_item, parent, false);
                    return new NavigationMenuViewHolder(view);

                case NAIGATION_ITEM_TYPE_ACCOUNT_MANAGE:
                    view = LayoutInflater.from(mContext).inflate(R.layout.navigation_logged_in_user_item, parent, false);

                    return new NavigationAccountItemViewHolder(view);
            }

            return null;
        }


        @Override
        public void onBindViewHolder(BaseNavigationViewHolder holder, int position) {

            holder.refreshView(mNavigationItemTypes.get(position));
        }

        @Override
        public int getItemViewType(int position) {
            return mNavigationItemTypes.get(position).getType();
        }

        @Override
        public int getItemCount() {
            return mNavigationItemTypes.size();
        }
    }

    private abstract class BaseNavigationViewHolder extends RecyclerView.ViewHolder {

        BaseNavigationViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void refreshView(NavigationItemType type);
    }

    class NavigationMenuViewHolder extends BaseNavigationViewHolder {

        @BindView(R.id.menu_icon)
        ImageView menuIcon;
        @BindView(R.id.menu_text)
        TextView menuTextView;
        @BindView(R.id.menu_layout)
        ViewGroup menuLayout;

        NavigationMenuViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void refreshView(NavigationItemType type) {

            final NavigationMenuItem item = (NavigationMenuItem) type;

            menuIcon.setImageResource(item.getMenuIconResID());
            menuTextView.setText(item.getMenuText());
            menuLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.onClick();

                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
            });
        }
    }

    class NavigationLoggedInUserViewHolder extends BaseNavigationViewHolder {

        @BindView(R.id.avatar)
        TextView avatar;
        @BindView(R.id.item_title)
        TextView itemTitle;
        @BindView(R.id.item_sub_title)
        TextView itemSubTitle;
        @BindView(R.id.logged_in_user_item_layout)
        ViewGroup itemLayout;

        NavigationLoggedInUserViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void refreshView(NavigationItemType type) {

            final NavigationLoggerInUserItem loggerInUserItem = (NavigationLoggerInUserItem) type;

            LoggedInUser loggedInUser = loggerInUserItem.getLoggedInUser();
            User user = loggedInUser.getUser();
            avatar.setText(user.getDefaultAvatar());
            avatar.setBackgroundResource(user.getDefaultAvatarBgColorResourceId());
            itemTitle.setText(user.getUserName());

            itemSubTitle.setText(loggedInUser.getEquipmentName());

            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loggerInUserItem.onClick();

                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
            });
        }
    }

    private class NavigationDividerViewHolder extends BaseNavigationViewHolder {


        NavigationDividerViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void refreshView(NavigationItemType type) {

        }
    }

    private class NavigationAccountItemViewHolder extends BaseNavigationViewHolder {

        @BindView(R.id.item_title)
        TextView itemTitle;
        @BindView(R.id.item_sub_title)
        TextView itemSubTitle;
        @BindView(R.id.logged_in_user_item_layout)
        ViewGroup itemLayout;

        NavigationAccountItemViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void refreshView(NavigationItemType type) {

            final NavigationAccountManageItem item = (NavigationAccountManageItem) type;

            itemTitle.setText(item.getItemTextResID());
            itemSubTitle.setVisibility(View.GONE);

            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.onClick();
                }
            });
        }
    }
}
