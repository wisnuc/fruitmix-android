package com.winsun.fruitmix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.services.LocalShareService;
import com.winsun.fruitmix.services.LocalCommentService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2016/5/9.
 */
public class SplashScreenActivity extends Activity {

    public static final String TAG = SplashScreenActivity.class.getSimpleName();

    String mUuid;
    String mPassword;
    String mGateway;
    String mToken;

    private Context mContext;

    private CustomHandler mHandler;
    public static final int WELCOME = 0x0010;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        mContext = this;

        mHandler = new CustomHandler(this);
        mHandler.sendEmptyMessageDelayed(WELCOME, 3 * 1000);

//        writeOneByteToFile();
    }

    private void welcome() {
        mGateway = LocalCache.getGateway(mContext);
        mUuid = LocalCache.getUuidValue(mContext);
        mPassword = LocalCache.getPasswordValue(mContext);
        mToken = LocalCache.getJWT(mContext);

        if (mUuid != null && mPassword != null && mGateway != null && mToken != null) {

            login();

        } else {
            Intent intent = new Intent();
            intent.setClass(SplashScreenActivity.this, EquipmentSearchActivity.class);
            //intent.setClass(MainActivity.this, AlbumPicContentActivity.class);
            startActivity(intent);
            finish();
        }
    }


    /**
     * use uuid and password to login
     */
    private void login() {

        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {

                HttpURLConnection conn;
                String str;
                try {
                    conn = (HttpURLConnection) (new URL(mGateway + Util.TOKEN_PARAMETER).openConnection()); //output:{"type":"JWT","token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1dWlkIjoiZGIzYWVlZWYtNzViYS00ZTY2LThmMGUtNWQ3MTM2NWEwNGRiIn0.LqISPNt6T5M1Ae4GN3iL0d8D1bj6m0tX7YOwqZqlnvg"}
                    conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_BASE_HEAD + Base64.encodeToString((mUuid + ":" + mPassword).getBytes(), Base64.DEFAULT));
                    conn.setConnectTimeout(15 * 1000);
                    if (conn.getResponseCode() != 200) {

                        FNAS.Gateway = mGateway;
                        FNAS.JWT = mToken;
                        FNAS.userUUID = mUuid;
                        Util.loginState = false;

                        LocalCache.DeviceID = LocalCache.GetGlobalData(Util.DEVICE_ID_MAP_NAME);

                        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;
                        dbUtils.doOneTaskInCachedThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    FNAS.LoadDocuments();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });

                        return false;

                    } else {

                        Log.i(TAG, "login success");

                        FNAS.Gateway = mGateway;
                        FNAS.userUUID = mUuid;

                        str = FNAS.ReadFull(conn.getInputStream());
                        FNAS.JWT = new JSONObject(str).getString("token"); // get token

                        Util.loginState = true;

                        if (LocalCache.DeviceID == null || LocalCache.DeviceID.equals("")) {
                            //SetGlobalData("deviceID", UUID.randomUUID().toString());
                            str = FNAS.PostRemoteCall("/library/", "");
                            LocalCache.DeviceID = str.replace("\"", "");
                            LocalCache.SetGlobalData(Util.DEVICE_ID_MAP_NAME, LocalCache.DeviceID);
                        } // get deviceID
                        Log.d(TAG, "deviceID: " + LocalCache.GetGlobalData(Util.DEVICE_ID_MAP_NAME));

//                        FNAS.checkOfflineTask(mContext);

                        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;
                        dbUtils.doOneTaskInCachedThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    FNAS.LoadDocuments();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });

                        FNAS.checkLocalShareAndComment(mContext);

                        return true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                    Util.loginState = false;

                    FNAS.Gateway = mGateway;
                    FNAS.JWT = mToken;
                    FNAS.userUUID = mUuid;

                    LocalCache.DeviceID = LocalCache.GetGlobalData(Util.DEVICE_ID_MAP_NAME);

                    DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;
                    dbUtils.doOneTaskInCachedThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                FNAS.LoadDocuments();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

                    return false;

                }

            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (!aBoolean) {
                    Toast.makeText(Util.APPLICATION_CONTEXT, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent(SplashScreenActivity.this, NavPagerActivity.class);
                intent.putExtra(Util.EQUIPMENT_CHILD_NAME, LocalCache.getUserNameValue(mContext));
                startActivity(intent);
                finish();

            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private static class CustomHandler extends Handler {

        WeakReference<SplashScreenActivity> weakReference = null;

        CustomHandler(SplashScreenActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WELCOME:
                    weakReference.get().welcome();
                    break;
                default:
            }
        }
    }

    private void writeOneByteToFile() {

        File file = new File(getDir("test", MODE_PRIVATE), "test");

        FileOutputStream fileOutputStream;

        try {

            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);

            byte[] bytes = {1};

            fileOutputStream.write(bytes);
            fileOutputStream.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}