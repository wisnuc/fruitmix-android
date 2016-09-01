package com.winsun.fruitmix;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Share;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Administrator on 2016/4/28.
 */
public class ModifyAlbumActivity extends AppCompatActivity {

    TextInputLayout mTitleLayout;
    TextInputEditText tfTitle, tfDesc;
    CheckBox ckPublic;
    CheckBox ckSetMaintainer;
    TextView btOK;
    ImageView ivBack;

    String mSelectedImageUUIDStr;

    private String mMediaShareUuid;
    private ConcurrentMap<String, String> mAblumMap;

    private Context mContext;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_album);

        mContext = this;

        mMediaShareUuid = getIntent().getStringExtra(Util.MEDIASHARE_UUID);
        mAblumMap = LocalCache.SharesMap.get(mMediaShareUuid);
        mSelectedImageUUIDStr = mAblumMap.get("images");
        Log.d("winsun", mSelectedImageUUIDStr);

        tfTitle = (TextInputEditText) findViewById(R.id.title_edit);
        mTitleLayout = (TextInputLayout) findViewById(R.id.title_textlayout);
        mTitleLayout.setHint(mAblumMap.get("title"));
        tfDesc = (TextInputEditText) findViewById(R.id.desc);
        tfDesc.setText(mAblumMap.get("desc"));
        ckPublic = (CheckBox) findViewById(R.id.sPublic);
        if (mAblumMap.get("private").equals("true")) {
            ckPublic.setChecked(false);
        } else {
            ckPublic.setChecked(true);
        }
        ckSetMaintainer = (CheckBox) findViewById(R.id.set_maintainer);
        if (mAblumMap.get("maintained").equals("false")) {
            ckSetMaintainer.setChecked(false);
        } else {
            ckSetMaintainer.setChecked(true);
        }
        btOK = (TextView) findViewById(R.id.ok);
        ivBack = (ImageView) findViewById(R.id.back);

        ckPublic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ckSetMaintainer.setClickable(true);
                } else {
                    ckSetMaintainer.setClickable(false);
                    if (ckSetMaintainer.isChecked()) {
                        ckSetMaintainer.setChecked(false);
                    }
                }
            }
        });

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.hideSoftInput(ModifyAlbumActivity.this);

                final boolean sPublic, sSetMaintainer;
                final String title, desc;

                sPublic = ckPublic.isChecked();
                sSetMaintainer = ckSetMaintainer.isChecked();
                title = tfTitle.getText().toString();
                desc = tfDesc.getText().toString();


                new AsyncTask<Object, Object, Boolean>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);
                    }

                    @Override
                    protected Boolean doInBackground(Object... params) {
                        String data, viewers, maintainers;
                        String[] selectedUIDArr;
                        int i;

                        selectedUIDArr = mSelectedImageUUIDStr.split(",");
                        data = "";
                        for (i = 0; i < selectedUIDArr.length; i++) {
                            data += ",{\\\"type\\\":\\\"media\\\",\\\"digest\\\":\\\"" + selectedUIDArr[i] + "\\\"}";
                        }

                        viewers = "";
                        if (sPublic) {
                            for (String key : LocalCache.UsersMap.keySet()) {
                                viewers += ",\\\"" + key + "\\\"";
                            }
                        } else viewers = ",";

                        if (sSetMaintainer) {
                            maintainers = viewers;
                        } else {
                            maintainers = ",";
                        }

                        if (Util.getNetworkState(mContext)) {

                            data = "{\"commands\": \"[{\\\"op\\\":\\\"replace\\\", \\\"path\\\":\\\"" + mMediaShareUuid + "\\\", \\\"value\\\":{\\\"archived\\\":\\\"false\\\",\\\"album\\\":\\\"true\\\", \\\"maintainers\\\":[\\\"" + FNAS.userUUID + "\\\"], \\\"tags\\\":[{\\\"albumname\\\":\\\"" + title + "\\\", \\\"desc\\\":\\\"" + desc + "\\\"}], \\\"viewers\\\":[" + viewers.substring(1) + "], \\\"maintainers\\\":[" + maintainers.substring(1) + "]}}]\"}";

                            Log.d("winsun", data);
                            try {
                                FNAS.PatchRemoteCall(Util.MEDIASHARE_PARAMETER, data);
                                FNAS.retrieveShareMap();
                                return true;
                            } catch (Exception e) {
                                return false;
                            }
                        } else {

                            DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

                            Share share = dbUtils.getLocalShareByUuid(mMediaShareUuid);

                            share.setTitle(title);
                            share.setDesc(desc);

                            if (sPublic) {
                                share.setViewer(new ArrayList<>(LocalCache.UsersMap.keySet()));
                            }else share.setViewer(new ArrayList<String>());


                            if (sSetMaintainer) {
                                share.setMaintainer(new ArrayList<>(LocalCache.UsersMap.keySet()));
                            } else {
                                share.setMaintainer(Collections.singletonList(FNAS.userUUID));
                            }

                            dbUtils.updateLocalShare(share, share.getUuid());

                            FNAS.loadLocalShare();

                            return true;
                        }


                    }

                    @Override
                    protected void onPostExecute(Boolean sSuccess) {

                        mDialog.dismiss();

                        if (sSuccess) {

                            if (!Util.getNetworkState(mContext)) {
                                LocalBroadcastManager mBroadcastManager = LocalBroadcastManager.getInstance(mContext);
                                mBroadcastManager.sendBroadcast(new Intent(Util.LOCAL_SHARE_CHANGED));
                            }

                            getIntent().putExtra(Util.UPDATED_ALBUM_TITLE, title);
                            setResult(RESULT_OK, getIntent());
                            finish();
                        } else {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }

                }.execute();
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
