package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.equipment.search.data.EquipmentBootInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/12/7.
 */

public class RemoteEquipmentBootInfoParser implements RemoteDatasParser<EquipmentBootInfo> {
    @Override
    public List<EquipmentBootInfo> parse(String json) throws JSONException {

        JSONObject jsonObject = new JSONObject(json);

        return Collections.singletonList(new EquipmentBootInfo(jsonObject.optString("mode"),
                jsonObject.optString("last"), jsonObject.optString("state"), jsonObject.optString("current"),
                jsonObject.optString("error")));

    }
}
