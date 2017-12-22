package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.plugin.data.PluginStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/22.
 */

public class RemotePluginStatusParser extends BaseRemoteDataParser implements RemoteDatasParser<PluginStatus> {

    @Override
    public List<PluginStatus> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONObject jsonObject = new JSONObject(root);

        List<PluginStatus> pluginStatuses = new ArrayList<>(1);

        if (jsonObject.has("status")) {
            String status = jsonObject.optString("status");

            if (status.equals("active"))
                pluginStatuses.add(new PluginStatus(true));
            else
                pluginStatuses.add(new PluginStatus(false));


        } else if (jsonObject.has("switch")) {

            boolean result = jsonObject.optBoolean("switch");

            pluginStatuses.add(new PluginStatus(result));

        }

        return pluginStatuses;

    }

}
