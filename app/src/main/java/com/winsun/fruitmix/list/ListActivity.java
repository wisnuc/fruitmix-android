package com.winsun.fruitmix.list;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.BaseToolbarActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityMediaListBinding;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.inbox.data.model.GroupUserComment;
import com.winsun.fruitmix.util.Util;

public class ListActivity extends BaseToolbarActivity {

    private ActivityMediaListBinding mActivityMediaListBinding;

    private static UserComment mUserComment;

    public static void startListActivity(UserComment userComment, Context context) {

        mUserComment = userComment;

        Util.startActivity(context, ListActivity.class);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setToolbarWhiteStyle(toolbarViewModel);

        setStatusBarToolbarBgColor(R.color.login_ui_blue);

        UserComment userComment = mUserComment;

        RecyclerView recyclerView = mActivityMediaListBinding.mediaRecyclerView;

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (userComment instanceof MediaComment) {

            ImageLoader imageLoader = InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this);

            MediaListPresenter mediaListPresenter = new MediaListPresenter(((MediaComment) userComment).getMedias(), imageLoader,
                    this);

            mediaListPresenter.refreshView(this,recyclerView);

        } else if (userComment instanceof FileComment) {

            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            FileListPresenter fileListPresenter = new FileListPresenter(((FileComment) userComment).getFiles());

            fileListPresenter.refreshView(recyclerView);

        }

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
