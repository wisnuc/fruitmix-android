package com.winsun.fruitmix.contract;

import android.content.Context;
import android.view.View;

import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.common.BasePresenter;
import com.winsun.fruitmix.common.BaseView;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;

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

        void modifyMediaShare(MediaShare mediaShare);

        void deleteMediaShare(MediaShare mediaShare);

        void refreshData();

        Media loadMedia(String mediaKey);

        User loadUser(String userUUID);

        void loadMediaToView(Context context, Media media, NetworkImageView view);
    }

}
