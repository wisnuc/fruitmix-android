package com.winsun.fruitmix.refactor.data;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.LoadTokenParam;
import com.winsun.fruitmix.refactor.data.dataOperationResult.DeviceIDLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FileDownloadLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FileSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FilesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.MediaSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.MediasLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.OperateMediaShareResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.OperateUserResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.UsersLoadOperationResult;
import com.winsun.fruitmix.refactor.model.EquipmentAlias;

import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/1/24.
 */

public interface DataSource {

    //media share
    OperateMediaShareResult insertRemoteMediaShare(MediaShare mediaShare);

    OperationResult insertRemoteMediaShares(Collection<MediaShare> mediaShares);

    OperationResult modifyRemoteMediaShare(String requestData, MediaShare modifiedMediaShare);

    OperationResult deleteRemoteMediaShare(MediaShare mediaShare);

    MediaShare loadRemoteMediaShare(String mediaShareUUID);

    MediaSharesLoadOperationResult loadAllMediaShares();

    //media
    OperationResult insertLocalMedias(List<Media> medias);

    OperationResult insertRemoteMedias(List<Media> medias);

    MediasLoadOperationResult loadAllRemoteMedias();

    MediasLoadOperationResult loadAllLocalMedias();

    Collection<String> loadLocalMediaUUIDs();

    MediasLoadOperationResult loadLocalMediaInCamera(Collection<String> loadedMediaUUIDs);

    Media loadMedia(String mediaKey);

    void updateLocalMediasUploadedFalse();

    //user

    OperateUserResult insertUser(String userName, String userPassword);

    OperationResult insertUsers(List<User> users);

    UsersLoadOperationResult loadUsers();

    User loadUser(String userUUID);

    User loadCurrentLoginUser();

    List<User> loadUserByLoginApi(String url);

    //file
    FilesLoadOperationResult loadRemoteFiles(String folderUUID);

    //file share
    FileSharesLoadOperationResult loadRemoteFileRootShares();

    //file download
    FileDownloadLoadOperationResult loadDownloadedFiles();

    OperationResult deleteDownloadedFile(List<String> fileUUIDs);

    //token
    TokenLoadOperationResult loadToken(LoadTokenParam param);

    LoadTokenParam getLoadTokenParam();

    void deleteToken();

    //device id
    DeviceIDLoadOperationResult loadDeviceID();

    //others
    boolean getShowAlbumTipsValue();

    void saveShowAlbumTipsValue(boolean value);

    boolean getShowPhotoReturnTipsValue();

    void saveShowPhotoReturnTipsValue(boolean value);

    List<EquipmentAlias> loadEquipmentAlias(String url);

}
