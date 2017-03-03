package com.winsun.fruitmix.refactor.contract;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface AlbumContentContract {

    interface AlbumContentView extends BaseView {

        void setTitle(String title);

        void showAlbumContent(List<Media> medias);

        void showNoOperationPermission();

        View findViewWithTag(String tag);

        void smoothScrollToPosition(int position);

        void startPostponedEnterTransition();

        void finishActivity();

        void setResult(int result);

        void showUploadingToast();

        void showOperationResultToast(OperationResult result);

        void setPrivatePublicMenuItemTitle(int titleResID);

    }

    interface AlbumContentPresenter extends BasePresenter<AlbumContentView> {

        void toggleAlbumPublicState();

        void deleteCurrentAlbum();

        boolean preTreatItemOnOptionsItemSelected();

        void onMapSharedElements(List<String> names, Map<String, View> sharedElements);

        void onActivityReenter(int resultCode, Intent data);

        void setMediaShareTitle();

        void loadMediaInMediaShare();

        void showMenuItemPrivateOrPublic();

        List<Media> getMedias();

        String getMediaShareUUID();

        void loadMediaToView(Context context, Media media, NetworkImageView view);
    }
}
