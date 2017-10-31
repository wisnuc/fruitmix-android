package com.winsun.fruitmix.equipment.manage.presenter;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.equipment.manage.data.EquipmentInfoDataSource;
import com.winsun.fruitmix.equipment.manage.model.BaseEquipmentInfo;
import com.winsun.fruitmix.equipment.manage.model.EquipmentCPU;
import com.winsun.fruitmix.equipment.manage.model.EquipmentFileSystem;
import com.winsun.fruitmix.equipment.manage.model.EquipmentHardware;
import com.winsun.fruitmix.equipment.manage.model.EquipmentMemory;
import com.winsun.fruitmix.equipment.manage.model.EquipmentStorage;
import com.winsun.fruitmix.equipment.manage.view.EquipmentInfoView;
import com.winsun.fruitmix.equipment.manage.viewmodel.EquipmentInfoDivide;
import com.winsun.fruitmix.equipment.manage.viewmodel.EquipmentInfoItem;
import com.winsun.fruitmix.equipment.manage.viewmodel.EquipmentInfoViewModel;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/18.
 */

public class EquipmentInfoPresenter extends BaseEquipmentInfoPresenter {

    public EquipmentInfoPresenter(EquipmentInfoDataSource equipmentInfoDataSource, EquipmentInfoView equipmentInfoView, LoadingViewModel loadingViewModel, NoContentViewModel noContentViewModel) {
        super(equipmentInfoDataSource, equipmentInfoView, loadingViewModel, noContentViewModel);
    }

