package com.winsun.fruitmix.strategy;

import android.util.LongSparseArray;

import com.winsun.fruitmix.mediaModule.model.Media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/4/28.
 */

public class ChooseRecommendAlbumStrategyB extends ChooseRecommendAlbumStrategy {

    private int dateIntervalParam = 1;

    public ChooseRecommendAlbumStrategyB(int days, double averageValueWeightedValue) {
        super(days, averageValueWeightedValue);
    }

    public void setDateIntervalParam(int dateIntervalParam) {
        this.dateIntervalParam = dateIntervalParam;
    }

    @Override
    public Collection<List<Long>> chooseRecommendAlbum(Collection<Long> times, LongSparseArray<List<Media>> mapKeyIsTimeValueIsMedias, int photoCountAverageValue) {

        int greatThanAverageValueCount = 0;

        Collection<List<Long>> results = new ArrayList<>();

        List<Long> greatThanAverageValue = null;

        Long preTime = 0L;

        boolean isNextDay;

        for (Long time : times) {

            int photoCount = mapKeyIsTimeValueIsMedias.get(time).size();

            isNextDay = preTime == 0L || preTime + 24 * 60 * 60 * 1000 * dateIntervalParam < time && time < preTime + 48 * 60 * 1000 * dateIntervalParam;

            if (photoCount > photoCountAverageValue * averageValueWeightedValue && isNextDay) {

                if (greatThanAverageValueCount == 0)
                    greatThanAverageValue = new ArrayList<>();

                greatThanAverageValueCount++;

                greatThanAverageValue.add(time);

            } else {

                greatThanAverageValueCount = 0;

                if (greatThanAverageValue != null)
                    greatThanAverageValue.clear();

            }

            preTime = time;

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
