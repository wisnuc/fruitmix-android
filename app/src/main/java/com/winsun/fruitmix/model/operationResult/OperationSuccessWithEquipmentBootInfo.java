package com.winsun.fruitmix.model.operationResult;

import com.winsun.fruitmix.equipment.search.data.EquipmentBootInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/12/10.
 */

public class OperationSuccessWithEquipmentBootInfo extends OperationSuccess {

    private List<EquipmentBootInfo> mEquipmentBootInfos;

    public OperationSuccessWithEquipmentBootInfo(List<EquipmentBootInfo> equipmentBootInfos) {
        mEquipmentBootInfos = equipmentBootInfos;
    }

    public List<EquipmentBootInfo> getEquipmentBootInfos() {
        return mEquipmentBootInfos;
    }
}
