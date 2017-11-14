package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.stations.Station;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/14.
 */

public class RemoteStationsCallByCloudAPIParser extends BaseRemoteDataParser implements RemoteDatasParser<Station> {

    @Override
    public List<Station> parse(String json) throws JSONException {

        String rootStr = checkHasWrapper(json);

        JSONArray root = new JSONArray(rootStr);

        List<Station> stations = new ArrayList<>();

        for (int i = 0; i < root.length(); i++) {

            JSONObject jsonObject = root.getJSONObject(i);

            Station station = new Station();

            station.setId(jsonObject.optString("id"));
            station.setLabel(jsonObject.optString("name"));

            JSONArray ips = jsonObject.optJSONArray("LANIP");

            if (ips == null)
                continue;

            for (int j = 0; j < ips.length(); j++) {

                station.addIp(ips.optString(j));

            }

            station.setOnline(jsonObject.optBoolean("isOnline"));

            stations.add(station);

        }

        return stations;
    }
}
