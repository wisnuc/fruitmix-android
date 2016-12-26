package com.winsun.fruitmix.mediaModule.model;

import android.os.AsyncTask;
import android.util.Log;

import com.winsun.fruitmix.util.LocalCache;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/19.
 */

public enum NewPhotoListDataLoader {

    INSTANCE;

    public static final String TAG = NewPhotoListDataLoader.class.getSimpleName();

    private List<String> mPhotoDateGroups;

    private Map<String, List<Media>> mMapKeyIsDateValueIsPhotoList;

    private Map<Integer, String> mMapKeyIsPhotoPositionValueIsPhotoDate;
    private Map<Integer, Media> mMapKeyIsPhotoPositionValueIsPhoto;

    private int mAdapterItemTotalCount = 0;

    private boolean needRefreshData = true;

    NewPhotoListDataLoader() {

        mPhotoDateGroups = new ArrayList<>();
        mMapKeyIsDateValueIsPhotoList = new HashMap<>();

        mMapKeyIsPhotoPositionValueIsPhotoDate = new HashMap<>();
        mMapKeyIsPhotoPositionValueIsPhoto = new HashMap<>();

    }

    public void setNeedRefreshData(boolean needRefreshData) {
        this.needRefreshData = needRefreshData;
    }

    public List<String> getmPhotoDateGroups() {
        return mPhotoDateGroups;
    }

    public Map<String, List<Media>> getmMapKeyIsDateValueIsPhotoList() {
        return mMapKeyIsDateValueIsPhotoList;
    }

    public Map<Integer, String> getmMapKeyIsPhotoPositionValueIsPhotoDate() {
        return mMapKeyIsPhotoPositionValueIsPhotoDate;
    }

    public Map<Integer, Media> getmMapKeyIsPhotoPositionValueIsPhoto() {
        return mMapKeyIsPhotoPositionValueIsPhoto;
    }

    public int getmAdapterItemTotalCount() {
        return mAdapterItemTotalCount;
    }

    public interface OnPhotoListDataListener {
        void onDataLoadFinished();
    }

    public void retrieveData(final OnPhotoListDataListener listener) {

        if (!needRefreshData) {
            listener.onDataLoadFinished();

            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                reloadData();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                needRefreshData = false;

                listener.onDataLoadFinished();
            }
        }.execute();

    }

    private void reloadData() {

        String date;
        List<Media> mediaList;

        mPhotoDateGroups.clear();
        mMapKeyIsDateValueIsPhotoList.clear();

        mMapKeyIsPhotoPositionValueIsPhotoDate.clear();
        mMapKeyIsPhotoPositionValueIsPhoto.clear();

        Collection<Media> medias = LocalCache.LocalMediaMapKeyIsThumb.values();
        Map<String,Media> remoteMediaMap = new HashMap<>(LocalCache.RemoteMediaMapKeyIsUUID);

        Log.i(TAG, "reloadData: before add local media time:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        for (Media media : medias) {

            String mediaUUID = media.getUuid();
            remoteMediaMap.remove(mediaUUID);

            date = media.getTime().substring(0, 10);
            if (mMapKeyIsDateValueIsPhotoList.containsKey(date)) {
                mediaList = mMapKeyIsDateValueIsPhotoList.get(date);
            } else {
                mPhotoDateGroups.add(date);
                mediaList = new ArrayList<>();
                mMapKeyIsDateValueIsPhotoList.put(date, mediaList);
            }

            media.setLocal(true);
            media.setDate(date);
            media.setSelected(false);

            mediaList.add(media);
        }

        Log.i(TAG, "reloadData: after add local media time:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        medias = remoteMediaMap.values();

        for (Media media : medias) {

            date = media.getTime().substring(0, 10);
            if (mMapKeyIsDateValueIsPhotoList.containsKey(date)) {
                mediaList = mMapKeyIsDateValueIsPhotoList.get(date);
            } else {
                mPhotoDateGroups.add(date);
                mediaList = new ArrayList<>();
                mMapKeyIsDateValueIsPhotoList.put(date, mediaList);
            }

            media.setLocal(false);
            media.setDate(date);
            media.setSelected(false);

            mediaList.add(media);

        }

        Log.i(TAG, "reloadData: after add remote media time:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));


        Collections.sort(mPhotoDateGroups, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return -lhs.compareTo(rhs);
            }
        });

        Log.i(TAG, "reloadData: after sort photo date groups time:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        calcPhotoPositionNumber();

        Log.i(TAG, "reloadData: after calc photo position number time:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

    }

    public void calcPhotoPositionNumber() {

        int titlePosition = 0;
        int photoListSize;
        mAdapterItemTotalCount = 0;

        for (String title : mPhotoDateGroups) {
            mMapKeyIsPhotoPositionValueIsPhotoDate.put(titlePosition, title);

            mAdapterItemTotalCount++;

            List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(title);
            photoListSize = mediaList.size();
            mAdapterItemTotalCount += photoListSize;

            Collections.sort(mediaList, new Comparator<Media>() {
                @Override
                public int compare(Media lhs, Media rhs) {
                    return -lhs.getTime().compareTo(rhs.getTime());
                }
            });

            for (int i = 0; i < photoListSize; i++) {
                mMapKeyIsPhotoPositionValueIsPhoto.put(titlePosition + 1 + i, mediaList.get(i));
            }

            titlePosition = mAdapterItemTotalCount;
        }

    }
}
