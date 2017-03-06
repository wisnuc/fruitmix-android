package com.winsun.fruitmix.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.common.BaseActivity;
import com.winsun.fruitmix.common.Injection;
import com.winsun.fruitmix.contract.CreateAlbumContract;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.presenter.CreateAlbumPresenterImpl;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/4/28.
 */
public class CreateAlbumActivity extends BaseActivity implements CreateAlbumContract.CreateAlbumView {

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

    private Context mContext;

    private CreateAlbumContract.CreateAlbumPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        setContentView(R.layout.activity_create_album);

        ButterKnife.bind(this);

        mPresenter = new CreateAlbumPresenterImpl(Injection.injectDataRepository(mContext));
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
        ckSetMaintainer.setClickable(false);

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                boolean sPublic, sSetMaintainer;
                String title, desc;

                sPublic = ckPublic.isChecked();
                sSetMaintainer = ckSetMaintainer.isChecked();

                title = tfTitle.getText().toString();
                desc = tfDesc.getText().toString();

                mPresenter.createAlbum(title,desc,sPublic,sSetMaintainer);
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

        mContext = null;

        mPresenter.detachView();
    }


    @Override
    public void setLayoutTitle(String title) {
        mLayoutTitle.setText(title);
    }

    @Override
    public void setAlbumTitleHint(String hint) {
        tfTitle.setHint(hint);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void showOperationResultToast(OperationResult result) {
        Toast.makeText(mContext, result.getResultMessage(mContext), Toast.LENGTH_SHORT).show();
    }
}
