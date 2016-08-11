package com.winsun.fruitmix;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PersonInfoActivity extends Activity implements EditText.OnFocusChangeListener, View.OnClickListener {

    @BindView(R.id.back)
    ImageView mBack;

    @BindView(R.id.finish)
    TextView mFinish;

    @BindView(R.id.email_edit)
    EditText mEmailEdit;

    @BindView(R.id.password_edit)
    EditText mPasswordEdit;

    @BindView(R.id.confirm_password_edit)
    EditText mConfirmPasswordEdit;

    @BindView(R.id.login_by_password_check)
    CheckBox mLoginByPasswordCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);

        ButterKnife.bind(this);

        mEmailEdit.setOnFocusChangeListener(this);
        mPasswordEdit.setOnFocusChangeListener(this);
        mConfirmPasswordEdit.setOnFocusChangeListener(this);

        mBack.setOnClickListener(this);
        mFinish.setOnClickListener(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        EditText editText = (EditText) v;

        switch (v.getId()) {
            case R.id.email_edit:
                if (hasFocus) {
                    editText.setHint("");
                } else {
                    editText.setHint(getString(R.string.email_text));
                }
                break;
            case R.id.password_edit:
                if (hasFocus) {
                    editText.setHint("");
                } else {
                    editText.setHint(getString(R.string.password_text));
                }
                break;
            case R.id.confirm_password_edit:
                if (hasFocus) {
                    editText.setHint("");
                } else {
                    editText.setHint(getString(R.string.confirm_password_text));
                }
                break;
            default:
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.finish:
                break;
            default:
        }
    }
}
