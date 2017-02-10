package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface AlbumFragmentContract {

    interface AlbumFragmentView extends BaseView {

        void setAlbumBalloonVisibility(int visibility);

        void setNoContentImageView(int resID);

        void showAlbums(List<MediaShare> mediaShares);

    }

    interface AlbumFragmentPresenter extends BasePresenter<AlbumFragmentView> {

        void createAlbum();

        void modifyMediaShare(MediaShare mediaShare);

        void deleteMediaShare(MediaShare mediaShare);

        void refreshData();

        void onPageSelected();

    }

}
