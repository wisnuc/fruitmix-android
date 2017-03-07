package com.winsun.fruitmix;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.back)
    ImageView mBackImageView;
    @BindView(R.id.auto_upload_photos_switch)
    SwitchCompat mAutoUploadPhotosSwitch;
    @BindView(R.id.mobile_network_upload_switch)
    SwitchCompat mMobileNetworkUploadSwitch;
    @BindView(R.id.cache_size)
    TextView mCacheSize;
    @BindView(R.id.clear_cache_layout)
    ViewGroup mClearCacheLayout;

    private boolean mAutoUploadOrNot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ButterKnife.bind(this);

        mBackImageView.setOnClickListener(this);
        mClearCacheLayout.setOnClickListener(this);

        mCacheSize.setText(FileUtil.formatFileSize(FileUtil.getTotalCacheSize(this)));

        mAutoUploadOrNot = LocalCache.getAutoUploadOrNot(this);
        if (mAutoUploadOrNot) {
            mAutoUploadPhotosSwitch.setChecked(true);
        } else {
            mAutoUploadPhotosSwitch.setChecked(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        boolean isChecked = mAutoUploadPhotosSwitch.isChecked();

        if (mAutoUploadOrNot != isChecked) {
            LocalCache.setAutoUploadOrNot(this, isChecked);

            if (isChecked) {
                LocalCache.setCurrentUploadDeviceID(this, LocalCache.DeviceID);
                EventBus.getDefault().post(new RequestEvent(OperationType.START_UPLOAD, null));
            } else {
                EventBus.getDefault().post(new RequestEvent(OperationType.STOP_UPLOAD, null));
            }
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.clear_cache_layout:

                new AlertDialog.Builder(this).setMessage(getString(R.string.confirm_delete))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                FileUtil.clearAllCache(SettingActivity.this);
                                dialog.dismiss();
                                mCacheSize.setText(FileUtil.formatFileSize(FileUtil.getTotalCacheSize(SettingActivity.this)));
                            }
                        }).setNegativeButton(getString(R.string.cancel), null).create().show();

                break;
        }

    }

}
