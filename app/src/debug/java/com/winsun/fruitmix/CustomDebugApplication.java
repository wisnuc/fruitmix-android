package com.winsun.fruitmix;

import com.facebook.stetho.Stetho;

/**
 * Created by Administrator on 2018/3/27.
 */

public class CustomDebugApplication extends CustomApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initializeWithDefaults(this);
    }
}
