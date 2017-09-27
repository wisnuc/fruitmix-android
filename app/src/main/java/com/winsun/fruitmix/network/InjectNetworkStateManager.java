package com.winsun.fruitmix.network;

import android.content.Context;

/**
 * Created by Administrator on 2017/9/26.
 */

public class InjectNetworkStateManager {

    public static NetworkStateManager provideNetworkStateManager(Context context){

        return NetworkStateManager.getInstance(context);
    }

}
