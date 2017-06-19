package com.winsun.fruitmix.mediaModule;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

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
public class NewAlbumPicChooseActivity extends BaseActivity implements IPhotoListListener {

    public static final String TAG = "NewAlbumPicChooseActivity";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.title)
    TextView mTitleTextView;
    @BindView(R.id.right)
    TextView rightTextView;

    @BindView(R.id.reveal_toolbar)
    Toolbar revealToolbar;
    @BindView(R.id.select_count_title)
    TextView mSelectCountTitle;
    @BindView(R.id.enter_select_mode)
    TextView mEnterSelectMode;

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

//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Util.setStatusBarColor(this, R.color.fab_bg_color);

        revealToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setSelectCountText(getString(R.string.choose_text));

        setEnterSelectModeVisibility(View.VISIBLE);

        mEnterSelectMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    }

    @Override
    protected void onStop() {
        super.onStop();

        Util.setStatusBarColor(this, R.color.colorPrimaryDark);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mNewPhotoList.removePhotoListListener(this);

    }

    public List<String> getSelectedImageUUIDs() {
        return mNewPhotoList.getSelectedImageUUIDs();
    }

    @Override
    public void onPhotoItemClick(int selectedItemCount) {

        if (selectedItemCount > mAlreadySelectedImageKeyListSize) {
            setEnterSelectModeVisibility(View.VISIBLE);
        } else {
            setEnterSelectModeVisibility(View.INVISIBLE);
        }

        setSelectCountText(String.format(getString(R.string.select_count), selectedItemCount));

    }

    public void setSelectCountText(String text) {
        mSelectCountTitle.setText(text);
    }

    private void setEnterSelectModeVisibility(int visibility) {
        mEnterSelectMode.setVisibility(visibility);
    }

    @Override
    public void onPhotoItemLongClick() {
    }

    @Override
    public void onNoPhotoItem(boolean noPhotoItem) {
    }

    @Override
    public void onPhotoListScrollDown() {

        if (revealToolbar.getVisibility() == View.INVISIBLE)
            return;

        ViewCompat.setElevation(revealToolbar, Util.dip2px(this, 6f));

        revealToolbar.setVisibility(View.GONE);
    }

    @Override
    public void onPhotoListScrollUp() {

        if (revealToolbar.getVisibility() == View.VISIBLE)
            return;

        ViewCompat.setElevation(revealToolbar, Util.dip2px(this, 6f));

        revealToolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPhotoListScrollFinished() {

        ViewCompat.setElevation(revealToolbar, Util.dip2px(this, 2f));

    }

}
