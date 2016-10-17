package com.winsun.fruitmix;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    List<String> mSelectedImageUUIDStr;

    private String mMediaShareUuid;
    private MediaShare mAblumMap;

    private Context mContext;

    private ProgressDialog mDialog;

    private LocalBroadcastManager localBroadcastManager;
    private CustomReceiver customReceiver;
    private IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_album);

        mContext = this;

        mMediaShareUuid = getIntent().getStringExtra(Util.MEDIASHARE_UUID);
        mAblumMap = LocalCache.RemoteMediaShareMapKeyIsUUID.get(mMediaShareUuid);
        mSelectedImageUUIDStr = mAblumMap.getMediaDigestInMediaShareContents();

        tfTitle = (TextInputEditText) findViewById(R.id.title_edit);
        mTitleLayout = (TextInputLayout) findViewById(R.id.title_textlayout);
        mTitleLayout.setHint(mAblumMap.getTitle());

        tfDesc = (TextInputEditText) findViewById(R.id.desc);
        tfDesc.setText(mAblumMap.getDesc());

        ckPublic = (CheckBox) findViewById(R.id.sPublic);
        ckPublic.setChecked(!mAblumMap.getViewers().isEmpty());

        ckSetMaintainer = (CheckBox) findViewById(R.id.set_maintainer);
        ckSetMaintainer.setChecked(mAblumMap.getMaintainers().contains(FNAS.userUUID));

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

                mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);

                if (sPublic) {
                    for(String userUUID:LocalCache.RemoteUserMapKeyIsUUID.keySet()){
                        mAblumMap.addViewer(userUUID);
                    }
                } else mAblumMap.clearViewers();

                if (sSetMaintainer) {
                    for(String userUUID:LocalCache.RemoteUserMapKeyIsUUID.keySet()){
                        mAblumMap.addMaintainer(userUUID);
                    }
                } else {
                    mAblumMap.clearMaintainers();
                }
                mAblumMap.setTitle(title);
                mAblumMap.setDesc(desc);

                Intent intent = new Intent(Util.OPERATION);
                intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.MODIFY.name());
                if (Util.getNetworkState(mContext)) {
                    intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIASHARE.name());
                } else {
                    intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIASHARE.name());
                }
                intent.putExtra(Util.OPERATION_MEDIASHARE, mAblumMap);
                localBroadcastManager.sendBroadcast(intent);

            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        customReceiver = new CustomReceiver();
        filter = new IntentFilter(Util.LOCAL_SHARE_MODIFIED);
        filter.addAction(Util.REMOTE_SHARE_MODIFIED);

    }

    @Override
    protected void onResume() {
        super.onResume();

        localBroadcastManager.registerReceiver(customReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        localBroadcastManager.unregisterReceiver(customReceiver);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private class CustomReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (mDialog != null && mDialog.isShowing())
                mDialog.dismiss();

            String result = intent.getStringExtra(Util.OPERATION_RESULT_NAME);

            OperationResult operationResult = OperationResult.valueOf(result);

            if (intent.getAction().equals(Util.LOCAL_SHARE_MODIFIED) || intent.getAction().equals(Util.REMOTE_SHARE_MODIFIED)) {

                switch (operationResult) {
                    case SUCCEED:
                        MediaShare mediaShare = intent.getParcelableExtra(Util.OPERATION_MEDIASHARE);
                        getIntent().putExtra(Util.UPDATED_ALBUM_TITLE, mediaShare.getTitle());
                        ModifyAlbumActivity.this.setResult(RESULT_OK, getIntent());
                        finish();
                        break;
                    case FAIL:
                        ModifyAlbumActivity.this.setResult(RESULT_CANCELED);
                        finish();
                        break;
                }

            }

        }
    }

}
