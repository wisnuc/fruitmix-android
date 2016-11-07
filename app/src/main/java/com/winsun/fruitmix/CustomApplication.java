package com.winsun.fruitmix;

import android.app.Application;

import com.winsun.fruitmix.services.ButlerService;
import com.wisnun.fruitmix.MyEventBusIndex;

import org.greenrobot.eventbus.EventBus;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Administrator on 2016/7/26.
 */
public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/fangzheng.ttf").setFontAttrId(R.attr.fontPath).build());

        ButlerService.startButlerService(getApplicationContext());

        EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
    }
}
