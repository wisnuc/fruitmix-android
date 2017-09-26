package com.winsun.fruitmix.mediaModule;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.databinding.NewActivityAlbumPicChooseBinding;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.view.LocalFileFragment;
import com.winsun.fruitmix.file.view.interfaces.HandleFileListOperateCallback;
import com.winsun.fruitmix.group.data.model.MultiFileComment;
import com.winsun.fruitmix.group.data.model.MultiPhotoComment;
import com.winsun.fruitmix.group.data.model.SingleFileComment;
import com.winsun.fruitmix.group.data.model.SinglePhotoComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.mediaModule.fragment.NewPhotoList;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.RevealToolbarViewModel;

import java.util.List;

/**
 * Created by Administrator on 2016/5/9.
 */
public class NewPicChooseActivity extends BaseActivity implements IPhotoListListener, RevealToolbarViewModel.RevealToolbarRightTextOnClickListener, HandleFileListOperateCallback {

    public static final String TAG = "NewAlbumPicChooseActivity";

    public static final String KEY_SHOW_MEDIA = "key_show_media";

    public static final String KEY_CREATE_COMMENT = "key_create_comment";

    public static final String KEY_PIN_UUID = "key_pin_uuid";

    public static final String ALREADY_SELECT_FILE_NAME = "already_select_file_name";

    Toolbar revealToolbar;

    FrameLayout mMainFrameLayout;

    private NewPhotoList mNewPhotoList;

    private LocalFileFragment localFileFragment;

    private boolean onResume = false;

    private int mAlreadySelectedImageKeyListSize = 0;

    private RevealToolbarViewModel viewModel;

    private GroupRepository groupDataSource;

    private User currentUser;

    private String groupUUID;

    private boolean showMedia;

    private boolean createComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NewActivityAlbumPicChooseBinding binding = DataBindingUtil.setContentView(this, R.layout.new_activity_album_pic_choose);

        revealToolbar = binding.revealToolbarLayout.revealToolbar;

        mMainFrameLayout = binding.mainFramelayout;

        viewModel = new RevealToolbarViewModel();

        viewModel.selectCountTitleText.set(getString(R.string.choose_text));

        viewModel.setBaseView(this);

        viewModel.setRevealToolbarRightTextOnClickListener(this);

        viewModel.showRevealToolbar.set(true);

        viewModel.enterSelectModeText.set("发送");

        binding.setRevealToolbarViewModel(viewModel);

        groupDataSource = InjectGroupDataSource.provideGroupRepository();

        currentUser = InjectUser.provideRepository(this).getUserByUUID(InjectSystemSettingDataSource.provideSystemSettingDataSource(this)
                .getCurrentLoginUserUUID());

        groupUUID = getIntent().getStringExtra(Util.KEY_GROUP_UUID);

        createComment = getIntent().getBooleanExtra(KEY_CREATE_COMMENT, true);

        showMedia = getIntent().getBooleanExtra(KEY_SHOW_MEDIA, true);

