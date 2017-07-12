package com.winsun.fruitmix.account.manage;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.widget.BaseExpandableListAdapter;

/**
 * Created by Administrator on 2017/6/22.
 */

public interface AccountManagePresenter {

    void onDestroy();

    void addAccount();

    void handleBack();

    void onActivityResult(int requestCode, int resultCode, Intent data);

    BaseExpandableListAdapter getAdapter();

}
