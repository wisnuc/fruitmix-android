package com.winsun.fruitmix.equipment.manage.presenter;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.equipment.manage.data.EquipmentInfoDataSource;
import com.winsun.fruitmix.equipment.manage.model.EquipmentTimeInfo;
import com.winsun.fruitmix.equipment.manage.view.EquipmentInfoView;
import com.winsun.fruitmix.equipment.manage.viewmodel.EquipmentInfoItem;
import com.winsun.fruitmix.equipment.manage.viewmodel.EquipmentInfoViewModel;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/18.
 */

public class EquipmentTimePresenter extends BaseEquipmentInfoPresenter {

    public EquipmentTimePresenter(EquipmentInfoDataSource equipmentInfoDataSource, EquipmentInfoView equipmentInfoView, LoadingViewModel loadingViewModel, NoContentViewModel noContentViewModel) {
        super(equipmentInfoDataSource, equipmentInfoView, loadingViewModel, noContentViewModel);
    }

    @Override
    protected void getEquipmentInfoItem(final BaseLoadDataCallback<EquipmentInfoItem> callback) {

        equipmentInfoDataSource.getEquipmentTimeInfo(new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<EquipmentTimeInfo>() {
            @Override
            public void onSucceed(List<EquipmentTimeInfo> data, OperationResult operationResult) {

                handleGetEquipmentTimeInfoSucceed(data.get(0), callback);

            }

            @Override
            public void onFail(OperationResult operationResult) {
                callback.onFail(operationResult);
            }
        },this));

    }

    private void handleGetEquipmentTimeInfoSucceed(EquipmentTimeInfo equipmentTimeInfo, BaseLoadDataCallback<EquipmentInfoItem> callback) {

        List<EquipmentInfoItem> equipmentInfoItems = new ArrayList<>();

        EquipmentInfoViewModel localTime = new EquipmentInfoViewModel();
        localTime.setIconResID(R.drawable.time);
        localTime.setInfoKey(equipmentInfoView.getString(R.string.local_time));
        localTime.setInfoValue(equipmentTimeInfo.getLocalTime());

        equipmentInfoItems.add(localTime);

        EquipmentInfoViewModel universalTime = new EquipmentInfoViewModel();
        universalTime.setInfoKey(equipmentInfoView.getString(R.string.universal_time));
        universalTime.setInfoValue(equipmentTimeInfo.getUniversalTime());

        equipmentInfoItems.add(universalTime);

        EquipmentInfoViewModel rtcTime = new EquipmentInfoViewModel();
        rtcTime.setInfoKey(equipmentInfoView.getString(R.string.rtc_time));
        rtcTime.setInfoValue(equipmentTimeInfo.getRTCTime());

        equipmentInfoItems.add(rtcTime);

        EquipmentInfoViewModel timeZone = new EquipmentInfoViewModel();
        timeZone.setInfoKey(equipmentInfoView.getString(R.string.time_zone));
        timeZone.setInfoValue(equipmentTimeInfo.getTimeZone());

        equipmentInfoItems.add(timeZone);

        EquipmentInfoViewModel timeSynchronized = new EquipmentInfoViewModel();
        timeSynchronized.setInfoKey(equipmentInfoView.getString(R.string.ntp_synchronized));
        timeSynchronized.setInfoValue(equipmentTimeInfo.isNTPSynchronized() ? "yes" : "no");

        equipmentInfoItems.add(timeSynchronized);

        EquipmentInfoViewModel useNetworkTime = new EquipmentInfoViewModel();
        useNetworkTime.setInfoKey(equipmentInfoView.getString(R.string.network_time_on));
        useNetworkTime.setInfoValue(equipmentTimeInfo.isNetworkTimeOn() ? "yes" : "no");

        equipmentInfoItems.add(useNetworkTime);

        callback.onSucceed(equipmentInfoItems, new OperationSuccess());

    }


}
