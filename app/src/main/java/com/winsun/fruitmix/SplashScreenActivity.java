package com.winsun.fruitmix;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

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

    String mUuid;
    String mPassword;
    String mGateway;
    String mToken;

    private Context mContext;

    public static final int WELCOME = 0x0010;

    public static final int DELAY_TIME_MILLISECOND = 3 * 1000;

    private LocalBroadcastManager localBroadcastManager;
    private CustomReceiver customReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        mContext = this;

        LocalCache.Init(this);

        initFilterForReceiver();

        CustomHandler mHandler = new CustomHandler(this);
        mHandler.sendEmptyMessageDelayed(WELCOME, DELAY_TIME_MILLISECOND);
    }

    private void initFilterForReceiver() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter(Util.REMOTE_TOKEN_RETRIEVED);
        filter.addAction(Util.REMOTE_DEVICEID_RETRIEVED);
        filter.addAction(Util.REMOTE_USER_RETRIEVED);
        customReceiver = new CustomReceiver();
        localBroadcastManager.registerReceiver(customReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        localBroadcastManager.unregisterReceiver(customReceiver);
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

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.GET.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_TOKEN.name());
        intent.putExtra(Util.GATEWAY, mGateway);
        intent.putExtra(Util.USER_UUID, mUuid);
        intent.putExtra(Util.PASSWORD, mPassword);
        localBroadcastManager.sendBroadcast(intent);

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

    private class CustomReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Util.REMOTE_TOKEN_RETRIEVED)) {

                handleRemoteTokenRetrieved(intent);

            } else if (intent.getAction().equals(Util.REMOTE_DEVICEID_RETRIEVED)) {

                handleRemoteDeviceIDRetrieved(intent);

            } else if (intent.getAction().equals(Util.REMOTE_USER_RETRIEVED)) {

                startNavPagerActivity();

            }

        }

        private void startNavPagerActivity() {
            Intent jumpIntent = new Intent(SplashScreenActivity.this, NavPagerActivity.class);
            jumpIntent.putExtra(Util.EQUIPMENT_CHILD_NAME, LocalCache.getUserNameValue(mContext));
            startActivity(jumpIntent);
            finish();
        }

        private void handleRemoteDeviceIDRetrieved(Intent intent) {
            OperationResult result = OperationResult.valueOf(intent.getStringExtra(Util.OPERATION_RESULT_NAME));
            switch (result) {
                case SUCCEED:
                    Log.i(TAG, "login success");
                    Util.loginState = true;

                    break;
                case FAIL:
                    Util.loginState = false;
                    LocalCache.DeviceID = LocalCache.GetGlobalData(Util.DEVICE_ID_MAP_NAME);
                    Toast.makeText(SplashScreenActivity.this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();

                    break;
            }

            FNAS.Gateway = mGateway;
            FNAS.userUUID = mUuid;
            FNAS.retrieveUserMap(mContext);
        }

        private void handleRemoteTokenRetrieved(Intent intent) {
            OperationResult result = OperationResult.valueOf(intent.getStringExtra(Util.OPERATION_RESULT_NAME));

            FNAS.userUUID = mUuid;
            FNAS.Gateway = mGateway;

            Log.i(TAG, "onReceive: remote token retrieve:" + result.name());

            switch (result) {
                case SUCCEED:

                    Intent operationIntent = new Intent(Util.OPERATION);
                    operationIntent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.GET.name());
                    operationIntent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_DEVICEID.name());
                    localBroadcastManager.sendBroadcast(operationIntent);

                    break;
                case FAIL:

                    Util.loginState = false;

                    FNAS.JWT = mToken;

                    LocalCache.DeviceID = LocalCache.GetGlobalData(Util.DEVICE_ID_MAP_NAME);

                    Toast.makeText(SplashScreenActivity.this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();

                    FNAS.retrieveUserMap(mContext);

                    break;
            }
        }
    }

}