package com.winsun.fruitmix.mediaModule.model;

import android.media.ExifInterface;
import android.os.AsyncTask;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;

import com.winsun.fruitmix.util.LocalCache;

import java.io.IOException;
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

    private SparseArray<Media> mMapKeyIsPhotoPositionValueIsPhoto;

    private List<Media> medias;

    private int mAdapterItemTotalCount = 0;

    private boolean needRefreshData = true;

    NewPhotoListDataLoader() {

        mPhotoDateGroups = new ArrayList<>();
        mMapKeyIsDateValueIsPhotoList = new ArrayMap<>();

        mMapKeyIsPhotoPositionValueIsPhotoDate = new HashMap<>();
        mMapKeyIsPhotoPositionValueIsPhoto = new SparseArray<>();

        medias = new ArrayList<>();
    }

    public void setNeedRefreshData(boolean needRefreshData) {
        this.needRefreshData = needRefreshData;
    }

    public List<String> getPhotoDateGroups() {
        return mPhotoDateGroups;
    }

    public Map<String, List<Media>> getMapKeyIsDateValueIsPhotoList() {
        return mMapKeyIsDateValueIsPhotoList;
    }

    public Map<Integer, String> getMapKeyIsPhotoPositionValueIsPhotoDate() {
        return mMapKeyIsPhotoPositionValueIsPhotoDate;
    }

    public SparseArray<Media> getMapKeyIsPhotoPositionValueIsPhoto() {
        return mMapKeyIsPhotoPositionValueIsPhoto;
    }

    public List<Media> getMedias() {
        return medias;
    }

    public int getAdapterItemTotalCount() {
        return mAdapterItemTotalCount;
    }

    public interface OnPhotoListDataListener {
        void onDataLoadFinished();
    }

    public void retrieveData(final OnPhotoListDataListener listener, final List<Media> medias) {

        if (!needRefreshData) {
            listener.onDataLoadFinished();
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                reloadData(medias);

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

    private void reloadData(List<Media> medias) {

        String date;
        List<Media> mediaList;

        mPhotoDateGroups.clear();
        mMapKeyIsDateValueIsPhotoList.clear();

        mMapKeyIsPhotoPositionValueIsPhotoDate.clear();
        mMapKeyIsPhotoPositionValueIsPhoto.clear();


        Log.i(TAG, "reloadData: before load list :" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        for (Media media : medias) {

            date = media.getTime().substring(0, 10);
            if (mMapKeyIsDateValueIsPhotoList.containsKey(date)) {
                mediaList = mMapKeyIsDateValueIsPhotoList.get(date);
            } else {
                mPhotoDateGroups.add(date);
                mediaList = new ArrayList<>();
                mMapKeyIsDateValueIsPhotoList.put(date, mediaList);
            }

            media.setDate(date);
            media.setSelected(false);

            mediaList.add(media);

        }

        Log.i(TAG, "reloadData: after list :" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        Collections.sort(mPhotoDateGroups, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return -lhs.compareTo(rhs);
            }
        });

        Log.i(TAG, "reloadData: after sort photo date groups :" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        calcPhotoPositionNumber();

        Log.i(TAG, "reloadData: after calc photo position number" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
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
