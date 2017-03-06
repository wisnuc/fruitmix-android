package com.winsun.fruitmix.contract;

import com.winsun.fruitmix.common.BasePresenter;
import com.winsun.fruitmix.common.BaseView;

/**
 * Created by Administrator on 2017/2/16.
 */

public interface ModifyAlbumContract {

    interface ModifyAlbumView extends BaseView {

        void setAlbumTitle(String albumTitle);

        void setDescription(String description);

        void setIsPublic(boolean isPublic);

        void setIsMaintained(boolean isMaintained);

        void setResult(int result);

        void finishActivity();
    }

    interface ModifyAlbumPresenter extends BasePresenter<ModifyAlbumView> {

        void modifyAlbum(String title, String desc, boolean isPublic, boolean isMaintained);

        void initView();
    }

}
