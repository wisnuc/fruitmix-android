package com.winsun.fruitmix.mediaModule.model;

import android.util.SparseArray;

import com.winsun.fruitmix.mediaModule.viewmodel.MediaViewModel;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/25.
 */

public interface MediaListConverter {

    void setNeedRefreshData(boolean needRefreshData);

    Map<String, List<MediaViewModel>> getMapKeyIsDateList();

    Map<Integer, String> getMapKeyIsPhotoPositionValueIsPhotoDate();

    SparseArray<MediaViewModel> getMapKeyIsPhotoPosition();

    List<MediaViewModel> getMediaViewModels();

    int getAdapterItemTotalCount();

    interface OnPhotoListDataListener {
        void onDataLoadFinished();
    }

    void convertData(final OnPhotoListDataListener listener, final List<Media> medias);

    void calcPhotoPositionNumber();

}
