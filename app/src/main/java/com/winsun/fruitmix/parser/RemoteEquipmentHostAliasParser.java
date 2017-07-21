package com.winsun.fruitmix.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/11.
 */

public class RemoteEquipmentHostAliasParser implements RemoteDatasParser<String> {

    @Override
    public List<String> parse(String json) throws JSONException {

        JSONObject alias;
        JSONArray root;

        int length;

        root = new JSONArray(json);

        length = root.length();

        List<String> aliasList = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            alias = root.getJSONObject(i);

            String ip = alias.getString("ipv4");

            aliasList.add(ip);

        }

        return aliasList;
    }
}
