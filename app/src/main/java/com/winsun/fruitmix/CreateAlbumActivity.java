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
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Administrator on 2016/4/28.
 */
public class CreateAlbumActivity extends AppCompatActivity {

    public static final String TAG = CreateAlbumActivity.class.getSimpleName();

    TextInputLayout mTitleLayout;
    TextInputEditText tfTitle, tfDesc;
    CheckBox ckPublic;
    CheckBox ckSetMaintainer;
    TextView btOK;
    ImageView ivBack;
    TextView mLayoutTitle;

    String[] mSelectedImageUUIDArray;

    private Context mContext;

    private ProgressDialog mDialog;

    private String mTitle;

    private LocalBroadcastManager localBroadcastManager;
    private CustomReceiver customReceiver;
    private IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        mSelectedImageUUIDArray = getIntent().getStringArrayExtra(Util.KEY_SELECTED_IMAGE_UUID_ARRAY);

        setContentView(R.layout.activity_create_album);

        tfTitle = (TextInputEditText) findViewById(R.id.title_edit);
        mTitleLayout = (TextInputLayout) findViewById(R.id.title_textlayout);
        tfDesc = (TextInputEditText) findViewById(R.id.desc);
        ckPublic = (CheckBox) findViewById(R.id.sPublic);
        btOK = (TextView) findViewById(R.id.ok);
        ivBack = (ImageView) findViewById(R.id.back);
        ckSetMaintainer = (CheckBox) findViewById(R.id.set_maintainer);
        mLayoutTitle = (TextView) findViewById(R.id.layout_title);
        mLayoutTitle.setText(getString(R.string.create_album_text));

        mTitle = String.format(getString(R.string.title_hint), new SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE).format(new Date(System.currentTimeMillis())));
        mTitleLayout.setHint(mTitle);

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
        ckSetMaintainer.setClickable(false);

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                Util.hideSoftInput(CreateAlbumActivity.this);

                final boolean sPublic, sSetMaintainer;
                final String title, desc;

                sPublic = ckPublic.isChecked();
                sSetMaintainer = ckSetMaintainer.isChecked();

                if (tfTitle.getText().toString().equals("")) {
                    title = mTitleLayout.getHint().toString();
                } else {
                    title = tfTitle.getText().toString();
                }

                desc = tfDesc.getText().toString();

                mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);

                Intent intent = new Intent(Util.OPERATION);
                intent.putExtra(Util.OPERATION_TYPE, OperationType.CREATE.name());
                intent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.LOCAL_MEDIASHARE.name());
                intent.putExtra(Util.OPERATION_MEDIASHARE, generateMediaShare(sPublic, sSetMaintainer, title, desc, mSelectedImageUUIDArray));
                localBroadcastManager.sendBroadcast(intent);

            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        customReceiver = new CustomReceiver();
        filter = new IntentFilter(Util.LOCAL_SHARE_CREATED);

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

    private MediaShare generateMediaShare(boolean isPublic, boolean otherMaintianer, String title, String desc, String[] digests) {

        MediaShare mediaShare = new MediaShare();
        mediaShare.setUuid(Util.createLocalUUid());

        Log.i(TAG, "create album digest:" + digests);

        mediaShare.setImageDigests(Arrays.asList(digests));
        mediaShare.setCoverImageDigest(digests[0]);
        mediaShare.setTitle(title);
        mediaShare.setDesc(desc);

        if (isPublic) {
            mediaShare.setViewer(new ArrayList<>(LocalCache.RemoteUserMapKeyIsUUID.keySet()));
        } else mediaShare.setViewer(Collections.<String>emptyList());

        if (otherMaintianer) {
            mediaShare.setMaintainer(new ArrayList<>(LocalCache.RemoteUserMapKeyIsUUID.keySet()));
        } else {
            mediaShare.setMaintainer(Collections.singletonList(FNAS.userUUID));
        }

        mediaShare.setCreatorUUID(FNAS.userUUID);
        mediaShare.setTime(String.valueOf(System.currentTimeMillis()));
        mediaShare.setAlbum(true);
        mediaShare.setLocked(true);
        mediaShare.setArchived(false);
        mediaShare.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(mediaShare.getTime()))));

        return mediaShare;

    }

    private class CustomReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Util.LOCAL_SHARE_CREATED)) {

                if (mDialog != null && mDialog.isShowing())
                    mDialog.dismiss();

                String result = intent.getStringExtra(Util.OPERATION_RESULT);

                OperationResult operationResult = OperationResult.valueOf(result);

                switch (operationResult) {
                    case SUCCEED:
                        if (Util.getNetworkState(mContext)) {
                            MediaShare mediaShare = intent.getParcelableExtra(Util.OPERATION_MEDIASHARE);
                            Intent operationIntent = new Intent(Util.OPERATION);
                            operationIntent.putExtra(Util.OPERATION_TYPE, OperationType.CREATE.name());
                            operationIntent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.REMOTE_MEDIASHARE.name());
                            operationIntent.putExtra(Util.OPERATION_MEDIASHARE, mediaShare);
                            localBroadcastManager.sendBroadcast(operationIntent);
                        }

                        CreateAlbumActivity.this.setResult(RESULT_OK);
                        break;
                    case FAIL:
                        Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
                        CreateAlbumActivity.this.setResult(RESULT_CANCELED);
                        break;
                }

                finish();
            }


        }
    }
}
