package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface CreateAlbumContract {

    interface CreateAlbumView extends BaseView {

        void setLayoutTitle(String layoutTitle);

        void setAlbumTitleHint(String hint);


    }

    interface CreateAlbumPresenter extends BasePresenter<CreateAlbumView> {

        void createAlbum(String title, String desc, boolean isPublic, boolean isMaintained);

    }

}
