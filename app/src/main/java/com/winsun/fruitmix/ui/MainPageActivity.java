package com.winsun.fruitmix.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.CustomApplication;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.common.BaseActivity;
import com.winsun.fruitmix.common.Injection;
import com.winsun.fruitmix.contract.MainPageContract;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.navigationItem.NavigationItemType;
import com.winsun.fruitmix.presenter.MainPagePresenterImpl;
import com.winsun.fruitmix.util.Util;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.winsun.fruitmix.presenter.MainPagePresenterImpl.NAVIGATION_ITEM_TYPE_ACCOUNT_MANAGE;
import static com.winsun.fruitmix.presenter.MainPagePresenterImpl.NAVIGATION_ITEM_TYPE_DIVIDER;
import static com.winsun.fruitmix.presenter.MainPagePresenterImpl.NAVIGATION_ITEM_TYPE_LOGGED_IN_USER;
import static com.winsun.fruitmix.presenter.MainPagePresenterImpl.NAVIGATION_ITEM_TYPE_MENU;

public class MainPageActivity extends BaseActivity
        implements MainPageContract.MainPageView {

    public static final String TAG = MainPageActivity.class.getSimpleName();

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.version_name)
    TextView versionName;
    @BindView(R.id.frame_layout)
    FrameLayout frameLayout;
    @BindView(R.id.avatar)
    TextView mUserAvatar;
    @BindView(R.id.upload_percent)
    TextView mUploadMediaPercentTextView;
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

    private MediaMainFragment mMediaMainFragment;
    private FileMainFragment mFileMainFragment;

    private View mediaMainFragmentView;
    private View fileMainFragmentView;

    private MainPageContract.MainPagePresenter mPresenter;

    private SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            mPresenter.onMapSharedElements(names, sharedElements);
        }
    };

    private NavigationItemAdapter mNavigationItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: ");

        setContentView(R.layout.activity_nav_pager);

        mContext = this;

        setExitSharedElementCallback(sharedElementCallback);

        ButterKnife.bind(this);

