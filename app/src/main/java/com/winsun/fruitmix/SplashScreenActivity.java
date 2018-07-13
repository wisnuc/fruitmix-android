package com.winsun.fruitmix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.init.system.InitSystem;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.newdesign201804.introduction.ProductIntroductionActivity;
import com.winsun.fruitmix.newdesign201804.login.usecase.InjectNewDesignLoginCase;
import com.winsun.fruitmix.newdesign201804.login.LoginEntranceActivity;
import com.winsun.fruitmix.newdesign201804.login.usecase.NewDesignLoginUseCase;
import com.winsun.fruitmix.newdesign201804.mainpage.MainPageActivity;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.util.ToastUtil;

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

    private Context mContext;

    public static final int WELCOME = 0x0010;

    public static final int DELAY_TIME_MILLISECOND = 3 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        mContext = this;

        InitSystem.initSystem(this);

        CustomHandler handler = new CustomHandler(this);
        handler.sendEmptyMessageDelayed(WELCOME, DELAY_TIME_MILLISECOND);

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
//        mHandler.removeMessages(WELCOME);
        finish();
    }

    private void welcome() {

        final Intent intent = new Intent();

/*
        if (loginWithNoParamResult) {

            Log.d(TAG, "welcome: start nav pager");

            intent.setClass(SplashScreenActivity.this, NavPagerActivity.class);
        } else {
            intent.setClass(SplashScreenActivity.this, EquipmentSearchActivity.class);
            intent.putExtra(Util.KEY_SHOULD_STOP_SERVICE, true);
        }
*/

        final SystemSettingDataSource systemSettingDataSource = InjectSystemSettingDataSource.provideSystemSettingDataSource(this);

        if (systemSettingDataSource.needShowProductIntroduction()) {
            intent.setClass(SplashScreenActivity.this, ProductIntroductionActivity.class);

            startActivity(intent);

            finish();

        } else {

            NewDesignLoginUseCase newDesignLoginUseCase = InjectNewDesignLoginCase.Companion.provideInstance(this);

            newDesignLoginUseCase.loginWithNoParam(new BaseOperateCallback() {
                @Override
                public void onSucceed() {

                    MainPageActivity.Companion.start(mContext, systemSettingDataSource.getCurrentEquipmentIp(), "");

                    finish();
                }

                @Override
                public void onFail(OperationResult operationResult) {

                    LoginEntranceActivity.Companion.start(SplashScreenActivity.this);

                }
            });

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

            ToastUtil.showToast(mContext, " 创建成功");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ToastUtil.showToast(mContext, " 创建失败");
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtil.showToast(mContext, " 创建失败");
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