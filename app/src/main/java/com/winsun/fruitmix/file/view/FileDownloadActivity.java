package com.winsun.fruitmix.file.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityFileDownloadBinding;
import com.winsun.fruitmix.eventbus.TaskStateChangedEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.view.fragment.FileDownloadFragment;
import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FileDownloadActivity extends BaseActivity implements BaseView {

    private FileDownloadFragment fileDownloadFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityFileDownloadBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_file_download);

        initToolbar(binding);

        fileDownloadFragment = new FileDownloadFragment(this, null, null);

        binding.mainFramelayout.addView(fileDownloadFragment.getView());

    }

    private void initToolbar(ActivityFileDownloadBinding binding) {
        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set(getString(R.string.transmission_manage));
        toolbarViewModel.titleTextColorResID.set(ContextCompat.getColor(this,R.color.eighty_seven_percent_black));

        toolbarViewModel.showMenu.set(true);
        toolbarViewModel.setBaseView(this);

        toolbarViewModel.setToolbarMenuBtnOnClickListener(new ToolbarViewModel.ToolbarMenuBtnOnClickListener() {
            @Override
            public void onClick() {
                fileDownloadFragment.getBottomSheetDialog(fileDownloadFragment.getMainMenuItem()).show();
            }
        });

        binding.setToolbarViewModel(toolbarViewModel);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        if (action.equals(Util.DOWNLOADED_FILE_DELETED) || action.equals(Util.DOWNLOADED_FILE_RETRIEVED)) {
            fileDownloadFragment.handleOperationEvent(operationEvent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleEvent(TaskStateChangedEvent taskStateChangedEvent) {

        EventBus.getDefault().removeStickyEvent(taskStateChangedEvent);

        if (fileDownloadFragment != null)
            fileDownloadFragment.handleEvent(taskStateChangedEvent);

    }

    @Override
    public void onBackPressed() {

        if (fileDownloadFragment.handleBackPressedOrNot()) {
            fileDownloadFragment.onBackPressed();
        } else {
            super.onBackPressed();
        }

    }
}
