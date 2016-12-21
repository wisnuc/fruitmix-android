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
import com.winsun.fruitmix.operationResult.OperationResult;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResultType;
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

        CustomHandler mHandler = new CustomHandler(this);
        mHandler.sendEmptyMessage(WELCOME);

        FNAS.retrieveLocalMediaMap(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        OperationEvent stickyEvent = EventBus.getDefault().removeStickyEvent(OperationEvent.class);

        if (stickyEvent != null) {
            String action = stickyEvent.getAction();

            if (action.equals(Util.REMOTE_TOKEN_RETRIEVED)) {

                handleRemoteTokenRetrieved(stickyEvent);

            } else if (action.equals(Util.REMOTE_DEVICEID_RETRIEVED)) {

                handleRemoteDeviceIDRetrieved(stickyEvent);

            } else if (action.equals(Util.REMOTE_USER_RETRIEVED)) {

                startNavPagerActivity();

            }
        }

    }

    private void startNavPagerActivity() {
        Intent jumpIntent = new Intent(SplashScreenActivity.this, NavPagerActivity.class);
        jumpIntent.putExtra(Util.EQUIPMENT_CHILD_NAME, LocalCache.getUserNameValue(mContext));
        startActivity(jumpIntent);
        finish();
    }

    private void handleRemoteDeviceIDRetrieved(OperationEvent operationEvent) {

        OperationResult result = operationEvent.getOperationResult();

        OperationResultType resultType = result.getOperationResultType();
        switch (resultType) {
            case SUCCEED:
                Log.i(TAG, "login success");
                Util.loginState = true;
                break;
            default:
                Util.loginState = false;
                LocalCache.DeviceID = LocalCache.GetGlobalData(this, Util.DEVICE_ID_MAP_NAME);
                Toast.makeText(SplashScreenActivity.this, result.getResultMessage(this), Toast.LENGTH_SHORT).show();
                break;
        }

        FNAS.retrieveUserMap(mContext);
    }

    private void handleRemoteTokenRetrieved(OperationEvent operationEvent) {

        OperationResult result = operationEvent.getOperationResult();

        OperationResultType resultType = result.getOperationResultType();
        FNAS.userUUID = mUuid;
        FNAS.Gateway = mGateway;

        Log.i(TAG, "onReceive: remote token retrieve:" + resultType.name());

        switch (resultType) {
            case SUCCEED:

                FNAS.retrieveRemoteDeviceID(mContext);

                break;
            default:

                Util.loginState = false;

                FNAS.JWT = mToken;

                LocalCache.DeviceID = LocalCache.GetGlobalData(this, Util.DEVICE_ID_MAP_NAME);

                Toast.makeText(SplashScreenActivity.this, result.getResultMessage(this), Toast.LENGTH_SHORT).show();

                FNAS.retrieveUserMap(mContext);

                break;
        }
    }


    private void welcome() {
        mGateway = LocalCache.getGateway(mContext);
        mUuid = LocalCache.getUuidValue(mContext);
        mPassword = LocalCache.getPasswordValue(mContext);
        mToken = LocalCache.getToken(mContext);

        if (mUuid != null && mPassword != null && mGateway != null && mToken != null) {

            login();

        } else {
            Intent intent = new Intent();
            intent.setClass(SplashScreenActivity.this, EquipmentSearchActivity.class);
            startActivity(intent);
            finish();
        }
    }


    /**
     * use uuid and password to login
     */
    private void login() {

        FNAS.retrieveRemoteToken(mContext, mGateway, mUuid, mPassword);

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