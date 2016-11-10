package com.winsun.fruitmix;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateNewEquipmentActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.ip_edit)
    TextInputEditText ipInputEditText;
    @BindView(R.id.ok)
    TextView okTextView;
    @BindView(R.id.back)
    ImageView backImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_equipment);

        ButterKnife.bind(this);

        backImageView.setOnClickListener(this);

        okTextView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.ok:
                String ip = ipInputEditText.getText().toString();

                if (ip.equals("")) {

                    Toast.makeText(CreateNewEquipmentActivity.this, getString(R.string.edit_ip_text), Toast.LENGTH_SHORT).show();

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
