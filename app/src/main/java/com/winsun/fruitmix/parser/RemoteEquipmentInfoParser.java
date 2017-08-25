package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.model.EquipmentInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/8/24.
 */

public class RemoteEquipmentInfoParser implements RemoteDatasParser<EquipmentInfo> {

    @Override
    public List<EquipmentInfo> parse(String json) throws JSONException {

        EquipmentInfo equipmentInfo = new EquipmentInfo();

        JSONObject jsonObject = new JSONObject(json);

        String type = jsonObject.optString(EquipmentInfo.WS215I);

        if (type.isEmpty()) {

            type = EquipmentInfo.VIRTUAL_MACHINE;

            equipmentInfo.setType(type);

        }

        equipmentInfo.setLabel(type);

        return Collections.singletonList(equipmentInfo);

    }
}
