package com.winsun.fruitmix.equipment.manage.presenter;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.equipment.manage.data.EquipmentInfoDataSource;
import com.winsun.fruitmix.equipment.manage.model.EquipmentNetworkInfo;
import com.winsun.fruitmix.equipment.manage.view.EquipmentInfoView;
import com.winsun.fruitmix.equipment.manage.viewmodel.EquipmentInfoItem;
import com.winsun.fruitmix.equipment.manage.viewmodel.EquipmentInfoViewModel;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/18.
 */

public class EquipmentNetworkPresenter extends BaseEquipmentInfoPresenter {

    private SystemSettingDataSource systemSettingDataSource;

    public EquipmentNetworkPresenter(EquipmentInfoDataSource equipmentInfoDataSource, EquipmentInfoView equipmentInfoView,
                                     LoadingViewModel loadingViewModel, NoContentViewModel noContentViewModel,
                                     SystemSettingDataSource systemSettingDataSource) {
        super(equipmentInfoDataSource, equipmentInfoView, loadingViewModel, noContentViewModel);
        this.systemSettingDataSource = systemSettingDataSource;
    }

    @Override
    protected void getEquipmentInfoItem(final BaseLoadDataCallback<EquipmentInfoItem> callback) {

        equipmentInfoDataSource.getEquipmentNetworkInfo(new BaseLoadDataCallback<EquipmentNetworkInfo>() {
            @Override
            public void onSucceed(List<EquipmentNetworkInfo> data, OperationResult operationResult) {

                if (equipmentInfoView == null)
                    return;

                handleGetEquipmentNetworkInfoSucceed(data.get(0), callback);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);
            }
        });

    }

    private void handleGetEquipmentNetworkInfoSucceed(EquipmentNetworkInfo equipmentNetworkInfo, BaseLoadDataCallback<EquipmentInfoItem> callback) {

        List<EquipmentInfoItem> equipmentInfoItems = new ArrayList<>();

        EquipmentInfoViewModel equipmentInfoViewModel = new EquipmentInfoViewModel();
        equipmentInfoViewModel.setIconResID(R.drawable.network);
        equipmentInfoViewModel.setInfoKey(equipmentInfoView.getString(R.string.nic_name));
        equipmentInfoViewModel.setInfoValue(equipmentNetworkInfo.getNicName());

        equipmentInfoItems.add(equipmentInfoViewModel);

        EquipmentInfoViewModel bandwidth = new EquipmentInfoViewModel();
        bandwidth.setInfoKey(equipmentInfoView.getString(R.string.bandwidth));
        bandwidth.setInfoValue(equipmentNetworkInfo.getBandwidth());

        equipmentInfoItems.add(bandwidth);

        EquipmentInfoViewModel type = new EquipmentInfoViewModel();
        type.setInfoKey(equipmentInfoView.getString(R.string.type));
        type.setInfoValue(equipmentNetworkInfo.getType());

        equipmentInfoItems.add(type);

        EquipmentInfoViewModel networkAddress = new EquipmentInfoViewModel();
        networkAddress.setInfoKey(equipmentInfoView.getString(R.string.network_address));
        networkAddress.setInfoValue(equipmentNetworkInfo.getAddress());

        equipmentInfoItems.add(networkAddress);

        EquipmentInfoViewModel subnetMask = new EquipmentInfoViewModel();
        subnetMask.setInfoKey(equipmentInfoView.getString(R.string.subnet_mask));
        subnetMask.setInfoValue(equipmentNetworkInfo.getSubnetMask());

        equipmentInfoItems.add(subnetMask);

        EquipmentInfoViewModel nicMacAddress = new EquipmentInfoViewModel();
        nicMacAddress.setInfoKey(equipmentInfoView.getString(R.string.nic_mac_address));
        nicMacAddress.setInfoValue(equipmentNetworkInfo.getMacAddress());

        equipmentInfoItems.add(nicMacAddress);

        EquipmentInfoViewModel networkConnectionType = new EquipmentInfoViewModel();
        networkConnectionType.setInfoKey(equipmentInfoView.getString(R.string.network_connection_type));

        if (systemSettingDataSource.getLoginWithWechatCodeOrNot()) {
            networkConnectionType.setInfoValue(equipmentInfoView.getString(R.string.remote_connection));
        } else {
            networkConnectionType.setInfoValue(equipmentInfoView.getString(R.string.local_station_connection));
        }

        equipmentInfoItems.add(networkConnectionType);

        callback.onSucceed(equipmentInfoItems, new OperationSuccess());

    }


}
