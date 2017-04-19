package com.winsun.fruitmix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.LoginType;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2016/5/9.
 */
public class SplashScreenActivity extends AppCompatActivity {

    public static final String TAG = "SplashScreenActivity";

    private String mUuid;
    private String mGateway;
    private String mToken;

    private Context mContext;

    public static final int WELCOME = 0x0010;

    public static final int DELAY_TIME_MILLISECOND = 3 * 1000;

    private CustomHandler mHandler;

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

        result = FileUtil.createLocalPhotoMiniThumbnailFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create local photo mini thumbnail folder failed");
        }

        result = FileUtil.createLocalPhotoThumbnailFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create local photo thumbnail folder failed");
        }

        result = FileUtil.createOriginalPhotoFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create shared photo folder failed");
        }

        FNAS.retrieveLocalMedia(mContext);

        DBUtils dbUtils = DBUtils.getInstance(this);
        LocalCache.LocalLoggedInUsers.addAll(dbUtils.getAllLoggedInUser());

        Log.i(TAG, "onCreate: LocalLoggedInUsers size: " + LocalCache.LocalLoggedInUsers.size());

        mGateway = LocalCache.getGateway(mContext);
        mUuid = LocalCache.getUserUUID(mContext);
        mToken = LocalCache.getToken(mContext);
        String mDeviceID = LocalCache.GetGlobalData(mContext, Util.DEVICE_ID_MAP_NAME);

        if (!mUuid.isEmpty() && mGateway != null && mToken != null) {

            Util.loginType = LoginType.SPLASH_SCREEN;

            FNAS.Gateway = mGateway;
            FNAS.JWT = mToken;
            LocalCache.DeviceID = mDeviceID;
            FNAS.userUUID = mUuid;

            FNAS.retrieveUser(this);
        }

        mHandler = new CustomHandler(this);
        mHandler.sendEmptyMessageDelayed(WELCOME, DELAY_TIME_MILLISECOND);

    }

    @Override
    protected void onResume() {
        super.onResume();

        MobclickAgent.onPageStart(TAG);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        MobclickAgent.onPageEnd(TAG);
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;
    }

    @Override
    public void onBackPressed() {
        mHandler.removeMessages(WELCOME);
        finish();
    }

    private void welcome() {

        Intent intent = new Intent();

        if (mUuid != null && mGateway != null && mToken != null) {
            intent.setClass(SplashScreenActivity.this, NavPagerActivity.class);
        } else {
            intent.setClass(SplashScreenActivity.this, EquipmentSearchActivity.class);
            intent.putExtra(Util.KEY_SHOULD_STOP_SERVICE, true);
        }
        startActivity(intent);
        finish();
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