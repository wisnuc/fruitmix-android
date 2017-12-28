package com.winsun.fruitmix.firmware;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityFirmwareBinding;
import com.winsun.fruitmix.firmware.data.InjectFirmwareDataSource;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class FirmwareActivity extends BaseActivity implements FirmwareView {

    private FirmwarePresenter firmwarePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityFirmwareBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_firmware);

        ToolbarViewModel toolbarViewModel = initToolBar(binding, binding.toolbarLayout, getString(R.string.firmware_update));

        toolbarViewModel.menuResID.set(R.drawable.refresh);

        LoadingViewModel loadingViewModel = new LoadingViewModel();

        binding.setLoadingViewModel(loadingViewModel);

        NoContentViewModel noContentViewModel = new NoContentViewModel();
        noContentViewModel.showNoContentImg.set(false);

        noContentViewModel.setNoContentText(getString(R.string.fail, getString(R.string.get_firmware_version)));

        binding.setNoContentViewModel(noContentViewModel);

        FirmwareViewModel firmwareViewModel = new FirmwareViewModel();

        binding.setFirmwareViewModel(firmwareViewModel);

        firmwarePresenter = new FirmwarePresenter(toolbarViewModel,
                loadingViewModel, noContentViewModel, firmwareViewModel, InjectFirmwareDataSource.provideInstance(this),
                this);

        binding.setFirmwarePresenter(firmwarePresenter);

        firmwarePresenter.refreshView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        firmwarePresenter.onDestroy();
    }

    @Override
    public Context getContext() {
        return this;
    }
}
