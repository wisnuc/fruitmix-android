package com.winsun.fruitmix.refactor.contract;

import android.util.SparseArray;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface MediaFragmentContract {

    interface MediaFragmentView extends BaseView {

        void setFABVisibility(int visibility);

        void setCreateAlbumBtnVisibility(int visibility);

        void setCreateMediaShareBtnVisibility(int visibility);

        void collapseFab();

        void expandFab();

        void showMedias(SparseArray<String> mapKeyIsPhotoPositionValueIsPhotoDate, SparseArray<Media> mapKeyIsPhotoPositionValueIsPhoto);

        void setNoContentImageView(int resID);
    }

    interface MediaFragmentPresenter extends BasePresenter<MediaFragmentView> {

        void enterChooseMode();

        void quitChooseMode();

        void createMediaShare();

        void createAlbum();
    }


}
