package com.winsun.fruitmix.mainpage;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.OkHttpUtil;
import com.winsun.fruitmix.invitation.Invitation;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BindingViewHolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/6/23.
 */

public class MainPagePresenterImpl implements MainPagePresenter {

    public static final String TAG = MainPagePresenterImpl.class.getSimpleName();

    private static final int NAVIGATION_ITEM_TYPE_MENU = 0;
    private static final int NAVIGATION_ITEM_TYPE_LOGGED_IN_USER = 1;
    private static final int NAVIGATION_ITEM_TYPE_DIVIDER = 2;
    private static final int NAVIGATION_ITEM_TYPE_ACCOUNT_MANAGE = 3;

    private List<BaseNavigationItemViewModel> mNavigationMenuItems;
    private List<BaseNavigationItemViewModel> mNavigationMenuLoggedInUsers;

    private NavigationItemAdapter navigationItemAdapter;

    private NavPagerActivity.NavPagerViewModel navPagerViewModel;

    private NavigationLoggedInUserViewModel navigationAccountManageViewModel;

    private boolean mNavigationHeaderArrowDown = true;

    private MainPageView mainPageView;

    private ThreadManager threadManager;

    private Invitation invitation;

    private Resources resources;

    public MainPagePresenterImpl(Context context, NavPagerActivity.NavPagerViewModel navPagerViewModel, MainPageView mainPageView) {

        mNavigationMenuItems = new ArrayList<>();
        mNavigationMenuLoggedInUsers = new ArrayList<>();

        navigationItemAdapter = new NavigationItemAdapter();

        this.navPagerViewModel = navPagerViewModel;

        this.mainPageView = mainPageView;

        initNavigationAccountManageViewModel(context);

        initNavigationMenuItems(context);

        threadManager = ThreadManager.getInstance();

        IHttpUtil iHttpUtil = new OkHttpUtil();

        IWXAPI iwxapi = Util.registerToWX(context);

        invitation = new Invitation(iHttpUtil, iwxapi);

        resources = context.getResources();

    }

    @Override
    public NavigationItemAdapter getNavigationItemAdapter() {
        return navigationItemAdapter;
    }

    private void initNavigationAccountManageViewModel(Context context) {

        navigationAccountManageViewModel = new NavigationLoggedInUserViewModel(null) {
            @Override
            public void onClick() {
                mainPageView.closeDrawer();
                mainPageView.gotoAccountManageActivity();
            }
        };
        navigationAccountManageViewModel.setType(NAVIGATION_ITEM_TYPE_ACCOUNT_MANAGE);

        String title = context.getString(R.string.account_manage);

        navigationAccountManageViewModel.setTitleText(title);

        String avatar = title.substring(0, 1).toUpperCase();

        navigationAccountManageViewModel.setAvatarText(avatar);

        navigationAccountManageViewModel.setItemSubTitleVisibility(false);

    }

    @Override
    public void refreshNavigationLoggedInUsers() {

        if (LocalCache.LocalLoggedInUsers == null) return;

        List<LoggedInUser> loggedInUsers = new ArrayList<>(LocalCache.LocalLoggedInUsers);

        int loggedInUserListSize = loggedInUsers.size();

        navPagerViewModel.equipmentNameVisibility.set(loggedInUserListSize != 1);

        Iterator<LoggedInUser> iterator = loggedInUsers.iterator();
        while (iterator.hasNext()) {
            LoggedInUser loggedInUser = iterator.next();
            if (loggedInUser.getUser().getUuid().equals(FNAS.userUUID)) {

                navPagerViewModel.equipmentNameText.set(loggedInUser.getEquipmentName());

                iterator.remove();
            }
        }

        if (loggedInUserListSize > 0) {

            mNavigationMenuLoggedInUsers.clear();

            for (LoggedInUser loggedInUser : loggedInUsers) {

                NavigationLoggedInUserViewModel model = new NavigationLoggedInUserViewModel(loggedInUser);

                User user = loggedInUser.getUser();

                model.setAvatarBackgroundResId(user.getDefaultAvatarBgColorResourceId());
                model.setAvatarText(user.getDefaultAvatar());
                model.setTitleText(user.getUserName());
                model.setSubTitleText(loggedInUser.getEquipmentName());
                model.setItemSubTitleVisibility(true);

                mNavigationMenuLoggedInUsers.add(model);

            }

            mNavigationMenuLoggedInUsers.add(navigationAccountManageViewModel);

        } else {

            mNavigationMenuLoggedInUsers.clear();
            mNavigationMenuLoggedInUsers.add(navigationAccountManageViewModel);

        }

    }

