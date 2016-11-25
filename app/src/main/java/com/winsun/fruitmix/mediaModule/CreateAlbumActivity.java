package com.winsun.fruitmix.mediaModule;

import android.app.ProgressDialog;
import android.content.Context;
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

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.MediaShareOperationEvent;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.operationResult.OperationResult;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResultType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

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

    private String[] mSelectedImageUUIDArray;

    private Context mContext;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        mSelectedImageUUIDArray = getIntent().getStringArrayExtra(Util.KEY_NEW_SELECTED_IMAGE_UUID_ARRAY);

        setContentView(R.layout.activity_create_album);

        ButterKnife.bind(this);

        mLayoutTitle.setText(getString(R.string.create_album_text));

        String mTitle = String.format(getString(R.string.title_hint), new SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE).format(new Date(System.currentTimeMillis())));
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

                FNAS.createLocalMediaShare(mContext,generateMediaShare(sPublic, sSetMaintainer, title, desc, mSelectedImageUUIDArray));

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(MediaShareOperationEvent operationEvent){

        String action = operationEvent.getAction();

        if (action.equals(Util.LOCAL_SHARE_CREATED)) {

            if (mDialog != null && mDialog.isShowing())
                mDialog.dismiss();


            OperationResult operationResult = operationEvent.getOperationResult();

            OperationResultType operationResultType = operationResult.getOperationResultType();

            switch (operationResultType) {
                case SUCCEED:
                    if (Util.getNetworkState(mContext)) {
                        MediaShare mediaShare = operationEvent.getMediaShare();

                        FNAS.createRemoteMediaShare(mContext,mediaShare);
                    }

                    CreateAlbumActivity.this.setResult(RESULT_OK);
                    break;
                default:
                    Toast.makeText(mContext, operationResult.getResultMessage(mContext), Toast.LENGTH_SHORT).show();
                    CreateAlbumActivity.this.setResult(RESULT_CANCELED);
                    break;
            }

            finish();
        }

    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private MediaShare generateMediaShare(boolean isPublic, boolean otherMaintianer, String title, String desc, String[] digests) {

        MediaShare mediaShare = new MediaShare();
        mediaShare.setUuid(Util.createLocalUUid());

        Log.i(TAG, "create album digest:" + digests);

        List<MediaShareContent> mediaShareContents = new ArrayList<>();
        for (String digest:digests){
            MediaShareContent mediaShareContent = new MediaShareContent();
            mediaShareContent.setDigest(digest);
            mediaShareContent.setAuthor(FNAS.userUUID);
            mediaShareContent.setTime(String.valueOf(System.currentTimeMillis()));
            mediaShareContents.add(mediaShareContent);
        }

        mediaShare.initMediaShareContents(mediaShareContents);

        mediaShare.setCoverImageDigest(digests[0]);

        mediaShare.setTitle(title);
        mediaShare.setDesc(desc);

        if (isPublic) {
            for(String userUUID:LocalCache.RemoteUserMapKeyIsUUID.keySet()){
                mediaShare.addViewer(userUUID);
            }
        } else mediaShare.clearViewers();

        if (otherMaintianer) {
            for(String userUUID:LocalCache.RemoteUserMapKeyIsUUID.keySet()){
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
