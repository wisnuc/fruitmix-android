package com.winsun.fruitmix.network;

/**
 * Created by Administrator on 2017/9/26.
 */

public class NetworkState {

    private boolean wifiConnected;
    private boolean mobileConnected;

    public NetworkState(boolean wifiConnected, boolean mobileConnected) {
        this.wifiConnected = wifiConnected;
        this.mobileConnected = mobileConnected;
    }

    public boolean isWifiConnected() {
        return wifiConnected;
    }

    public boolean isMobileConnected() {
        return mobileConnected;
    }

    public void setMobileConnected(boolean mobileConnected) {
        this.mobileConnected = mobileConnected;
    }

    public void setWifiConnected(boolean wifiConnected) {
        this.wifiConnected = wifiConnected;
    }

}
