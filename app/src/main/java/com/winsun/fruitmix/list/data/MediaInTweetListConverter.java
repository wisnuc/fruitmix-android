package com.winsun.fruitmix.list.data;

import android.util.SparseArray;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaListConverter;
import com.winsun.fruitmix.mediaModule.viewmodel.MediaViewModel;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2018/2/27.
 */

public class MediaInTweetListConverter implements MediaListConverter {

    private Map<String, List<MediaViewModel>> mMapKeyIsDateList;

    private Map<Integer, String> mMapKeyIsPhotoPositionValueIsPhotoDate;
    private SparseArray<MediaViewModel> mMapKeyIsPhotoPosition;
    private List<MediaViewModel> mMediaViewModels;

    private int mAdapterItemTotalCount;

    private boolean isOperate = false;

    public MediaInTweetListConverter() {

        mMapKeyIsDateList = new HashMap<>();

        mMapKeyIsPhotoPositionValueIsPhotoDate = new HashMap<>();
        mMapKeyIsPhotoPosition = new SparseArray<>();

        mMediaViewModels = new ArrayList<>();
    }

    @Override
    public void setNeedRefreshData(boolean needRefreshData) {

    }

    @Override
    public Map<String, List<MediaViewModel>> getMapKeyIsDateList() {
        return Collections.unmodifiableMap(mMapKeyIsDateList);
    }

    @Override
    public Map<Integer, String> getMapKeyIsPhotoPositionValueIsPhotoDate() {
        return Collections.unmodifiableMap(mMapKeyIsPhotoPositionValueIsPhotoDate);
    }

    @Override
    public SparseArray<MediaViewModel> getMapKeyIsPhotoPosition() {
        return mMapKeyIsPhotoPosition.clone();
    }

    @Override
    public List<MediaViewModel> getMediaViewModels() {
        return Collections.unmodifiableList(mMediaViewModels);
    }

    @Override
    public int getAdapterItemTotalCount() {
        return mAdapterItemTotalCount;
    }

    @Override
    public void convertData(OnPhotoListDataListener listener, final List<Media> medias) {

        if (isOperate)
            return;

        isOperate = true;

        Future<Void> future = ThreadManagerImpl.getInstance().runOnCacheThread(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                convertData(medias);

                return null;
            }
        });

        try {

            future.get();

            listener.onDataLoadFinished();

            isOperate = false;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    private void convertData(List<Media> medias) {

        int size = medias.size();

        for (int i = 0; i < size; i++) {

            Media media = medias.get(i);

            MediaViewModel mediaViewModel = new MediaViewModel(media);

            mMapKeyIsPhotoPosition.put(i, mediaViewModel);

            mMediaViewModels.add(mediaViewModel);

        }

        mAdapterItemTotalCount = size;

    }


    @Override
    public void calcPhotoPositionNumber() {


    }


}
