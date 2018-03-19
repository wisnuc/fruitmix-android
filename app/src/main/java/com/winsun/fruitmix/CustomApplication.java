package com.winsun.fruitmix;

import android.app.Application;

import com.facebook.stetho.Stetho;
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

        Stetho.initializeWithDefaults(this);

    }

}
