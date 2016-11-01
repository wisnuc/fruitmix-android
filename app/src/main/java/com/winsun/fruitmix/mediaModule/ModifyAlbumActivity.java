package com.winsun.fruitmix.mediaModule;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.MediaShareOperationEvent;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

    private MediaShare mAlbumMap;

    private Context mContext;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_album);

        ButterKnife.bind(this);

        mContext = this;

        String mMediaShareUuid = getIntent().getStringExtra(Util.MEDIASHARE_UUID);
        mAlbumMap = LocalCache.RemoteMediaShareMapKeyIsUUID.get(mMediaShareUuid);

        tfTitle.setText(mAlbumMap.getTitle());

        tfDesc.setText(mAlbumMap.getDesc());

        ckPublic.setChecked(mAlbumMap.getViewersListSize() != 0);

        ckSetMaintainer.setChecked(mAlbumMap.checkMaintainersListContainCurrentUserUUID());

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
                if (requestData == null) finish();

                mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);

                Intent intent = new Intent(Util.OPERATION);
                intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.MODIFY.name());
                if (Util.getNetworkState(mContext)) {
                    intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIASHARE.name());
                } else {
                    intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIASHARE.name());
                }
                intent.putExtra(Util.OPERATION_MEDIASHARE, mAlbumMap);
                intent.putExtra(Util.KEY_MODIFY_REMOTE_MEDIASHARE_REQUEST_DATA,requestData);

                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
                localBroadcastManager.sendBroadcast(intent);

            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Nullable
    private String createRequestData(boolean sPublic, boolean sSetMaintainer, String title, String desc) {
        String requestData;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");

        boolean isPublic = mAlbumMap.getViewersListSize() != 0;
        if (sPublic != isPublic) {
            if (sPublic) {

                for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
                    mAlbumMap.addViewer(userUUID);
                }

                stringBuilder.append(mAlbumMap.createStringOperateViewersInMediaShare(Util.ADD));

            } else {

                stringBuilder.append(mAlbumMap.createStringOperateViewersInMediaShare(Util.DELETE));

                mAlbumMap.clearViewers();
            }

            stringBuilder.append(",");
        }

        boolean isMaintained = mAlbumMap.checkMaintainersListContainCurrentUserUUID();

        if (sSetMaintainer != isMaintained) {

            if (sSetMaintainer) {

                for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
                    mAlbumMap.addMaintainer(userUUID);
                }

                stringBuilder.append(mAlbumMap.createStringOperateMaintainersInMediaShare(Util.ADD));

            } else {

                stringBuilder.append(mAlbumMap.createStringOperateMaintainersInMediaShare(Util.DELETE));

                mAlbumMap.clearMaintainers();
            }

            stringBuilder.append(",");

        }

        if (!mAlbumMap.getTitle().equals(title) || !mAlbumMap.getDesc().equals(desc)) {

            mAlbumMap.setTitle(title);
            mAlbumMap.setDesc(desc);

            stringBuilder.append(mAlbumMap.createStringReplaceTitleTextAboutMediaShare());

            stringBuilder.append(",");
        }

        requestData = stringBuilder.substring(0,stringBuilder.length() - 1);

        requestData += "]";

        if (requestData.length() == 2) {
            return null;
        }

        return requestData;
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(MediaShareOperationEvent operationEvent){

        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();

        OperationResult operationResult = operationEvent.getOperationResult();

        String action = operationEvent.getAction();

        if (action.equals(Util.LOCAL_SHARE_MODIFIED) || action.equals(Util.REMOTE_SHARE_MODIFIED)) {

            switch (operationResult) {
                case SUCCEED:
                    MediaShare mediaShare = operationEvent.getMediaShare();
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
