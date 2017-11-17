package com.winsun.fruitmix;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.winsun.fruitmix.dialog.DialogFactory;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.logout.InjectLogoutUseCase;
import com.winsun.fruitmix.network.InjectNetworkStateManager;
import com.winsun.fruitmix.network.NetworkReceiver;
import com.winsun.fruitmix.network.NetworkState;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Administrator on 2017/3/7.
 */

public class BaseActivity extends AppCompatActivity implements BaseView {

    public static final String TAG = BaseActivity.class.getSimpleName();

    protected String action;

    private ProgressDialog mDialog;

    private NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*
        networkReceiver = new NetworkReceiver();

        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, intentFilter);
*/


    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        MobclickAgent.onPageStart(TAG);
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

//        MobclickAgent.onPageEnd(TAG);
//        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {

        EventBus.getDefault().unregister(this);

        super.onStop();
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        dismissDialog();

        mDialog = null;

/*
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
*/

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        action = operationEvent.getAction();

        if (action.equals(Util.TOKEN_INVALID)) {
            FNAS.handleLogout();
            InjectLogoutUseCase.provideLogoutUseCase(this).logout();

            showToast("token失效");

            EquipmentSearchActivity.gotoEquipmentActivity(this, true);

        } else if (action.equals(Util.NETWORK_CHANGED)) {

//            checkShowAutoUploadWhenConnectedWithMobileNetwork();

        } else if (action.equals(Util.KEY_STOP_CURRENT_ACTIVITY)) {

            Log.d(TAG, "handleOperationEvent: call onBackPressed in current activity " + this);

        }

    }

    @Override
    public void finishView() {
        finish();
    }

    @Override
    public void setResultCode(int resultCode) {
        setResult(resultCode);
    }

    @Override
    public Dialog showProgressDialog(String message) {
        mDialog = ProgressDialog.show(this, null, message, true, false);

        return mDialog;
    }

    @Override
    public void dismissDialog() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showCustomErrorCode(String text) {
        Toast.makeText(this, String.format(getString(R.string.server_exception), text), Toast.LENGTH_SHORT).show();
    }

}
