package com.winsun.fruitmix;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.util.ToastUtil;
import com.winsun.fruitmix.util.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CreateNewEquipmentActivity extends BaseActivity implements View.OnClickListener {

    TextView mLayoutTitle;
    Toolbar mToolBar;
    TextView rightTextView;
    TextInputEditText ipInputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_equipment);

        mLayoutTitle = findViewById(R.id.title);
        mToolBar = findViewById(R.id.toolbar);
        rightTextView = findViewById(R.id.select);
        ipInputEditText = findViewById(R.id.ip_edit);

        mLayoutTitle.setText(getString(R.string.enter_ip));

        setSupportActionBar(mToolBar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);

        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rightTextView.setVisibility(View.VISIBLE);
        rightTextView.setText(getString(R.string.finish_text));
        rightTextView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select:

                Util.hideSoftInput(CreateNewEquipmentActivity.this);

                String ip = ipInputEditText.getText().toString();

                if (ip.equals("")) {

                    ToastUtil.showToast(CreateNewEquipmentActivity.this,getString(R.string.enter_ip));

                } else {

                    if (Util.checkIpLegal(ip)) {

                        Intent intent = new Intent();
                        intent.putExtra(Util.KEY_MANUAL_INPUT_IP, ip);
                        setResult(RESULT_OK, intent);
                        finish();

                    } else {

                        ToastUtil.showToast(CreateNewEquipmentActivity.this, getString(R.string.ip_illegal));

                    }

                }
                break;
        }
    }
}
