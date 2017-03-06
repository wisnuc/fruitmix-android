package com.winsun.fruitmix.contract;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.common.BasePresenter;
import com.winsun.fruitmix.common.BaseView;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface MediaShareFragmentContract {

    interface MediaShareFragmentView extends BaseView {

        void showMediaShares(List<MediaShare> mediaShares);

        View findViewWithMedia(Media media);

        void startPostponedEnterTransition();

        void onDestroyView();
    }

    interface MediaShareFragmentPresenter extends BasePresenter<MediaShareFragmentView> {

        List<Media> loadMedias(List<String> imageKeys);

        void onActivityReenter(int resultCode, Intent data);

        void onMapSharedElements(List<String> names, Map<String, View> sharedElements);

        void refreshData();

        User loadUser(String userUUID);

        Media loadMedia(String mediaKey);

        void loadThumbMediaToView(Context context, Media media, NetworkImageView view);

        void loadOriginalMediaToView(Context context, Media media, NetworkImageView view);

        String loadImageThumbUrl(Media media);

    }

}
