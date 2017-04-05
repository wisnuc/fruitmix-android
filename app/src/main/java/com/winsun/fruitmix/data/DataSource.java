package com.winsun.fruitmix.data;

import com.winsun.fruitmix.business.LoadTokenParam;
import com.winsun.fruitmix.data.dataOperationResult.DeviceIDLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.FileDownloadLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.FileSharesLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.FilesLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.MediaSharesLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.MediasLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.OperateMediaShareResult;
import com.winsun.fruitmix.data.dataOperationResult.OperateUserResult;
import com.winsun.fruitmix.data.dataOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.UsersLoadOperationResult;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadState;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.EquipmentAlias;

import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/1/24.
 */

public interface DataSource {

    void init();

    //media share
    OperateMediaShareResult insertRemoteMediaShare( MediaShare mediaShare);

    OperationResult insertRemoteMediaShares(Collection<MediaShare> mediaShares);

    OperationResult modifyRemoteMediaShare(String requestData, MediaShare modifiedMediaShare);

    OperationResult modifyMediaInRemoteMediaShare(String requestData,MediaShare diffContentsOriginalMediaShare,MediaShare diffContentsModifiedMediaShare,MediaShare modifiedMediaShare);

    OperationResult deleteRemoteMediaShare( MediaShare mediaShare);

    boolean deleteAllRemoteMediaShare();

    MediaShare loadRemoteMediaShare(String mediaShareUUID);

    MediaSharesLoadOperationResult loadAllRemoteMediaShares();

    //media
    OperationResult insertLocalMedias(List<Media> medias);

    OperationResult insertRemoteMedias(List<Media> medias);

    OperationResult insertLocalMedia(Media media);

    boolean deleteAllRemoteMedia();

    MediasLoadOperationResult loadAllRemoteMedias();

    MediasLoadOperationResult loadAllLocalMedias();

    Collection<String> loadLocalMediaThumbs();

    Collection<String> loadRemoteMediaUUIDs();

    Media loadLocalMediaByThumb(String thumb);

    MediasLoadOperationResult loadLocalMediaInCamera(Collection<String> loadedMediaThumbs);

    Media loadMedia(String mediaKey);

    OperationResult updateLocalMediaMiniThumb(Media media);

    OperationResult updateLocalMediaUploadedDeviceID(Media media);

    //user

    OperateUserResult insertRemoteUser(String userName, String userPassword);

    OperationResult insertRemoteUsers(List<User> users);

    boolean deleteAllRemoteUsers();

    UsersLoadOperationResult loadRemoteUsers();

    User loadRemoteUser(String userUUID);

    Collection<String> loadAllRemoteUserUUID();

    List<User> loadRemoteUserByLoginApi(String url);

    //file
    FilesLoadOperationResult loadRemoteFolder(String folderUUID);

    OperationResult downloadRemoteFile(FileDownloadState fileDownloadState);

    OperationResult insertRemoteFiles(AbstractRemoteFile folder);

    OperationResult deleteAllRemoteFiles();

    //file share
    FileSharesLoadOperationResult loadRemoteFileRootShares();

    OperationResult insertRemoteFileShare(List<AbstractRemoteFile> files);

    OperationResult deleteAllRemoteFileShare();

    //file download

    FileDownloadLoadOperationResult loadDownloadedFilesRecord(String userUUID);

    OperationResult deleteDownloadedFileRecord(List<String> fileUUIDs,String userUUID);

    OperationResult insertDownloadedFileRecord(FileDownloadItem fileDownloadItem);

    //token
    TokenLoadOperationResult loadToken(LoadTokenParam param);

    String loadToken();

    void deleteToken();

    OperationResult insertToken(String token);

    //device id
    DeviceIDLoadOperationResult loadDeviceID();

    OperationResult insertDeviceID(String deviceID);

    void deleteDeviceID();

    //others

    void insertLoggedInUser(List<LoggedInUser> loggedInUsers);

    List<LoggedInUser> loadLoggedInUser();

    void deleteLoggedInUser(LoggedInUser loggedInUser);

    String loadGateway();

    OperationResult insertGateway(String gateway);

    OperationResult insertLoginUserUUID(String userUUID);

    String loadLoginUserUUID();

    boolean getShowAlbumTipsValue();

    void saveShowAlbumTipsValue(boolean value);

    boolean getShowPhotoReturnTipsValue();

    void saveShowPhotoReturnTipsValue(boolean value);

    List<EquipmentAlias> loadEquipmentAlias(String url);

    boolean getAutoUploadOrNot();

    void saveAutoUploadOrNot(boolean autoUploadOrNot);

    String getCurrentUploadDeviceID();

    void saveCurrentUploadDeviceID(String currentUploadDeviceID);

}
