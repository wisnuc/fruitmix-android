package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.equipment.manage.model.EquipmentFileSystem;
import com.winsun.fruitmix.equipment.manage.model.EquipmentInfoInStorage;
import com.winsun.fruitmix.equipment.manage.model.EquipmentStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

        List<EquipmentInfoInStorage> equipmentInfoInStorages = new ArrayList<>();

        for (int i = 0; i < volumes.length(); i++) {

            JSONObject volume = volumes.optJSONObject(i);

            EquipmentInfoInStorage equipmentInfoInStorage = new EquipmentInfoInStorage();

            EquipmentFileSystem equipmentFileSystem = new EquipmentFileSystem();

            equipmentFileSystem.setUuid(volume.optString("fileSystemUUID"));
            equipmentFileSystem.setType(volume.optString("fileSystemType"));
            equipmentFileSystem.setNumber(volume.optInt("total"));

            JSONObject usage = volume.optJSONObject("usage");

            JSONObject data = usage.optJSONObject("data");

            equipmentFileSystem.setMode(data.optString("mode").toUpperCase());

            equipmentInfoInStorage.setEquipmentFileSystem(equipmentFileSystem);

            EquipmentStorage equipmentStorage = new EquipmentStorage();

            JSONObject overall = usage.optJSONObject("overall");

            equipmentStorage.setTotalSize(overall.optLong("deviceSize"));
            equipmentStorage.setFreeSize(overall.optLong("free"));
            equipmentStorage.setUserDataSize(data.optLong("size"));

            equipmentInfoInStorage.setEquipmentStorage(equipmentStorage);

            equipmentInfoInStorages.add(equipmentInfoInStorage);

        }

        return equipmentInfoInStorages;

    }
}
