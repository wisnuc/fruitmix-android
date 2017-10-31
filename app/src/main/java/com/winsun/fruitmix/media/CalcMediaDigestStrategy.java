package com.winsun.fruitmix.media;

import android.media.ThumbnailUtils;
import android.util.Log;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.Util;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/7/19.
 */

public class CalcMediaDigestStrategy {

    public static final String TAG = CalcMediaDigestStrategy.class.getSimpleName();

    public interface CalcMediaDigestCallback {

        void handleFinished();

        void handleNothing();

    }

    private static CalcMediaDigestStrategy ourInstance;

    private CalcMediaDigestCallback calcMediaDigestCallback;

    private volatile boolean finishCalcMediaDigest = false;

    public static CalcMediaDigestStrategy getInstance() {

        if (ourInstance == null)
            ourInstance = new CalcMediaDigestStrategy();
        return ourInstance;
    }

    private CalcMediaDigestStrategy() {
    }

    public boolean isFinishCalcMediaDigest() {
        return finishCalcMediaDigest;
    }

    public void setCalcMediaDigestCallback(CalcMediaDigestCallback callback) {
        calcMediaDigestCallback = callback;
    }

    public <T extends Media> Collection<T> handleMedia(Collection<T> medias) {

        List<T> newMediaList = new ArrayList<>();

        Log.d(TAG, "start calc media digest" + Util.getCurrentFormatTime());

        finishCalcMediaDigest = false;

        for (T media : medias) {
            if (media.getUuid().isEmpty()) {
                String uuid = Util.calcSHA256OfFile(media.getOriginalPhotoPath());
                media.setUuid(uuid);

                newMediaList.add(media);

                Log.d(TAG, "media original photo path: " + media.getOriginalPhotoPath() + " uuid: " + media.getUuid());
            }
        }

        finishCalcMediaDigest = true;

        return newMediaList;

    }

    public void notifyCalcFinished(int newMediaListSize) {
        if (calcMediaDigestCallback != null) {
            if (newMediaListSize > 0) {

                Log.d(TAG, "handleMedia: finish calc");

                calcMediaDigestCallback.handleFinished();
            } else {

                Log.d(TAG, "handleMedia: nothing need calc");

                calcMediaDigestCallback.handleNothing();
            }
        }
    }


}
