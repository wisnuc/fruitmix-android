package com.winsun.fruitmix.regeocode;

import com.winsun.fruitmix.mediaModule.model.Media;

import java.util.List;

/**
 * Created by Administrator on 2017/5/5.
 */

public interface ReverseGeocode {

    boolean reverseGeocodeLongitudeLatitudeInMedias(List<Media> medias);

}
