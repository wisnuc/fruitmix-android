package com.winsun.fruitmix.refactor.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.refactor.common.BaseActivity;
import com.winsun.fruitmix.refactor.common.Injection;
import com.winsun.fruitmix.refactor.contract.MediaFragmentContract;
import com.winsun.fruitmix.refactor.contract.NewPhotoSelectContract;
import com.winsun.fruitmix.refactor.presenter.MediaFragmentInNewPhotoSelectPresenterImpl;
import com.winsun.fruitmix.refactor.presenter.NewPhotoSelectPresenterImpl;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewPhotoSelectActivity extends BaseActivity implements NewPhotoSelectContract.NewPhotoSelectView, View.OnClickListener {

    @BindView(R.id.back)
    ImageView ivBack;
    @BindView(R.id.ok)
    TextView tfOK;
    @BindView(R.id.title)
    TextView titleTextView;
    @BindView(R.id.main_framelayout)
    FrameLayout mMainFrameLayout;

    private NewPhotoSelectContract.NewPhotoSelectPresenter mNewPhotoSelectPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_photo_select);

        ButterKnife.bind(this);

        MediaFragmentContract.MediaFragmentPresenter presenter = new MediaFragmentInNewPhotoSelectPresenterImpl();

        MediaFragment mMediaFragment = new MediaFragment(this, presenter);

        mMainFrameLayout.addView(mMediaFragment.getView());

        ivBack.setOnClickListener(this);
        tfOK.setOnClickListener(this);

        mNewPhotoSelectPresenter = new NewPhotoSelectPresenterImpl(presenter);
        mNewPhotoSelectPresenter.attachView(this);

        ((MediaFragmentInNewPhotoSelectPresenterImpl) presenter).setNewPhotoSelectPresenter(mNewPhotoSelectPresenter, Injection.injectDataRepository(), Collections.<String>emptyList());

        mNewPhotoSelectPresenter.initView();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back:
                mNewPhotoSelectPresenter.handleBackEvent();
                break;
            case R.id.ok:
                mNewPhotoSelectPresenter.handleSelectFinished();
                break;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mNewPhotoSelectPresenter.handleBackEvent();
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mNewPhotoSelectPresenter.handleOnActivityResult(requestCode, resultCode, data);
    }
}
