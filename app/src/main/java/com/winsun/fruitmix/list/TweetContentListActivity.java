package com.winsun.fruitmix.list;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.anim.AnimatorBuilder;
import com.winsun.fruitmix.anim.SharpCurveInterpolator;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.component.FabMenuLayoutViewComponent;
import com.winsun.fruitmix.databinding.ActivityMediaListBinding;
import com.winsun.fruitmix.dialog.ShareMenuBottomDialogFactory;
import com.winsun.fruitmix.eventbus.TaskStateChangedEvent;
import com.winsun.fruitmix.file.data.FileListViewDataSource;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.file.view.fragment.FileFragment;
import com.winsun.fruitmix.file.view.interfaces.FileListSelectModeListener;
import com.winsun.fruitmix.file.view.interfaces.HandleFileListOperateCallback;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.list.data.FileInTweetViewDataSource;
import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.RevealToolbarViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TweetContentListActivity extends BaseActivity implements FileListSelectModeListener,
        HandleFileListOperateCallback, FabMenuLayoutViewComponent.FabMenuItemOnClickListener, IPhotoListListener {

    private ActivityMediaListBinding mActivityMediaListBinding;

    private static UserComment mUserComment;

    private MediaListPresenter mMediaListPresenter;
//    private FileListPresenter mFileListPresenter;

    private FileFragment mFileFragment;

    private FabMenuLayoutViewComponent mFabMenuLayoutViewComponent;

    private Toolbar toolbar;

    private Toolbar revealToolbar;

    private ToolbarViewModel mToolbarViewModel;

    private RevealToolbarViewModel mRevealToolbarViewModel;

    private boolean sInChooseMode = false;

    public static final int ITEM_MEDIA = 1;
    public static final int ITEM_FILE = 2;

    private int currentItem = 0;

    private Activity mActivity;

    public static void startListActivity(UserComment userComment, Context context) {

        mUserComment = userComment;

        Util.startActivity(context, TweetContentListActivity.class);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;

        mActivityMediaListBinding = DataBindingUtil.setContentView(this, R.layout.activity_media_list);

        toolbar = mActivityMediaListBinding.toolbarLayout.toolbar;

        revealToolbar = mActivityMediaListBinding.revealToolbarLayout.revealToolbar;

        mToolbarViewModel = initToolBar(mActivityMediaListBinding, mActivityMediaListBinding.toolbarLayout, "");

        initToolbarViewModel(mToolbarViewModel);

        mRevealToolbarViewModel = new RevealToolbarViewModel();

        mActivityMediaListBinding.setRevealToolbarViewModel(mRevealToolbarViewModel);

        initRevealToolbar(mRevealToolbarViewModel);

        mFabMenuLayoutViewComponent = new FabMenuLayoutViewComponent(mActivityMediaListBinding.fabMenuLayout,
                false, this);

        mFabMenuLayoutViewComponent.hideFabMenuItem();

        UserComment userComment = mUserComment;

        if (userComment == null)
            return;

        mediaDataSourceRepository = InjectMedia.provideMediaDataSourceRepository(mActivity);

        RecyclerView recyclerView = mActivityMediaListBinding.mediaRecyclerView;

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (userComment instanceof MediaComment) {

            currentItem = ITEM_MEDIA;

            ImageLoader imageLoader = InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this);

            mMediaListPresenter = new MediaListPresenter(mActivityMediaListBinding.toolbarLayout, ((MediaComment) userComment), imageLoader,
                    this, this);

            mMediaListPresenter.refreshView(this, recyclerView);

        } else if (userComment instanceof FileComment) {

            currentItem = ITEM_FILE;

            String creatorName = userComment.getCreateUserName(this);

            List<AbstractFile> files = ((FileComment) userComment).getFiles();

            String title;

            if (files.size() == 1) {

                title = getString(R.string.file_tweet_title, creatorName, files.get(0).getName());

            } else {

                title = getString(R.string.file_tweet_title, creatorName, files.size() + getString(R.string.file_unit));

            }

            mToolbarViewModel.titleText.set(title);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));