    @Override
    public void notifyAdapterDataSetChanged() {
        navigationItemAdapter.notifyDataSetChanged();
    }

    private void toggleUserManageNavigationItem(Context context, User user) {
        if (user.isAdmin()) {

            if (mNavigationMenuItems.size() == 4) {

                NavigationMenuViewModel model = new NavigationMenuViewModel() {
                    @Override
                    public void onClick() {
                        super.onClick();
                        mainPageView.gotoUserManageActivity();
                    }
                };
                model.setMenuIconResId(R.drawable.ic_person_add_black_24dp);
                model.setMenuText(context.getString(R.string.user_manage));

                mNavigationMenuItems.add(2, model);
            }

        } else {

            if (mNavigationMenuItems.size() == 5)
                mNavigationMenuItems.remove(2);
        }
    }


    private void initNavigationMenuItems(Context context) {

        NavigationMenuViewModel model = new NavigationMenuViewModel() {
            @Override
            public void onClick() {

                super.onClick();

                mainPageView.gotoFileDownloadActivity();

            }
        };
        model.setMenuIconResId(R.drawable.navigation_file_menu_bg);
        model.setMenuText(context.getString(R.string.download_manage));

        mNavigationMenuItems.add(model);

        NavigationDividerViewModel dividerViewModel = new NavigationDividerViewModel();
        mNavigationMenuItems.add(dividerViewModel);

        model = new NavigationMenuViewModel() {
            @Override
            public void onClick() {
                super.onClick();
                mainPageView.gotoSettingActivity();
            }
        };
        model.setMenuIconResId(R.drawable.ic_settings_black_24dp);
        model.setMenuText(context.getString(R.string.setting));

        mNavigationMenuItems.add(model);

        model = new NavigationMenuViewModel() {
            @Override
            public void onClick() {
                super.onClick();
                mainPageView.logout();
            }
        };
        model.setMenuIconResId(R.drawable.ic_power_settings_new_black_24dp);
        model.setMenuText(context.getString(R.string.logout));

        mNavigationMenuItems.add(model);

        model = new NavigationMenuViewModel() {
            @Override
            public void onClick() {
                super.onClick();

                threadManager.runOnCacheThread(new Runnable() {
                    @Override
                    public void run() {

                        invitation.createTicket(new BaseOperateDataCallback<String>() {
                            @Override
                            public void onSucceed(final String data, OperationResult result) {

                                Log.d(TAG, "onSucceed: data: " + data);

                                threadManager.runOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        invitation.shareMiniWXApp(resources, data);
                                    }
                                });

                            }

                            @Override
                            public void onFail(OperationResult result) {

                                Log.d(TAG, "onFail: result: " + result);

                                threadManager.runOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        invitation.shareMiniWXApp(resources, "");


                                    }
                                });

                            }
                        });

                    }
                });

            }
        };

        model.setMenuIconResId(R.drawable.ic_settings_black_24dp);
        model.setMenuText("发起邀请");
        mNavigationMenuItems.add(model);

    }

    @Override
    public void toggleNavigationHeaderArrow() {

        if (mNavigationHeaderArrowDown) {
            switchToNavigationItemLoggedInUsers();
        } else {
            switchToNavigationItemMenu();
        }
    }

    @Override
    public void switchToNavigationItemMenu() {

        mNavigationHeaderArrowDown = true;

        navPagerViewModel.headerArrowResID.set(R.drawable.navigation_header_arrow_down);

        navigationItemAdapter.setmNavigationItems(mNavigationMenuItems);
        navigationItemAdapter.notifyDataSetChanged();

    }

    private void switchToNavigationItemLoggedInUsers() {
        mNavigationHeaderArrowDown = false;

        navPagerViewModel.headerArrowResID.set(R.drawable.navigation_header_arrow_up);

        navigationItemAdapter.setmNavigationItems(mNavigationMenuLoggedInUsers);
        navigationItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshUserInNavigationView(Context context, User user) {

        toggleUserManageNavigationItem(context, user);
        refreshNavigationLoggedInUsers();
    }


    private class NavigationItemAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<BaseNavigationItemViewModel> mNavigationItems;

        public void setmNavigationItems(List<BaseNavigationItemViewModel> mNavigationItems) {
            this.mNavigationItems = mNavigationItems;
        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ViewDataBinding viewDataBinding =
                    DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), viewType, parent, false);

            return new BindingViewHolder(viewDataBinding);

        }

        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            holder.getViewDataBinding().setVariable(BR.navigationItemViewModel, mNavigationItems.get(position));
            holder.getViewDataBinding().executePendingBindings();

        }

        @Override
        public int getItemViewType(int position) {
            return mNavigationItems.get(position).getLayout();
        }

        @Override
        public int getItemCount() {
            return mNavigationItems.size();
        }
    }

    private abstract class BaseNavigationItemViewModel {

        private int layout;

        private int type;

        public int getLayout() {
            return layout;
        }

        public void setLayout(int layout) {
            this.layout = layout;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void onClick() {
            mainPageView.closeDrawer();
        }

    }

    public class NavigationMenuViewModel extends BaseNavigationItemViewModel {

        private int menuIconResId;
        private String menuText;
        private boolean menuIconSelect;

        public NavigationMenuViewModel() {
            setLayout(R.layout.navigation_menu_item);
            setType(NAVIGATION_ITEM_TYPE_MENU);
        }

        public int getMenuIconResId() {
            return menuIconResId;
        }

        public void setMenuIconResId(int menuIconResId) {
            this.menuIconResId = menuIconResId;
        }

        public String getMenuText() {
            return menuText;
        }

        public void setMenuText(String menuText) {
            this.menuText = menuText;
        }

        public boolean isMenuIconSelect() {
            return menuIconSelect;
        }

        public void setMenuIconSelect(boolean menuIconSelect) {
            this.menuIconSelect = menuIconSelect;
        }

    }

    public class NavigationLoggedInUserViewModel extends BaseNavigationItemViewModel {

        private String avatarText;
        private int avatarBackgroundResId;
        private String titleText;
        private String subTitleText;
        private boolean itemSubTitleVisibility;

        private LoggedInUser loggedInUser;

        public NavigationLoggedInUserViewModel(LoggedInUser loggedInUser) {
            setLayout(R.layout.navigation_logged_in_user_item);
            setType(NAVIGATION_ITEM_TYPE_LOGGED_IN_USER);

            avatarBackgroundResId = R.drawable.share_portrait_bg;

            this.loggedInUser = loggedInUser;
        }

        public String getAvatarText() {
            return avatarText;
        }

        public void setAvatarText(String avatarText) {
            this.avatarText = avatarText;
        }

        public int getAvatarBackgroundResId() {
            return avatarBackgroundResId;
        }

        public void setAvatarBackgroundResId(int avatarBackgroundResId) {
            this.avatarBackgroundResId = avatarBackgroundResId;
        }

        public String getTitleText() {
            return titleText;
        }

        public void setTitleText(String titleText) {
            this.titleText = titleText;
        }

        public String getSubTitleText() {
            return subTitleText;
        }

        public void setSubTitleText(String subTitleText) {
            this.subTitleText = subTitleText;
        }

        public boolean isItemSubTitleVisibility() {
            return itemSubTitleVisibility;
        }

        public void setItemSubTitleVisibility(boolean itemSubTitleVisibility) {
            this.itemSubTitleVisibility = itemSubTitleVisibility;
        }

        @Override
        public void onClick() {
            super.onClick();
            mainPageView.loggedInUserItemOnClick(loggedInUser);
        }
    }

    public class NavigationDividerViewModel extends BaseNavigationItemViewModel {

        public NavigationDividerViewModel() {
            setLayout(R.layout.navigation_divider_item);
            setType(NAVIGATION_ITEM_TYPE_DIVIDER);
        }

        @Override
        public void onClick() {
        }
    }


}
