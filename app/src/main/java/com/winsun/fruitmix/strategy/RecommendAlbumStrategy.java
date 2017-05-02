package com.winsun.fruitmix.strategy;

import android.util.LongSparseArray;

import com.winsun.fruitmix.mediaModule.model.Media;

import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/4/28.
 */

public abstract class RecommendAlbumStrategy {

    double averageValueWeightedValue = 1;

    int days = 1;

    RecommendAlbumStrategy(int days, double averageValueWeightedValue) {
        this.days = days;
        this.averageValueWeightedValue = averageValueWeightedValue;
    }

    public void setAverageValueWeightedValue(double averageValueWeightedValue) {
        this.averageValueWeightedValue = averageValueWeightedValue;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public abstract Collection<List<Long>> chooseRecommendAlbum(Collection<Long> times, LongSparseArray<List<Media>> mapKeyIsTimeValueIsMedias, int photoCountAverageValue);

}
