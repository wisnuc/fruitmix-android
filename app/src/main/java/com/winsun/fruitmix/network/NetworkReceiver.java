package com.winsun.fruitmix.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017/9/26.
 */

public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //TODO:comment this for refactor NetworkChangeUseCase logic in new design 201804
//        EventBus.getDefault().post(new OperationEvent(Util.NETWORK_CHANGED, new OperationSuccess()));

    }

}
