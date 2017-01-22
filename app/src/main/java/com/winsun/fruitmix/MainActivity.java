package com.winsun.fruitmix;


import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Util.setRemoteMediaLoaded(false);
        Util.setRemoteMediaShareLoaded(false);

        ButlerService.startButlerService(getApplicationContext());

        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SplashScreenActivity.class);
        startActivity(intent);
        finish();

    }

}
