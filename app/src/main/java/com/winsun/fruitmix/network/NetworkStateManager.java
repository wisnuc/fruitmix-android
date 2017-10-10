package com.winsun.fruitmix.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Administrator on 2017/9/26.
 */

public class NetworkStateManager {

    private ConnectivityManager manager;

    private static NetworkStateManager instance;

    public static NetworkStateManager getInstance(Context context) {

        if (instance == null)
            instance = new NetworkStateManager(context);

        return instance;
    }

    public static void destroyInstance() {

        instance = null;

    }

    public NetworkStateManager(Context context) {

        manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public NetworkState getNetworkState() {

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        NetworkState networkState = new NetworkState(false, false);

        if (networkInfo != null && networkInfo.isConnected()) {

            int type = networkInfo.getType();

            if(type == ConnectivityManager.TYPE_WIFI){
                networkState.setWifiConnected(true);
            }

            if(type == ConnectivityManager.TYPE_MOBILE){

                networkState.setMobileConnected(true);
            }

        }

        return networkState;

    }

}
