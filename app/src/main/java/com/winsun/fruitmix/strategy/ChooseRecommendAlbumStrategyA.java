package com.winsun.fruitmix.strategy;

import android.util.LongSparseArray;

import com.winsun.fruitmix.mediaModule.model.Media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/4/28.
 */

public class ChooseRecommendAlbumStrategyA extends ChooseRecommendAlbumStrategy {

    public ChooseRecommendAlbumStrategyA(int days, double averageValueWeightedValue) {
        super(days, averageValueWeightedValue);
    }

    @Override
    public Collection<List<Long>> chooseRecommendAlbum(Collection<Long> times, LongSparseArray<List<Media>> mapKeyIsTimeValueIsMedias, int photoCountAverageValue) {

        int greatThanAverageValueCount = 0;

        Collection<List<Long>> results = new ArrayList<>();

        List<Long> greatThanAverageValue = null;

        for (Long time : times) {

            int photoCount = mapKeyIsTimeValueIsMedias.get(time).size();

            if (photoCount > photoCountAverageValue * averageValueWeightedValue) {

                if (greatThanAverageValueCount == 0)
                    greatThanAverageValue = new ArrayList<>();

                greatThanAverageValueCount++;

                greatThanAverageValue.add(time);
            } else {

                greatThanAverageValueCount = 0;

                if (greatThanAverageValue != null)
                    greatThanAverageValue.clear();

            }

            if (greatThanAverageValueCount >= days) {
                greatThanAverageValueCount = 0;

                if (greatThanAverageValue != null) {

                    results.add(greatThanAverageValue);

                    greatThanAverageValue = null;
                }

            }

        }

        return results;
    }
}
