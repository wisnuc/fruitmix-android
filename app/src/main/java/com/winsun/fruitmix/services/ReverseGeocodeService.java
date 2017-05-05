package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.regeocode.AMapReverseGeocode;
import com.winsun.fruitmix.regeocode.ReverseGeocode;
import com.winsun.fruitmix.util.LocalCache;

import java.util.ArrayList;
import java.util.List;


public class ReverseGeocodeService extends IntentService {

    private static final String ACTION_REVERSE_GEOCODE = "com.winsun.fruitmix.services.action.reverse.geocode";

    public ReverseGeocodeService() {
        super("ReverseGeocodeService");
    }

    public static void startActionReverseGeocode(Context context) {
        Intent intent = new Intent(context, ReverseGeocodeService.class);
        intent.setAction(ACTION_REVERSE_GEOCODE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REVERSE_GEOCODE.equals(action)) {
                handleActionReverseGeocode();
            }
        }
    }

    private void handleActionReverseGeocode() {

        List<Media> medias = new ArrayList<>(20);

        for (Media media : LocalCache.LocalMediaMapKeyIsOriginalPhotoPath.values()) {

            if (medias.size() >= 20) {
                break;
            } else if (!media.getLongitude().isEmpty()) {
                medias.add(media);
            }

        }

        if (medias.size() == 0)
            return;

        ReverseGeocode geocode = new AMapReverseGeocode();
        boolean result = geocode.reverseGeocodeLongitudeLatitudeInMedias(medias);

        if (result) {
            startActionReverseGeocode(this);
        }

    }

}