    @Override
    protected void getEquipmentInfoItem(final BaseLoadDataCallback<EquipmentInfoItem> callback) {

        equipmentInfoDataSource.getBaseEquipmentInfo(new BaseLoadDataCallback<BaseEquipmentInfo>() {
            @Override
            public void onSucceed(List<BaseEquipmentInfo> data, OperationResult operationResult) {

                if(equipmentInfoView == null)
                    return;

                handleGetBaseEquipmentInfoSucceed(data.get(0), callback);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);

            }
        });

    }

    private void handleGetBaseEquipmentInfoSucceed(BaseEquipmentInfo baseEquipmentInfo, BaseLoadDataCallback<EquipmentInfoItem> callback) {
        List<EquipmentInfoItem> equipmentInfoItems = new ArrayList<>();

        EquipmentInfoViewModel equipmentInfoViewModel = new EquipmentInfoViewModel();
        equipmentInfoViewModel.setIconResID(R.drawable.my_equipment);
        equipmentInfoViewModel.setInfoKey(equipmentInfoView.getString(R.string.equiment_name));
        equipmentInfoViewModel.setInfoValue(baseEquipmentInfo.getEquipmentName());

        equipmentInfoItems.add(equipmentInfoViewModel);

        equipmentInfoItems.add(new EquipmentInfoDivide());

        EquipmentHardware equipmentHardware = baseEquipmentInfo.getEquipmentHardware();

        if(equipmentHardware != null){

            EquipmentInfoViewModel equipmentTypeViewModel = new EquipmentInfoViewModel();
            equipmentTypeViewModel.setIconResID(R.drawable.equipment_blue);
            equipmentTypeViewModel.setInfoKey(equipmentInfoView.getString(R.string.equipment_type));
            equipmentTypeViewModel.setInfoValue(equipmentHardware.getEquipmentType());

            equipmentInfoItems.add(equipmentTypeViewModel);

            EquipmentInfoViewModel hardwareSerialNumber = new EquipmentInfoViewModel();
            hardwareSerialNumber.setInfoKey(equipmentInfoView.getString(R.string.hardware_serial_number));
            hardwareSerialNumber.setInfoValue(equipmentHardware.getEquipmentHardwareSerialNumber());

            equipmentInfoItems.add(hardwareSerialNumber);

            EquipmentInfoViewModel macAddress = new EquipmentInfoViewModel();
            macAddress.setInfoKey(equipmentInfoView.getString(R.string.mac_address));
            macAddress.setInfoValue(equipmentHardware.getEquipmentMacAddress());

            equipmentInfoItems.add(macAddress);

            equipmentInfoItems.add(new EquipmentInfoDivide());

        }

        EquipmentMemory equipmentMemory = baseEquipmentInfo.getEquipmentMemory();

        EquipmentInfoViewModel totalMemory = new EquipmentInfoViewModel();
        totalMemory.setIconResID(R.drawable.sd_storage);
        totalMemory.setInfoKey(equipmentInfoView.getString(R.string.total_memory_size));
        totalMemory.setInfoValue(FileUtil.formatFileSize(equipmentMemory.getTotalMemorySize()));

        equipmentInfoItems.add(totalMemory);

        EquipmentInfoViewModel freeMemory = new EquipmentInfoViewModel();
        freeMemory.setInfoKey(equipmentInfoView.getString(R.string.free_memory_size));
        freeMemory.setInfoValue(FileUtil.formatFileSize(equipmentMemory.getFreeMemorySize()));

        equipmentInfoItems.add(freeMemory);

        EquipmentInfoViewModel availableMemory = new EquipmentInfoViewModel();
        availableMemory.setInfoKey(equipmentInfoView.getString(R.string.available_memory_size));
        availableMemory.setInfoValue(FileUtil.formatFileSize(equipmentMemory.getAvailableMemorySize()));

        equipmentInfoItems.add(availableMemory);

        equipmentInfoItems.add(new EquipmentInfoDivide());

        EquipmentCPU equipmentCPU = baseEquipmentInfo.getEquipmentCPU();

        EquipmentInfoViewModel cpuCoreNumber = new EquipmentInfoViewModel();
        cpuCoreNumber.setIconResID(R.drawable.cpu);
        cpuCoreNumber.setInfoKey(equipmentInfoView.getString(R.string.cpu_core_number));
        cpuCoreNumber.setInfoValue(equipmentCPU.getCpuCoreNumber() + "");

        equipmentInfoItems.add(cpuCoreNumber);

        EquipmentInfoViewModel cpuType = new EquipmentInfoViewModel();
        cpuType.setInfoKey(equipmentInfoView.getString(R.string.cpu_type));
        cpuType.setInfoValue(equipmentCPU.getCpuType());

        equipmentInfoItems.add(cpuType);

        EquipmentInfoViewModel cpuCacheSize = new EquipmentInfoViewModel();
        cpuCacheSize.setInfoKey(equipmentInfoView.getString(R.string.cpu_cache_size));
        cpuCacheSize.setInfoValue(FileUtil.formatFileSize(equipmentCPU.getCpuCacheSize()));

        equipmentInfoItems.add(cpuCacheSize);

        equipmentInfoItems.add(new EquipmentInfoDivide());

        EquipmentFileSystem equipmentFileSystem = baseEquipmentInfo.getEquipmentFileSystem();

        EquipmentInfoViewModel fileSystemType = new EquipmentInfoViewModel();
        fileSystemType.setIconResID(R.drawable.brtfs);
        fileSystemType.setInfoKey(equipmentInfoView.getString(R.string.file_system_type));
        fileSystemType.setInfoValue(equipmentFileSystem.getType());

        equipmentInfoItems.add(fileSystemType);

        EquipmentInfoViewModel diskCount = new EquipmentInfoViewModel();
        diskCount.setInfoKey(equipmentInfoView.getString(R.string.disk_count));
        diskCount.setInfoValue(equipmentFileSystem.getNumber() + "");

        equipmentInfoItems.add(diskCount);

        EquipmentInfoViewModel diskArrayMode = new EquipmentInfoViewModel();
        diskArrayMode.setInfoKey(equipmentInfoView.getString(R.string.disk_array_mode));
        diskArrayMode.setInfoValue(equipmentFileSystem.getMode());

        equipmentInfoItems.add(diskArrayMode);

        equipmentInfoItems.add(new EquipmentInfoDivide());

        EquipmentStorage equipmentStorage = baseEquipmentInfo.getEquipmentStorage();

        EquipmentInfoViewModel totalSpace = new EquipmentInfoViewModel();
        totalSpace.setIconResID(R.drawable.ic_storage_blue);
        totalSpace.setInfoKey(equipmentInfoView.getString(R.string.total_space));
        totalSpace.setInfoValue(FileUtil.formatFileSize(equipmentStorage.getTotalSize()));

        equipmentInfoItems.add(totalSpace);

        EquipmentInfoViewModel userDataSpace = new EquipmentInfoViewModel();
        userDataSpace.setInfoKey(equipmentInfoView.getString(R.string.user_data_space));
        userDataSpace.setInfoValue(FileUtil.formatFileSize(equipmentStorage.getUserDataSize()));

        equipmentInfoItems.add(userDataSpace);

        EquipmentInfoViewModel availableSpace = new EquipmentInfoViewModel();
        availableSpace.setInfoKey(equipmentInfoView.getString(R.string.available_space));
        availableSpace.setInfoValue(FileUtil.formatFileSize(equipmentStorage.getFreeSize()));

        equipmentInfoItems.add(availableSpace);

        callback.onSucceed(equipmentInfoItems, new OperationSuccess());

    }
}
