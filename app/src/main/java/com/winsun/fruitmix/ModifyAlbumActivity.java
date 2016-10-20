package com.winsun.fruitmix;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/4/28.
 */
public class ModifyAlbumActivity extends AppCompatActivity {


    @BindView(R.id.title_textlayout)
    TextInputLayout mTitleLayout;
    @BindView(R.id.title_edit)
    TextInputEditText tfTitle;
    @BindView(R.id.desc)
    TextInputEditText tfDesc;
    @BindView(R.id.sPublic)
    CheckBox ckPublic;
    @BindView(R.id.set_maintainer)
    CheckBox ckSetMaintainer;
    @BindView(R.id.ok)
    TextView btOK;
    @BindView(R.id.back)
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

        ButterKnife.bind(this);

        mContext = this;

        mMediaShareUuid = getIntent().getStringExtra(Util.MEDIASHARE_UUID);
        mAblumMap = LocalCache.RemoteMediaShareMapKeyIsUUID.get(mMediaShareUuid);
        mSelectedImageUUIDStr = mAblumMap.getMediaDigestInMediaShareContents();

        mTitleLayout.setHint(mAblumMap.getTitle());

        tfDesc.setText(mAblumMap.getDesc());

        ckPublic.setChecked(mAblumMap.getViewersListSize() != 0);

        ckSetMaintainer.setChecked(mAblumMap.checkMaintainersListContainCurrentUserUUID());

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

                String requestData = createRequestData(sPublic, sSetMaintainer, title, desc);
                if (requestData == null) return;

                mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);

                Intent intent = new Intent(Util.OPERATION);
                intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.MODIFY.name());
                if (Util.getNetworkState(mContext)) {
                    intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIASHARE.name());
                } else {
                    intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIASHARE.name());
                }
                intent.putExtra(Util.OPERATION_MEDIASHARE, mAblumMap);
                intent.putExtra(Util.KEY_MODIFY_REMOTE_MEDIASHARE_REQUEST_DATA,requestData);
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

    @Nullable
    private String createRequestData(boolean sPublic, boolean sSetMaintainer, String title, String desc) {
        String requestData;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");

        boolean isPublic = mAblumMap.getViewersListSize() != 0;
        if (sPublic != isPublic) {
            if (sPublic) {

                for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
                    mAblumMap.addViewer(userUUID);
                }

                stringBuilder.append(mAblumMap.createStringOperateViewersInMediaShare(Util.ADD));

            } else {

                stringBuilder.append(mAblumMap.createStringOperateViewersInMediaShare(Util.DELETE));

                mAblumMap.clearViewers();
            }

            stringBuilder.append(",");
        }

        boolean isMaintained = mAblumMap.checkMaintainersListContainCurrentUserUUID();

        if (sSetMaintainer != isMaintained) {

            if (sSetMaintainer) {

                for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
                    mAblumMap.addMaintainer(userUUID);
                }

                stringBuilder.append(mAblumMap.createStringOperateMaintainersInMediaShare(Util.ADD));

            } else {

                stringBuilder.append(mAblumMap.createStringOperateMaintainersInMediaShare(Util.DELETE));

                mAblumMap.clearMaintainers();
            }

            stringBuilder.append(",");

        }

        if (!mAblumMap.getTitle().equals(title) || !mAblumMap.getDesc().equals(desc)) {

            mAblumMap.setTitle(title);
            mAblumMap.setDesc(desc);

            stringBuilder.append(mAblumMap.createStringReplaceTitleTextAboutMediaShare());

            stringBuilder.append(",");
        }

        requestData = stringBuilder.substring(0,stringBuilder.length() - 1);

        requestData += "]";

        if (requestData.length() == 2) {
            Toast.makeText(mContext, getString(R.string.modify_nothing_about_mediashare), Toast.LENGTH_SHORT).show();
            return null;
        }

        return requestData;
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
