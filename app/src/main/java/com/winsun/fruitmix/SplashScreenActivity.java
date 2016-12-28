package com.winsun.fruitmix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.model.LoginType;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2016/5/9.
 */
public class SplashScreenActivity extends Activity {

    public static final String TAG = SplashScreenActivity.class.getSimpleName();

    private String mUuid;
    private String mPassword;
    private String mGateway;
    private String mToken;

    private Context mContext;

    public static final int WELCOME = 0x0010;

    public static final int DELAY_TIME_MILLISECOND = 3 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        mContext = this;

        LocalCache.Init();

        boolean result = FileUtil.createDownloadFileStoreFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create download file store folder failed");
        }

        FNAS.retrieveLocalMediaMap(getApplicationContext());

        mGateway = LocalCache.getGateway(mContext);
        mUuid = LocalCache.getUuidValue(mContext);
        mPassword = LocalCache.getPasswordValue(mContext);
        mToken = LocalCache.getToken(mContext);

        if (mUuid != null && mPassword != null && mGateway != null && mToken != null) {

            Util.loginType = LoginType.SPLASH_SCREEN;

            FNAS.retrieveRemoteToken(mContext, mGateway, mUuid, mPassword);
        }

        CustomHandler mHandler = new CustomHandler(this);
        mHandler.sendEmptyMessageDelayed(WELCOME, DELAY_TIME_MILLISECOND);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;
    }

    private void startNavPagerActivity() {
        Intent jumpIntent = new Intent(SplashScreenActivity.this, NavPagerActivity.class);
        startActivity(jumpIntent);
        finish();
    }


    private void welcome() {

        if (mUuid != null && mPassword != null && mGateway != null && mToken != null) {

            startNavPagerActivity();

        } else {
            Intent intent = new Intent();
            intent.setClass(SplashScreenActivity.this, EquipmentSearchActivity.class);
            startActivity(intent);
            finish();
        }
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
                    weakReference.get().welcome();
                    break;
                default:
            }
        }
    }

    private void writeOneByteToFile() {

        File file = new File(getExternalFilesDir(null), "test");

        FileOutputStream fileOutputStream = null;

        try {

            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);

            byte[] bytes = {1};

            fileOutputStream.write(bytes);
            fileOutputStream.flush();

            Toast.makeText(mContext, " 创建成功", Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(mContext, " 创建失败", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mContext, " 创建失败", Toast.LENGTH_SHORT).show();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}