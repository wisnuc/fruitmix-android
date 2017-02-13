package com.winsun.fruitmix.refactor.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.winsun.fruitmix.EquipmentSearchActivity;
import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.refactor.common.BaseActivity;
import com.winsun.fruitmix.refactor.common.Injection;
import com.winsun.fruitmix.refactor.contract.SplashContract;
import com.winsun.fruitmix.refactor.presenter.SplashPresenterImpl;
import com.winsun.fruitmix.util.LocalCache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2016/5/9.
 */
public class SplashScreenActivity extends BaseActivity implements SplashContract.SplashView {

    public static final String TAG = SplashScreenActivity.class.getSimpleName();

    private Context mContext;

    private SplashContract.SplashPresenter mSplashPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        mContext = this;

        LocalCache.Init();

        mSplashPresenter = new SplashPresenterImpl(Injection.injectDataRepository());

        mSplashPresenter.attachView(this);

        mSplashPresenter.createDownloadFileStoreFolder();
        mSplashPresenter.loadToken();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSplashPresenter.detachView();

        mContext = null;
    }

    @Override
    public void onBackPressed() {
        mSplashPresenter.handleBackEvent();
    }

    @Override
    public void welcome() {

        Intent intent = new Intent();
        intent.setClass(SplashScreenActivity.this, NavPagerActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void emptyCacheToken() {

        Intent intent = new Intent();
        intent.setClass(SplashScreenActivity.this, EquipmentSearchActivity.class);
        startActivity(intent);
        finish();
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