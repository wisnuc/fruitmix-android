package com.winsun.fruitmix.mediaModule;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
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
import com.winsun.fruitmix.model.OperationResultType;
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
    @BindView(R.id.desc_textlayout)
    TextInputLayout mDescLayout;
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
        mAlbumMap = LocalCache.RemoteMediaShareMapKeyIsUUID.get(mMediaShareUuid).cloneMyself();

        mTitleLayout.getEditText().setText(mAlbumMap.getTitle());
        mDescLayout.getEditText().setText(mAlbumMap.getDesc());

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
                title = mTitleLayout.getEditText().getText().toString();
                desc = mDescLayout.getEditText().getText().toString();

                String requestData = createRequestData(sPublic, sSetMaintainer, title, desc);
                if (requestData == null) {

                    ModifyAlbumActivity.this.setResult(RESULT_CANCELED);
                    finish();

                    return;
                }

                mDialog = ProgressDialog.show(mContext, null, getString(R.string.operating_title), true, false);

                mAlbumMap.sendModifyMediaShareRequest(mContext, requestData);
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ModifyAlbumActivity.this.setResult(RESULT_CANCELED);
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

        requestData = stringBuilder.substring(0, stringBuilder.length() - 1);

        requestData += "]";

        if (requestData.length() <= 2) {
            return null;
        }

        return requestData;
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(MediaShareOperationEvent operationEvent) {

        String action = operationEvent.getAction();

        if (action.equals(Util.LOCAL_SHARE_MODIFIED) || action.equals(Util.REMOTE_SHARE_MODIFIED)) {

            if (mDialog != null && mDialog.isShowing())
                mDialog.dismiss();

            OperationResultType operationResultType = operationEvent.getOperationResult().getOperationResultType();

            switch (operationResultType) {
                case SUCCEED:
                    MediaShare mediaShare = operationEvent.getMediaShare();
                    getIntent().putExtra(Util.UPDATED_ALBUM_TITLE, mediaShare.getTitle());
                    ModifyAlbumActivity.this.setResult(RESULT_OK, getIntent());
                    finish();
                    break;
                default:
                    ModifyAlbumActivity.this.setResult(RESULT_CANCELED);
                    finish();
                    break;
            }

        }

    }

}
