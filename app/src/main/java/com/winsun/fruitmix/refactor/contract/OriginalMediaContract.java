package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface OriginalMediaContract {

    interface OriginalMediaView extends BaseView {

        void setDate(String date);

        void setHeaderVisibility(int visibility);

        void setFooterVisibility(int visibility);

        void setCommentImage(int resID);

        void setReturnResizeVisibility(int visibility);

    }

    interface OriginalMediaPresenter extends BasePresenter<OriginalMediaView> {

        void onPageChanged(int position);

    }

}
