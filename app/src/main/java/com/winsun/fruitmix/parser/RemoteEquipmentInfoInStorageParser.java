package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.equipment.manage.model.EquipmentFileSystem;
import com.winsun.fruitmix.equipment.manage.model.EquipmentInfoInStorage;
import com.winsun.fruitmix.equipment.manage.model.EquipmentStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/10/17.
 */

public class RemoteEquipmentInfoInStorageParser extends BaseRemoteDataParser implements RemoteDatasParser<EquipmentInfoInStorage> {

    @Override
    public List<EquipmentInfoInStorage> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONObject rootObject = new JSONObject(root);

        JSONArray volumes = rootObject.optJSONArray("volumes");

        JSONObject volume0 = volumes.optJSONObject(0);

        EquipmentInfoInStorage equipmentInfoInStorage = new EquipmentInfoInStorage();

        EquipmentFileSystem equipmentFileSystem = new EquipmentFileSystem();

        equipmentFileSystem.setType(volume0.optString("fileSystemType"));
        equipmentFileSystem.setNumber(volume0.optInt("total"));

        JSONObject usage = volume0.optJSONObject("usage");

        JSONObject data = usage.optJSONObject("data");

        equipmentFileSystem.setMode(data.optString("mode").toUpperCase());

        equipmentInfoInStorage.setEquipmentFileSystem(equipmentFileSystem);

        EquipmentStorage equipmentStorage = new EquipmentStorage();

        JSONObject overall = usage.optJSONObject("overall");

        equipmentStorage.setTotalSize(overall.optLong("deviceSize"));
        equipmentStorage.setFreeSize(overall.optLong("free"));
        equipmentStorage.setUserDataSize(data.optLong("size"));

        equipmentInfoInStorage.setEquipmentStorage(equipmentStorage);

        return Collections.singletonList(equipmentInfoInStorage);

    }
}
