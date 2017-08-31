package com.winsun.fruitmix;

import android.util.LongSparseArray;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mock.MockApplication;
import com.winsun.fruitmix.strategy.RecommendAlbumStrategy;
import com.winsun.fruitmix.strategy.RecommendAlbumStrategyWithDateIntervalParam;
import com.winsun.fruitmix.strategy.RecommendAlbumStrategyWithoutDateIntervalParam;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/5/2.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class RecommendAlbumStrategyTest {

    private Collection<Media> medias;
    private int averageValue;

    private List<Long> times;
    private LongSparseArray<List<Media>> mapKeyIsDateValueIsMedias;

    @Before
    public void init() {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        times = new ArrayList<>();

        mapKeyIsDateValueIsMedias = new LongSparseArray<>();

        medias = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Media media = new Media();
            media.setTime("2016-12-16");
            medias.add(media);
        }

        for (int i = 0; i < 2; i++) {
            Media media = new Media();
            media.setTime("2016-12-18");
            medias.add(media);
        }

        for (int i = 0; i < 14; i++) {
            Media media = new Media();
            media.setTime("2016-12-19");
            medias.add(media);
        }

        for (int i = 0; i < 10; i++) {
            Media media = new Media();
            media.setTime("2016-12-30"); //1483027200
            medias.add(media);
        }

        for (int i = 0; i < 6; i++) {
            Media media = new Media();
            media.setTime("2017-01-01"); //1483200000
            medias.add(media);
        }

        for (int i = 0; i < 5; i++) {
            Media media = new Media();
            media.setTime("2017-01-16");
            medias.add(media);
        }

        fillTimesAndMap(medias, df, times, mapKeyIsDateValueIsMedias);

        Collections.sort(times, new Comparator<Long>() {
            @Override
            public int compare(Long lhs, Long rhs) {
                return (int) (lhs - rhs);
            }
        });

        int count = 0;

        for (int i = 0; i < mapKeyIsDateValueIsMedias.size(); i++) {
            List<Media> medias1 = mapKeyIsDateValueIsMedias.get(mapKeyIsDateValueIsMedias.keyAt(i));

            count += medias1.size();

        }

        averageValue = count / mapKeyIsDateValueIsMedias.size();


    }

    @Test
    public void recommendAlbumStrategyWithDateIntervalParamTest() {

        RecommendAlbumStrategyWithDateIntervalParam strategy = new RecommendAlbumStrategyWithDateIntervalParam(2, 1.5);

        strategy.setDateIntervalParam(2);

        Collection<List<Long>> results = strategy.chooseRecommendAlbum(times, mapKeyIsDateValueIsMedias, 1);

        Assert.assertEquals(2, results.size());

    }

    @Test
    public void recommendAlbumStrategyWithOutDateIntervalParamTest() {

        RecommendAlbumStrategy strategy = new RecommendAlbumStrategyWithoutDateIntervalParam(2, 1.5);

        Collection<List<Long>> results = strategy.chooseRecommendAlbum(times, mapKeyIsDateValueIsMedias, 3);

        Assert.assertEquals(1, results.size());

    }


    private void fillTimesAndMap(Collection<Media> allLocalMedias, SimpleDateFormat df, List<Long> times, LongSparseArray<List<Media>> mapKeyIsDateValueIsMedias) {
        List<Media> mediaList;

        for (Media media : allLocalMedias) {

            String time = media.getTime();

            long timeStamp = 0;

            try {
                timeStamp = df.parse(time).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (timeStamp == 0)
                continue;

            if (mapKeyIsDateValueIsMedias.indexOfKey(timeStamp) >= 0) {
                mediaList = mapKeyIsDateValueIsMedias.get(timeStamp);
            } else {
                times.add(timeStamp);
                mediaList = new ArrayList<>();
                mapKeyIsDateValueIsMedias.put(timeStamp, mediaList);
            }

            mediaList.add(media);

        }
    }


}