/*            mFileListPresenter = new FileListPresenter(((FileComment) userComment),
                    FileTaskManager.getInstance(), this,
                    InjectStationFileRepository.provideStationFileRepository(this),
                    InjectNetworkStateManager.provideNetworkStateManager(this),
                    InjectSystemSettingDataSource.provideSystemSettingDataSource(this).getCurrentLoginUserUUID(),
                    InjectGroupDataSource.provideGroupRepository(this).getCloudToken());

            mFileListPresenter.refreshView(recyclerView);*/

            StationFileRepository stationFileRepository = InjectStationFileRepository.provideStationFileRepository(this);

            FileListViewDataSource fileListViewDataSource = new FileInTweetViewDataSource(this, stationFileRepository,
                    (FileComment) mUserComment, InjectGroupDataSource.provideGroupRepository(this).getCloudToken(),
                    InjectSystemSettingDataSource.provideSystemSettingDataSource(this).getCurrentLoginUserUUID());

            mFileFragment = new FileFragment(this, this, this,
                    fileListViewDataSource);

            mActivityMediaListBinding.containerLayout.removeAllViews();

            mActivityMediaListBinding.containerLayout.addView(mFileFragment.getView());

            mFileFragment.refreshView();

        }

    }

    private void initToolbarViewModel(ToolbarViewModel toolbarViewModel) {

        toolbarViewModel.setToolbarSelectBtnOnClickListener(new ToolbarViewModel.ToolbarSelectBtnOnClickListener() {
            @Override
            public void onClick() {

//                if (viewPager.getCurrentItem() == PAGE_PHOTO) {
//
//                    if (!photoList.canEnterSelectMode()) {
//                        return;
//                    }
//
//                } else if (viewPager.getCurrentItem() == PAGE_FILE) {
//
//                    if (!fileFragment.canEnterSelectMode()) {
//                        return;
//                    }
//
//                }

                enterSelectMode();

            }
        });

        toolbarViewModel.showSelect.set(true);

        toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(mActivity, R.color.eighty_seven_percent_white));

    }

    private void initRevealToolbar(RevealToolbarViewModel revealToolbarViewModel) {

        revealToolbarViewModel.selectCountTitleText.set(getString(R.string.choose_text));

        revealToolbarViewModel.rightTextVisibility.set(View.INVISIBLE);

        revealToolbarViewModel.setRevealToolbarNavigationOnClickListener(new RevealToolbarViewModel.RevealToolbarNavigationOnClickListener() {
            @Override
            public void onClick() {
                if (sInChooseMode) {

                    quitSelectMode();

                }
            }
        });

    }

    @Override
    public void onBackPressed() {

        if (handleBackPressedOrNot())
            handleOnBackPressed();
        else
            super.onBackPressed();

    }

    public boolean handleBackPressedOrNot() {

        if (currentItem == ITEM_FILE) {
            return mFileFragment.handleBackPressedOrNot();
        } else
            return sInChooseMode;

    }

    private void handleOnBackPressed() {
        if (currentItem == ITEM_FILE) {

            if (sInChooseMode) {

                quitSelectMode();

            } else
                mFileFragment.onBackPressed();

        } else if (currentItem == ITEM_MEDIA) {

            quitSelectMode();

        }
    }

    private void enterSelectMode() {
        setSelectMode(true);
        showChooseHeader();

    }

    private void quitSelectMode() {
        setSelectMode(false);
        hideChooseHeader();

    }

    private void setSelectMode(boolean selectMode) {
        sInChooseMode = selectMode;

        if (currentItem == ITEM_MEDIA) {
//            photoList.setSelectMode(sInChooseMode);

            mMediaListPresenter.setSelectMode(sInChooseMode);

            setSelectCountText(getString(R.string.select_photo), 0);
        } else if (currentItem == ITEM_FILE) {

            if (selectMode)
                mFileFragment.enterSelectMode();
            else
                mFileFragment.quitSelectMode();

            setSelectCountText(getString(R.string.select_file), 0);
        }

    }

    private void showChooseHeader() {

        showRevealToolbarAnim();

    }

    public void setSelectCountText(String text, int selectCount) {
//        title.setText(text);

        mRevealToolbarViewModel.selectCountTitleText.set(text);

        if (selectCount > 0)
            mFabMenuLayoutViewComponent.showFab();

    }

    private void hideChooseHeader() {

        mFabMenuLayoutViewComponent.collapseFab();

        mFabMenuLayoutViewComponent.dismissFab();

        dismissRevealToolbarAnim();

    }

    private void showRevealToolbarAnim() {

        mToolbarViewModel.showToolbar.set(false);

        mRevealToolbarViewModel.showRevealToolbar.set(true);

        new AnimatorBuilder(mActivity, R.animator.reveal_toolbar_translation, revealToolbar).addAdapter(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);


            }
        }).setInterpolator(new LinearOutSlowInInterpolator()).startAnimator();

    }

    private void dismissRevealToolbarAnim() {

        new AnimatorBuilder(this, R.animator.reveal_toolbar_translation_restore, revealToolbar).addAdapter(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                mRevealToolbarViewModel.showRevealToolbar.set(false);

                mToolbarViewModel.showToolbar.set(true);

            }
        }).setInterpolator(SharpCurveInterpolator.getSharpCurveInterpolator()).startAnimator();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mUserComment = null;

        if (mMediaListPresenter != null)
            mMediaListPresenter.onDestroy();
