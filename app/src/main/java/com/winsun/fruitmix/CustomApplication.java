package com.winsun.fruitmix;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.druk.rxdnssd.RxDnssd;
import com.github.druk.rxdnssd.RxDnssdBindable;
import com.github.druk.rxdnssd.RxDnssdEmbedded;
import com.winsun.fruitmix.services.ButlerService;
import com.wisnun.fruitmix.MyEventBusIndex;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/7/26.
 */
public class CustomApplication extends Application {

    public static final String TAG = CustomApplication.class.getSimpleName();

    private RxDnssd mRxDnssd;

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();

        mRxDnssd = createDnssd();
    }

    public static RxDnssd getRxDnssd(@NonNull Context context) {
        return ((CustomApplication) context.getApplicationContext()).mRxDnssd;
    }

    private RxDnssd createDnssd() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            Log.i(TAG, "Using embedded version of dns sd because of API < 16");
            return new RxDnssdEmbedded();
        }
        if (Build.VERSION.RELEASE.contains("4.4.2") && Build.MANUFACTURER.toLowerCase().contains("samsung")) {
            Log.i(TAG, "Using embedded version of dns sd because of Samsung 4.4.2");
            return new RxDnssdEmbedded();
        }
        Log.i(TAG, "Using systems dns sd daemon");
        return new RxDnssdBindable(this);
    }
}
