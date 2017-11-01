package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.stations.StationInfoCallByStationAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/11/1.
 */

public class RemoteStationsCallByStationAPIParser implements RemoteDatasParser<StationInfoCallByStationAPI> {
    @Override
    public List<StationInfoCallByStationAPI> parse(String json) throws JSONException {

        JSONObject jsonObject = new JSONObject(json);

        StationInfoCallByStationAPI stationInfoCallByStationAPI = new StationInfoCallByStationAPI();
        stationInfoCallByStationAPI.setId(jsonObject.optString("id"));
        stationInfoCallByStationAPI.setName(jsonObject.optString("name"));

        return Collections.singletonList(stationInfoCallByStationAPI);

    }
}
