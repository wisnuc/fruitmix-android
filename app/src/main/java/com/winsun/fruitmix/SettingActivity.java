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
import android.widget.TextView;

import com.winsun.fruitmix.databinding.ActivitySettingBinding;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.setting.SettingPresenter;
import com.winsun.fruitmix.setting.SettingPresenterImpl;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.LocalCache;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends BaseActivity {

    public static final String TAG = "SettingActivity";

    SwitchCompat mAutoUploadPhotosSwitch;

    private boolean mAutoUploadOrNot = false;

    private int mAlreadyUploadMediaCount = -1;

    private int mTotalLocalMediaCount = 0;

    private long mTotalCacheSize = 0;

    private SettingViewModel settingViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySettingBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_setting);

        settingViewModel = new SettingViewModel();

        SettingPresenter settingPresenter = new SettingPresenterImpl();

        binding.setSettingPresenter(settingPresenter);

        binding.setSetting(settingViewModel);

        binding.setBaseView(this);

        mAutoUploadPhotosSwitch = binding.autoUploadPhotosSwitch;

        mAutoUploadOrNot = LocalCache.getAutoUploadOrNot(this);
        settingViewModel.autoUploadOrNot.set(mAutoUploadOrNot);

        calcAlreadyUploadMediaCountAndTotalCacheSize();

    }

    private void calcAlreadyUploadMediaCountAndTotalCacheSize() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                int alreadyUploadMediaCount = 0;
                int totalUploadMediaCount = 0;

                for (Media media : LocalCache.LocalMediaMapKeyIsOriginalPhotoPath.values()) {

                    if (media.getUploadedDeviceIDs().contains(LocalCache.DeviceID)) {
                        alreadyUploadMediaCount++;
                    }

                    totalUploadMediaCount++;
                }

                mAlreadyUploadMediaCount = alreadyUploadMediaCount;
                mTotalLocalMediaCount = totalUploadMediaCount;

                mTotalCacheSize = FileUtil.getTotalCacheSize(SettingActivity.this);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                settingViewModel.alreadyUploadMediaCountTextViewVisibility.set(true);
                settingViewModel.alreadyUploadMediaCountText.set(String.format(getString(R.string.already_upload_media_count_text), mAlreadyUploadMediaCount, mTotalLocalMediaCount));

                settingViewModel.cacheSizeText.set(FileUtil.formatFileSize(mTotalCacheSize));

            }
        }.execute();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        boolean isChecked = mAutoUploadPhotosSwitch.isChecked();

        if (mAutoUploadOrNot != isChecked) {
            LocalCache.setAutoUploadOrNot(this, isChecked);

            if (isChecked) {
                LocalCache.setCurrentUploadDeviceID(this, LocalCache.DeviceID);
                EventBus.getDefault().post(new RequestEvent(OperationType.START_UPLOAD, null));
            } else {
                LocalCache.setCurrentUploadDeviceID(this, "");
                EventBus.getDefault().post(new RequestEvent(OperationType.STOP_UPLOAD, null));
            }
        }

    }

    public class SettingViewModel {

        public final ObservableBoolean autoUploadOrNot = new ObservableBoolean(false);
        public final ObservableBoolean alreadyUploadMediaCountTextViewVisibility = new ObservableBoolean(false);
        public final ObservableField<String> alreadyUploadMediaCountText = new ObservableField<>();
        public final ObservableField<String> cacheSizeText = new ObservableField<>();

    }

}