        if (showMedia) {

            initPhotoList();

        } else {

            setEnterSelectModeVisibility(View.VISIBLE);

            localFileFragment = new LocalFileFragment(this);

            localFileFragment.setSelectMode(true);

            localFileFragment.setAlreadySelectedFileArrayList(getIntent().getStringArrayListExtra(ALREADY_SELECT_FILE_NAME));

            mMainFrameLayout.addView(localFileFragment.getView());

        }

//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Util.setStatusBarColor(this, R.color.fab_bg_color);

    }

    private void initPhotoList() {
        mNewPhotoList = new NewPhotoList(this);

        final List<String> alreadySelectedImageKeyArrayList = getIntent().getStringArrayListExtra(Util.KEY_ALREADY_SELECTED_IMAGE_KEY_ARRAYLIST);

        if (alreadySelectedImageKeyArrayList != null)
            mAlreadySelectedImageKeyListSize = alreadySelectedImageKeyArrayList.size();

        mMainFrameLayout.addView(mNewPhotoList.getView());
        mNewPhotoList.setSelectMode(true);
        mNewPhotoList.setAlreadySelectedImageKeysFromChooseActivity(alreadySelectedImageKeyArrayList);

        mNewPhotoList.setPhotoListListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!onResume) {

            if (showMedia)
                mNewPhotoList.refreshView();
            else
                localFileFragment.refreshView();

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

        if (showMedia)
            mNewPhotoList.onDestroy();
        else
            localFileFragment.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (showMedia)
            super.onBackPressed();
        else {

            if (!localFileFragment.onBackPressed())
                super.onBackPressed();
        }
    }

    @Override
    public void onPhotoItemClick(int selectedItemCount) {

        if (selectedItemCount > mAlreadySelectedImageKeyListSize) {
            setEnterSelectModeVisibility(View.VISIBLE);
        } else {
            setEnterSelectModeVisibility(View.INVISIBLE);
        }

        setSelectCountText(String.format(getString(R.string.select_photo_count), selectedItemCount));

    }

    private void setSelectCountText(String text) {
        viewModel.selectCountTitleText.set(text);
    }

    private void setEnterSelectModeVisibility(int visibility) {
        viewModel.rightTextVisibility.set(visibility);
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
        return revealToolbar;
    }

    @Override
    public void onRightTextClick() {

        if (createComment)
            handleCreateComment();
        else {

            handleAddMediaOrFileToPin();

        }
    }

    private void handleAddMediaOrFileToPin() {
        String pinUUID = getIntent().getStringExtra(KEY_PIN_UUID);

        if (showMedia) {

            groupDataSource.insertMediaToPin(mNewPhotoList.getSelectedMedias(), groupUUID, pinUUID, new BaseOperateDataCallback<Boolean>() {
                @Override
                public void onSucceed(Boolean data, OperationResult result) {
                    setResultCode(RESULT_OK);

                    finish();
                }

                @Override
                public void onFail(OperationResult result) {
                    Toast.makeText(NewPicChooseActivity.this, "插入失败", Toast.LENGTH_SHORT).show();

                    setResultCode(RESULT_CANCELED);

                    finish();
                }
            });

        } else {

            groupDataSource.insertFileToPin(localFileFragment.getSelectFiles(), groupUUID, pinUUID, new BaseOperateDataCallback<Boolean>() {

                @Override
                public void onSucceed(Boolean data, OperationResult result) {
                    setResultCode(RESULT_OK);

                    finish();
                }

                @Override
                public void onFail(OperationResult result) {
                    Toast.makeText(NewPicChooseActivity.this, "插入失败", Toast.LENGTH_SHORT).show();

                    setResultCode(RESULT_CANCELED);

                    finish();
                }
            });


        }
    }

    private void handleCreateComment() {
        UserComment userComment;

        if (showMedia) {
            userComment = createMediaComment();
        } else {
            userComment = createFileComment();
        }

        groupDataSource.insertUserComment(groupUUID, userComment, new BaseOperateDataCallbackImpl<UserComment>() {
            @Override
            public void onSucceed(UserComment data, OperationResult result) {
                super.onSucceed(data, result);

                setResultCode(RESULT_OK);

                finish();

            }

            @Override
            public void onFail(OperationResult result) {
                super.onFail(result);

                Toast.makeText(NewPicChooseActivity.this, "插入失败", Toast.LENGTH_SHORT).show();

                setResultCode(RESULT_CANCELED);

                finish();
            }
        });
    }

    @NonNull
    private UserComment createFileComment() {
        UserComment userComment;
        List<AbstractFile> files = localFileFragment.getSelectFiles().subList(0, 6);

        if (files.size() == 1) {
            userComment = new SingleFileComment(currentUser, System.currentTimeMillis(), files.get(0));
        } else {
            userComment = new MultiFileComment(currentUser, System.currentTimeMillis(), files);
        }
        return userComment;
    }

    @NonNull
    private UserComment createMediaComment() {
        UserComment userComment;
        List<Media> medias = mNewPhotoList.getSelectedMedias().subList(0, 6);

        if (medias.size() == 1) {
            userComment = new SinglePhotoComment(currentUser, System.currentTimeMillis(), medias.get(0));
        } else {
            userComment = new MultiPhotoComment(currentUser, System.currentTimeMillis(), medias);
        }
        return userComment;
    }


    @Override
    public void handleFileListOperate(String currentFolderName) {

    }
}