/*        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();*/

        mPresenter = new MainPagePresenterImpl(Injection.injectDataRepository(mContext),CustomApplication.getRxDnssd(mContext));
        mPresenter.attachView(this);

        mMediaMainFragment = new MediaMainFragment(mPresenter, this);
        mFileMainFragment = new FileMainFragment(mPresenter, this);

        setVersionNameText(getVersionName());

        mediaMainFragmentView = mMediaMainFragment.getView();
        fileMainFragmentView = mFileMainFragment.getView();

        frameLayout.addView(fileMainFragmentView);
        frameLayout.addView(mediaMainFragmentView);

        initNavigationMenuRecyclerView();

        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                mPresenter.calcAlreadyUploadMediaCount();
            }
        });

        mPresenter.initNavigationItemMenu();

        mPresenter.loadCurrentUser();
    }

    private void initNavigationMenuRecyclerView() {
        mNavigationItemAdapter = new NavigationItemAdapter();
        mNavigationMenuRecyclerView.setAdapter(mNavigationItemAdapter);
        mNavigationMenuRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mNavigationMenuRecyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    public void setUploadMediaPercentText(String text) {
        mUploadMediaPercentTextView.setText(text);
    }

    @Override
    public void setNavigationItemTypes(List<NavigationItemType> navigationItemTypes) {
        mNavigationItemAdapter.setNavigationItemTypes(navigationItemTypes);
        mNavigationItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void setLoggedInUser0AvatarVisibility(int visibility) {
        mLoggedInUser0Avatar.setVisibility(visibility);
    }

    @Override
    public void setLoggedInUser1AvatarVisibility(int visibility) {
        mLoggedInUser1Avatar.setVisibility(visibility);
    }

    @Override
    public void setNavigationHeaderArrowVisibility(int visibility) {
        mNavigationHeaderArrow.setVisibility(visibility);
    }

    @Override
    public int getLoggedInUser0AvatarVisibility() {
        return mLoggedInUser0Avatar.getVisibility();
    }

    @Override
    public int getLoggedInUser1AvatarVisibility() {
        return mLoggedInUser1Avatar.getVisibility();
    }

    @Override
    public void setLoggedInUser0AvatarText(String text) {
        mLoggedInUser0Avatar.setText(text);
    }

    @Override
    public void setLoggedInUser1AvatarText(String text) {
        mLoggedInUser1Avatar.setText(text);
    }

    @Override
    public void setLoggedInUser0AvatarBackgroundResource(int resID) {
        mLoggedInUser0Avatar.setBackgroundResource(resID);
    }

    @Override
    public void setLoggedInUser1AvatarBackgroundResource(int resID) {
        mLoggedInUser1Avatar.setBackgroundResource(resID);
    }

    @Override
    public void setNavigationHeaderArrowImageResource(int resID) {
        mNavigationHeaderArrow.setImageResource(resID);
    }

    @Override
    public void setLoggedInUser0AvatarOnClickListener(View.OnClickListener onClickListener) {
        mLoggedInUser0Avatar.setOnClickListener(onClickListener);
    }

    @Override
    public void setLoggedInUser1AvatarOnClickListener(View.OnClickListener onClickListener) {
        mLoggedInUser1Avatar.setOnClickListener(onClickListener);
    }

    @Override
    public void setNavigationHeaderArrowOnClickListener(View.OnClickListener onClickListener) {
        mNavigationHeaderArrow.setOnClickListener(onClickListener);
    }

    @Override
    public void showOperateFailed() {
        Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMediaMainFragment.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy: ");

        mMediaMainFragment.onDestroyView();
        mFileMainFragment.onDestroyView();

        mPresenter.detachView();

        mContext = null;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void refreshUserInNavigationView(User user) {

        String userName = user.getUserName();

        Log.d(TAG, "refreshUserInNavigationView: userName: " + userName);

        mUserNameTextView.setText(userName);

        mUserAvatar.setText(user.getDefaultAvatar());
        mUserAvatar.setBackgroundResource(user.getDefaultAvatarBgColorResourceId());

    }

    private void setVersionNameText(String versionNameText) {
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
    public void gotoSettingActivity() {
        Intent intent = new Intent(MainPageActivity.this, SettingActivity.class);
        startActivity(intent);
    }

    @Override
    public void gotoAccountManageActivity() {
        Intent intent = new Intent(MainPageActivity.this, AccountMangeActivity.class);
        startActivityForResult(intent,MainPagePresenterImpl.START_ACCOUNT_MANAGE);
    }

    private String getVersionName() {
        return Util.getVersionName(mContext);
    }

    @Override
    public void showMediaAndHideFileFragment() {

        mediaMainFragmentView.setVisibility(View.VISIBLE);
        fileMainFragmentView.setVisibility(View.INVISIBLE);

    }

    @Override
    public void hideMediaAndShowFileFragment() {

        mediaMainFragmentView.setVisibility(View.INVISIBLE);
        fileMainFragmentView.setVisibility(View.VISIBLE);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void showAutoUploadAlreadyClose() {
        Toast.makeText(mContext, getString(R.string.photo_auto_upload_already_close), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        mPresenter.handleBackEvent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        mPresenter.handleOnActivityResult(requestCode, resultCode, intent);
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

                case NAVIGATION_ITEM_TYPE_ACCOUNT_MANAGE:
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

            final MainPagePresenterImpl.NavigationMenuItem item = (MainPagePresenterImpl.NavigationMenuItem) type;

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

            final MainPagePresenterImpl.NavigationLoggerInUserItem loggerInUserItem = (MainPagePresenterImpl.NavigationLoggerInUserItem) type;

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

    class NavigationAccountItemViewHolder extends BaseNavigationViewHolder {

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

            final MainPagePresenterImpl.NavigationAccountManageItem item = (MainPagePresenterImpl.NavigationAccountManageItem) type;

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
