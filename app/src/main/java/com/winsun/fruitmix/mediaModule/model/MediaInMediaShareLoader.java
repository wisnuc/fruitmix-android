package com.winsun.fruitmix.mediaModule.model;

import android.os.AsyncTask;

import com.winsun.fruitmix.util.LocalCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/22.
 */

public enum MediaInMediaShareLoader {

    INSTANCE;

    private LinkedList<Media> medias;

    private boolean mClearMedias = true;
    private boolean mReloadMedias = true;

    MediaInMediaShareLoader() {
        medias = new LinkedList<>();
    }

    public List<Media> getMedias() {
        return new ArrayList<>(medias);
    }

    public interface OnMediaInMediaShareLoadListener {
        void onMediaInMediaShareLoaded();
    }

    public void startLoad(final List<String> mediaKeyList, final OnMediaInMediaShareLoadListener loadListener, boolean clearMedias, boolean reloadMedias) {

        mClearMedias = clearMedias;
        mReloadMedias = reloadMedias;

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                if (mediaKeyList == null)
                    return null;

                if (!mReloadMedias)
                    return null;

                fillPicList(mediaKeyList);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mReloadMedias = true;
                mClearMedias = true;

                if (loadListener != null)
                    loadListener.onMediaInMediaShareLoaded();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void fillPicList(List<String> imageKeys) {

        Media picItemRaw;
        Media picItem;

        if (mClearMedias)
            medias.clear();

        Map<String,Media> remoteMediaMaps = new HashMap<>(LocalCache.RemoteMediaMapKeyIsUUID);

        for (String aStArr : imageKeys) {

            picItemRaw = LocalCache.findMediaInLocalMediaMap(aStArr);

            if (picItemRaw == null) {

                picItemRaw = remoteMediaMaps.get(aStArr);

                if (picItemRaw == null) {
                    picItem = new Media();
                } else {

                    picItem = picItemRaw;
                    picItem.setLocal(false);
                }

            } else {

                picItem = picItemRaw;

                picItem.setLocal(true);
            }

            picItem.setSelected(false);

            if (mClearMedias)
                medias.add(picItem);
            else
                medias.addFirst(picItem);
        }

//        Collections.sort(medias, new Comparator<Media>() {
//            @Override
//            public int compare(Media lhs, Media rhs) {
//                return -lhs.getTime().compareTo(rhs.getTime());
//            }
//        });
    }
}