//        else if (mFileListPresenter != null)
//            mFileListPresenter.onDestroy();
        else if (mFileFragment != null)
            mFileFragment.onDestroy();

        mActivity = null;

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleEvent(TaskStateChangedEvent taskStateChangedEvent) {

        EventBus.getDefault().removeStickyEvent(taskStateChangedEvent);

/*        if (mFileListPresenter != null)
            mFileListPresenter.handleEvent(taskStateChangedEvent);*/

        if (mFileFragment != null)
            mFileFragment.handleEvent(taskStateChangedEvent);

    }

    @Override
    public void onFileSelectItemClick(int selectItemCount) {

        mFabMenuLayoutViewComponent.collapseFab();

        if (selectItemCount == 0) {
            quitSelectMode();
        } else
            setSelectCountText(String.format(getString(R.string.select_count), selectItemCount), selectItemCount);


    }

    @Override
    public void onFileItemLongClick() {

        enterSelectMode();

        setSelectCountText(String.format(getString(R.string.select_count), 1), 1);

    }

    @Override
    public void onFileSelectOperationUnavailable() {

        Log.d(TAG, "onFileSelectOperationUnavailable: ");


/*        if (mActivity == null)
            return;

        if (currentItem == ITEM_FILE) {

            mToolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(mActivity, R.color.twenty_six_percent_black));

            mToolbarViewModel.selectTextEnable.set(false);

        }*/


    }

    @Override
    public void onFileSelectOperationAvailable() {

        Log.d(TAG, "onFileSelectOperationAvailable: ");

 /*       if (mActivity == null)
            return;

        if (currentItem == ITEM_FILE) {
            mToolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(mActivity, R.color.eighty_seven_percent_black));

            mToolbarViewModel.selectTextEnable.set(true);
        }
*/
    }

    @Override
    public void handleFileListOperate(String currentFolderName) {

    }

    @Override
    public void systemShareBtnOnClick() {

        if (!Util.isNetworkConnected(this)) {
            Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            return;
        }

        AbstractCommand shareInAppCommand = new AbstractCommand() {
            @Override
            public void execute() {
            }

            @Override
            public void unExecute() {
            }
        };

        AbstractCommand shareToOtherAppCommand = new AbstractCommand() {
            @Override
            public void execute() {
                handleShareToOtherApp();

                quitSelectMode();
            }

            @Override
            public void unExecute() {
            }
        };

        new ShareMenuBottomDialogFactory(shareInAppCommand, shareToOtherAppCommand).createDialog(this).show();


    }

    private void handleShareToOtherApp() {

        if (currentItem == ITEM_MEDIA)
            shareMediaToOtherApp();
        else if (currentItem == ITEM_FILE)
            shareFileToOtherApp();

    }

    private List<Media> mSelectMedias;

    private List<String> mSelectMediaOriginalPhotoPaths;

    private ProgressDialog mDialog;

    private MediaDataSourceRepository mediaDataSourceRepository;

    private void shareMediaToOtherApp() {

        mSelectMedias = new ArrayList<>(mMediaListPresenter.getSelectedMedias());

        mSelectMediaOriginalPhotoPaths = new ArrayList<>(mSelectMedias.size());

        Iterator<Media> iterator = mSelectMedias.iterator();
        while (iterator.hasNext()) {
            Media media = iterator.next();
            String originalPhotoPath = media.getOriginalPhotoPath();

            if (originalPhotoPath.length() != 0) {
                mSelectMediaOriginalPhotoPaths.add(originalPhotoPath);
                iterator.remove();
            }
        }

        if (mSelectMedias.size() == 0) {
            FileUtil.sendShareToOtherApp(mActivity, mSelectMediaOriginalPhotoPaths);
        } else {

            mDialog = ProgressDialog.show(mActivity, null, String.format(getString(R.string.operating_title), getString(R.string.download_original_photo)), true, true);
            mDialog.setCanceledOnTouchOutside(false);

            mediaDataSourceRepository.downloadMedia(mSelectMedias, new BaseOperateDataCallbackImpl<Boolean>() {
                @Override
                public void onSucceed(Boolean data, OperationResult result) {
                    super.onSucceed(data, result);

                    handleDownloadMedia();

                }
            });

//            EventBus.getDefault().post(new RetrieveMediaOriginalPhotoRequestEvent(OperationType.GET, OperationTargetType.MEDIA_ORIGINAL_PHOTO, mSelectMedias));

        }
    }

    private void handleDownloadMedia() {
        if (mDialog == null || !mDialog.isShowing()) {
            return;
        }

        dismissDialog();

        String path;
        for (Media media : mSelectMedias) {
            path = media.getOriginalPhotoPath();

            if (!path.isEmpty())
                mSelectMediaOriginalPhotoPaths.add(path);
        }

        if (mSelectMediaOriginalPhotoPaths.isEmpty()) {
            Toast.makeText(mActivity, getString(R.string.download_original_photo_fail), Toast.LENGTH_SHORT).show();
        } else {
            FileUtil.sendShareToOtherApp(mActivity, mSelectMediaOriginalPhotoPaths);
        }
    }

    private void shareFileToOtherApp() {

        mFileFragment.shareSelectFilesToOtherApp();

    }

    @Override
    public void downloadFileBtnOnClick() {

        mFileFragment.downloadSelectItems();

        quitSelectMode();

    }

    @Override
    public void onPhotoItemClick(int selectedItemCount) {

        mFabMenuLayoutViewComponent.collapseFab();

        if (selectedItemCount == 0) {
            handleOnBackPressed();
        } else {
            setSelectCountText(String.format(getString(R.string.select_count), selectedItemCount), selectedItemCount);
        }


    }

    @Override
    public void onPhotoItemLongClick() {

        enterSelectMode();

        setSelectCountText(String.format(getString(R.string.select_count), 1), 1);

    }

    @Override
    public void onNoPhotoItem(boolean noPhotoItem) {


    }

    @Override
    public void onPhotoListScrollUp() {

    }

    @Override
    public void onPhotoListScrollDown() {

    }

    @Override
    public void onPhotoListScrollFinished() {

    }

    @Override
    public View getToolbar() {
        return toolbar;
    }
}
