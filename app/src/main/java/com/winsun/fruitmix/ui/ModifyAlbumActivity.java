package com.winsun.fruitmix.ui;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.common.BaseActivity;
import com.winsun.fruitmix.common.Injection;
import com.winsun.fruitmix.contract.ModifyAlbumContract;
import com.winsun.fruitmix.presenter.ModifyAlbumPresenterImpl;
import com.winsun.fruitmix.util.Util;

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

    private ModifyAlbumContract.ModifyAlbumPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_album);

        ButterKnife.bind(this);

        String mMediaShareUuid = getIntent().getStringExtra(Util.MEDIASHARE_UUID);

        mPresenter = new ModifyAlbumPresenterImpl(Injection.injectDataRepository(this), mMediaShareUuid);
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
    protected void onDestroy() {
        super.onDestroy();

        mPresenter.detachView();
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
