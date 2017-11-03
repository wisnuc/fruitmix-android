package com.winsun.fruitmix;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import com.github.druk.rxdnssd.RxDnssd;
import com.github.druk.rxdnssd.RxDnssdBindable;
import com.github.druk.rxdnssd.RxDnssdEmbedded;
import com.wisnun.fruitmix.MyEventBusIndex;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/7/26.
 */
public class CustomApplication extends Application {

    public static final String TAG = CustomApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();

    }

}
