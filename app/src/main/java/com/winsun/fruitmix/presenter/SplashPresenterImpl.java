package com.winsun.fruitmix.presenter;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.winsun.fruitmix.business.DataRepository;
import com.winsun.fruitmix.business.callback.LoadTokenOperationCallback;
import com.winsun.fruitmix.contract.SplashContract;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.FileUtil;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/2/6.
 */

public class SplashPresenterImpl implements SplashContract.SplashPresenter {

    public static final String TAG = SplashPresenterImpl.class.getSimpleName();

    private SplashContract.SplashView mView;

    private static final int WELCOME = 0x0010;

    private static final int DELAY_TIME_MILLISECOND = 3 * 1000;

    private CustomHandler mHandler;

    private DataRepository mRepository;

    public SplashPresenterImpl(DataRepository repository) {

        mRepository = repository;

    }

    @Override
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
    public void loadToken() {

        mRepository.loadTokenDeviceIDAndGatewayInSplash(new LoadTokenOperationCallback.LoadTokenCallback() {
            @Override
            public void onLoadSucceed(OperationResult result, String token) {

                mHandler = new CustomHandler(SplashPresenterImpl.this);
                mHandler.sendEmptyMessageDelayed(WELCOME, DELAY_TIME_MILLISECOND);

                loadData();
            }

            @Override
            public void onLoadFail(OperationResult result) {

                if (mView == null) return;

                mView.emptyCacheToken();
            }
        });

    }

    private void loadData() {
        mRepository.loadUsersInThread(null);
        mRepository.loadMediasInThread(null);
        mRepository.loadMediaSharesInThread(null);
    }

    @Override
    public void handleBackEvent() {
        mHandler.removeMessages(WELCOME);
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
    }

    private static class CustomHandler extends Handler {

        WeakReference<SplashPresenterImpl> weakReference = null;

        CustomHandler(SplashPresenterImpl splashPresenter) {
            weakReference = new WeakReference<>(splashPresenter);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WELCOME:
                    weakReference.get().mView.welcome();
                    break;
                default:
            }
        }
    }

}
