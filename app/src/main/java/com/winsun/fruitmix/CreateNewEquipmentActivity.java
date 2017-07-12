package com.winsun.fruitmix;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateNewEquipmentActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.title)
    TextView mLayoutTitle;
    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.select)
    TextView rightTextView;

    @BindView(R.id.ip_edit)
    TextInputEditText ipInputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_equipment);

        ButterKnife.bind(this);

        mLayoutTitle.setText("手动输入ip");

        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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
                String ip = ipInputEditText.getText().toString();

                if (ip.equals("")) {

                    Toast.makeText(CreateNewEquipmentActivity.this, "请输入ip", Toast.LENGTH_SHORT).show();

                } else {
                    Intent intent = new Intent();
                    intent.putExtra(Util.KEY_MANUAL_INPUT_IP, ip);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
        }
    }
}
