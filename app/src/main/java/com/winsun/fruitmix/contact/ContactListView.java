package com.winsun.fruitmix.contact;

import android.content.Context;

import com.winsun.fruitmix.interfaces.BaseView;

/**
 * Created by Administrator on 2018/1/27.
 */

public interface ContactListView extends BaseView{

    Context getContext();

    void onSelectItemChanged(int selectedItemCount);

}
