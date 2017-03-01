package com.winsun.fruitmix.refactor.data;

import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
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

    MediaSharesLoadOperationResult loadAllRemoteMediaShares(String url,String token);

    //media
    OperationResult insertLocalMedias(List<Media> medias);

    OperationResult insertRemoteMedias(List<Media> medias);

    OperationResult insertLocalMedia(String url,String token,Media media);

    OperationResult deleteAllRemoteMedia();

    MediasLoadOperationResult loadAllRemoteMedias(String url,String token);

    MediasLoadOperationResult loadAllLocalMedias();

    Collection<String> loadLocalMediaUUIDs();

    Collection<String> loadRemoteMediaUUIDs();

    MediasLoadOperationResult loadLocalMediaInCamera(Collection<String> loadedMediaUUIDs);

    Media loadMedia(String mediaKey);

    void updateLocalMediasUploadedFalse();

    OperationResult updateLocalMedia(Media media);

    //user

    OperateUserResult insertUser(String url,String token,String userName, String userPassword);

    OperationResult insertUsers(List<User> users);

    OperationResult deleteAllRemoteUsers();

    UsersLoadOperationResult loadUsers(String loadUserUrl, String loadOtherUserUrl, String token);

    User loadUser(String userUUID);

    Collection<String> loadAllUserUUID();

    List<User> loadUserByLoginApi(String token,String url);

    //file
    FilesLoadOperationResult loadRemoteFolder(String url, String token);

    OperationResult loadRemoteFile(String url,String token);

    OperationResult insertRemoteFiles(AbstractRemoteFile folder);

    OperationResult deleteAllRemoteFiles();

    //file share
    FileSharesLoadOperationResult loadRemoteFileRootShares(String loadFileSharedWithMeUrl,String loadFileShareWithOthersUrl,String token);

    OperationResult insertRemoteFileShare(List<AbstractRemoteFile> files);

    OperationResult deleteAllRemoteFileShare();

    //file download
    FileDownloadLoadOperationResult loadDownloadedFilesRecord();

    OperationResult deleteDownloadedFileRecord(List<String> fileUUIDs);

    OperationResult insertDownloadedFileRecord(FileDownloadItem fileDownloadItem);

    //token
    TokenLoadOperationResult loadToken(LoadTokenParam param);

    void deleteToken();

    OperationResult insertToken(String token);

    //device id
    DeviceIDLoadOperationResult loadDeviceID(String url, String token);

    OperationResult insertDeviceID(String deviceID);

    void deleteDeviceID();

    //others

    String loadGateway();

    OperationResult insertGateway(String gateway);

    OperationResult insertLoginUserUUID(String userUUID);

    String loadLoginUserUUID();

    boolean getShowAlbumTipsValue();

    void saveShowAlbumTipsValue(boolean value);

    boolean getShowPhotoReturnTipsValue();

    void saveShowPhotoReturnTipsValue(boolean value);

    List<EquipmentAlias> loadEquipmentAlias(String token,String url);

}
