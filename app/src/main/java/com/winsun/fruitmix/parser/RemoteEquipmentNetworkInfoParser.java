package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.equipment.manage.model.EquipmentNetworkInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/10/18.
 */

public class RemoteEquipmentNetworkInfoParser extends BaseRemoteDataParser implements RemoteDatasParser<EquipmentNetworkInfo> {

    @Override
    public List<EquipmentNetworkInfo> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONArray rootJsonArray = new JSONArray(root);

        JSONObject rootJsonObject = null;

        JSONArray ipAddresses = null;

        for (int i = 0; i < rootJsonArray.length(); i++) {

            rootJsonObject = rootJsonArray.optJSONObject(i);

            if (rootJsonObject.has("ipAddresses")) {

                ipAddresses = rootJsonObject.optJSONArray("ipAddresses");
                if (ipAddresses.length() > 0)
                    break;
            }

        }

        EquipmentNetworkInfo equipmentNetworkInfo = new EquipmentNetworkInfo();

        if (rootJsonObject != null && ipAddresses != null) {

            equipmentNetworkInfo.setNicName(rootJsonObject.optString("name"));
            equipmentNetworkInfo.setBandwidth(rootJsonObject.optInt("speed") + " M");

            for (int j = 0; j < ipAddresses.length(); j++) {

                JSONObject ipAddress = ipAddresses.optJSONObject(j);

                String family = ipAddress.optString("family");
                if (family.equals("IPv4")) {

                    equipmentNetworkInfo.setType(family);
                    equipmentNetworkInfo.setAddress(ipAddress.optString("address"));
                    equipmentNetworkInfo.setSubnetMask(ipAddress.optString("netmask"));
                    equipmentNetworkInfo.setMacAddress(ipAddress.optString("mac").toUpperCase());
                    break;
                }

            }

        }

        return Collections.singletonList(equipmentNetworkInfo);
    }
}
