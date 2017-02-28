package com.winsun.fruitmix.refactor.data;

import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
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
    OperateMediaShareResult insertRemoteMediaShare(String url,String token,MediaShare mediaShare);

    OperationResult insertRemoteMediaShares(Collection<MediaShare> mediaShares);

    OperationResult modifyRemoteMediaShare(String url,String token,String requestData, MediaShare modifiedMediaShare);

    OperationResult deleteRemoteMediaShare(String url,String token,MediaShare mediaShare);

    OperationResult deleteAllRemoteMediaShare();

    MediaShare loadRemoteMediaShare(String mediaShareUUID);

    MediaSharesLoadOperationResult loadAllRemoteMediaShares();

    //media
    OperationResult insertLocalMedias(List<Media> medias);

    OperationResult insertRemoteMedias(List<Media> medias);

    OperationResult deleteAllRemoteMedia();

    MediasLoadOperationResult loadAllRemoteMedias();

    MediasLoadOperationResult loadAllLocalMedias();

    Collection<String> loadLocalMediaUUIDs();

    MediasLoadOperationResult loadLocalMediaInCamera(Collection<String> loadedMediaUUIDs);

    Media loadMedia(String mediaKey);

    void updateLocalMediasUploadedFalse();

    //user

    OperateUserResult insertUser(String url,String token,String userName, String userPassword);

    OperationResult insertUsers(List<User> users);

    OperationResult insertCurrentLoginUser(User user);

    OperationResult deleteAllRemoteUsers();

    UsersLoadOperationResult loadUsers();

    User loadUser(String userUUID);

    User loadCurrentLoginUser();

    List<User> loadUserByLoginApi(String token,String url);

    //file
    FilesLoadOperationResult loadRemoteFiles(String folderUUID);

    //file share
    FileSharesLoadOperationResult loadRemoteFileRootShares();

    //file download
    FileDownloadLoadOperationResult loadDownloadedFilesRecord();

    OperationResult deleteDownloadedFileRecord(List<String> fileUUIDs);

    OperationResult insertDownloadedFileRecord(FileDownloadItem fileDownloadItem);

    //token
    TokenLoadOperationResult loadToken(LoadTokenParam param);

    LoadTokenParam getLoadTokenParam();

    void deleteToken();

    OperationResult insertToken(String token);

    //device id
    DeviceIDLoadOperationResult loadDeviceID();

    OperationResult insertDeviceID(String deviceID);

    void deleteDeviceID();

    //others
    boolean getShowAlbumTipsValue();

    void saveShowAlbumTipsValue(boolean value);

    boolean getShowPhotoReturnTipsValue();

    void saveShowPhotoReturnTipsValue(boolean value);

    List<EquipmentAlias> loadEquipmentAlias(String token,String url);

    String loadGateway();

    String loadPort();

}
