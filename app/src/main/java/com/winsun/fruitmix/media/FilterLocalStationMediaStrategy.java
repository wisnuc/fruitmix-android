package com.winsun.fruitmix.media;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.LocalCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/19.
 */

public class FilterLocalStationMediaStrategy {

    public static FilterLocalStationMediaStrategy instance;

    private FilterLocalStationMediaStrategy() {
    }

    public static FilterLocalStationMediaStrategy getInstance() {
        if (instance == null)
            instance = new FilterLocalStationMediaStrategy();
        return instance;
    }

    public List<Media> filter(Collection<Media> localMedias, Collection<Media> stationMedias) {

        List<Media> result = new ArrayList<>(localMedias);

        Map<String, Media> stationMediaMap = LocalCache.BuildMediaMapKeyIsUUID(stationMedias);

        for (Media media : localMedias) {

            String mediaUUID = media.getUuid();

            stationMediaMap.remove(mediaUUID);

        }

        result.addAll(stationMediaMap.values());

        return result;

    }

}
