package com.winsun.fruitmix.refactor.presenter;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.winsun.fruitmix.refactor.contract.SplashContract;
import com.winsun.fruitmix.refactor.ui.SplashScreenActivity;
import com.winsun.fruitmix.util.FileUtil;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/2/6.
 */

public class SplashPresenterImpl implements SplashContract.SplashPresenter {

    public static final String TAG = SplashPresenterImpl.class.getSimpleName();

    private SplashContract.SplashView mView;

    public static final int WELCOME = 0x0010;

    public static final int DELAY_TIME_MILLISECOND = 3 * 1000;

    private CustomHandler mHandler;

    public void retrieveLocalMedia() {

    }

    public void retrieveToken() {

    }

    public void createDownloadFileStoreFolder() {
        boolean result = FileUtil.createDownloadFileStoreFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create download file store folder failed");
        }
    }

    @Override
    public void attachView(SplashContract.SplashView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void startMission() {


    }

    @Override
    public void handleBackEvent() {
        mHandler.removeMessages(WELCOME);
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode) {

    }

    private static class CustomHandler extends Handler {

        WeakReference<SplashScreenActivity> weakReference = null;

        CustomHandler(SplashScreenActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WELCOME:
                    break;
                default:
            }
        }
    }

}
