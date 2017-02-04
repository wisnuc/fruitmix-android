package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface MediaShareFragmentContract {

    interface MediaShareFragmentView extends BaseView {

        void setNoContentImageView(int resID);

        void showMediaShares(List<MediaShare> mediaShares);

    }

    interface MediaShareFragmentPresenter extends BasePresenter<MediaShareFragmentView> {

        List<Media> getMedias(List<String> imageKeys);

    }

}
