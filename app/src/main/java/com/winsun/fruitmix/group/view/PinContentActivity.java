package com.winsun.fruitmix.group.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.winsun.fruitmix.BaseToolbarActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityPinContentBinding;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.group.presenter.PinContentPresenter;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.mediaModule.NewPicChooseActivity;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

public class PinContentActivity extends BaseToolbarActivity implements PinContentView {

    private ActivityPinContentBinding binding;

    private PinContentPresenter pinContentPresenter;

    public static final String KEY_PIN_UUID = "key_pin_uuid";
    public static final String KEY_GROUP_UUID = "key_group_uuid";

    public static final int REQUEST_MEDIA_FILE_SELECT = 0;

    public static final int REQUEST_MODIFY_PIN = 1;

    private String pinUUID;
    private String groupUUID;

    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pinUUID = getIntent().getStringExtra(KEY_PIN_UUID);
        groupUUID = getIntent().getStringExtra(KEY_GROUP_UUID);

        LoadingViewModel loadingViewModel = new LoadingViewModel();

        NoContentViewModel noContentViewModel = new NoContentViewModel();
        noContentViewModel.setNoContentText(getString(R.string.no_files));
        noContentViewModel.setNoContentImgResId(R.drawable.no_file);

        binding.setLoadingViewModel(loadingViewModel);
        binding.setNoContentViewModel(noContentViewModel);

        RecyclerView pinContentRecyclerView = binding.pinContentRecyclerview;

        pinContentRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        pinContentRecyclerView.setItemAnimator(new DefaultItemAnimator());

        pinContentPresenter = new PinContentPresenter(groupUUID, pinUUID, InjectGroupDataSource.provideGroupRepository(), loadingViewModel, noContentViewModel,
                toolbarViewModel, this, InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this));

        pinContentRecyclerView.setAdapter(pinContentPresenter.getPinContentAdapter());

        pinContentPresenter.refreshPinContent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        pinContentPresenter.onDestroy();
    }

    private void startSelect(boolean showMedia) {
        Intent intent = new Intent(PinContentActivity.this, NewPicChooseActivity.class);

        intent.putExtra(NewPicChooseActivity.KEY_SHOW_MEDIA, showMedia);
        intent.putExtra(NewPicChooseActivity.KEY_CREATE_COMMENT, false);
        intent.putExtra(NewPicChooseActivity.KEY_PIN_UUID, pinUUID);
        intent.putExtra(Util.KEY_GROUP_UUID, groupUUID);

        if (showMedia)
            intent.putStringArrayListExtra(Util.KEY_ALREADY_SELECTED_IMAGE_KEY_ARRAYLIST, pinContentPresenter.getPinMediaKeys());
        else
            intent.putStringArrayListExtra(NewPicChooseActivity.ALREADY_SELECT_FILE_NAME, pinContentPresenter.getPinFileNames());

        startActivityForResult(intent, REQUEST_MEDIA_FILE_SELECT);
    }

    @Override
    protected View generateContent() {

        binding = ActivityPinContentBinding.inflate(LayoutInflater.from(this), null, false);

        return binding.getRoot();
    }

    @Override
    protected String getToolbarTitle() {
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MEDIA_FILE_SELECT && resultCode == RESULT_OK) {
            pinContentPresenter.refreshPinContent();
            setResultCode(RESULT_OK);
        } else if (requestCode == REQUEST_MODIFY_PIN && resultCode == RESULT_OK) {
            pinContentPresenter.refreshPinName();
            setResultCode(RESULT_OK);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pin_menu, menu);

        mMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_media:
                startSelect(true);
                break;
            case R.id.add_file:
                startSelect(false);
                break;
            case R.id.modify_pin:

                Intent intent = new Intent(this, OperatePinActivity.class);
                intent.putExtra(Util.KEY_GROUP_UUID, groupUUID);
                intent.putExtra(OperatePinActivity.CREATE_PIN, false);
                intent.putExtra(OperatePinActivity.KEY_PIN_UUID, pinUUID);

                startActivityForResult(intent, REQUEST_MODIFY_PIN);

                break;
            case R.id.modify_pin_content:

                pinContentPresenter.modifyPinContent();

                break;
            case R.id.delete_pin:

                pinContentPresenter.deletePin();

                break;
        }

        return true;


    }

    private void setMenuVisible(boolean visible) {
        for (int i = 0; i < mMenu.size(); i++) {
            mMenu.getItem(i).setVisible(visible);
        }
    }

    @Override
    public void dismissMenu() {
        setMenuVisible(false);
    }

    @Override
    public void showMenu() {
        setMenuVisible(true);
    }

    @Override
    public Context getContext() {
        return this;
    }
}
