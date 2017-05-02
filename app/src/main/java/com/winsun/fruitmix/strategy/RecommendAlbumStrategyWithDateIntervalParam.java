package com.winsun.fruitmix.strategy;

import android.util.LongSparseArray;

import com.winsun.fruitmix.mediaModule.model.Media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/4/28.
 */

public class RecommendAlbumStrategyWithDateIntervalParam extends RecommendAlbumStrategy {

    private int dateIntervalParam = 1;

    public RecommendAlbumStrategyWithDateIntervalParam(int days, double averageValueWeightedValue) {
        super(days, averageValueWeightedValue);
    }

    public void setDateIntervalParam(int dateIntervalParam) {
        this.dateIntervalParam = dateIntervalParam;
    }

    @Override
    public Collection<List<Long>> chooseRecommendAlbum(Collection<Long> times, LongSparseArray<List<Media>> mapKeyIsTimeValueIsMedias, int photoCountAverageValue) {

        int greatThanAverageValueCount = 0;

        Collection<List<Long>> results = new ArrayList<>();

        List<Long> greatThanAverageValue = new ArrayList<>();

        Long preTime = 0L;

        boolean isNextDay;

        double photoCountThreshold = photoCountAverageValue * averageValueWeightedValue;

        for (Long time : times) {

            if (preTime == 0) {
                preTime = time;
                continue;
            }

            isNextDay = time - preTime <= 24 * 60 * 60 * 1000 * dateIntervalParam;

            int preTimePhotoCount = mapKeyIsTimeValueIsMedias.get(preTime).size();

            int currentTimePhotoCount = mapKeyIsTimeValueIsMedias.get(time).size();

            if (preTimePhotoCount > photoCountThreshold && currentTimePhotoCount > photoCountThreshold && isNextDay) {

                if (!greatThanAverageValue.contains(preTime)) {

                    greatThanAverageValueCount++;

                    greatThanAverageValue.add(preTime);

                }

                if (!greatThanAverageValue.contains(time)) {

                    greatThanAverageValueCount++;

                    greatThanAverageValue.add(time);

                }

            } else {

                if (greatThanAverageValueCount >= days) {

                    results.add(new ArrayList<>(greatThanAverageValue));

                }

                greatThanAverageValueCount = 0;

                greatThanAverageValue.clear();
            }

            preTime = time;

        }

        //check the end
        if (greatThanAverageValueCount >= days) {

            results.add(new ArrayList<>(greatThanAverageValue));

        }

        return results;
    }
}
