package com.winsun.fruitmix.torrent.view;

import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityTorrentDownloadManageBinding;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class TorrentDownloadManageActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTorrentDownloadManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_torrent_download_manage);

        initToolBar(binding);

        LoadingViewModel loadingViewModel = new LoadingViewModel();

        binding.setLoadingViewModel(loadingViewModel);

        RecyclerView recyclerView = binding.torrentDownloadRecyclerview;




    }

    private void initToolBar(ActivityTorrentDownloadManageBinding binding) {

        Toolbar mToolbar = binding.toolbarLayout.toolbar;

        binding.toolbarLayout.title.setTextColor(ContextCompat.getColor(this, R.color.eighty_seven_percent_white));

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();
        toolbarViewModel.setBaseView(this);

        toolbarViewModel.navigationIconResId.set(R.drawable.ic_back);
        toolbarViewModel.titleText.set(getString(R.string.download_manage));

        binding.setToolbarViewModel(toolbarViewModel);

        mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.login_ui_blue));

        Util.setStatusBarColor(this, R.color.login_ui_blue);

    }


}
