package com.winsun.fruitmix.invitation;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

/**
 * Created by Administrator on 2017/11/13.
 */

public class ConfirmInviteUserViewModel {

    public final ObservableBoolean showOperateBtn = new ObservableBoolean();
    public final ObservableField<String> operateResult = new ObservableField<>();

}
