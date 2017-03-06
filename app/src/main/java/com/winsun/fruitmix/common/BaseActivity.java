package com.winsun.fruitmix.common;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/2/6.
 */

public abstract class BaseActivity extends AppCompatActivity implements BaseView {

    protected ProgressDialog dialog;

    @Override
    public void showNoNetwork() {
        Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean isNetworkAlive() {
        return Util.getNetworkState(this);
    }

    @Override
    public void dismissDialog() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void showLoadingUI() {

    }

    @Override
    public void dismissLoadingUI() {

    }

    @Override
    public void showNoContentUI() {

    }

    @Override
    public void dismissNoContentUI() {

    }

    @Override
    public void showContentUI() {

    }

    @Override
    public void dismissContentUI() {

    }

    @Override
    public void showDialog() {
        dialog = ProgressDialog.show(this, getString(R.string.operating_title), null, true, false);
    }

    @Override
    public void hideSoftInput() {
        Util.hideSoftInput(this);
    }
}
