package com.winsun.fruitmix.mediaModule.model;

import android.util.Log;
import android.util.SparseArray;

import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2016/12/19.
 */

public class NewPhotoListDataLoader {

    public static final String TAG = NewPhotoListDataLoader.class.getSimpleName();

    private List<String> mPhotoDateGroups;

    private HashMap<String, List<Media>> mMapKeyIsDateValueIsPhotoList;

    private Map<Integer, String> mMapKeyIsPhotoPositionValueIsPhotoDate;

    private SparseArray<Media> mMapKeyIsPhotoPositionValueIsPhoto;

    private List<Media> medias;

    private int mAdapterItemTotalCount = 0;

    private boolean needRefreshData = true;

    private static NewPhotoListDataLoader instance;

    private boolean isOperate = false;

    private NewPhotoListDataLoader() {

        mPhotoDateGroups = new ArrayList<>();
        mMapKeyIsDateValueIsPhotoList = new HashMap<>();

        mMapKeyIsPhotoPositionValueIsPhotoDate = new HashMap<>();
        mMapKeyIsPhotoPositionValueIsPhoto = new SparseArray<>();

        medias = new ArrayList<>();
    }

    public static NewPhotoListDataLoader getInstance() {
        if (instance == null)
            instance = new NewPhotoListDataLoader();

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public void setNeedRefreshData(boolean needRefreshData) {
        this.needRefreshData = needRefreshData;
    }

    public List<String> getPhotoDateGroups() {
        return Collections.unmodifiableList(mPhotoDateGroups);
    }

    public Map<String, List<Media>> getMapKeyIsDateValueIsPhotoList() {

        return Collections.unmodifiableMap(mMapKeyIsDateValueIsPhotoList);
    }

    public Map<Integer, String> getMapKeyIsPhotoPositionValueIsPhotoDate() {

        return Collections.unmodifiableMap(mMapKeyIsPhotoPositionValueIsPhotoDate);

    }

    public SparseArray<Media> getMapKeyIsPhotoPositionValueIsPhoto() {

        return mMapKeyIsPhotoPositionValueIsPhoto.clone();
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

        if (isOperate)
            return;

        isOperate = true;

        if (!needRefreshData) {
            listener.onDataLoadFinished();

            isOperate = false;
            return;
        }

        Future<Void> future = ThreadManagerImpl.getInstance().runOnCacheThread(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                reloadData(medias);

                return null;
            }
        });

        try {

            future.get();

            needRefreshData = false;

            listener.onDataLoadFinished();

            isOperate = false;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    private void reloadData(List<Media> medias) {

        String date;
        List<Media> mediaList;

        mPhotoDateGroups.clear();
        mMapKeyIsDateValueIsPhotoList.clear();

        mMapKeyIsPhotoPositionValueIsPhotoDate.clear();
        mMapKeyIsPhotoPositionValueIsPhoto.clear();

        Log.i(TAG, "reloadData: before load list :" + Util.getCurrentFormatTime());

        for (Media media : medias) {

            if (media.getFormattedTime().length() > 10) {
                date = media.getFormattedTime().substring(0, 10);
            } else
                date = media.getFormattedTime();

            if (mMapKeyIsDateValueIsPhotoList.containsKey(date)) {
                mediaList = mMapKeyIsDateValueIsPhotoList.get(date);
            } else {
                mPhotoDateGroups.add(date);
                mediaList = new ArrayList<>();
                mMapKeyIsDateValueIsPhotoList.put(date, mediaList);
            }

            media.setDateWithoutHourMinSec(date);
            media.setSelected(false);

            mediaList.add(media);

        }

        Log.i(TAG, "reloadData: after list :" + Util.getCurrentFormatTime());

        Collections.sort(mPhotoDateGroups, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return -lhs.compareTo(rhs);
            }
        });

        Log.i(TAG, "reloadData: after sort photo date groups :" + Util.getCurrentFormatTime());

        calcPhotoPositionNumber();

        Log.i(TAG, "reloadData: after calc photo position number" + Util.getCurrentFormatTime());
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
                    return -lhs.getFormattedTime().compareTo(rhs.getFormattedTime());
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
