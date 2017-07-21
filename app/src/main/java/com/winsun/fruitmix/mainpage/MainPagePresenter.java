package com.winsun.fruitmix.mainpage;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/6/23.
 */

public interface MainPagePresenter {

    void switchToNavigationItemMenu();

    void toggleNavigationHeaderArrow();

    void refreshUserInNavigationView(Context context,User user);

    RecyclerView.Adapter getNavigationItemAdapter();

    void refreshNavigationLoggedInUsers();

    void notifyAdapterDataSetChanged();
}
