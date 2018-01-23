package com.winsun.fruitmix.model.operationResult;

import java.util.List;

/**
 * Created by Administrator on 2018/1/19.
 */

public class OperationSucceedWithData <T> extends OperationSuccess {

    private List<T> mDatas;

    public OperationSucceedWithData(List<T> datas) {
        mDatas = datas;
    }

    public List<T> getDatas() {
        return mDatas;
    }

}
