package com.winsun.fruitmix.business.callback;

import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.EquipmentAlias;

import java.util.Collection;

/**
 * Created by Administrator on 2017/2/9.
 */

public interface LoadEquipmentAliasCallback {

    void onLoadSucceed(OperationResult result, Collection<EquipmentAlias> equipmentAliases);

    void onLoadFail(OperationResult result);

}
