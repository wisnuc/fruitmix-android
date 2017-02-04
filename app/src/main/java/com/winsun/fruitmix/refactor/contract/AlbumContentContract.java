package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface AlbumContentContract {

    interface AlbumContentView extends BaseView {

        void setTitle(String title);

        void showAlbumContent(MediaShare mediaShare);

        void showNoOperationPermission();
    }

    interface AlbumContentPresenter extends BasePresenter<AlbumContentView> {

        void toggleAlbumPublicState();

        void deleteCurrentAlbum();

        boolean pretreatItemOnOptionsItemSelected();

    }
}
