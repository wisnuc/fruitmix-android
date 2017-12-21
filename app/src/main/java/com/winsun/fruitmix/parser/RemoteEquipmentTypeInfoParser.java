package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.equipment.search.data.EquipmentTypeInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/8/24.
 */

public class RemoteEquipmentTypeInfoParser extends BaseRemoteDataParser implements RemoteDatasParser<EquipmentTypeInfo> {

    @Override
    public List<EquipmentTypeInfo> parse(String json) throws JSONException {

        EquipmentTypeInfo equipmentTypeInfo = new EquipmentTypeInfo();

        String root = checkHasWrapper(json);

        JSONObject jsonObject = new JSONObject(root);

        String type;

        if (jsonObject.has(EquipmentTypeInfo.WS215I)) {

            type = EquipmentTypeInfo.WS215I;

            equipmentTypeInfo.setType(type);

        } else {
            type = "";

            equipmentTypeInfo.setType(type);
        }

        equipmentTypeInfo.setLabel(type);

        return Collections.singletonList(equipmentTypeInfo);

    }
}
