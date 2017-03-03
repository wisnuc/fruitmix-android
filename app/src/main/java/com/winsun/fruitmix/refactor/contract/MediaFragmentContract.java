package com.winsun.fruitmix.refactor.contract;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.View;

import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;
import com.winsun.fruitmix.refactor.model.MediaFragmentDataLoader;

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

        void showMedias(MediaFragmentDataLoader loader);

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

        void setSelectCountText(String selectCountText);

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

        void onCreate();

        void onResume();

        void loadMediaToView(Context context, Media media, NetworkImageView view);

        void cancelLoadMediaToView(NetworkImageView view);

        String loadImageThumbUrl(Media media);
    }


}
