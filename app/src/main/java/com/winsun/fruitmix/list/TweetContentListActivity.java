package com.winsun.fruitmix.list;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.View;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.anim.AnimatorBuilder;
import com.winsun.fruitmix.anim.CustomTransitionListener;
import com.winsun.fruitmix.anim.SharpCurveInterpolator;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.component.fab.menu.FabMenuItemOnClickDefaultListener;
import com.winsun.fruitmix.component.fab.menu.FabMenuItemOnClickListener;
import com.winsun.fruitmix.component.fab.menu.FabMenuLayoutViewComponent;
import com.winsun.fruitmix.databinding.ActivityTweetContentListBinding;
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
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.list.data.FileInTweetViewDataSource;
import com.winsun.fruitmix.list.data.InjectMediaInTweetDataRepository;
import com.winsun.fruitmix.list.data.MediaInTweetDataRepository;
import com.winsun.fruitmix.list.data.MediaInTweetListConverter;
import com.winsun.fruitmix.list.data.MediaInTweetRemoteDataSource;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.fragment.NewPhotoList;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.RevealToolbarViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Map;

public class TweetContentListActivity extends BaseActivity implements FileListSelectModeListener,
        HandleFileListOperateCallback, IPhotoListListener {

    private static UserComment mUserComment;

//    private MediaListPresenter mMediaListPresenter;
//    private FileListPresenter mFileListPresenter;

    private NewPhotoList mNewPhotoList;

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

    private MediaDataSourceRepository mediaDataSourceRepository;

    public static void startListActivity(UserComment userComment, Context context) {

        mUserComment = userComment;

        Util.startActivity(context, TweetContentListActivity.class);

    }

    private SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            if (currentItem == ITEM_FILE)
                mFileFragment.onMapSharedElements(names, sharedElements);
            else if (currentItem == ITEM_MEDIA)
                mNewPhotoList.onMapSharedElements(names, sharedElements);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;

        ActivityTweetContentListBinding activityTweetContentListBinding = DataBindingUtil.setContentView(this, R.layout.activity_tweet_content_list);

        toolbar = activityTweetContentListBinding.toolbarLayout.toolbar;

        revealToolbar = activityTweetContentListBinding.revealToolbarLayout.revealToolbar;

        mToolbarViewModel = initToolBar(activityTweetContentListBinding, activityTweetContentListBinding.toolbarLayout, "");

        initToolbarViewModel(mToolbarViewModel);

        mRevealToolbarViewModel = new RevealToolbarViewModel();

        activityTweetContentListBinding.setRevealToolbarViewModel(mRevealToolbarViewModel);

        initRevealToolbar(mRevealToolbarViewModel);

        UserComment userComment = mUserComment;

        if (userComment == null)
            return;

        setExitSharedElementCallback(sharedElementCallback);

        String creatorName = userComment.getCreateUserName(this);

        if (userComment instanceof MediaComment) {

            currentItem = ITEM_MEDIA;

            List<Media> medias = ((MediaComment) userComment).getMedias();

            String title = getString(R.string.media_tweet_title,creatorName,medias.size() + getString(R.string.file_unit));

            mToolbarViewModel.titleText.set(title);

            GroupRequestParam groupRequestParam = new GroupRequestParam(userComment.getGroupUUID(), userComment.getStationID());

            mediaDataSourceRepository = InjectMediaInTweetDataRepository.provideInstance(this, (MediaComment) mUserComment);

            mNewPhotoList = new NewPhotoList(this, this, false, false, mediaDataSourceRepository,
                    new MediaInTweetListConverter(), groupRequestParam);

            activityTweetContentListBinding.containerLayout.removeAllViews();

            activityTweetContentListBinding.containerLayout.addView(mNewPhotoList.getView());

            mNewPhotoList.refreshView();

/*            mMediaListPresenter = new MediaListPresenter(mActivityTweetContentListBinding.toolbarLayout, ((MediaComment) userComment), imageLoader,
                    this, this);

            mMediaListPresenter.refreshView(this, recyclerView);*/

        } else if (userComment instanceof FileComment) {

            currentItem = ITEM_FILE;

            List<AbstractFile> files = ((FileComment) userComment).getFiles();

            String title;

            if (files.size() == 1) {

                title = getString(R.string.file_tweet_title, creatorName, files.get(0).getName());

            } else {

                title = getString(R.string.file_tweet_title, creatorName, files.size() + getString(R.string.file_unit));

            }

            mToolbarViewModel.titleText.set(title);

/*            mFileListPresenter = new FileListPresenter(((FileComment) userComment),
                    FileTaskManager.getInstance(), this,
                    InjectStationFileRepository.provideStationFileRepository(this),
                    InjectNetworkStateManager.provideNetworkStateManager(this),
                    InjectSystemSettingDataSource.provideSystemSettingDataSource(this).getCurrentLoginUserUUID(),
                    InjectGroupDataSource.provideGroupRepository(this).getCloudToken());

            mFileListPresenter.refreshView(recyclerView);*/

            StationFileRepository stationFileRepository = InjectStationFileRepository.provideStationFileRepository(this);

            FileListViewDataSource fileListViewDataSource = new FileInTweetViewDataSource(this, stationFileRepository,
                    (FileComment) mUserComment, InjectSystemSettingDataSource.provideSystemSettingDataSource(this).getCurrentLoginUserUUID());

            mFileFragment = new FileFragment(this, this, this,
                    fileListViewDataSource);

            activityTweetContentListBinding.containerLayout.removeAllViews();

            activityTweetContentListBinding.containerLayout.addView(mFileFragment.getView());

            mFileFragment.refreshView();

        }

        FabMenuItemOnClickListener fabMenuItemOnClickListener = new FabMenuItemOnClickDefaultListener(
                mNewPhotoList, mFileFragment, mediaDataSourceRepository, new AbstractCommand() {
            @Override
            public void execute() {
                quitSelectMode();
            }

            @Override
            public void unExecute() {

            }
        }
        );

        mFabMenuLayoutViewComponent = new FabMenuLayoutViewComponent(activityTweetContentListBinding.fabMenuLayout,
                currentItem == ITEM_MEDIA ? FabMenuItemOnClickDefaultListener.ITEM_MEDIA : FabMenuItemOnClickListener.ITEM_FILE
                , fabMenuItemOnClickListener);

        mFabMenuLayoutViewComponent.hideFabMenuItem();

    }

    private void initToolbarViewModel(ToolbarViewModel toolbarViewModel) {

        toolbarViewModel.setToolbarSelectBtnOnClickListener(new ToolbarViewModel.ToolbarSelectBtnOnClickListener() {
            @Override
            public void onClick() {

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
            mNewPhotoList.setSelectMode(sInChooseMode);

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

   /*     if (mMediaListPresenter != null)
            mMediaListPresenter.onDestroy();
        else if (mFileListPresenter != null)
            mFileListPresenter.onDestroy();*/

        if (mNewPhotoList != null)
            mNewPhotoList.onDestroy();
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
    public void onActivityReenter(int resultCode, Intent data) {

        dismissToolbarWhenSharedElementTransition();

        if (currentItem == ITEM_FILE)
            mFileFragment.onActivityReenter(resultCode, data);
        else if (currentItem == ITEM_MEDIA)
            mNewPhotoList.onActivityReenter(resultCode, data);

    }

    private void dismissToolbarWhenSharedElementTransition() {
        if (Util.checkRunningOnLollipopOrHigher()) {

            if (mActivity == null)
                return;

            mActivity.getWindow().getSharedElementEnterTransition().addListener(new CustomTransitionListener() {

                @Override
                public void onTransitionStart(Transition transition) {
                    super.onTransitionStart(transition);

                    mToolbarViewModel.showToolbar.set(false);

                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);

                    mToolbarViewModel.showToolbar.set(true);

                }
            });

        }
    }


    @Override
    public View getToolbar() {
        return toolbar;
    }
}
