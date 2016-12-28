package com.winsun.fruitmix.mediaModule;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.MediaShareOperationEvent;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/4/28.
 */
public class CreateAlbumActivity extends AppCompatActivity {

    public static final String TAG = CreateAlbumActivity.class.getSimpleName();

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
    @BindView(R.id.layout_title)
    TextView mLayoutTitle;

    private List<String> mSelectedImageKeys;

    private Context mContext;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        mSelectedImageKeys = LocalCache.mediaKeysInCreateAlbum;

        setContentView(R.layout.activity_create_album);

        ButterKnife.bind(this);

        mLayoutTitle.setText(getString(R.string.create_album));

        String mTitle = String.format(getString(R.string.album_item_title), new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())));
        tfTitle.setHint(mTitle);

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

                if (!Util.getNetworkState(mContext)) {
                    Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean sPublic, sSetMaintainer;
                String title, desc;

                sPublic = ckPublic.isChecked();
                sSetMaintainer = ckSetMaintainer.isChecked();

                title = tfTitle.getText().toString();

                if (title.equals("")) {
                    title = tfTitle.getHint().toString();
                } else {
                    title = tfTitle.getText().toString();
                }

                desc = tfDesc.getText().toString();

                mDialog = ProgressDialog.show(mContext, null, getString(R.string.operating_title), true, false);

                FNAS.createRemoteMediaShare(mContext, generateMediaShare(sPublic, sSetMaintainer, title, desc, mSelectedImageKeys));

                LocalCache.mediaKeysInCreateAlbum.clear();
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

        OperationResult operationResult = operationEvent.getOperationResult();

        OperationResultType operationResultType = operationResult.getOperationResultType();

        switch (action) {
            case Util.REMOTE_SHARE_CREATED:

                dismissDialog();

                Toast.makeText(mContext, operationResult.getResultMessage(mContext), Toast.LENGTH_SHORT).show();

                boolean mCreateAlbumSucceed = operationResultType.equals(OperationResultType.SUCCEED);
                if (mCreateAlbumSucceed)
                    CreateAlbumActivity.this.setResult(RESULT_OK);
                else
                    CreateAlbumActivity.this.setResult(RESULT_CANCELED);

                finish();

                break;
        }

    }

    private void dismissDialog() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    private MediaShare generateMediaShare(boolean isPublic, boolean otherMaintainer, String title, String desc, List<String> mediaKeys) {

        MediaShare mediaShare = new MediaShare();
        mediaShare.setUuid(Util.createLocalUUid());

        Log.i(TAG, "create album digest:" + mediaKeys);

        List<MediaShareContent> mediaShareContents = new ArrayList<>();
        for (String mediaKey : mediaKeys) {
            MediaShareContent mediaShareContent = new MediaShareContent();
            mediaShareContent.setKey(mediaKey);
            mediaShareContent.setAuthor(FNAS.userUUID);
            mediaShareContent.setTime(String.valueOf(System.currentTimeMillis()));
            mediaShareContents.add(mediaShareContent);
        }

        mediaShare.initMediaShareContents(mediaShareContents);

        mediaShare.setCoverImageKey(mediaKeys.get(0));

        mediaShare.setTitle(title);
        mediaShare.setDesc(desc);

        if (isPublic) {
            for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
                mediaShare.addViewer(userUUID);
            }
        } else mediaShare.clearViewers();

        if (otherMaintainer) {
            for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
                mediaShare.addMaintainer(userUUID);
            }
        } else {
            mediaShare.clearMaintainers();
            mediaShare.addMaintainer(FNAS.userUUID);
        }

        mediaShare.setCreatorUUID(FNAS.userUUID);
        mediaShare.setTime(String.valueOf(System.currentTimeMillis()));
        mediaShare.setAlbum(true);
        mediaShare.setLocal(true);
        mediaShare.setArchived(false);
        mediaShare.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(mediaShare.getTime()))));

        return mediaShare;

    }

}
