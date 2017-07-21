package com.winsun.fruitmix.mediaModule;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.NewActivityAlbumPicChooseBinding;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.mediaModule.fragment.NewPhotoList;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.RevealToolbarViewModel;

import java.util.List;

/**
 * Created by Administrator on 2016/5/9.
 */
public class NewAlbumPicChooseActivity extends BaseActivity implements IPhotoListListener {

    public static final String TAG = "NewAlbumPicChooseActivity";

    Toolbar revealToolbar;

    FrameLayout mMainFrameLayout;

    private NewPhotoList mNewPhotoList;

    private boolean onResume = false;

    private int mAlreadySelectedImageKeyListSize = 0;

    private RevealToolbarViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NewActivityAlbumPicChooseBinding binding = DataBindingUtil.setContentView(this, R.layout.new_activity_album_pic_choose);

        revealToolbar = binding.revealToolbarLayout.revealToolbar;

        mMainFrameLayout = binding.mainFramelayout;

        viewModel = new RevealToolbarViewModel();

        viewModel.selectCountTitleText.set(getString(R.string.choose_text));

        viewModel.setBaseView(this);

        binding.setRevealToolbarViewModel(viewModel);

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

        mNewPhotoList.setPhotoListListener(this);

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
        viewModel.selectCountTitleText.set(text);
    }

    private void setEnterSelectModeVisibility(int visibility) {
        viewModel.enterSelectModeVisibility.set(visibility);
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

    @Override
    public View getToolbar() {
        return null;
    }
}
