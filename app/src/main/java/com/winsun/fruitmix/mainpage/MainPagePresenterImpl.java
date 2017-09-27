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
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.invitation.data.InjectInvitationDataSource;
import com.winsun.fruitmix.invitation.data.InvitationDataSource;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.logged.in.user.LoggedInWeChatUser;
import com.winsun.fruitmix.model.operationResult.OperationFail;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.usecase.GetAllBindingLocalUserUseCase;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.wxapi.MiniProgram;

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

    private InvitationDataSource invitationDataSource;

    private Resources resources;

    private IWXAPI iwxapi;

    private LoggedInUserDataSource loggedInUserDataSource;

    private SystemSettingDataSource systemSettingDataSource;

    private GetAllBindingLocalUserUseCase getAllBindingLocalUserUseCase;

    private List<LoggedInUser> bindingWeChatLoggedInUser;

    public MainPagePresenterImpl(Context context, SystemSettingDataSource systemSettingDataSource, LoggedInUserDataSource loggedInUserDataSource,
                                 NavPagerActivity.NavPagerViewModel navPagerViewModel, MainPageView mainPageView,
                                 GetAllBindingLocalUserUseCase getAllBindingLocalUserUseCase) {

        mNavigationMenuItems = new ArrayList<>();
        mNavigationMenuLoggedInUsers = new ArrayList<>();

        navigationItemAdapter = new NavigationItemAdapter();

        this.navPagerViewModel = navPagerViewModel;

        this.mainPageView = mainPageView;

        this.loggedInUserDataSource = loggedInUserDataSource;

        this.systemSettingDataSource = systemSettingDataSource;

        this.getAllBindingLocalUserUseCase = getAllBindingLocalUserUseCase;

        initNavigationAccountManageViewModel(context);

        initNavigationMenuItems(context);

        threadManager = ThreadManagerImpl.getInstance();

        iwxapi = MiniProgram.registerToWX(context);

        invitationDataSource = InjectInvitationDataSource.provideInvitationDataSource(context);

        resources = context.getResources();

        bindingWeChatLoggedInUser = new ArrayList<>();

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

        getBindingWeChatLoggedInUser(new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                refreshAllLoggedInUsers(true);

            }

            @Override
            public void onFail(OperationResult result) {

                refreshAllLoggedInUsers(false);

            }
        });

    }

    private void refreshAllLoggedInUsers(boolean getBindingWeChatLoggedInUserSucceed) {
        if (loggedInUserDataSource == null) return;

        String currentUserUUID = systemSettingDataSource.getCurrentLoginUserUUID();

        List<LoggedInUser> loggedInUsers = new ArrayList<>(loggedInUserDataSource.getAllLoggedInUsers());

        loggedInUsers.addAll(bindingWeChatLoggedInUser);

        int loggedInUserListSize = loggedInUsers.size();

        if(!getBindingWeChatLoggedInUserSucceed){

            navPagerViewModel.equipmentNameVisibility.set(loggedInUserListSize != 1);

            Iterator<LoggedInUser> iterator = loggedInUsers.iterator();
            while (iterator.hasNext()) {
                LoggedInUser loggedInUser = iterator.next();
                if (loggedInUser.getUser().getUuid().equals(currentUserUUID)) {

                    navPagerViewModel.equipmentNameText.set(loggedInUser.getEquipmentName());

                    iterator.remove();
                }
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

    private void getBindingWeChatLoggedInUser(final BaseOperateDataCallback<Boolean> callback) {

        final String guid = systemSettingDataSource.getCurrentLoginUserGUID();

        if (guid == null || guid.isEmpty()) {
            callback.onFail(new OperationFail("no guid"));
            return;
        }

        String token = systemSettingDataSource.getCurrentLoginToken();

        getAllBindingLocalUserUseCase.getAllBindingLocalUser(guid, token, new BaseLoadDataCallback<LoggedInWeChatUser>() {
            @Override
            public void onSucceed(List<LoggedInWeChatUser> data, OperationResult operationResult) {

                String stationID = systemSettingDataSource.getCurrentLoginStationID();

                bindingWeChatLoggedInUser.clear();

                for (LoggedInWeChatUser loggedInUser : data) {

                    if (!loggedInUser.getStationID().equals(stationID)) {

                        bindingWeChatLoggedInUser.add(loggedInUser);

                    }else {

                        navPagerViewModel.equipmentNameVisibility.set(true);
                        navPagerViewModel.equipmentNameText.set(loggedInUser.getGateway());

                    }

                }

                callback.onSucceed(true, new OperationSuccess());

            }

            @Override
            public void onFail(OperationResult operationResult) {

                Log.d(TAG, "onFail: get all binding local user");

                callback.onFail(new OperationFail("fail on get binding local user"));

            }
        });

    }

    @Override
    public void notifyAdapterDataSetChanged() {
        navigationItemAdapter.notifyDataSetChanged();
    }

    private void toggleUserManageNavigationItem(Context context, User user) {
        if (user.isAdmin()) {

            if (((NavigationMenuViewModel) mNavigationMenuItems.get(2)).getMenuIconResId() != R.drawable.ic_person_add_black_24dp) {

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

            if (((NavigationMenuViewModel) mNavigationMenuItems.get(2)).getMenuIconResId() == R.drawable.ic_person_add_black_24dp)
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

 /*       model = new NavigationMenuViewModel() {
            @Override
            public void onClick() {
                super.onClick();

                createInvitationInThread();

            }
        };

        model.setMenuIconResId(R.drawable.ic_settings_black_24dp);
        model.setMenuText("发起邀请");
        mNavigationMenuItems.add(model);

        model = new NavigationMenuViewModel() {
            @Override
            public void onClick() {
                super.onClick();

                mainPageView.gotoConfirmInviteUserActivity();
            }
        };

        model.setMenuIconResId(R.drawable.ic_settings_black_24dp);
        model.setMenuText("确认邀请");
        mNavigationMenuItems.add(model);*/

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

    }

    private void createInvitationInThread() {
        invitationDataSource.createInvitation(new BaseOperateDataCallbackImpl<String>() {
            @Override
            public void onSucceed(final String data, OperationResult result) {

                Log.d(TAG, "onSucceed: data: " + data);

                MiniProgram.shareMiniWXApp(iwxapi, resources, data);

            }

        });
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
