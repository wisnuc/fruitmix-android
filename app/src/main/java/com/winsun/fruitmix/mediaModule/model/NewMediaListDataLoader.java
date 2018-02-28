package com.winsun.fruitmix.mediaModule.model;

import android.util.Log;
import android.util.SparseArray;

import com.winsun.fruitmix.mediaModule.viewmodel.MediaViewModel;
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

public class NewMediaListDataLoader implements MediaListConverter {

    public static final String TAG = NewMediaListDataLoader.class.getSimpleName();

    private List<String> mPhotoDateGroups;

    private HashMap<String, List<MediaViewModel>> mMapKeyIsDateList;

    private Map<Integer, String> mMapKeyIsPhotoPositionValueIsPhotoDate;

    private SparseArray<MediaViewModel> mMapKeyIsPhotoPosition;

    private List<MediaViewModel> mMediaViewModels;

    private int mAdapterItemTotalCount = 0;

    private boolean needRefreshData = true;

    private static NewMediaListDataLoader instance;

    private boolean isOperate = false;

    private NewMediaListDataLoader() {

        mPhotoDateGroups = new ArrayList<>();
        mMapKeyIsDateList = new HashMap<>();

        mMapKeyIsPhotoPositionValueIsPhotoDate = new HashMap<>();
        mMapKeyIsPhotoPosition = new SparseArray<>();

        mMediaViewModels = new ArrayList<>();
    }

    public static NewMediaListDataLoader getInstance() {
        if (instance == null)
            instance = new NewMediaListDataLoader();

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public void setNeedRefreshData(boolean needRefreshData) {
        this.needRefreshData = needRefreshData;
    }

    public Map<String, List<MediaViewModel>> getMapKeyIsDateList() {

        return Collections.unmodifiableMap(mMapKeyIsDateList);
    }

    public Map<Integer, String> getMapKeyIsPhotoPositionValueIsPhotoDate() {

        return Collections.unmodifiableMap(mMapKeyIsPhotoPositionValueIsPhotoDate);

    }

    public SparseArray<MediaViewModel> getMapKeyIsPhotoPosition() {

        return mMapKeyIsPhotoPosition.clone();
    }

    public List<MediaViewModel> getMediaViewModels() {
        return mMediaViewModels;
    }

    public int getAdapterItemTotalCount() {
        return mAdapterItemTotalCount;
    }

    @Override
    public void convertData(OnPhotoListDataListener listener, List<Media> medias) {

        retrieveData(listener,medias);
    }

    private void retrieveData(final OnPhotoListDataListener listener, final List<Media> medias) {

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
        List<MediaViewModel> mediaViewModels;

        mPhotoDateGroups.clear();
        mMapKeyIsDateList.clear();

        mMapKeyIsPhotoPositionValueIsPhotoDate.clear();
        mMapKeyIsPhotoPosition.clear();

        Log.i(TAG, "reloadData: before load list :" + Util.getCurrentFormatTime());

        for (Media media : medias) {

            if (media.getFormattedTime().length() > 10) {
                date = media.getFormattedTime().substring(0, 10);
            } else
                date = media.getFormattedTime();

            if (mMapKeyIsDateList.containsKey(date)) {
                mediaViewModels = mMapKeyIsDateList.get(date);
            } else {
                mPhotoDateGroups.add(date);
                mediaViewModels = new ArrayList<>();
                mMapKeyIsDateList.put(date, mediaViewModels);
            }

            media.setDateWithoutHourMinSec(date);

            MediaViewModel mediaViewModel = new MediaViewModel(media);
            mediaViewModel.setSelected(false);

            mediaViewModels.add(mediaViewModel);

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

        mMediaViewModels.clear();

        for (String title : mPhotoDateGroups) {
            mMapKeyIsPhotoPositionValueIsPhotoDate.put(titlePosition, title);

            mAdapterItemTotalCount++;

            List<MediaViewModel> mediaViewModels = mMapKeyIsDateList.get(title);
            photoListSize = mediaViewModels.size();
            mAdapterItemTotalCount += photoListSize;

            Collections.sort(mediaViewModels, new Comparator<MediaViewModel>() {
                @Override
                public int compare(MediaViewModel lhs, MediaViewModel rhs) {
                    return -lhs.getMedia().getFormattedTime().compareTo(rhs.getMedia().getFormattedTime());
                }
            });

            for (int i = 0; i < photoListSize; i++) {
                mMapKeyIsPhotoPosition.put(titlePosition + 1 + i, mediaViewModels.get(i));
            }

            mMediaViewModels.addAll(mediaViewModels);

            titlePosition = mAdapterItemTotalCount;
        }

    }

}
