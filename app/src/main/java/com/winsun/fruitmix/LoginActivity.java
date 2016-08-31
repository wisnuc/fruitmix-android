package com.winsun.fruitmix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.ExecutorServiceInstance;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends Activity implements View.OnClickListener, EditText.OnFocusChangeListener {

    @BindView(R.id.back)
    ImageView mBack;

    @BindView(R.id.equipment_group_name)
    TextView mEquipmentGroupNameTextView;

    @BindView(R.id.equipment_child_name)
    TextView mEquipmentChildNameTextView;

    @BindView(R.id.pwd_edit)
    EditText mPwdEdit;

    @BindView(R.id.login_btn)
    Button mLoginBtn;

    @BindView(R.id.user_default_portrait)
    TextView mUserDefaultPortrait;

    private Context mContext;

    private String mEquipmentGroupName;
    private String mEquipmentChildName;
    private String mUserUUid;
    private String mPwd;
    private String mGateway;
    private String mJwt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        mContext = this;

        mBack.setOnClickListener(this);
        mPwdEdit.setOnFocusChangeListener(this);
        mLoginBtn.setOnClickListener(this);

        Intent intent = getIntent();
        mEquipmentGroupName = intent.getStringExtra(Util.EQUIPMENT_GROUP_NAME);
        mEquipmentChildName = intent.getStringExtra(Util.EQUIPMENT_CHILD_NAME);
        mUserUUid = intent.getStringExtra(Util.USER_UUID);
        mGateway = intent.getStringExtra(Util.GATEWAY);

        mEquipmentGroupNameTextView.setText(mEquipmentGroupName);
        mEquipmentChildNameTextView.setText(mEquipmentChildName);

        StringBuilder stringBuilder = new StringBuilder();
        String[] splitStrings = mEquipmentChildName.split(" ");
        for (String splitString : splitStrings) {
            stringBuilder.append(splitString.substring(0, 1).toUpperCase());
        }
        mUserDefaultPortrait.setText(stringBuilder.toString());
        int color = (int) (Math.random() * 3);
        switch (color) {
            case 0:
                mUserDefaultPortrait.setBackgroundResource(R.drawable.user_portrait_bg_blue);
                break;
            case 1:
                mUserDefaultPortrait.setBackgroundResource(R.drawable.user_portrait_bg_green);
                break;
            case 2:
                mUserDefaultPortrait.setBackgroundResource(R.drawable.user_portrait_bg_yellow);
                break;
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.login_btn:
                Util.hideSoftInput(LoginActivity.this);

                mPwd = mPwdEdit.getText().toString();
                login(mLoginBtn);
                break;
            default:
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mPwdEdit.setHint("");
        } else {
            mPwdEdit.setHint(getString(R.string.password_text));
        }
    }

    /**
     * use uuid and password to login
     *
     * @param view show Snackbar when error occurs
     */
    private void login(final View view) {

        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {

                HttpURLConnection conn;
                String str;
                try {
                    conn = (HttpURLConnection) (new URL(mGateway + Util.TOKEN_PARAMETER).openConnection()); //output:{"type":"JWT","token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1dWlkIjoiZGIzYWVlZWYtNzViYS00ZTY2LThmMGUtNWQ3MTM2NWEwNGRiIn0.LqISPNt6T5M1Ae4GN3iL0d8D1bj6m0tX7YOwqZqlnvg"}
                    conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_BASE_HEAD + Base64.encodeToString((mUserUUid + ":" + mPwd).getBytes(), Base64.DEFAULT));
                    if (conn.getResponseCode() != 200) {

                        Util.loginState = false;
                        return false;

                    } else {

                        if (!mUserUUid.equals(FNAS.userUUID)) {
                            LocalCache.CleanAll();
                            LocalCache.Init(LoginActivity.this);
                        }

                        FNAS.Gateway = mGateway;
                        FNAS.userUUID = mUserUUid;

                        str = FNAS.ReadFull(conn.getInputStream());
                        mJwt = new JSONObject(str).getString("token"); // get token
                        FNAS.JWT = mJwt;

                        Util.loginState = true;

                        if (LocalCache.DeviceID == null || LocalCache.DeviceID.equals("")) {
                            //SetGlobalData("deviceID", UUID.randomUUID().toString());
                            str = FNAS.PostRemoteCall("/library/", "");
                            LocalCache.DeviceID = str.replace("\"", "");
                            LocalCache.SetGlobalData(Util.DEVICE_ID_MAP_NAME, LocalCache.DeviceID);
                        } // get deviceID
                        Log.d("uuid", LocalCache.GetGlobalData(Util.DEVICE_ID_MAP_NAME));

                        ExecutorServiceInstance instance = ExecutorServiceInstance.SINGLE_INSTANCE;
                        instance.doOneTaskInCachedThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    FNAS.loadData();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });

                        LocalCache.saveGateway(mGateway, mContext);
                        LocalCache.saveJwt(mJwt, mContext);
                        setGroupNameUserName(mEquipmentGroupName, mEquipmentChildName);
                        setUuidPassword(mUserUUid, mPwd);

                        FNAS.checkLocalShareAndComment(mContext);

                        Intent intent = new Intent(mContext, NavPagerActivity.class);
                        intent.putExtra(Util.EQUIPMENT_CHILD_NAME, mEquipmentChildName);
                        startActivity(intent);

                        setResult(RESULT_OK);

                        finish();

                        return true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                    Util.loginState = false;
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {

                if (!aBoolean) {
                    Snackbar.make(view, getString(R.string.password_error), Snackbar.LENGTH_SHORT).show();
                }

            }
        }.execute();

    }

    private void setGroupNameUserName(String groupName, String userName) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.EQUIPMENT_GROUP_NAME, groupName);
        editor.putString(Util.EQUIPMENT_CHILD_NAME, userName);
        editor.apply();
    }

    private void setUuidPassword(String uuid, String password) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.USER_UUID, uuid);
        editor.putString(Util.PASSWORD, password);
        editor.apply();
    }

}
