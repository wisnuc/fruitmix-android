package com.winsun.fruitmix.refactor.contract;

import android.content.Context;
import android.support.v4.app.SharedElementCallback;
import android.view.View;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;
import java.util.Map;

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

        void setReturnResizeOnClickListener(View.OnClickListener listener);

        View findViewWithTag(String tag);

        String getString(int resID);

        void setEnterSharedElementCallback(SharedElementCallback sharedElementCallback);

        void postponeEnterTransition();

        String getImageUrl(boolean isThumb, Media media);

    }

    interface OriginalMediaPresenter extends BasePresenter<OriginalMediaView> {

        void onPageChanged(int position);

        void initView();
    }

}
