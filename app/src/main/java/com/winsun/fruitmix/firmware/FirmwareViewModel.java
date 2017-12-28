package com.winsun.fruitmix.firmware;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.firmware.model.CheckUpdateState;
import com.winsun.fruitmix.firmware.model.Firmware;
import com.winsun.fruitmix.firmware.model.FirmwareState;
import com.winsun.fruitmix.firmware.model.NewFirmwareState;

/**
 * Created by Administrator on 2017/12/27.
 */

public class FirmwareViewModel {

    public void setData(Firmware firmware, Context context) {

        setCurrentFirmwareVersion(firmware, context);

        setCurrentFirmwareState(firmware.getFirmwareState(), context);

        setFindNewVersion(firmware.getCurrentFirmwareVersion(), firmware.getNewFirmwareVersion(), context);

        setNewVersionState(firmware.getNewFirmwareState(), firmware.getDownloaded(), firmware.getLength(), context);

        setReleaseDate(firmware.getReleaseDate(), context);

        setShowCheckUpdate(firmware.getCheckUpdateState());

    }

    public final ObservableField<String> currentFirmwareVersion = new ObservableField<>();

    private void setCurrentFirmwareVersion(Firmware firmware, Context context) {

        if (firmware.getFirmwareState() == FirmwareState.NULL) {

            currentFirmwareVersion.set(context.getString(R.string.no_firmware));

        } else {

            currentFirmwareVersion.set(context.getString(R.string.current_firmware_version, firmware.getCurrentFirmwareVersion()));

        }


    }

    public final ObservableField<String> currentFirmwareState = new ObservableField<>();

    private void setCurrentFirmwareState(FirmwareState firmwareState, Context context) {

        if (firmwareState == FirmwareState.NULL) {

            showFirmwareState.set(false);

            showStartStopBtn.set(false);

        } else {

            showFirmwareState.set(true);

            showStartStopBtn.set(false);

            if (firmwareState == FirmwareState.STARTING || firmwareState == FirmwareState.STOPPING) {

                enableStartStopBtn.set(false);

                if (firmwareState == FirmwareState.STARTING)
                    currentFirmwareState.set(context.getString(R.string.operating_title, context.getString(R.string.start)));
                else
                    currentFirmwareState.set(context.getString(R.string.operating_title, context.getString(R.string.stop)));

            } else {

                enableStartStopBtn.set(true);

                if (firmwareState == FirmwareState.STARTED) {

                    startStopBtnStr.set(context.getString(R.string.stop));

                    currentFirmwareState.set(context.getString(R.string.running));

                } else {

                    startStopBtnStr.set(context.getString(R.string.start));

                    currentFirmwareState.set(context.getString(R.string.stopped));

                }

            }
        }


    }

    public final ObservableBoolean showFirmwareState = new ObservableBoolean(true);

    public final ObservableBoolean enableStartStopBtn = new ObservableBoolean();

    public final ObservableBoolean showStartStopBtn = new ObservableBoolean();

    public final ObservableField<String> startStopBtnStr = new ObservableField<>();

    public final ObservableField<String> findNewVersion = new ObservableField<>();

    public final ObservableBoolean showNewVersionState = new ObservableBoolean();

    public final ObservableBoolean enableInstallOrRetryBtn = new ObservableBoolean();

    public final ObservableBoolean showInstallOrRetryBtn = new ObservableBoolean();

    public final ObservableBoolean showReleaseDate = new ObservableBoolean();

    private void setFindNewVersion(String currentVersion, String newVersion, Context context) {

        if (currentVersion.equals(newVersion)) {

            findNewVersion.set(context.getString(R.string.already_latest_version));

            showNewVersionState.set(false);
            showInstallOrRetryBtn.set(false);
            showReleaseDate.set(false);

            newVersionIconResId.set(R.drawable.firmware_done);

        } else {

            findNewVersion.set(context.getString(R.string.find_new_firmware_version, newVersion));

            showNewVersionState.set(true);
            showInstallOrRetryBtn.set(true);
            showReleaseDate.set(true);

            newVersionIconResId.set(R.drawable.new_releases);

        }

    }

    private void setNewVersionState(NewFirmwareState state, long downloaded, long length, Context context) {

        if (state == NewFirmwareState.READY) {

            enableInstallOrRetryBtn.set(true);

            installOrRetryBtn.set(context.getString(R.string.install));

            newVersionState.set(context.getString(R.string.downloaded));

            showProgress.set(false);

        } else if (state == NewFirmwareState.FAILED) {

            enableInstallOrRetryBtn.set(true);

            installOrRetryBtn.set(context.getString(R.string.re_download_the_item));

            newVersionState.set(context.getString(R.string.fail, context.getString(R.string.download)));

            showProgress.set(false);

        } else if (state == NewFirmwareState.DOWNLOADING) {

            enableInstallOrRetryBtn.set(false);

            newVersionState.set(context.getString(R.string.operating_title, context.getString(R.string.download)));

            if (downloaded != 0 && length != 0) {

                showProgress.set(true);

                double result = ((double) downloaded / length) * 100;

                progress.set((int) result + "%");

            }

        } else {

            showProgress.set(false);

            enableInstallOrRetryBtn.set(false);

            switch (state) {
                case IDLE:
                    newVersionState.set(context.getString(R.string.fail));
                    break;
                case REPACKING:
                    newVersionState.set(context.getString(R.string.repacking));
                    break;
                case VERIFYING:
                    newVersionState.set(context.getString(R.string.verifying));
                    break;
            }

        }

    }

    private void setShowCheckUpdate(CheckUpdateState checkUpdate) {

        if (checkUpdate == CheckUpdateState.PENDING)
            showCheckUpdate.set(false);
        else
            showCheckUpdate.set(true);
    }


    public final ObservableInt newVersionIconResId = new ObservableInt();

    public final ObservableField<String> newVersionState = new ObservableField<>();

    public final ObservableBoolean showProgress = new ObservableBoolean(false);

    public final ObservableField<String> progress = new ObservableField<>();

    public final ObservableBoolean showCheckUpdate = new ObservableBoolean(false);

    public final ObservableField<String> installOrRetryBtn = new ObservableField<>();

    public final ObservableField<String> releaseDate = new ObservableField<>();

    private void setReleaseDate(String date, Context context) {

        releaseDate.set(context.getString(R.string.release_date, date));

    }

}
