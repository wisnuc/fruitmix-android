package com.winsun.fruitmix;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.winsun.fruitmix.dialog.DialogFactory;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Administrator on 2017/3/7.
 */

public class BaseActivity extends AppCompatActivity implements BaseView {

    protected String action;

    private ProgressDialog mDialog;

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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        action = operationEvent.getAction();

        if (action.equals(Util.TOKEN_INVALID)) {
            FNAS.handleLogout();
            EquipmentSearchActivity.gotoEquipmentActivity(this, true);
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
}
