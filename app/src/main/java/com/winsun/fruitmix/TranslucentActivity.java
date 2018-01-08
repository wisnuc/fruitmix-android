package com.winsun.fruitmix;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.winsun.fruitmix.databinding.NewFirmwareVersionPromptBinding;
import com.winsun.fruitmix.firmware.FirmwareActivity;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.util.Util;

public class TranslucentActivity extends AppCompatActivity {

    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translucent);

        final NewFirmwareVersionPromptBinding binding = NewFirmwareVersionPromptBinding.inflate(LayoutInflater.from(this),
                null, false);

        mAlertDialog = new AlertDialog.Builder(this)
                .setView(binding.getRoot())
                .setCancelable(false)
                .setPositiveButton(R.string.to_upgrade, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        handleToUpgrade(binding.newFirmwareVersionPromptCheckbox.isChecked());

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();

                    }
                }).create();

        mAlertDialog.show();

    }

    private void handleToUpgrade(boolean noLongerPrompt) {

        InjectSystemSettingDataSource.provideSystemSettingDataSource(this).setAskIfNewFirmwareVersionOccur(!noLongerPrompt);

        finish();

        Intent intent = new Intent(this, FirmwareActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);

    }

    @Override
    public void finish() {
        super.finish();

        if (mAlertDialog != null && mAlertDialog.isShowing())
            mAlertDialog.dismiss();

        overridePendingTransition(0, 0);
    }
}
