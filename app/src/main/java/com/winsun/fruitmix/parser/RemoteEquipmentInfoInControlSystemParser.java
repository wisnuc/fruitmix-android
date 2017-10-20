package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.equipment.manage.model.EquipmentCPU;
import com.winsun.fruitmix.equipment.manage.model.EquipmentHardware;
import com.winsun.fruitmix.equipment.manage.model.EquipmentInfoInControlSystem;
import com.winsun.fruitmix.equipment.manage.model.EquipmentMemory;
import com.winsun.fruitmix.equipment.search.data.EquipmentTypeInfo;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/10/17.
 */

public class RemoteEquipmentInfoInControlSystemParser extends BaseRemoteDataParser implements RemoteDatasParser<EquipmentInfoInControlSystem> {

    @Override
    public List<EquipmentInfoInControlSystem> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        EquipmentInfoInControlSystem equipmentInfoInControlSystem = new EquipmentInfoInControlSystem();

        JSONObject jsonObject = new JSONObject(root);

        equipmentInfoInControlSystem.setEquipmentName("我的盒子");

        parseHardware(equipmentInfoInControlSystem, jsonObject);

        parseCPU(equipmentInfoInControlSystem, jsonObject);

        parseMemory(equipmentInfoInControlSystem, jsonObject);

        return Collections.singletonList(equipmentInfoInControlSystem);
    }

    private void parseHardware(EquipmentInfoInControlSystem equipmentInfoInControlSystem, JSONObject jsonObject) {
        if (jsonObject.has(EquipmentTypeInfo.WS215I)) {

            EquipmentHardware equipmentHardware = new EquipmentHardware();
            equipmentHardware.setEquipmentType(EquipmentTypeInfo.WS215I.toUpperCase());

            JSONObject ws215i = jsonObject.optJSONObject(EquipmentTypeInfo.WS215I);

            equipmentHardware.setEquipmentHardwareSerialNumber(ws215i.optString("serial"));

            equipmentHardware.setEquipmentMacAddress(ws215i.optString("mac").toUpperCase());

            equipmentInfoInControlSystem.setEquipmentHardware(equipmentHardware);

        }
    }

    private void parseCPU(EquipmentInfoInControlSystem equipmentInfoInControlSystem, JSONObject jsonObject) {
        JSONArray cpuInfos = jsonObject.optJSONArray("cpuInfo");

        EquipmentCPU equipmentCPU = new EquipmentCPU();
        equipmentCPU.setCpuCoreNumber(cpuInfos.length());

        JSONObject cpuInfo0 = cpuInfos.optJSONObject(0);

        equipmentCPU.setCpuType(cpuInfo0.optString("modelName"));

        String cacheSize = cpuInfo0.optString("cacheSize");

        equipmentCPU.setCpuCacheSize(Util.getFirstNumInStr(cacheSize) * 1024);

        equipmentInfoInControlSystem.setEquipmentCPU(equipmentCPU);
    }

    private void parseMemory(EquipmentInfoInControlSystem equipmentInfoInControlSystem, JSONObject jsonObject) {
        JSONObject memoryInfo = jsonObject.optJSONObject("memInfo");

        EquipmentMemory equipmentMemory = new EquipmentMemory();

        String memTotal = memoryInfo.optString("memTotal");

        equipmentMemory.setTotalMemorySize(Util.getFirstNumInStr(memTotal) * 1024);

        String memFree = memoryInfo.optString("memFree");

        equipmentMemory.setFreeMemorySize(Util.getFirstNumInStr(memFree) * 1024);

        String memAvailable = memoryInfo.optString("memAvailable");

        equipmentMemory.setAvailableMemorySize(Util.getFirstNumInStr(memAvailable) * 1024);

        equipmentInfoInControlSystem.setEquipmentMemory(equipmentMemory);
    }

}
