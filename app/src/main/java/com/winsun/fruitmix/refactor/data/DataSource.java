package com.winsun.fruitmix.refactor.data;

import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.List;

/**
 * Created by Administrator on 2017/1/24.
 */

public interface DataSource {

    interface DataOperationCallback {

        void onOperationFinish(OperationResult operationResult);

    }

    interface RetrieveDeviceIDOperationCallback {

        void onOperationSucceed(OperationResult operationResult, String deviceID);

        void onOperationFail(OperationResult operationResult);
    }

    interface RetrieveTokenOperationCallback {

        void onOperationSucceed(OperationResult operationResult, String token);

        void onOperationFail(OperationResult operationResult);
    }

    interface RetrieveUserOperationCallback {
        void onOperationSucceed(OperationResult operationResult, List<User> users);

        void onOperationFail(OperationResult operationResult);
    }

    interface RetrieveMediaOperationCallback {

        void onOperationSucceed(OperationResult operationResult, List<Media> medias);

        void onOperationFail(OperationResult operationResult);
    }

    interface RetrieveMediaShareOperationCallback {

        void onOperationSucceed(OperationResult operationResult, List<MediaShare> mediaShares);

        void onOperationFail(OperationResult operationResult);
    }

    interface RetrieveFileOperationCallback {

        void onOperationSucceed(OperationResult operationResult, List<AbstractRemoteFile> mediaShares);

        void onOperationFail(OperationResult operationResult);

    }

    void saveUser(User user, DataOperationCallback callback);

    void saveMedia(Media media, DataOperationCallback callback);

    void saveMediaShare(MediaShare mediaShare, DataOperationCallback callback);

    void modifyMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare, DataOperationCallback callback);

    void modifyMediaInMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare, DataOperationCallback callback);

    void deleteMediaShare(MediaShare mediaShare, DataOperationCallback callback);

    void retrieveDeviceID(RetrieveDeviceIDOperationCallback callback);

    void retrieveUser(RetrieveUserOperationCallback callback);

    void retrieveMedia(RetrieveMediaOperationCallback callback);

    void retrieveMediaShare(RetrieveMediaShareOperationCallback callback);

    void retrieveFile(RetrieveFileOperationCallback callback);

    void retrieveFileShare(RetrieveFileOperationCallback callback);

    void retrieveToken(RetrieveTokenOperationCallback callback);
}
