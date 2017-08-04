package com.winsun.fruitmix.media;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mock.MockApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/19.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = MockApplication.class)
public class FilterLocalStationMediaStrategyTest {

    private FilterLocalStationMediaStrategy filterLocalStationMediaStrategy;

    @Test
    public void testFilter() {

        filterLocalStationMediaStrategy = FilterLocalStationMediaStrategy.getInstance();

        String firstMediaUUID = "firstTestMediaUUID";
        String secondMediaUUId = "senondTestMediaUUID";

        Media firstMedia = new Media();
        firstMedia.setUuid(firstMediaUUID);

        Media secondMedia = new Media();
        secondMedia.setUuid(secondMediaUUId);

        List<Media> firstMedias = new ArrayList<>();
        firstMedias.add(firstMedia);
        firstMedias.add(secondMedia);

        Collection<Media> medias = filterLocalStationMediaStrategy.filter(firstMedias, Collections.singleton(firstMedia));

        assertEquals(2, medias.size());


    }


}
