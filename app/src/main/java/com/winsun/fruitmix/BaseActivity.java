package com.winsun.fruitmix;

import android.support.v7.app.AppCompatActivity;

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
}
