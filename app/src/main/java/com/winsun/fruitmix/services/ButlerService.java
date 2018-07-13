package com.winsun.fruitmix.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.generate.media.GenerateMediaThumbUseCase;
import com.winsun.fruitmix.generate.media.InjectGenerateMediaThumbUseCase;
import com.winsun.fruitmix.media.CalcMediaDigestStrategy;
import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.network.change.InjectNetworkChangeUseCase;
import com.winsun.fruitmix.network.change.NetworkChangeUseCase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ButlerService extends Service {

    private static final String TAG = ButlerService.class.getSimpleName();

    private GenerateMediaThumbUseCase generateMediaThumbUseCase;

    private boolean alreadyStart = false;

    private CalcMediaDigestStrategy.CalcMediaDigestCallback calcMediaDigestCallback;

    public static void startButlerService(Context context) {
        Intent intent = new Intent(context, ButlerService.class);
        context.startService(intent);
    }

    public static void stopButlerService(Context context) {
        context.stopService(new Intent(context, ButlerService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: start service");

        EventBus.getDefault().register(this);

        initInstance();

        alreadyStart = true;

    }

    private void initInstance() {

        calcMediaDigestCallback = new CalcMediaDigestStrategy.CalcMediaDigestCallback() {
            @Override
            public void handleFinished() {

                generateMediaThumbUseCase.startGenerateMediaMiniThumb();
                generateMediaThumbUseCase.startGenerateMediaThumb();

            }

            @Override
            public void handleNothing() {

                generateMediaThumbUseCase.startGenerateMediaMiniThumb();
                generateMediaThumbUseCase.startGenerateMediaThumb();

            }
        };

        MediaDataSourceRepository mediaDataSourceRepository = InjectMedia.provideMediaDataSourceRepository(this);

        mediaDataSourceRepository.registerCalcDigestCallback(calcMediaDigestCallback);

        generateMediaThumbUseCase = InjectGenerateMediaThumbUseCase.provideGenerateMediaThumbUseCase(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: " + alreadyStart);

        //if app is killed by user,onCreate will not be called,so check alreadyStart and initInstance
        if (!alreadyStart) {
            initInstance();
        } else
            alreadyStart = false;

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        generateMediaThumbUseCase.stopGenerateLocalPhotoThumbnail();
        generateMediaThumbUseCase.stopGenerateLocalPhotoMiniThumbnail();

        MediaDataSourceRepository mediaDataSourceRepository = InjectMedia.provideMediaDataSourceRepository(this);

        mediaDataSourceRepository.unregisterCalcDigestCallback(calcMediaDigestCallback);

        super.onDestroy();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        Log.i(TAG, "handleOperationEvent: action:" + action);

        switch (action) {

            case NetworkChangeUseCase.NETWORK_CHANGED:

                NetworkChangeUseCase networkChangeUseCase = InjectNetworkChangeUseCase.provideInstance(this);
                networkChangeUseCase.handleNetworkChange();

                break;

        }

    }

}
