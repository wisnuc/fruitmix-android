package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LongSparseArray;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.strategy.RecommendAlbumStrategy;
import com.winsun.fruitmix.strategy.RecommendAlbumStrategyWithoutDateIntervalParam;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class CreateRecommendAlbumService extends IntentService {

    public static final String TAG = CreateRecommendAlbumService.class.getSimpleName();

    private static final String ACTION_GenerateRecommendAlbum = "com.winsun.fruitmix.services.action.generate.recommend.album";

    public CreateRecommendAlbumService() {
        super("GenerateRecommendAlbumService");
    }

    public static void startActionCreateRecommendAlbum(Context context) {
        Intent intent = new Intent(context, CreateRecommendAlbumService.class);
        intent.setAction(ACTION_GenerateRecommendAlbum);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GenerateRecommendAlbum.equals(action)) {
                handleActionCreateRecommendAlbumService();
            }
        }
    }


    private void handleActionCreateRecommendAlbumService() {

        LocalCache.RecommendMediaShares.addAll(createRecommendAlbums(LocalCache.LocalMediaMapKeyIsOriginalPhotoPath.values()));

        Log.i(TAG, "handleActionCreateRecommendAlbumService: LocalCache.RecommendMediaShare size: " + LocalCache.RecommendMediaShares.size());

        EventBus.getDefault().postSticky(new OperationEvent(Util.RECOMMEND_ALBUM_CREATED, new OperationSuccess(R.string.operate)));

    }

    private List<MediaShare> createRecommendAlbums(Collection<Media> allLocalMedias) {

        List<MediaShare> recommendAlbums = new ArrayList<>();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Date date = new Date();

        List<Long> times = new ArrayList<>();

        LongSparseArray<List<Media>> mapKeyIsDateValueIsMedias = new LongSparseArray<>();

        fillTimesAndMap(allLocalMedias, df, times, mapKeyIsDateValueIsMedias);

        Collections.sort(times, new Comparator<Long>() {
            @Override
            public int compare(Long lhs, Long rhs) {
                return (int) (lhs - rhs);
            }
        });

        int averageValue;
        int count = 0;

        for (int i = 0; i < mapKeyIsDateValueIsMedias.size(); i++) {
            List<Media> medias1 = mapKeyIsDateValueIsMedias.get(mapKeyIsDateValueIsMedias.keyAt(i));

            count += medias1.size();

        }

        averageValue = count / mapKeyIsDateValueIsMedias.size();

        Log.i(TAG, "handleActionCreateRecommendAlbumService: averageValue: " + averageValue);

        RecommendAlbumStrategy strategy = new RecommendAlbumStrategyWithoutDateIntervalParam(2, 1.5);

        Collection<List<Long>> results = strategy.chooseRecommendAlbum(times, mapKeyIsDateValueIsMedias, averageValue);

        strategy.setAverageValueWeightedValue(2);
        strategy.setDays(1);

        results.addAll(strategy.chooseRecommendAlbum(times, mapKeyIsDateValueIsMedias, averageValue));

        for (List<Long> result : results) {
            MediaShare recommendAlbum = createRecommendAlbum(df, date, mapKeyIsDateValueIsMedias, result);

            recommendAlbums.add(recommendAlbum);
        }

        return recommendAlbums;

    }

    @NonNull
    private MediaShare createRecommendAlbum(SimpleDateFormat df, Date date, LongSparseArray<List<Media>> mapKeyIsDateValueIsMedias, List<Long> greatThanAverageValue) {

        List<String> mediaUUIDs = new ArrayList<>();

        for (Long time1 : greatThanAverageValue) {

            List<Media> medias = mapKeyIsDateValueIsMedias.get(time1);

            int currentMediaUUIDsSize = mediaUUIDs.size();
            int mediaSize = medias.size();

            int length;

            if (currentMediaUUIDsSize == 1000) {
                break;
            }

            if (currentMediaUUIDsSize + mediaSize >= 1000) {
                length = 1000 - currentMediaUUIDsSize;
            } else {
                length = mediaSize;
            }

            for (int i = 0; i < length; i++) {

                Media media = medias.get(i);

                mediaUUIDs.add(media.getUuid());

            }
        }

        date.setTime(greatThanAverageValue.get(0));
        String preTime = df.format(date).substring(0, 10);

        String recommendPhotoTime;

        if (greatThanAverageValue.size() > 1) {

            date.setTime(greatThanAverageValue.get(greatThanAverageValue.size() - 1));
            String lastTime = df.format(date).substring(0, 10);

            recommendPhotoTime = preTime + " - " + lastTime;
        } else {

            recommendPhotoTime = preTime;
        }

        return Util.createMediaShare(true, true, false, "", "", mediaUUIDs, true, recommendPhotoTime);
    }

    private void fillTimesAndMap(Collection<Media> allLocalMedias, SimpleDateFormat df, List<Long> times, LongSparseArray<List<Media>> mapKeyIsDateValueIsMedias) {
        List<Media> mediaList;

        for (Media media : allLocalMedias) {

            String time = media.getTime().substring(0, 10);

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
