package com.winsun.fruitmix.media;

import android.util.Log;

import com.winsun.fruitmix.mediaModule.model.Media;

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

    public static CalcMediaDigestStrategy getInstance() {

        if (ourInstance == null)
            ourInstance = new CalcMediaDigestStrategy();
        return ourInstance;
    }

    private CalcMediaDigestStrategy() {
    }

    public void setCalcMediaDigestCallback(CalcMediaDigestCallback callback) {
        calcMediaDigestCallback = callback;
    }

    public Collection<Media> handleMedia(Collection<Media> medias) {

        List<Media> newMediaList = new ArrayList<>();

        Log.d(TAG, "start calc media digest" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        for (Media media : medias) {
            if (media.getUuid().isEmpty()) {
                String uuid = calcSHA256OfFile(media.getOriginalPhotoPath());
                media.setUuid(uuid);

                newMediaList.add(media);

                Log.d(TAG, "media original photo path: " + media.getOriginalPhotoPath() + " uuid: " + media.getUuid());
            }
        }

        if (calcMediaDigestCallback != null) {
            if (newMediaList.size() > 0) {
                calcMediaDigestCallback.handleFinished();
            } else {
                calcMediaDigestCallback.handleNothing();
            }
        }

        return newMediaList;

    }

    private String calcSHA256OfFile(String fname) {
        MessageDigest md;
        FileInputStream fin;
        byte[] buffer;
        byte[] digest;
        String digits = "0123456789abcdef";
        int len, i;
        String st;

        try {
            buffer = new byte[15000];
            md = MessageDigest.getInstance("SHA-256");
            fin = new FileInputStream(fname);
            len = 0;
            while ((len = fin.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fin.close();
            digest = md.digest();
            st = "";
            for (i = 0; i < digest.length; i++) {
                st += digits.charAt((digest[i] >> 4) & 0xf);
                st += digits.charAt(digest[i] & 0xf);
            }
            return st;
        } catch (Exception e) {
            return "";
        }
    }


}
