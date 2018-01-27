package com.winsun.fruitmix.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.print.PageRange;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.BaseToolbarActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityMediaListBinding;
import com.winsun.fruitmix.eventbus.TaskStateChangedEvent;
import com.winsun.fruitmix.file.data.model.FileTaskManager;
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.network.InjectNetworkStateManager;
import com.winsun.fruitmix.stations.InjectStation;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TweetContentListActivity extends BaseToolbarActivity {

    private ActivityMediaListBinding mActivityMediaListBinding;

    private static UserComment mUserComment;

    private MediaListPresenter mMediaListPresenter;
    private FileListPresenter mFileListPresenter;

    public static void startListActivity(UserComment userComment, Context context) {

        mUserComment = userComment;

        Util.startActivity(context, TweetContentListActivity.class);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setToolbarWhiteStyle(toolbarViewModel);

        setStatusBarToolbarBgColor(R.color.login_ui_blue);

        UserComment userComment = mUserComment;

        if (userComment == null)
            return;

        RecyclerView recyclerView = mActivityMediaListBinding.mediaRecyclerView;

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (userComment instanceof MediaComment) {

            ImageLoader imageLoader = InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this);

            mMediaListPresenter = new MediaListPresenter(mActivityBaseToolbarBinding, ((MediaComment) userComment), imageLoader,
                    this);

            mMediaListPresenter.refreshView(this, recyclerView);

        } else if (userComment instanceof FileComment) {

            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            mFileListPresenter = new FileListPresenter(((FileComment) userComment),
                    FileTaskManager.getInstance(), this,
                    InjectStationFileRepository.provideStationFileRepository(this),
                    InjectNetworkStateManager.provideNetworkStateManager(this),
                    InjectSystemSettingDataSource.provideSystemSettingDataSource(this).getCurrentLoginUserUUID(),
                    InjectGroupDataSource.provideGroupRepository(this).getCloudToken());

            mFileListPresenter.refreshView(recyclerView);

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mUserComment = null;

        if (mMediaListPresenter != null)
            mMediaListPresenter.onDestroy();
        else if (mFileListPresenter != null)
            mFileListPresenter.onDestroy();

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleEvent(TaskStateChangedEvent taskStateChangedEvent) {

        EventBus.getDefault().removeStickyEvent(taskStateChangedEvent);

        if (mFileListPresenter != null)
            mFileListPresenter.handleEvent(taskStateChangedEvent);

    }

    @Override
    protected View generateContent() {

        mActivityMediaListBinding = ActivityMediaListBinding.inflate(LayoutInflater.from(this),
                null, false);

        return mActivityMediaListBinding.getRoot();
    }

    @Override
    protected String getToolbarTitle() {
        return "";
    }


}
