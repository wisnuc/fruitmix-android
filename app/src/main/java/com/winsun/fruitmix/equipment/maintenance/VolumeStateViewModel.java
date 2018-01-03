package com.winsun.fruitmix.equipment.maintenance;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.equipment.maintenance.data.VolumeState;

/**
 * Created by Administrator on 2017/12/29.
 */

public class VolumeStateViewModel {

    public VolumeStateViewModel(VolumeState volumeState, Context context) {

        diskTitle.set(context.getString(R.string.disk_title, volumeState.getPosition() + ""));
        type.set(volumeState.getType());
        mode.set(volumeState.getMode());

        isMounted.set(volumeState.isMounted());
        noMissing.set(volumeState.getNoMissing());
        lastSystem.set(volumeState.getLastSystem());
        fruitmixOK.set(volumeState.getFruitmixOK());
        usersOK.set(volumeState.getUserOK());

        showStartBtn.set(volumeState.isMounted() && volumeState.getNoMissing() && volumeState.getLastSystem()
                && volumeState.getLastSystem() && volumeState.getFruitmixOK() && volumeState.getUserOK());

    }

    public final ObservableField<String> diskTitle = new ObservableField<>();

    public final ObservableField<String> type = new ObservableField<>();

    public final ObservableField<String> mode = new ObservableField<>();

    public final ObservableBoolean showStartBtn = new ObservableBoolean();

    public final ObservableBoolean isMounted = new ObservableBoolean();

    public final ObservableBoolean noMissing = new ObservableBoolean();

    public final ObservableBoolean lastSystem = new ObservableBoolean();

    public final ObservableBoolean fruitmixOK = new ObservableBoolean();

    public final ObservableBoolean usersOK = new ObservableBoolean();


}
