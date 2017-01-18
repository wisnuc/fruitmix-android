package com.winsun.fruitmix;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.fragment.FileMainFragment;
import com.winsun.fruitmix.fragment.MediaMainFragment;
import com.winsun.fruitmix.interfaces.OnMainFragmentInteractionListener;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavPagerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMainFragmentInteractionListener {

    public static final String TAG = NavPagerActivity.class.getSimpleName();

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.version_name)
    TextView versionName;

    private Context mContext;

    private ExecutorServiceInstance instance;

    private ProgressDialog mDialog;

    private MediaMainFragment mediaMainFragment;
    private FileMainFragment fileMainFragment;
    private FragmentManager fragmentManager;

    private NavigationView navigationView;

    public static final int PAGE_FILE = 1;
    public static final int PAGE_MEDIA = 0;

    private int currentPage = 0;

    private static final int TIME_INTERNAL = 2 * 1000;
    private long backPressedTimeMillis = 0;

    private SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            if (currentPage == PAGE_MEDIA)
                mediaMainFragment.onMapSharedElements(names, sharedElements);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_pager);

        mContext = this;

        setExitSharedElementCallback(sharedElementCallback);

        ButterKnife.bind(this);

        instance = ExecutorServiceInstance.SINGLE_INSTANCE;

/*        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();*/

        initNavigationView();

        versionName.setText(String.format(getString(R.string.android_version_name), Util.getVersionName(mContext)));

        mediaMainFragment = MediaMainFragment.newInstance();
        fileMainFragment = FileMainFragment.newInstance();

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.frame_layout, mediaMainFragment).add(R.id.frame_layout, fileMainFragment).hide(fileMainFragment).commit();

        currentPage = PAGE_MEDIA;
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

    private void initNavigationView() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        refreshUserInNavigationView();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        if (action.equals(Util.REFRESH_VIEW_AFTER_DATA_RETRIEVED)) {

            Log.i(TAG, "handleOperationEvent: refreshUser");

            refreshUserInNavigationView();

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

        if (FNAS.userUUID != null && !LocalCache.RemoteUserMapKeyIsUUID.isEmpty() && LocalCache.RemoteUserMapKeyIsUUID.containsKey(FNAS.userUUID)) {
            user = LocalCache.RemoteUserMapKeyIsUUID.get(FNAS.userUUID);
        } else {
            user = LocalCache.getUser(mContext);
        }

        String userName = user.getUserName();
        TextView mUserNameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name_textview);
        mUserNameTextView.setText(userName);

        TextView mUserAvatar = (TextView) navigationView.getHeaderView(0).findViewById(R.id.avatar);
        mUserAvatar.setText(user.getDefaultAvatar());
        mUserAvatar.setBackgroundResource(user.getDefaultAvatarBgColorResourceId());

        MenuItem userManage = navigationView.getMenu().findItem(R.id.user_manage);

        userManage.setVisible(user.isAdmin());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        instance.shutdownFixedThreadPoolNow();

        instance = null;
        mContext = null;

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

        if (currentPage == PAGE_MEDIA)
            mediaMainFragment.onActivityReenter(resultCode, data);

    }

    @Override
    public void onBackPress() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {

        if (currentPage == PAGE_FILE) {
            if (fileMainFragment.handleBackPressedOrNot()) {
                fileMainFragment.handleBackPressed();
            } else {
                finishApp();
            }
        } else if (currentPage == PAGE_MEDIA) {

            if (mediaMainFragment.handleBackPressedOrNot()) {
                mediaMainFragment.handleBackPressed();
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

        if (currentPage == PAGE_MEDIA)
            mediaMainFragment.onActivityResult(requestCode, resultCode, intent);
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

        if (id == R.id.user_manage) {
            Intent intent = new Intent(this, UserManageActivity.class);
            startActivity(intent);
        } else if (id == R.id.logout) {

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    mDialog = ProgressDialog.show(mContext, null, getString(R.string.operating_title), true, false);

                }

                @Override
                protected Void doInBackground(Void... params) {

                    LocalCache.clearToken(mContext);
                    FNAS.restoreLocalPhotoUploadState(mContext);

                    instance.shutdownFixedThreadPoolNow();

                    ButlerService.stopTimingRetrieveMediaShare();

                    Util.setRemoteMediaLoaded(false);

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

            toggleFileOrMediaFragment();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void toggleFileOrMediaFragment() {

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.file);

        if (currentPage == PAGE_MEDIA) {

            currentPage = PAGE_FILE;

            menuItem.setTitle(getString(R.string.my_photo));
            menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_photo));

            fragmentManager.beginTransaction().hide(mediaMainFragment).show(fileMainFragment).commit();

            ButlerService.stopTimingRetrieveMediaShare();

        } else {

            currentPage = PAGE_MEDIA;

            menuItem.setTitle(getString(R.string.my_file));
            menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_folder));

            fragmentManager.beginTransaction().hide(fileMainFragment).show(mediaMainFragment).commit();

            if (mediaMainFragment.isRemoteMediaShareLoaded())
                ButlerService.startTimingRetrieveMediaShare();
        }

    }

}
