package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface MoreMediaContract {

    interface MoreMediaView extends BaseView {

        void showMedias(List<Media> medias);

    }

    interface MoreMediaPresenter extends BasePresenter<MoreMediaView> {


    }

}
