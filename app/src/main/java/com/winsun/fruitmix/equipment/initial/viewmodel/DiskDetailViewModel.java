package com.winsun.fruitmix.equipment.initial.viewmodel;

import android.content.Context;

import com.winsun.fruitmix.R;

/**
 * Created by Administrator on 2017/12/9.
 */

public class DiskDetailViewModel {

    private DiskVolumeViewModel mDiskVolumeViewModel;

    public DiskDetailViewModel(DiskVolumeViewModel diskVolumeViewModel) {
        mDiskVolumeViewModel = diskVolumeViewModel;
    }

    public String getModel(Context context) {
        return context.getString(R.string.colon, context.getString(R.string.disk_model), mDiskVolumeViewModel.getModel(context));
    }

    public String getName(Context context) {
        return context.getString(R.string.colon, context.getString(R.string.disk_name), mDiskVolumeViewModel.getName());
    }

    public String getSize(Context context) {
        return context.getString(R.string.colon, context.getString(R.string.disk_size), mDiskVolumeViewModel.getSize(context));
    }

    public String getInterface(Context context) {
        return context.getString(R.string.colon, context.getString(R.string.disk_interface), mDiskVolumeViewModel.getInterface(context));
    }

    public String getState(Context context) {
        return context.getString(R.string.colon, context.getString(R.string.disk_state), mDiskVolumeViewModel.getState(context));
    }

    public String getInstruction(Context context) {
        return context.getString(R.string.colon, context.getString(R.string.disk_instruction), mDiskVolumeViewModel.getInstruction(context));
    }

    public boolean isAvailable(){
        return mDiskVolumeViewModel.isAvailable();
    }

}
