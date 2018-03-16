package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.stations.Station;

/**
 * Created by Administrator on 2018/3/14.
 */

public class LocalStationParser implements LocalDataParser<Station> {
    @Override
    public Station parse(Cursor cursor) {

        String stationID = cursor.getString(cursor.getColumnIndex(DBHelper.STATION_KEY_ID));
        String stationName = cursor.getString(cursor.getColumnIndex(DBHelper.STATION_KEY_NAME));

        Station station = new Station();
        station.setLabel(stationName);
        station.setId(stationID);

        return station;

    }
}
