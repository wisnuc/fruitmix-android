package com.winsun.fruitmix.mediaModule;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.MediaShareOperationEvent;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.refactor.common.BaseActivity;
import com.winsun.fruitmix.refactor.common.Injection;
import com.winsun.fruitmix.refactor.contract.ModifyAlbumContract;
import com.winsun.fruitmix.refactor.presenter.ModifyAlbumPresenterImpl;
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
public class ModifyAlbumActivity extends BaseActivity implements ModifyAlbumContract.ModifyAlbumView {

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

    private Context mContext;

    private ModifyAlbumContract.ModifyAlbumPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_album);

        ButterKnife.bind(this);

        mContext = this;

        String mMediaShareUuid = getIntent().getStringExtra(Util.MEDIASHARE_UUID);

        mPresenter = new ModifyAlbumPresenterImpl(Injection.injectDataRepository(), mMediaShareUuid);
        mPresenter.attachView(this);
        mPresenter.initView();

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

                boolean sPublic, sSetMaintainer;
                String title, desc;

                sPublic = ckPublic.isChecked();
                sSetMaintainer = ckSetMaintainer.isChecked();
                title = mTitleLayout.getEditText().getText().toString();
                desc = mDescLayout.getEditText().getText().toString();

                mPresenter.modifyAlbum(title, desc, sPublic, sSetMaintainer);


            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleBackEvent();
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

        mPresenter.detachView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(MediaShareOperationEvent operationEvent) {

        String action = operationEvent.getAction();

        if (action.equals(Util.LOCAL_SHARE_MODIFIED) || action.equals(Util.REMOTE_SHARE_MODIFIED)) {

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

    @Override
    public void setAlbumTitle(String albumTitle) {
        mTitleLayout.getEditText().setText(albumTitle);
    }

    @Override
    public void setDescription(String description) {
        mDescLayout.getEditText().setText(description);
    }

    @Override
    public void setIsPublic(boolean isPublic) {
        ckPublic.setChecked(isPublic);
    }

    @Override
    public void setIsMaintained(boolean isMaintained) {
        ckSetMaintainer.setChecked(isMaintained);
    }

    @Override
    public void finishActivity() {
        finish();
    }
}
