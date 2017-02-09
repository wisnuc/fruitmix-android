package com.winsun.fruitmix.refactor.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.EquipmentSearchActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.UserManageActivity;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.fragment.FileMainFragment;
import com.winsun.fruitmix.fragment.MediaMainFragment;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.refactor.common.BaseActivity;
import com.winsun.fruitmix.refactor.common.Injection;
import com.winsun.fruitmix.refactor.contract.MainPageContract;
import com.winsun.fruitmix.refactor.presenter.MainPagePresenterImpl;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainPageActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainPageContract.MainPageView {

    public static final String TAG = MainPageActivity.class.getSimpleName();

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.version_name)
    TextView versionName;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    MenuItem fileMenuItem;

    private Context mContext;

    private MediaMainFragment mediaMainFragment;
    private FileMainFragment fileMainFragment;
    private FragmentManager fragmentManager;

    private MainPageContract.MainPagePresenter mPresenter;

    private SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            mPresenter.onMapSharedElements(names, sharedElements);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_pager);

        mContext = this;

        setExitSharedElementCallback(sharedElementCallback);

        ButterKnife.bind(this);

/*        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();*/

        navigationView.setNavigationItemSelectedListener(this);

        mediaMainFragment = MediaMainFragment.newInstance();
        fileMainFragment = FileMainFragment.newInstance();

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.frame_layout, mediaMainFragment).add(R.id.frame_layout, fileMainFragment).hide(fileMainFragment).commit();

        fileMenuItem = navigationView.getMenu().findItem(R.id.file);

        Log.d(TAG, "onCreate: ");

    }

    @Override
    protected void onResume() {
        super.onResume();

        mPresenter = new MainPagePresenterImpl(Injection.injectDataRepository(), null, null);
        mPresenter.attachView(this);
        mPresenter.startMission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPresenter.detachView();

        mContext = null;

        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void refreshUserInNavigationView(User user) {
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
    public void setVersionNameText(String versionNameText) {
        versionName.setText(String.format(getString(R.string.android_version_name), versionNameText));
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

    @Override
    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void gotoUserManageActivity() {
        Intent intent = new Intent(this, UserManageActivity.class);
        startActivity(intent);
    }

    @Override
    public void gotoEquipmentActivity() {
        Intent intent = new Intent(MainPageActivity.this, EquipmentSearchActivity.class);
        startActivity(intent);
    }

    @Override
    public String getVersionName() {
        return Util.getVersionName(mContext);
    }

    @Override
    public void setFileItemMenuTitle(int resID) {
        fileMenuItem.setTitle(getString(resID));
    }

    @Override
    public void setFileItemMenuIcon(int resID) {
        fileMenuItem.setIcon(ContextCompat.getDrawable(this, resID));
    }

    @Override
    public void showMediaAndHideFileFragment() {
        fragmentManager.beginTransaction().hide(fileMainFragment).show(mediaMainFragment).commit();

    }

    @Override
    public void hideMediaAndShowFileFragment() {
        fragmentManager.beginTransaction().hide(mediaMainFragment).show(fileMainFragment).commit();

    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void showFinishAppToast() {
        Toast.makeText(mContext, getString(R.string.android_finishAppToast), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean isDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }


    private void showExplanation(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(MainPageActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                    }
                });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        mPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        mPresenter.onActivityReenter(resultCode, data);
    }

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
        mPresenter.handleBackEvent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        mPresenter.handleOnActivityResult(requestCode, resultCode, intent);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        mPresenter.onNavigationItemSelected(id);

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


        return true;
    }


}
