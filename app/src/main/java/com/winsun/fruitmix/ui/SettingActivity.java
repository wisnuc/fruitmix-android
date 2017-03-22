package com.winsun.fruitmix.ui;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.common.BaseActivity;
import com.winsun.fruitmix.common.Injection;
import com.winsun.fruitmix.contract.SettingContract;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.presenter.SettingPresenterImpl;
import com.winsun.fruitmix.util.FileUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends BaseActivity implements SettingContract.SettingView, View.OnClickListener {

    public static final String TAG = SettingActivity.class.getSimpleName();

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
    @BindView(R.id.already_upload_media_count)
    TextView mAlreadyUploadMediaCountTextView;

    private SettingContract.SettingPresenter mSettingPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        ButterKnife.bind(this);

        mBackImageView.setOnClickListener(this);
        mClearCacheLayout.setOnClickListener(this);

        mSettingPresenter = new SettingPresenterImpl(Injection.injectDataRepository(this));

        mSettingPresenter.attachView(this);
        mSettingPresenter.initView(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSettingPresenter.onDestroy();

        mSettingPresenter.detachView();
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

                                dialog.dismiss();

                                mSettingPresenter.clearCache(SettingActivity.this);

                            }
                        }).setNegativeButton(getString(R.string.cancel), null).create().show();

                break;
        }

    }

    @Override
    public void setAutoUploadPhotosSwitchChecked(boolean checked) {
        mAutoUploadPhotosSwitch.setChecked(checked);
    }

    @Override
    public void setAlreadyUploadMediaCountTextViewVisibility(int visibility) {
        mAlreadyUploadMediaCountTextView.setVisibility(visibility);
    }

    @Override
    public void setAlreadyUploadMediaCountText(String text) {
        mAlreadyUploadMediaCountTextView.setText(text);
    }

    @Override
    public void setCacheSizeText(String text) {
        mCacheSize.setText(text);
    }

    @Override
    public boolean getAutoUploadPhotosSwitchChecked() {
        return mAutoUploadPhotosSwitch.isChecked();
    }
}
