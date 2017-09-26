package com.winsun.fruitmix;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.winsun.fruitmix.databinding.ActivitySettingBinding;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.setting.SettingPresenter;
import com.winsun.fruitmix.setting.SettingPresenterImpl;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.upload.media.CheckMediaIsUploadStrategy;
import com.winsun.fruitmix.upload.media.InjectUploadMediaUseCase;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends BaseActivity {

    public static final String TAG = "SettingActivity";

    private SettingPresenter settingPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySettingBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);

        SettingViewModel settingViewModel = new SettingViewModel();

        SystemSettingDataSource systemSettingDataSource = InjectSystemSettingDataSource.provideSystemSettingDataSource(this);

        settingPresenter = new SettingPresenterImpl(this,settingViewModel, systemSettingDataSource,
                InjectMedia.provideMediaDataSourceRepository(this), CheckMediaIsUploadStrategy.getInstance(),
                InjectUploadMediaUseCase.provideUploadMediaUseCase(this),systemSettingDataSource.getCurrentLoginUserUUID());

        binding.setSettingPresenter(settingPresenter);

        binding.setSetting(settingViewModel);

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set(getString(R.string.setting));

        toolbarViewModel.setBaseView(this);

        binding.setToolbarViewModel(toolbarViewModel);

        binding.autoUploadPhotosSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                settingPresenter.onCheckedChanged(buttonView,isChecked);

            }
        });

        settingPresenter.onCreate(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        settingPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        settingPresenter.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        settingPresenter.onDestroy(this);

    }

    public class SettingViewModel {

        public final ObservableBoolean autoUploadOrNot = new ObservableBoolean(false);
        public final ObservableBoolean alreadyUploadMediaCountTextViewVisibility = new ObservableBoolean(false);
        public final ObservableBoolean autoUploadWhenConnectedWithMobileNetwork = new ObservableBoolean(false);
        public final ObservableField<String> alreadyUploadMediaCountText = new ObservableField<>();
        public final ObservableField<String> cacheSizeText = new ObservableField<>();

    }

}
