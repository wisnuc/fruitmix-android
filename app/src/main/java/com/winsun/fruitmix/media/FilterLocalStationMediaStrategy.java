package com.winsun.fruitmix.media;

import android.util.Log;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/19.
 */

public class FilterLocalStationMediaStrategy {

    public static FilterLocalStationMediaStrategy instance;

    public static final String TAG = FilterLocalStationMediaStrategy.class.getSimpleName();

    private FilterLocalStationMediaStrategy() {
    }

    public static FilterLocalStationMediaStrategy getInstance() {
        if (instance == null)
            instance = new FilterLocalStationMediaStrategy();
        return instance;
    }

    public List<Media> filter(Collection<Media> localMedias, Collection<Media> stationMedias) {

        Log.i(TAG, "before filter :" + Util.getCurrentFormatTime());

        List<Media> result = new ArrayList<>(localMedias);

        Map<String, Media> stationMediaMap = LocalCache.BuildMediaMapKeyIsUUID(stationMedias);

        for (Media media : localMedias) {

            String mediaUUID = media.getUuid();

            stationMediaMap.remove(mediaUUID);

        }

        Log.i(TAG, "after filter : " + Util.getCurrentFormatTime());

        result.addAll(stationMediaMap.values());

        return result;

    }

}
