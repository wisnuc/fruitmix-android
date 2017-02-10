package com.winsun.fruitmix.refactor.contract;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.View;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface MediaFragmentContract {

    interface MediaFragmentView extends BaseView {

        void setFABVisibility(int visibility);

        void collapseFab();

        void expandFab();

        void showMedias(SparseArray<String> mapKeyIsPhotoPositionValueIsPhotoDate, SparseArray<Media> mapKeyIsPhotoPositionValueIsPhoto,Map<String, List<Media>> mMapKeyIsDateValueIsPhotoList,List<Media> medias);

        View getView();

        void notifyDataSetChangedUseAnim();

        void smoothScrollToPosition(int position);

        void startPostponedEnterTransition();

        View findViewByMedia(Media media);

        void startCreateAlbumActivity();

        void showSelectNothingToast();

        void onDestroyView();
    }

    interface MediaFragmentPresenter extends BasePresenter<MediaFragmentView> {

        int getSelectCount();

        void calcSelectedPhoto();

        void enterChooseMode();

        void quitChooseMode();

        void albumBtnOnClick();

        void shareBtnOnClick();

        void fabOnClick();

        boolean isSelectState();

        void onActivityReenter(int resultCode, Intent data);

        void onMapSharedElements(List<String> names, Map<String, View> sharedElements);

        void onResume();

        void imageOnLongClick(Media media, Context context);

    }


}
