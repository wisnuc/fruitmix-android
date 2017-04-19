package com.winsun.fruitmix.mediaModule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.mediaModule.fragment.NewPhotoList;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/5/9.
 */
public class NewAlbumPicChooseActivity extends AppCompatActivity implements IPhotoListListener {

    public static final String TAG = "NewAlbumPicChooseActivity";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.title)
    TextView mTitleTextView;
    @BindView(R.id.right)
    TextView rightTextView;

    @BindView(R.id.main_framelayout)
    FrameLayout mMainFrameLayout;

    private NewPhotoList mNewPhotoList;

    private boolean onResume = false;

    private int mAlreadySelectedImageKeyListSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_activity_album_pic_choose);

        ButterKnife.bind(this);

        mNewPhotoList = new NewPhotoList(this);

        final List<String> alreadySelectedImageKeyArrayList = getIntent().getStringArrayListExtra(Util.KEY_ALREADY_SELECTED_IMAGE_UUID_ARRAYLIST);

        if (alreadySelectedImageKeyArrayList != null)
            mAlreadySelectedImageKeyListSize = alreadySelectedImageKeyArrayList.size();

        mMainFrameLayout.addView(mNewPhotoList.getView());
        mNewPhotoList.setSelectMode(true);
        mNewPhotoList.setAlreadySelectedImageKeyArrayList(alreadySelectedImageKeyArrayList);

        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTitleTextView.setText(getString(R.string.choose_text));

        rightTextView.setVisibility(View.VISIBLE);
        rightTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> selectImageKeys = getSelectedImageUUIDs();

                Intent intent = getIntent();

                if (intent.getBooleanExtra(Util.EDIT_PHOTO, false)) {

                    LocalCache.mediaKeysInCreateAlbum.addAll(selectImageKeys);
                    mNewPhotoList.clearSelectedPhoto();

                    setResult(RESULT_OK, intent);
                    finish();

                } else {
                    mNewPhotoList.createAlbum(selectImageKeys);
                }


            }
        });

        mNewPhotoList.addPhotoListListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!onResume) {
            mNewPhotoList.refreshView();
            onResume = true;
        }

//        MobclickAgent.onPageStart(TAG);
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

//        MobclickAgent.onPageEnd(TAG);
//        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mNewPhotoList.removePhotoListListener(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == Util.KEY_CREATE_ALBUM_REQUEST_CODE) {
            setResult(resultCode);
            finish();
        }
    }


    public List<String> getSelectedImageUUIDs() {
        return mNewPhotoList.getSelectedImageUUIDs();
    }

    @Override
    public void onPhotoItemClick(int selectedItemCount) {

        if (selectedItemCount > mAlreadySelectedImageKeyListSize) {
            rightTextView.setVisibility(View.VISIBLE);
        } else {
            rightTextView.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onPhotoItemLongClick() {
    }

    @Override
    public void onNoPhotoItem(boolean noPhotoItem) {
    }

    @Override
    public void onPhotoListScrollDown() {

        if (mToolbar.getVisibility() == View.INVISIBLE)
            return;

        ViewCompat.setElevation(mToolbar, Util.dip2px(this, 6f));

        mToolbar.setVisibility(View.GONE);
    }

    @Override
    public void onPhotoListScrollUp() {

        if (mToolbar.getVisibility() == View.VISIBLE)
            return;

        ViewCompat.setElevation(mToolbar, Util.dip2px(this, 6f));

        mToolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPhotoListScrollFinished() {

        ViewCompat.setElevation(mToolbar, Util.dip2px(this, 2f));

    }

}
