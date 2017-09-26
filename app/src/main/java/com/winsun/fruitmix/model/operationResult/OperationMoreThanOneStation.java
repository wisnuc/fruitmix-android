package com.winsun.fruitmix.model.operationResult;

import android.content.Context;

import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.stations.Station;
import com.winsun.fruitmix.token.WeChatTokenUserWrapper;

import java.util.List;

/**
 * Created by Administrator on 2017/9/21.
 */

public class OperationMoreThanOneStation extends OperationResult {

    private List<Station> stations;
    private WeChatTokenUserWrapper weChatTokenUserWrapper;

    public OperationMoreThanOneStation(List<Station> stations, WeChatTokenUserWrapper weChatTokenUserWrapper) {
        this.stations = stations;
        this.weChatTokenUserWrapper = weChatTokenUserWrapper;
    }

    @Override
    public String getResultMessage(Context context) {
        return "";
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.MORE_THAN_ONE_STATION;
    }

    public List<Station> getStations() {
        return stations;
    }

    public WeChatTokenUserWrapper getWeChatTokenUserWrapper() {
        return weChatTokenUserWrapper;
    }
}
