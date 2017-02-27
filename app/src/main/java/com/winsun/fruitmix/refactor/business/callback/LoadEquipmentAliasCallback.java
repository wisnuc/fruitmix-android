package com.winsun.fruitmix.refactor.business.callback;

import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.model.EquipmentAlias;

import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/2/9.
 */

public interface LoadEquipmentAliasCallback {

    void onLoadSucceed(OperationResult result, Collection<EquipmentAlias> equipmentAliases);

    void onLoadFail(OperationResult result);

}
