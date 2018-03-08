package com.winsun.fruitmix.group.data.viewmodel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

/**
 * Created by Administrator on 2017/7/21.
 */

public class GroupListViewModel {

    public final ObservableBoolean showRecyclerView = new ObservableBoolean(false);

    public final ObservableBoolean showAddFriendsFAB = new ObservableBoolean(false);

    public final ObservableBoolean showNoWATokenExplainLayout = new ObservableBoolean(false);

    public final ObservableField<String> explainTextField = new ObservableField<>();

    public final ObservableBoolean showGoToBindWeChatBtn = new ObservableBoolean();

}
