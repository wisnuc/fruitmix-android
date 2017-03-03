package com.winsun.fruitmix.refactor.contract;

import android.content.Context;
import android.view.View;

import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface AlbumFragmentContract {

    interface AlbumFragmentView extends BaseView {

        void setAlbumBalloonVisibility(int visibility);

        void setAlbumBalloonOnClickListener(View.OnClickListener listener);

        void showAlbums(List<MediaShare> mediaShares);

        void onDestroyView();

        void setAddAlbumBtnVisibility(int visibility);

        void showOperationResultToast(OperationResult result);

        void showNoOperatePermission();
    }

    interface AlbumFragmentPresenter extends BasePresenter<AlbumFragmentView> {

        void createAlbumBtnOnClick();

        void modifyMediaShare(MediaShare mediaShare);

        void deleteMediaShare(MediaShare mediaShare);

        void refreshData();

        void onResume();

        void onCreate();

        Media loadMedia(String mediaKey);

        User loadUser(String userUUID);

        void loadMediaToView(Context context, Media media, NetworkImageView view);
    }

}
