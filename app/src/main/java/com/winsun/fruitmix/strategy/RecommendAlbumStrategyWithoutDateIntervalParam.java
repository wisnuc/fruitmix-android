package com.winsun.fruitmix.strategy;

import android.util.LongSparseArray;

import com.winsun.fruitmix.mediaModule.model.Media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/4/28.
 */

public class RecommendAlbumStrategyWithoutDateIntervalParam extends RecommendAlbumStrategy {

    public RecommendAlbumStrategyWithoutDateIntervalParam(int days, double averageValueWeightedValue) {
        super(days, averageValueWeightedValue);
    }

    @Override
    public Collection<List<Long>> chooseRecommendAlbum(Collection<Long> times, LongSparseArray<List<Media>> mapKeyIsTimeValueIsMedias, int photoCountAverageValue) {

        int greatThanAverageValueCount = 0;

        Collection<List<Long>> results = new ArrayList<>();

        List<Long> greatThanAverageValue = new ArrayList<>();

        for (Long time : times) {

            int photoCount = mapKeyIsTimeValueIsMedias.get(time).size();

            if (photoCount > photoCountAverageValue * averageValueWeightedValue) {

                greatThanAverageValueCount++;

                greatThanAverageValue.add(time);
            } else {

                if (greatThanAverageValueCount >= days) {

                    results.add(new ArrayList<>(greatThanAverageValue));

                }

                greatThanAverageValueCount = 0;

                greatThanAverageValue.clear();

            }

        }

        //check the end
        if (greatThanAverageValueCount >= days) {

            results.add(new ArrayList<>(greatThanAverageValue));

        }

        return results;
    }
}
