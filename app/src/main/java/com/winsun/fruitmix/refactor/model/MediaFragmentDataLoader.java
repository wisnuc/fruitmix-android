package com.winsun.fruitmix.refactor.model;

import android.os.AsyncTask;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;

import com.winsun.fruitmix.mediaModule.model.Media;
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

public class MediaFragmentDataLoader {

    //TODO: just java bean,handle data in data repository

    public static final String TAG = MediaFragmentDataLoader.class.getSimpleName();

    private List<String> mPhotoDateGroups;

    private Map<String, List<Media>> mMapKeyIsDateValueIsPhotoList;

    private SparseArray<String> mMapKeyIsPhotoPositionValueIsPhotoDate;
    private SparseArray<Media> mMapKeyIsPhotoPositionValueIsPhoto;

    private List<Media> medias;

    private int mAdapterItemTotalCount = 0;

    private boolean needRefreshData = true;

    public MediaFragmentDataLoader() {

        mPhotoDateGroups = new ArrayList<>();
        mMapKeyIsDateValueIsPhotoList = new ArrayMap<>();

        mMapKeyIsPhotoPositionValueIsPhotoDate = new SparseArray<>();
        mMapKeyIsPhotoPositionValueIsPhoto = new SparseArray<>();

        medias = new ArrayList<>();
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

    public SparseArray<String> getmMapKeyIsPhotoPositionValueIsPhotoDate() {
        return mMapKeyIsPhotoPositionValueIsPhotoDate;
    }

    public SparseArray<Media> getmMapKeyIsPhotoPositionValueIsPhoto() {
        return mMapKeyIsPhotoPositionValueIsPhoto;
    }

    public List<Media> getMedias() {
        return medias;
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
        Map<String, Media> remoteMediaMap = new HashMap<>(LocalCache.RemoteMediaMapKeyIsUUID);

        Log.d(TAG, "reloadData: before load local key is date value is photo list :" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

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

        Log.d(TAG, "reloadData: after load local key is date value is photo list" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        medias = remoteMediaMap.values();

        Log.d(TAG, "reloadData: remote media length:" + medias.size());

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

        Log.d(TAG, "reloadData: after load remote key is date value is photo list :" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        Collections.sort(mPhotoDateGroups, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return -lhs.compareTo(rhs);
            }
        });

        Log.d(TAG, "reloadData: after sort photo date groups :" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        calcPhotoPositionNumber();

        Log.d(TAG, "reloadData: after calc photo position number" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
    }

    public void calcPhotoPositionNumber() {

        int titlePosition = 0;
        int photoListSize;
        mAdapterItemTotalCount = 0;

        medias.clear();

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

            medias.addAll(mediaList);

            titlePosition = mAdapterItemTotalCount;
        }

    }

}