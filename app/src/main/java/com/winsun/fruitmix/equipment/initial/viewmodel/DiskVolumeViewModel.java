package com.winsun.fruitmix.equipment.initial.viewmodel;

import android.content.Context;
import android.text.format.Formatter;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.equipment.initial.data.EquipmentDiskVolume;
import com.winsun.fruitmix.equipment.initial.data.InitialEquipmentRemoteDataSource;

/**
 * Created by Administrator on 2017/12/9.
 */

public class DiskVolumeViewModel {

    public static final String TAG = DiskVolumeViewModel.class.getSimpleName();

    private EquipmentDiskVolume equipmentDiskVolume;

    private boolean selected;

    public DiskVolumeViewModel(EquipmentDiskVolume equipmentDiskVolume) {
        this.equipmentDiskVolume = equipmentDiskVolume;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return equipmentDiskVolume.getName();
    }

    public String getSize(Context context) {
        return Formatter.formatFileSize(context, equipmentDiskVolume.getSize());
    }

    public boolean isAvailable() {

        return equipmentDiskVolume.getInstruction().isEmpty();

    }

    public String getState(Context context) {

        if (equipmentDiskVolume.getState().isEmpty())
            return context.getString(R.string.no_fileSystem_or_partition);
        else if (equipmentDiskVolume.getState().equals(InitialEquipmentRemoteDataSource.isPartitioned))
            return context.getString(R.string.is_partitioned);
        else
            return equipmentDiskVolume.getState() + context.getString(R.string.file_system);

    }

    String getInstruction(Context context) {

        String instruction = equipmentDiskVolume.getInstruction();

        if (instruction.contains("RootFS"))
            return context.getString(R.string.root_fs);
        else if (instruction.contains("ActiveSwap"))
            return context.getString(R.string.active_swap);
        else if (instruction.length() > 0)
            return context.getString(R.string.unformattable);
        else if (equipmentDiskVolume.getRemovable())
            return context.getString(R.string.removable);
        else
            return context.getString(R.string.formattable);

    }


    public String getModel(Context context) {

        if (equipmentDiskVolume.getModel().isEmpty())
            return context.getString(R.string.unknown_model);
        else
            return equipmentDiskVolume.getModel();
    }

    public String getInterface(Context context) {

        if (equipmentDiskVolume.getInterfaceType().isEmpty())
            return context.getString(R.string.unknown_interface);
        else
            return equipmentDiskVolume.getInterfaceType();

    }


}
