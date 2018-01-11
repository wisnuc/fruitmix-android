package com.winsun.fruitmix.inbox.view;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.InboxListPageBinding;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.interfaces.Page;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/1/10.
 */

public class InboxListPage implements Page, IShowHideFragmentListener {

    private Activity mActivity;

    private View mView;

    public InboxListPage(Activity activity) {

        mActivity = activity;

        InboxListPageBinding binding = InboxListPageBinding.inflate(LayoutInflater.from(mActivity), null, false);

        mView = binding.getRoot();

        LoadingViewModel loadingViewModel = new LoadingViewModel();

        binding.setLoadingViewModel(loadingViewModel);

        NoContentViewModel noContentViewModel = new NoContentViewModel();
        noContentViewModel.setNoContentImgResId(R.drawable.no_file);
        noContentViewModel.setNoContentText("没有内容");

        binding.setNoContentViewModel(noContentViewModel);


    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public void refreshView() {

    }

    @Override
    public void refreshViewForce() {

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

    }

    @Override
    public void onDestroy() {

        mActivity = null;

    }

    @Override
    public boolean canEnterSelectMode() {
        return false;
    }


    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }
}
