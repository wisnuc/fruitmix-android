package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.equipment.manage.model.EquipmentTimeInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/10/18.
 */

public class RemoteEquipmentTimeInfoParser extends BaseRemoteDataParser implements RemoteDatasParser<EquipmentTimeInfo> {
    @Override
    public List<EquipmentTimeInfo> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONObject rootJsonObject = new JSONObject(root);

        EquipmentTimeInfo equipmentTimeInfo = new EquipmentTimeInfo();

        equipmentTimeInfo.setLocalTime(rootJsonObject.optString("Local time"));
        equipmentTimeInfo.setUniversalTime(rootJsonObject.optString("Universal time"));
        equipmentTimeInfo.setRTCTime(rootJsonObject.optString("RTC time"));
        equipmentTimeInfo.setTimeZone(rootJsonObject.optString("Time zone"));
        equipmentTimeInfo.setNetworkTimeOn(rootJsonObject.optString("Network time on").equals("yes"));
        equipmentTimeInfo.setNTPSynchronized(rootJsonObject.optString("NTP synchronized").equals("yes"));

        return Collections.singletonList(equipmentTimeInfo);
    }
}
