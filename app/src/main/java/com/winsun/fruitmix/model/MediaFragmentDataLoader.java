package com.winsun.fruitmix.model;

import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;

import com.winsun.fruitmix.mediaModule.model.Media;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/19.
 */

public class MediaFragmentDataLoader {

    public static final String TAG = MediaFragmentDataLoader.class.getSimpleName();

    private List<String> mPhotoDateGroups;

    private Map<String, List<Media>> mMapKeyIsDateValueIsPhotoList;

    private SparseArray<String> mMapKeyIsPhotoPositionValueIsPhotoDate;
    private SparseArray<Media> mMapKeyIsPhotoPositionValueIsPhoto;

    private List<Media> medias;

    private int mAdapterItemTotalCount = 0;

    public MediaFragmentDataLoader() {

        mPhotoDateGroups = new ArrayList<>();
        mMapKeyIsDateValueIsPhotoList = new ArrayMap<>();

        mMapKeyIsPhotoPositionValueIsPhotoDate = new SparseArray<>();
        mMapKeyIsPhotoPositionValueIsPhoto = new SparseArray<>();

        medias = new ArrayList<>();
    }

    public List<String> getPhotoDateGroups() {
        return mPhotoDateGroups;
    }

    public Map<String, List<Media>> getMapKeyIsDateValueIsPhotoList() {
        return mMapKeyIsDateValueIsPhotoList;
    }

    public SparseArray<String> getMapKeyIsPhotoPositionValueIsPhotoDate() {
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

    public void reloadData(Collection<Media> medias) {

        String date;
        List<Media> mediaList;

        mPhotoDateGroups.clear();
        mMapKeyIsDateValueIsPhotoList.clear();

        mMapKeyIsPhotoPositionValueIsPhotoDate.clear();
        mMapKeyIsPhotoPositionValueIsPhoto.clear();

        Log.d(TAG, "reloadData: before load local key is date value is photo list :" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

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

    private void calcPhotoPositionNumber() {

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
